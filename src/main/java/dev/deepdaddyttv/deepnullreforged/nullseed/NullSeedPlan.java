package dev.deepdaddyttv.deepnullreforged.nullseed;

import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullData;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemStatus;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullData;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullEntry;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullTemplate;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.StoredChemical;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record NullSeedPlan(List<Entry> entries, int appliedCount, int selectedCount, int overflowCount, int blockedCount) {
    public enum Status {
        ALREADY_PRESENT,
        WILL_RESERVE,
        WILL_FILL,
        WILL_RULE,
        BLOCKED_OCCUPIED,
        OVERFLOW,
        INVALID
    }

    public record Entry(ResourceLocation itemId, int slot, Status status) {
    }

    public NullSeedPlan {
        entries = List.copyOf(entries == null ? List.of() : entries);
    }

    public static NullSeedPlan plan(DeepNullInventory inventory, List<ResourceLocation> itemIds) {
        return planItems(inventory, NullSeedEntry.itemEntries(itemIds));
    }

    public static NullSeedPlan planItems(DeepNullInventory inventory, List<NullSeedEntry> entries) {
        if (inventory == null || inventory.isFluidOnly()) {
            return new NullSeedPlan(List.of(), 0, 0, 0, 0);
        }
        List<NullSeedEntry> selected = normalizeEntries(entries, NullSeedKind.ITEM);
        List<Entry> planned = new ArrayList<>();
        int applied = 0;
        int overflow = 0;
        int blocked = 0;
        for (int index = 0; index < selected.size(); index++) {
            NullSeedEntry seed = selected.get(index);
            ResourceLocation itemId = seed.id();
            Item item = BuiltInRegistries.ITEM.get(itemId);
            if (item == Items.AIR || !BuiltInRegistries.ITEM.containsKey(itemId)) {
                planned.add(new Entry(itemId, -1, Status.INVALID));
                continue;
            }
            int slot = seed.targetIndex() >= 0 ? seed.targetIndex() : index;
            if (slot >= inventory.getSlots()) {
                planned.add(new Entry(itemId, -1, Status.OVERFLOW));
                overflow++;
                continue;
            }
            ItemStack target = new ItemStack(item);
            ItemStack stored = inventory.getStackInSlot(slot);
            if (!stored.isEmpty()) {
                if (ItemStack.isSameItemSameComponents(stored, target) || ItemStack.isSameItem(stored, target)) {
                    planned.add(new Entry(itemId, slot, Status.ALREADY_PRESENT));
                    applied++;
                } else {
                    planned.add(new Entry(itemId, slot, Status.BLOCKED_OCCUPIED));
                    blocked++;
                }
                continue;
            }
            planned.add(new Entry(itemId, slot, Status.WILL_RESERVE));
            applied++;
        }
        return new NullSeedPlan(planned, applied, selected.size(), overflow, blocked);
    }

    public static NullSeedPlan apply(DeepNullInventory inventory, List<ResourceLocation> itemIds, boolean replaceReservations) {
        return applyItems(inventory, NullSeedEntry.itemEntries(itemIds), replaceReservations);
    }

    public static NullSeedPlan applyItems(DeepNullInventory inventory, List<NullSeedEntry> entries, boolean replaceReservations) {
        NullSeedPlan plan = planItems(inventory, entries);
        if (inventory == null || inventory.isFluidOnly()) {
            return plan;
        }
        if (replaceReservations) {
            inventory.clearReservedStacks();
        }
        for (Entry entry : plan.entries()) {
            if (entry.status() != Status.WILL_RESERVE || entry.slot() < 0) {
                continue;
            }
            Item item = BuiltInRegistries.ITEM.get(entry.itemId());
            if (item != Items.AIR && BuiltInRegistries.ITEM.containsKey(entry.itemId())) {
                inventory.setReservedStack(entry.slot(), new ItemStack(item));
            }
        }
        return plan;
    }

    public static NullSeedPlan planFluids(DeepNullInventory inventory, List<NullSeedEntry> entries) {
        if (inventory == null || !inventory.isFluidOnly()) {
            return new NullSeedPlan(List.of(), 0, 0, 0, 0);
        }
        List<NullSeedEntry> selected = normalizeFluidEntries(entries);
        List<Entry> planned = new ArrayList<>();
        int applied = 0;
        int overflow = 0;
        int blocked = 0;
        for (int index = 0; index < selected.size(); index++) {
            NullSeedEntry seed = selected.get(index);
            ResourceLocation fluidId = seed.id();
            boolean chemical = seed.kind() == NullSeedKind.CHEMICAL;
            Fluid fluid = chemical ? Fluids.EMPTY : BuiltInRegistries.FLUID.get(fluidId);
            if ((!chemical && (!inventory.acceptsNormalFluids() || fluid == Fluids.EMPTY || !BuiltInRegistries.FLUID.containsKey(fluidId)))
                    || (chemical && !inventory.supportsChemicalStorage())
                    || seed.amount() <= 0) {
                planned.add(new Entry(fluidId, -1, Status.INVALID));
                continue;
            }
            int tank = seed.targetIndex() >= 0 ? seed.targetIndex() : index;
            if (tank >= inventory.getFluidSlotCount()) {
                planned.add(new Entry(fluidId, -1, Status.OVERFLOW));
                overflow++;
                continue;
            }
            FluidStack existingFluid = inventory.getFluidInSlot(tank);
            StoredChemical existingChemical = inventory.getChemicalInSlot(tank);
            FluidStack reservedFluid = inventory.getReservedFluidTemplate(tank);
            StoredChemical reservedChemical = inventory.getReservedChemicalTemplate(tank);
            if (chemical) {
                StoredChemical plannedChemical = new StoredChemical(seed.id().toString(), seed.amount(), "", 0xFFFFFFFF, "", true);
                if (!existingFluid.isEmpty()
                        || (!existingChemical.isEmpty() && !existingChemical.isSameChemical(plannedChemical))
                        || (!reservedFluid.isEmpty())
                        || (!reservedChemical.isEmpty() && !reservedChemical.isSameChemical(plannedChemical))) {
                    planned.add(new Entry(fluidId, tank, Status.BLOCKED_OCCUPIED));
                    blocked++;
                    continue;
                }
                if ((!existingChemical.isEmpty() && existingChemical.amount() >= seed.amount())
                        || (!reservedChemical.isEmpty() && reservedChemical.amount() >= seed.amount())) {
                    planned.add(new Entry(fluidId, tank, Status.ALREADY_PRESENT));
                    applied++;
                    continue;
                }
                planned.add(new Entry(fluidId, tank, Status.WILL_RESERVE));
                applied++;
                continue;
            }
            if (!existingChemical.isEmpty()
                    || !reservedChemical.isEmpty()
                    || (!existingFluid.isEmpty() && !existingFluid.getFluid().isSame(fluid))
                    || (!reservedFluid.isEmpty() && !FluidStack.isSameFluidSameComponents(reservedFluid, new FluidStack(fluid, 1)))) {
                planned.add(new Entry(fluidId, tank, Status.BLOCKED_OCCUPIED));
                blocked++;
                continue;
            }
            if ((!existingFluid.isEmpty() && existingFluid.getAmount() >= seed.amount())
                    || (!reservedFluid.isEmpty() && reservedFluid.getAmount() >= seed.amount())) {
                planned.add(new Entry(fluidId, tank, Status.ALREADY_PRESENT));
                applied++;
                continue;
            }
            planned.add(new Entry(fluidId, tank, Status.WILL_RESERVE));
            applied++;
        }
        return new NullSeedPlan(planned, applied, selected.size(), overflow, blocked);
    }

    public static NullSeedPlan applyFluids(DeepNullInventory inventory, List<NullSeedEntry> entries) {
        NullSeedPlan plan = planFluids(inventory, entries);
        if (inventory == null || !inventory.isFluidOnly()) {
            return plan;
        }
        inventory.clearReservedTankTemplates();
        List<NullSeedEntry> selected = normalizeFluidEntries(entries);
        for (int index = 0; index < plan.entries().size() && index < selected.size(); index++) {
            Entry planned = plan.entries().get(index);
            if (planned.status() != Status.WILL_RESERVE || planned.slot() < 0) {
                continue;
            }
            NullSeedEntry seed = selected.get(index);
            if (seed.kind() == NullSeedKind.CHEMICAL) {
                inventory.setReservedChemicalTemplate(planned.slot(), new StoredChemical(seed.id().toString(), seed.amount(), "", 0xFFFFFFFF, "", true));
            } else {
                Fluid fluid = BuiltInRegistries.FLUID.get(seed.id());
                if (fluid != Fluids.EMPTY && BuiltInRegistries.FLUID.containsKey(seed.id())) {
                    inventory.setReservedFluidTemplate(planned.slot(), new FluidStack(fluid, seed.amount()));
                }
            }
        }
        return plan;
    }

    public static NullSeedPlan planDump(ItemStack dumpStack, List<NullSeedEntry> entries) {
        List<NullSeedEntry> selected = normalizeEntries(entries, NullSeedKind.DUMP_RULE);
        List<Entry> planned = new ArrayList<>();
        int applied = 0;
        for (NullSeedEntry seed : selected) {
            ResourceLocation itemId = seed.id();
            Item item = BuiltInRegistries.ITEM.get(itemId);
            if (item == Items.AIR || !BuiltInRegistries.ITEM.containsKey(itemId)) {
                planned.add(new Entry(itemId, -1, Status.INVALID));
                continue;
            }
            DumpNullItemStatus current = DumpNullData.get(dumpStack).statusFor(itemId);
            if (current == seed.dumpStatus()) {
                planned.add(new Entry(itemId, -1, Status.ALREADY_PRESENT));
            } else {
                planned.add(new Entry(itemId, -1, Status.WILL_RULE));
            }
            applied++;
        }
        return new NullSeedPlan(planned, applied, selected.size(), 0, 0);
    }

    public static NullSeedPlan applyDump(ItemStack dumpStack, List<NullSeedEntry> entries) {
        NullSeedPlan plan = planDump(dumpStack, entries);
        DumpNullData data = DumpNullData.get(dumpStack);
        for (NullSeedEntry seed : normalizeEntries(entries, NullSeedKind.DUMP_RULE)) {
            data = data.withItemStatus(seed.id(), seed.dumpStatus());
        }
        DumpNullData.set(dumpStack, data);
        return plan;
    }

    public static NullSeedPlan planEntities(ItemStack denStack, DeepNullTier tier, List<NullSeedEntry> entries) {
        if (denStack == null || denStack.isEmpty() || tier == null) {
            return new NullSeedPlan(List.of(), 0, 0, 0, 0);
        }
        List<NullSeedEntry> selected = normalizeEntries(entries, NullSeedKind.ENTITY);
        DenNullData data = DenNullData.get(denStack);
        List<Entry> planned = new ArrayList<>();
        int applied = 0;
        int overflow = 0;
        int blocked = 0;
        for (int index = 0; index < selected.size(); index++) {
            NullSeedEntry seed = selected.get(index);
            ResourceLocation entityId = seed.id();
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);
            if (entityType == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(entityId) || entityType.getCategory() == MobCategory.MISC) {
                planned.add(new Entry(entityId, -1, Status.INVALID));
                continue;
            }
            int slot = seed.targetIndex() >= 0 ? seed.targetIndex() : index;
            if (slot >= tier.slotCount()) {
                planned.add(new Entry(entityId, -1, Status.OVERFLOW));
                overflow++;
                continue;
            }
            DenNullEntry captured = slot < data.entries().size() ? data.entries().get(slot) : null;
            if (captured != null) {
                if (captured.entityType().equals(entityId)) {
                    planned.add(new Entry(entityId, slot, Status.ALREADY_PRESENT));
                    applied++;
                } else {
                    planned.add(new Entry(entityId, slot, Status.BLOCKED_OCCUPIED));
                    blocked++;
                }
                continue;
            }
            DenNullTemplate template = data.templateAt(slot);
            if (template != null && template.entityType().equals(entityId)) {
                planned.add(new Entry(entityId, slot, Status.ALREADY_PRESENT));
                applied++;
                continue;
            }
            planned.add(new Entry(entityId, slot, Status.WILL_RESERVE));
            applied++;
        }
        return new NullSeedPlan(planned, applied, selected.size(), overflow, blocked);
    }

    public static NullSeedPlan applyEntities(ItemStack denStack, DeepNullTier tier, List<NullSeedEntry> entries, boolean replaceReservations) {
        NullSeedPlan plan = planEntities(denStack, tier, entries);
        if (denStack == null || denStack.isEmpty() || tier == null) {
            return plan;
        }
        DenNullData data = DenNullData.get(denStack);
        if (replaceReservations) {
            data = data.withTemplates(List.of());
        }
        List<DenNullTemplate> templates = new ArrayList<>(data.templates());
        List<NullSeedEntry> selected = normalizeEntries(entries, NullSeedKind.ENTITY);
        for (int index = 0; index < plan.entries().size() && index < selected.size(); index++) {
            Entry planned = plan.entries().get(index);
            if (planned.status() != Status.WILL_RESERVE || planned.slot() < 0) {
                continue;
            }
            templates.removeIf(template -> template.targetIndex() == planned.slot());
            templates.add(new DenNullTemplate(planned.slot(), planned.itemId()));
        }
        DenNullData.set(denStack, data.withTemplates(templates));
        return plan;
    }

    private static List<ResourceLocation> normalize(List<ResourceLocation> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return List.of();
        }
        Set<ResourceLocation> unique = new LinkedHashSet<>();
        for (ResourceLocation itemId : itemIds) {
            if (itemId != null) {
                unique.add(itemId);
            }
        }
        return List.copyOf(unique);
    }

    private static List<NullSeedEntry> normalizeEntries(List<NullSeedEntry> entries, NullSeedKind kind) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<NullSeedEntry> result = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (NullSeedEntry entry : entries) {
            if (entry == null || entry.kind() != kind) {
                continue;
            }
            String key = entry.kind() + "|" + entry.targetIndex() + "|" + entry.id() + "|" + entry.dumpStatus();
            if (seen.add(key)) {
                result.add(entry);
            }
        }
        return List.copyOf(result);
    }

    private static List<NullSeedEntry> normalizeFluidEntries(List<NullSeedEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<NullSeedEntry> result = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (NullSeedEntry entry : entries) {
            if (entry == null || (entry.kind() != NullSeedKind.FLUID && entry.kind() != NullSeedKind.CHEMICAL)) {
                continue;
            }
            String key = entry.kind() + "|" + entry.targetIndex() + "|" + entry.id();
            if (seen.add(key)) {
                result.add(entry);
            }
        }
        return List.copyOf(result);
    }
}
