package dev.deepdaddyttv.deepnullreforged.nullseed;

import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemCatalog;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemCatalogEntry;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemCatalogView;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullFilterMode;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public final class NullTestingPresets {
    private NullTestingPresets() {
    }

    public static List<ItemStack> creativeStacks(HolderLookup.Provider registries) {
        return List.of(deepResourcePreset(registries), dampFluidPreset(registries), filterUpgradePreset(registries));
    }

    public static ItemStack deepResourcePreset(HolderLookup.Provider registries) {
        ItemStack stack = named(new ItemStack(ModItems.EMERALD_DEEP_NULL.get()), "WIP Deep VI Resource Preset");
        DeepNullInventory inventory = new DeepNullInventory(DeepNullTier.EMERALD, stack, registries, null);
        Item[] items = {
                Items.IRON_INGOT, Items.GOLD_INGOT, Items.COPPER_INGOT, Items.DIAMOND, Items.EMERALD, Items.LAPIS_LAZULI,
                Items.REDSTONE, Items.COAL, Items.QUARTZ, Items.AMETHYST_SHARD, Items.RAW_IRON, Items.RAW_GOLD,
                Items.RAW_COPPER, Items.IRON_ORE, Items.GOLD_ORE, Items.COPPER_ORE, Items.DIAMOND_ORE, Items.EMERALD_ORE,
                Items.DEEPSLATE_IRON_ORE, Items.DEEPSLATE_GOLD_ORE, Items.DEEPSLATE_COPPER_ORE, Items.DEEPSLATE_DIAMOND_ORE,
                Items.DEEPSLATE_EMERALD_ORE, Items.NETHER_QUARTZ_ORE, Items.NETHER_GOLD_ORE, Items.IRON_BLOCK,
                Items.GOLD_BLOCK, Items.COPPER_BLOCK, Items.DIAMOND_BLOCK, Items.EMERALD_BLOCK, Items.REDSTONE_BLOCK,
                Items.LAPIS_BLOCK
        };
        for (int slot = 0; slot < items.length && slot < inventory.getSlots(); slot++) {
            inventory.setStackInSlot(slot, new ItemStack(items[slot], deterministicItemCount(slot)));
        }
        return stack;
    }

    public static ItemStack dampFluidPreset(HolderLookup.Provider registries) {
        ItemStack stack = named(new ItemStack(ModItems.EMERALD_DAMP_NULL.get()), "WIP Damp VI Fluid Preset");
        DeepNullInventory inventory = new DeepNullInventory(DeepNullTier.EMERALD, stack, registries, null);
        Fluid[] fluids = {Fluids.WATER, Fluids.LAVA, Fluids.WATER, Fluids.LAVA, Fluids.WATER, Fluids.LAVA};
        for (int tank = 0; tank < fluids.length && tank < inventory.getFluidSlotCount(); tank++) {
            inventory.fillFluid(tank, new FluidStack(fluids[tank], deterministicFluidAmount(tank)), false);
        }
        return stack;
    }

    public static ItemStack filterUpgradePreset(HolderLookup.Provider registries) {
        ItemStack stack = named(new ItemStack(ModItems.EMERALD_DEEP_NULL.get()), "WIP Filter Upgrade Preset");
        DeepNullInventory inventory = new DeepNullInventory(DeepNullTier.EMERALD, stack, registries, null);
        inventory.getUpgradeHandler().setStackInSlot(DeepNullUpgradeType.FILTER.slot(), new ItemStack(ModItems.FILTER_UPGRADE.get()));
        inventory.setFilterMode(DeepNullFilterMode.WHITELIST);
        List<DumpNullItemCatalogEntry> entries = DumpNullItemCatalog.build(DumpNullItemCatalogView.MOB_DROPS, null);
        for (int slotIndex = 0; slotIndex < inventory.getFilterSlotCount() && slotIndex < entries.size(); slotIndex++) {
            Item item = BuiltInRegistries.ITEM.get(entries.get(slotIndex).itemId());
            if (item != Items.AIR) {
                inventory.setFilterStack(slotIndex, new ItemStack(item));
            }
        }
        return stack;
    }

    private static ItemStack named(ItemStack stack, String name) {
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }

    private static int deterministicItemCount(int index) {
        return 8 + ((index * 37) % 121);
    }

    private static int deterministicFluidAmount(int index) {
        return 12_000 + ((index * 17_000) % 233_000);
    }
}
