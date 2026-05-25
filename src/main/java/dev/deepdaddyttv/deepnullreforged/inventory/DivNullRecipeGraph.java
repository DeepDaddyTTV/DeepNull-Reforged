package dev.deepdaddyttv.deepnullreforged.inventory;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Server-side catalogue of reversible crafting layers that DivNull slots may collapse into one canonical count.
 */
public final class DivNullRecipeGraph {
    private static final DivNullRecipeGraph EMPTY = new DivNullRecipeGraph(Map.of(), Map.of(), Audit.EMPTY);

    private static @Nullable MinecraftServer cachedServer;
    private static DivNullRecipeGraph cachedGraph = EMPTY;

    private final Map<Item, Family> familiesByItem;
    private final Map<ResourceLocation, Family> familiesById;
    private final Audit audit;

    private DivNullRecipeGraph(Map<Item, Family> familiesByItem, Map<ResourceLocation, Family> familiesById, Audit audit) {
        this.familiesByItem = familiesByItem;
        this.familiesById = familiesById;
        this.audit = audit;
    }

    public static DivNullRecipeGraph current() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return EMPTY;
        }
        if (server == cachedServer) {
            return cachedGraph;
        }
        cachedServer = server;
        cachedGraph = build(server);
        DeepNullReforged.LOGGER.debug(
                "DivNull recipe graph built: {} families, {} accepted reversible edges, {} rejected recipes",
                cachedGraph.familiesById.size(),
                cachedGraph.audit.acceptedEdges(),
                cachedGraph.audit.rejectedRecipes()
        );
        return cachedGraph;
    }

    public static DivNullRecipeGraph build(MinecraftServer server) {
        HolderLookup.Provider registries = server.registryAccess();
        List<Edge> reversibleEdges = reversibleEdges(server, registries);
        if (reversibleEdges.isEmpty()) {
            return new DivNullRecipeGraph(Map.of(), Map.of(), new Audit(0, 0));
        }

        Map<Item, List<Edge>> edgesByItem = new HashMap<>();
        for (Edge edge : reversibleEdges) {
            edgesByItem.computeIfAbsent(edge.input().getItem(), ignored -> new ArrayList<>()).add(edge);
            edgesByItem.computeIfAbsent(edge.output().getItem(), ignored -> new ArrayList<>()).add(edge.reversed());
        }

        Set<Item> visited = new HashSet<>();
        Map<Item, Family> familyByItem = new HashMap<>();
        Map<ResourceLocation, Family> familyById = new HashMap<>();
        for (Item start : edgesByItem.keySet()) {
            if (!visited.add(start)) {
                continue;
            }

            Map<Item, Long> units = new HashMap<>();
            ArrayDeque<Item> queue = new ArrayDeque<>();
            units.put(start, 1L);
            queue.add(start);
            boolean safe = true;

            while (!queue.isEmpty() && safe) {
                Item item = queue.removeFirst();
                long baseUnit = units.get(item);
                for (Edge edge : edgesByItem.getOrDefault(item, List.of())) {
                    if (edge.input().getItem() != item) {
                        continue;
                    }
                    long outputUnit = multiplySaturating(baseUnit, edge.inputCount()) / Math.max(1, edge.outputCount());
                    if (outputUnit <= 0 || multiplySaturating(outputUnit, edge.outputCount()) != multiplySaturating(baseUnit, edge.inputCount())) {
                        safe = false;
                        break;
                    }
                    Long existing = units.putIfAbsent(edge.output().getItem(), outputUnit);
                    if (existing == null) {
                        visited.add(edge.output().getItem());
                        queue.add(edge.output().getItem());
                    } else if (existing.longValue() != outputUnit) {
                        safe = false;
                        break;
                    }
                }
            }

            if (!safe || units.size() < 2) {
                continue;
            }

            long gcd = units.values().stream().mapToLong(Long::longValue).reduce(DivNullRecipeGraph::gcd).orElse(1L);
            List<Layer> layers = units.entrySet().stream()
                    .map(entry -> new Layer(itemId(entry.getKey()), entry.getKey(), Math.max(1L, entry.getValue() / gcd)))
                    .sorted(Comparator.comparingLong(Layer::unit).thenComparing(layer -> layer.id().toString()))
                    .toList();
            ResourceLocation familyId = layers.getFirst().id();
            Family family = new Family(familyId, layers);
            familyById.put(familyId, family);
            for (Layer layer : layers) {
                familyByItem.put(layer.item(), family);
            }
        }

        return new DivNullRecipeGraph(Map.copyOf(familyByItem), Map.copyOf(familyById), new Audit(reversibleEdges.size(), 0));
    }

    public boolean isEmpty() {
        return familiesByItem.isEmpty();
    }

    public Optional<Family> findFamily(ItemStack stack) {
        return stack.isEmpty() ? Optional.empty() : Optional.ofNullable(familiesByItem.get(stack.getItem()));
    }

    public Optional<Family> familyById(ResourceLocation familyId) {
        return Optional.ofNullable(familiesById.get(familyId));
    }

    public @Nullable Layer findLayer(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        Family family = familiesByItem.get(stack.getItem());
        return family == null ? null : family.layerFor(stack.getItem());
    }

    public Audit audit() {
        return audit;
    }

    private static List<Edge> reversibleEdges(MinecraftServer server, HolderLookup.Provider registries) {
        List<Candidate> candidates = new ArrayList<>();
        int rejected = 0;
        for (RecipeHolder<CraftingRecipe> holder : server.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            Candidate candidate = candidateFor(server, registries, holder);
            if (candidate == null) {
                rejected++;
                continue;
            }
            candidates.add(candidate);
        }

        List<Edge> edges = new ArrayList<>();
        for (Candidate candidate : candidates) {
            boolean reversible = candidates.stream().anyMatch(other ->
                    ItemStack.isSameItemSameComponents(other.input(), candidate.output())
                            && ItemStack.isSameItemSameComponents(other.output(), candidate.input())
                            && (long) candidate.inputCount() * other.inputCount() == (long) candidate.outputCount() * other.outputCount());
            if (reversible) {
                edges.add(new Edge(candidate.input(), candidate.inputCount(), candidate.output(), candidate.outputCount()));
            } else {
                rejected++;
            }
        }

        DeepNullReforged.LOGGER.debug("DivNull candidate scan accepted {} recipes and rejected {}", edges.size(), rejected);
        return edges;
    }

    private static @Nullable Candidate candidateFor(MinecraftServer server, HolderLookup.Provider registries, RecipeHolder<CraftingRecipe> holder) {
        CraftingRecipe recipe = holder.value();
        ItemStack output = recipe.getResultItem(registries);
        if (!isPlainSingleItem(output)) {
            return null;
        }

        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        ItemStack input = ItemStack.EMPTY;
        int inputCount = 0;
        for (Ingredient ingredient : ingredients) {
            if (ingredient.isEmpty()) {
                continue;
            }
            ItemStack[] matchingItems = ingredient.getItems();
            if (matchingItems.length != 1 || !isPlainSingleItem(matchingItems[0])) {
                return null;
            }
            ItemStack ingredientStack = matchingItems[0].copyWithCount(1);
            if (input.isEmpty()) {
                input = ingredientStack;
            } else if (!ItemStack.isSameItemSameComponents(input, ingredientStack)) {
                return null;
            }
            inputCount++;
        }
        if (input.isEmpty() || inputCount <= 0) {
            return null;
        }
        if (!recipe.getRemainingItems(simpleCraftingInput(input, inputCount)).stream().allMatch(ItemStack::isEmpty)) {
            return null;
        }
        return new Candidate(input, inputCount, output.copyWithCount(1), output.getCount());
    }

    private static CraftingInput simpleCraftingInput(ItemStack stack, int count) {
        int width = count <= 1 ? 1 : count <= 4 ? 2 : 3;
        int height = (int) Math.ceil(count / (double) width);
        List<ItemStack> inputs = new ArrayList<>(width * height);
        for (int index = 0; index < width * height; index++) {
            inputs.add(index < count ? stack.copyWithCount(1) : ItemStack.EMPTY);
        }
        return CraftingInput.of(width, height, inputs);
    }

    private static boolean isPlainSingleItem(ItemStack stack) {
        return !stack.isEmpty() && stack.getCount() > 0 && stack.getComponentsPatch().isEmpty();
    }

    private static ResourceLocation itemId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    private static long multiplySaturating(long left, long right) {
        if (left <= 0L || right <= 0L) {
            return 0L;
        }
        if (left > Long.MAX_VALUE / right) {
            return Long.MAX_VALUE;
        }
        return left * right;
    }

    private static long gcd(long left, long right) {
        long a = Math.abs(left);
        long b = Math.abs(right);
        while (b != 0L) {
            long next = a % b;
            a = b;
            b = next;
        }
        return Math.max(1L, a);
    }

    public record Layer(ResourceLocation id, Item item, long unit) {
        public ItemStack stack(int count) {
            return new ItemStack(item, Math.max(1, count));
        }
    }

    public record Family(ResourceLocation id, List<Layer> layers) {
        public @Nullable Layer layerFor(Item item) {
            for (Layer layer : layers) {
                if (layer.item() == item) {
                    return layer;
                }
            }
            return null;
        }

        public @Nullable Layer layerById(ResourceLocation id) {
            for (Layer layer : layers) {
                if (layer.id().equals(id)) {
                    return layer;
                }
            }
            return null;
        }

        public Layer smallestLayer() {
            return layers.getFirst();
        }

        public Layer largestLayer() {
            return layers.getLast();
        }

        public Layer bestLayerFor(long canonicalUnits) {
            Layer best = smallestLayer();
            for (Layer layer : layers) {
                if (canonicalUnits >= layer.unit()) {
                    best = layer;
                }
            }
            return best;
        }
    }

    public record Audit(int acceptedEdges, int rejectedRecipes) {
        private static final Audit EMPTY = new Audit(0, 0);
    }

    private record Candidate(ItemStack input, int inputCount, ItemStack output, int outputCount) {
    }

    private record Edge(ItemStack input, int inputCount, ItemStack output, int outputCount) {
        private Edge reversed() {
            return new Edge(output, outputCount, input, inputCount);
        }
    }
}
