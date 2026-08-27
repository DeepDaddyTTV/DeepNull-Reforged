package dev.deepdaddyttv.deepnullreforged.registry;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.DeepNullDockBlock;
import dev.deepdaddyttv.deepnullreforged.block.NullWorkbenchBlock;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.item.DampNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullUpgradeItem;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.Map;
import dev.deepdaddyttv.deepnullreforged.recipe.DampNullUpgradeRecipe;
import dev.deepdaddyttv.deepnullreforged.recipe.DeepNullUpgradeRecipe;
import dev.deepdaddyttv.deepnullreforged.recipe.SynchronizerClearRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;

public final class ModContent {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, DeepNullReforged.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DeepNullReforged.MODID);
    public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, DeepNullReforged.MODID);
    public static final DeferredRegister<ContainerType<?>> MENUS = DeferredRegister.create(ForgeRegistries.CONTAINERS, DeepNullReforged.MODID);
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, DeepNullReforged.MODID);

    public static final RegistryObject<DeepNullDockBlock> DEEP_NULL_DOCK = BLOCKS.register("deepnull_dock",
            () -> new DeepNullDockBlock(AbstractBlock.Properties.of(Material.METAL).strength(2.0F, 60.0F).sound(SoundType.METAL).noOcclusion()));
    public static final RegistryObject<Block> NULL_WORKBENCH = BLOCKS.register("null_workbench",
            () -> new NullWorkbenchBlock(AbstractBlock.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD)));

    @SuppressWarnings("unchecked")
    public static final RegistryObject<Item>[] DEEP_NULLS = new RegistryObject[7];
    @SuppressWarnings("unchecked")
    public static final RegistryObject<Item>[] DAMP_NULLS = new RegistryObject[7];
    @SuppressWarnings("unchecked")
    public static final RegistryObject<Item>[] PANELS = new RegistryObject[6];
    public static final Map<String, RegistryObject<Item>> UPGRADES = new LinkedHashMap<String, RegistryObject<Item>>();

    public static final RegistryObject<Item> FILTER;
    public static final RegistryObject<Item> UPGRADE_CORE;
    public static final RegistryObject<Item> ENDER_UPGRADE_CORE;
    public static final RegistryObject<Item> SYNCHRONIZER;
    public static final RegistryObject<Item> DEEP_NULL_DOCK_ITEM;
    public static final RegistryObject<Item> NULL_WORKBENCH_ITEM;

    public static final RegistryObject<TileEntityType<DeepNullDockBlockEntity>> DEEP_NULL_DOCK_TILE;
    public static final RegistryObject<ContainerType<DeepNullMenu>> DEEP_NULL_MENU;
    public static final RegistryObject<IRecipeSerializer<?>> DEEP_NULL_UPGRADE_RECIPE;
    public static final RegistryObject<IRecipeSerializer<?>> DAMP_NULL_UPGRADE_RECIPE;
    public static final RegistryObject<IRecipeSerializer<?>> SYNCHRONIZER_CLEAR_RECIPE;

    static {
        for (final DeepNullTier tier : DeepNullTier.values()) {
            DEEP_NULLS[tier.id()] = ITEMS.register(tier.deepNullId(),
                    () -> new DeepNullItem(tier, itemProperties().stacksTo(1)));
            DAMP_NULLS[tier.id()] = ITEMS.register(tier.dampNullId(),
                    () -> new DampNullItem(tier, itemProperties().stacksTo(1)));
            if (tier.id() < 6) {
                PANELS[tier.id()] = ITEMS.register(tier.panelId(), () -> new Item(itemProperties()));
            }
        }

        FILTER = ITEMS.register("filter", () -> new Item(itemProperties()));
        UPGRADE_CORE = ITEMS.register("upgrade_core", () -> new Item(itemProperties()));
        ENDER_UPGRADE_CORE = ITEMS.register("ender_upgrade_core", () -> new Item(itemProperties()));
        SYNCHRONIZER = ITEMS.register("synchronizer", () -> new Item(itemProperties().stacksTo(1)));

        registerUpgrade("filter_upgrade");
        registerUpgrade("fluid_upgrade");
        registerUpgrade("energy_upgrade");
        registerUpgrade("deep_energy_upgrade");
        registerUpgrade("auto_feeding_upgrade");
        registerUpgrade("auto_smelting_upgrade");
        registerUpgrade("basic_compression_upgrade");
        registerUpgrade("advanced_compression_upgrade");
        registerUpgrade("stoneworks_upgrade");
        registerUpgrade("stone_generator_upgrade");
        registerUpgrade("obsidian_generator_upgrade");
        registerUpgrade("sponge_upgrade");
        registerUpgrade("gas_upgrade");
        registerUpgrade("ender_upgrade");

        DEEP_NULL_DOCK_ITEM = ITEMS.register("deepnull_dock", () -> new BlockItem(DEEP_NULL_DOCK.get(), itemProperties()));
        NULL_WORKBENCH_ITEM = ITEMS.register("null_workbench", () -> new BlockItem(NULL_WORKBENCH.get(), itemProperties()));

        DEEP_NULL_DOCK_TILE = TILES.register("deepnull_dock",
                () -> TileEntityType.Builder.of(DeepNullDockBlockEntity::new, DEEP_NULL_DOCK.get()).build(null));
        DEEP_NULL_MENU = MENUS.register("deep_null",
                () -> IForgeContainerType.create(DeepNullMenu::fromNetwork));
        DEEP_NULL_UPGRADE_RECIPE = RECIPES.register("deepnull_upgrade",
                () -> new SpecialRecipeSerializer<DeepNullUpgradeRecipe>(DeepNullUpgradeRecipe::new));
        DAMP_NULL_UPGRADE_RECIPE = RECIPES.register("dampnull_upgrade",
                () -> new SpecialRecipeSerializer<DampNullUpgradeRecipe>(DampNullUpgradeRecipe::new));
        SYNCHRONIZER_CLEAR_RECIPE = RECIPES.register("synchronizer_clear",
                () -> new SpecialRecipeSerializer<SynchronizerClearRecipe>(SynchronizerClearRecipe::new));
    }

    private ModContent() {
    }

    private static Item.Properties itemProperties() {
        return new Item.Properties().tab(DeepNullReforged.TAB);
    }

    private static void registerUpgrade(final String id) {
        UPGRADES.put(id, ITEMS.register(id, () -> new DeepNullUpgradeItem(id, itemProperties().stacksTo(1))));
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILES.register(bus);
        MENUS.register(bus);
        RECIPES.register(bus);
    }
}
