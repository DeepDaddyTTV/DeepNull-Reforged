package dev.deepdaddyttv.deepnullreforged.registry;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullPanelItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullUpgradeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DeepNullReforged.MODID);

    public static final DeferredItem<Item> REDSTONE_DEEP_NULL = registerDeepNull(DeepNullTier.REDSTONE);
    public static final DeferredItem<Item> LAPIS_DEEP_NULL = registerDeepNull(DeepNullTier.LAPIS);
    public static final DeferredItem<Item> IRON_DEEP_NULL = registerDeepNull(DeepNullTier.IRON);
    public static final DeferredItem<Item> GOLD_DEEP_NULL = registerDeepNull(DeepNullTier.GOLD);
    public static final DeferredItem<Item> DIAMOND_DEEP_NULL = registerDeepNull(DeepNullTier.DIAMOND);
    public static final DeferredItem<Item> EMERALD_DEEP_NULL = registerDeepNull(DeepNullTier.EMERALD);
    public static final DeferredItem<Item> CREATIVE_DEEP_NULL = registerDeepNull(DeepNullTier.CREATIVE);

    public static final DeferredItem<Item> REDSTONE_PANEL = registerPanel(DeepNullTier.REDSTONE);
    public static final DeferredItem<Item> LAPIS_PANEL = registerPanel(DeepNullTier.LAPIS);
    public static final DeferredItem<Item> IRON_PANEL = registerPanel(DeepNullTier.IRON);
    public static final DeferredItem<Item> GOLD_PANEL = registerPanel(DeepNullTier.GOLD);
    public static final DeferredItem<Item> DIAMOND_PANEL = registerPanel(DeepNullTier.DIAMOND);
    public static final DeferredItem<Item> EMERALD_PANEL = registerPanel(DeepNullTier.EMERALD);

    public static final DeferredItem<Item> FILTER = ITEMS.register("filter", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FILTER_UPGRADE = registerUpgrade(DeepNullUpgradeType.FILTER);
    public static final DeferredItem<Item> FLUID_UPGRADE = registerUpgrade(DeepNullUpgradeType.FLUID);
    public static final DeferredItem<Item> ENERGY_UPGRADE = registerUpgrade(DeepNullUpgradeType.ENERGY);

    public static final DeferredItem<Item> DEEP_NULL_DOCK = ITEMS.register("deepnull_dock", () -> new BlockItem(ModBlocks.DEEP_NULL_DOCK.get(), new Item.Properties()));

    private ModItems() {
    }

    private static DeferredItem<Item> registerDeepNull(DeepNullTier tier) {
        return ITEMS.register(tier.deepNullId(), () -> new DeepNullItem(tier, new Item.Properties()));
    }

    private static DeferredItem<Item> registerPanel(DeepNullTier tier) {
        return ITEMS.register(tier.panelId(), () -> new DeepNullPanelItem(tier, new Item.Properties()));
    }

    private static DeferredItem<Item> registerUpgrade(DeepNullUpgradeType type) {
        return ITEMS.register(type.itemId(), () -> new DeepNullUpgradeItem(type, new Item.Properties()));
    }
}
