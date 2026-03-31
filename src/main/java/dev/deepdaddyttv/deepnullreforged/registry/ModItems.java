package dev.deepdaddyttv.deepnullreforged.registry;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.item.DampNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullPanelItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullUpgradeItem;
import dev.deepdaddyttv.deepnullreforged.item.EnderUpgradeItem;
import dev.deepdaddyttv.deepnullreforged.item.SynchronizerItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import dev.deepdaddyttv.deepnullreforged.compat.registries.DeferredItem;
import dev.deepdaddyttv.deepnullreforged.compat.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DeepNullReforged.MODID);

    public static final DeferredItem<Item> REDSTONE_DEEP_NULL = registerDeepNull(DeepNullTier.REDSTONE);
    public static final DeferredItem<Item> LAPIS_DEEP_NULL = registerDeepNull(DeepNullTier.LAPIS);
    public static final DeferredItem<Item> IRON_DEEP_NULL = registerDeepNull(DeepNullTier.IRON);
    public static final DeferredItem<Item> GOLD_DEEP_NULL = registerDeepNull(DeepNullTier.GOLD);
    public static final DeferredItem<Item> DIAMOND_DEEP_NULL = registerDeepNull(DeepNullTier.DIAMOND);
    public static final DeferredItem<Item> EMERALD_DEEP_NULL = registerDeepNull(DeepNullTier.EMERALD);
    public static final DeferredItem<Item> CREATIVE_DEEP_NULL = registerDeepNull(DeepNullTier.CREATIVE);

    public static final DeferredItem<Item> REDSTONE_DAMP_NULL = registerDampNull(DeepNullTier.REDSTONE);
    public static final DeferredItem<Item> LAPIS_DAMP_NULL = registerDampNull(DeepNullTier.LAPIS);
    public static final DeferredItem<Item> IRON_DAMP_NULL = registerDampNull(DeepNullTier.IRON);
    public static final DeferredItem<Item> GOLD_DAMP_NULL = registerDampNull(DeepNullTier.GOLD);
    public static final DeferredItem<Item> DIAMOND_DAMP_NULL = registerDampNull(DeepNullTier.DIAMOND);
    public static final DeferredItem<Item> EMERALD_DAMP_NULL = registerDampNull(DeepNullTier.EMERALD);
    public static final DeferredItem<Item> CREATIVE_DAMP_NULL = registerDampNull(DeepNullTier.CREATIVE);

    public static final DeferredItem<Item> REDSTONE_PANEL = registerPanel(DeepNullTier.REDSTONE);
    public static final DeferredItem<Item> LAPIS_PANEL = registerPanel(DeepNullTier.LAPIS);
    public static final DeferredItem<Item> IRON_PANEL = registerPanel(DeepNullTier.IRON);
    public static final DeferredItem<Item> GOLD_PANEL = registerPanel(DeepNullTier.GOLD);
    public static final DeferredItem<Item> DIAMOND_PANEL = registerPanel(DeepNullTier.DIAMOND);
    public static final DeferredItem<Item> EMERALD_PANEL = registerPanel(DeepNullTier.EMERALD);

    public static final DeferredItem<Item> FILTER = ITEMS.register("filter", () -> new Item(itemProperties("filter")));
    public static final DeferredItem<Item> UPGRADE_CORE = ITEMS.register("upgrade_core", () -> new Item(itemProperties("upgrade_core")));
    public static final DeferredItem<Item> ENDER_UPGRADE_CORE = ITEMS.register("ender_upgrade_core", () -> new Item(itemProperties("ender_upgrade_core")));
    public static final DeferredItem<Item> SYNCHRONIZER = ITEMS.register("synchronizer", () -> new SynchronizerItem(itemProperties("synchronizer")));
    public static final DeferredItem<Item> FILTER_UPGRADE = registerUpgrade(DeepNullUpgradeType.FILTER);
    public static final DeferredItem<Item> FLUID_UPGRADE = registerUpgrade(DeepNullUpgradeType.FLUID);
    public static final DeferredItem<Item> ENERGY_UPGRADE = registerUpgrade(DeepNullUpgradeType.ENERGY);
    public static final DeferredItem<Item> DEEP_ENERGY_UPGRADE = registerUpgrade(DeepNullUpgradeType.DEEP_ENERGY);
    public static final DeferredItem<Item> AUTO_FEEDING_UPGRADE = registerUpgrade(DeepNullUpgradeType.AUTO_FEEDING);
    public static final DeferredItem<Item> AUTO_SMELTING_UPGRADE = registerUpgrade(DeepNullUpgradeType.AUTO_SMELTING);
    public static final DeferredItem<Item> BASIC_COMPRESSION_UPGRADE = registerUpgrade(DeepNullUpgradeType.BASIC_COMPRESSION);
    public static final DeferredItem<Item> ADVANCED_COMPRESSION_UPGRADE = registerUpgrade(DeepNullUpgradeType.ADVANCED_COMPRESSION);
    public static final DeferredItem<Item> STONEWORKS_UPGRADE = registerUpgrade(DeepNullUpgradeType.STONEWORKS);
    public static final DeferredItem<Item> STONE_GENERATOR_UPGRADE = registerUpgrade(DeepNullUpgradeType.STONE_GENERATOR);
    public static final DeferredItem<Item> OBSIDIAN_GENERATOR_UPGRADE = registerUpgrade(DeepNullUpgradeType.OBSIDIAN_GENERATOR);
    public static final DeferredItem<Item> SPONGE_UPGRADE = registerUpgrade(DeepNullUpgradeType.SPONGE);
    public static final DeferredItem<Item> GAS_UPGRADE = registerUpgrade(DeepNullUpgradeType.GAS);
    public static final DeferredItem<Item> ENDER_UPGRADE = registerUpgrade(DeepNullUpgradeType.ENDER);

    public static final DeferredItem<Item> DEEP_NULL_DOCK = ITEMS.register("deepnull_dock", () -> new BlockItem(ModBlocks.DEEP_NULL_DOCK.get(), itemProperties("deepnull_dock")));
    public static final DeferredItem<Item> NULL_WORKBENCH = ITEMS.register("null_workbench", () -> new BlockItem(ModBlocks.NULL_WORKBENCH.get(), itemProperties("null_workbench")));

    private ModItems() {
    }

    public static void register() {
        ITEMS.register();
    }

    private static DeferredItem<Item> registerDeepNull(DeepNullTier tier) {
        return ITEMS.register(tier.deepNullId(), () -> new DeepNullItem(tier, itemProperties(tier.deepNullId())));
    }

    private static DeferredItem<Item> registerDampNull(DeepNullTier tier) {
        return ITEMS.register(tier.dampNullId(), () -> new DampNullItem(tier, itemProperties(tier.dampNullId())));
    }

    private static DeferredItem<Item> registerPanel(DeepNullTier tier) {
        return ITEMS.register(tier.panelId(), () -> new DeepNullPanelItem(tier, itemProperties(tier.panelId())));
    }

    private static DeferredItem<Item> registerUpgrade(DeepNullUpgradeType type) {
        if (type == DeepNullUpgradeType.ENDER) {
            return ITEMS.register(type.itemId(), () -> new EnderUpgradeItem(itemProperties(type.itemId())));
        }
        return ITEMS.register(type.itemId(), () -> new DeepNullUpgradeItem(type, itemProperties(type.itemId())));
    }

    private static Item.Properties itemProperties(String name) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, DeepNullReforged.id(name)));
    }
}
