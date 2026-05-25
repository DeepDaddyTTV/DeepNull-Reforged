package dev.deepdaddyttv.deepnullreforged.registry;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.item.DampNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DenNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DenNullUpgradeItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullPanelItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullUpgradeItem;
import dev.deepdaddyttv.deepnullreforged.item.DripNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DripNullUpgradeItem;
import dev.deepdaddyttv.deepnullreforged.item.EnderUpgradeItem;
import dev.deepdaddyttv.deepnullreforged.item.HubNullItem;
import dev.deepdaddyttv.deepnullreforged.item.SynchronizerItem;
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

    public static final DeferredItem<Item> REDSTONE_DAMP_NULL = registerDampNull(DeepNullTier.REDSTONE);
    public static final DeferredItem<Item> LAPIS_DAMP_NULL = registerDampNull(DeepNullTier.LAPIS);
    public static final DeferredItem<Item> IRON_DAMP_NULL = registerDampNull(DeepNullTier.IRON);
    public static final DeferredItem<Item> GOLD_DAMP_NULL = registerDampNull(DeepNullTier.GOLD);
    public static final DeferredItem<Item> DIAMOND_DAMP_NULL = registerDampNull(DeepNullTier.DIAMOND);
    public static final DeferredItem<Item> EMERALD_DAMP_NULL = registerDampNull(DeepNullTier.EMERALD);
    public static final DeferredItem<Item> CREATIVE_DAMP_NULL = registerDampNull(DeepNullTier.CREATIVE);

    public static final DeferredItem<Item> HUB_NULL = ITEMS.register("hub_null", () -> new HubNullItem(new Item.Properties()));
    public static final DeferredItem<Item> BAIT = ITEMS.register("bait", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> REDSTONE_DEN_NULL = registerDenNull(DeepNullTier.REDSTONE);
    public static final DeferredItem<Item> LAPIS_DEN_NULL = registerDenNull(DeepNullTier.LAPIS);
    public static final DeferredItem<Item> IRON_DEN_NULL = registerDenNull(DeepNullTier.IRON);
    public static final DeferredItem<Item> GOLD_DEN_NULL = registerDenNull(DeepNullTier.GOLD);
    public static final DeferredItem<Item> DIAMOND_DEN_NULL = registerDenNull(DeepNullTier.DIAMOND);
    public static final DeferredItem<Item> EMERALD_DEN_NULL = registerDenNull(DeepNullTier.EMERALD);
    public static final DeferredItem<Item> CREATIVE_DEN_NULL = registerDenNull(DeepNullTier.CREATIVE);

    public static final DeferredItem<Item> REDSTONE_DRIP_NULL = registerDripNull(DeepNullTier.REDSTONE);
    public static final DeferredItem<Item> LAPIS_DRIP_NULL = registerDripNull(DeepNullTier.LAPIS);
    public static final DeferredItem<Item> IRON_DRIP_NULL = registerDripNull(DeepNullTier.IRON);
    public static final DeferredItem<Item> GOLD_DRIP_NULL = registerDripNull(DeepNullTier.GOLD);
    public static final DeferredItem<Item> DIAMOND_DRIP_NULL = registerDripNull(DeepNullTier.DIAMOND);
    public static final DeferredItem<Item> EMERALD_DRIP_NULL = registerDripNull(DeepNullTier.EMERALD);
    public static final DeferredItem<Item> CREATIVE_DRIP_NULL = registerDripNull(DeepNullTier.CREATIVE);
    public static final DeferredItem<Item> DRIP_MEND_UPGRADE = ITEMS.register("drip_mend_upgrade", () -> new DripNullUpgradeItem(dev.deepdaddyttv.deepnullreforged.dripnull.DripNullUpgradeType.MEND, new Item.Properties()));

    public static final DeferredItem<Item> DEN_BREEDING_UPGRADE = registerDenUpgrade(dev.deepdaddyttv.deepnullreforged.dennull.DenNullUpgradeType.BREEDING);
    public static final DeferredItem<Item> DEN_CLONE_UPGRADE = registerDenUpgrade(dev.deepdaddyttv.deepnullreforged.dennull.DenNullUpgradeType.CLONE);
    public static final DeferredItem<Item> DEN_DYE_UPGRADE = registerDenUpgrade(dev.deepdaddyttv.deepnullreforged.dennull.DenNullUpgradeType.DYE);
    public static final DeferredItem<Item> DEN_MILK_UPGRADE = registerDenUpgrade(dev.deepdaddyttv.deepnullreforged.dennull.DenNullUpgradeType.MILK);
    public static final DeferredItem<Item> DEN_SHEAR_UPGRADE = registerDenUpgrade(dev.deepdaddyttv.deepnullreforged.dennull.DenNullUpgradeType.SHEAR);
    public static final DeferredItem<Item> DEN_BABY_UPGRADE = registerDenUpgrade(dev.deepdaddyttv.deepnullreforged.dennull.DenNullUpgradeType.BABY);
    public static final DeferredItem<Item> DEN_TAG_UPGRADE = registerDenUpgrade(dev.deepdaddyttv.deepnullreforged.dennull.DenNullUpgradeType.TAG);
    public static final DeferredItem<Item> DEN_CAPTURE_UPGRADE = registerDenUpgrade(dev.deepdaddyttv.deepnullreforged.dennull.DenNullUpgradeType.CAPTURE);
    public static final DeferredItem<Item> DEN_SPAWNER_UPGRADE = registerDenUpgrade(dev.deepdaddyttv.deepnullreforged.dennull.DenNullUpgradeType.SPAWNER);
    public static final DeferredItem<Item> DEN_FARM_UPGRADE = registerDenUpgrade(dev.deepdaddyttv.deepnullreforged.dennull.DenNullUpgradeType.FARM);

    public static final DeferredItem<Item> REDSTONE_PANEL = registerPanel(DeepNullTier.REDSTONE);
    public static final DeferredItem<Item> LAPIS_PANEL = registerPanel(DeepNullTier.LAPIS);
    public static final DeferredItem<Item> IRON_PANEL = registerPanel(DeepNullTier.IRON);
    public static final DeferredItem<Item> GOLD_PANEL = registerPanel(DeepNullTier.GOLD);
    public static final DeferredItem<Item> DIAMOND_PANEL = registerPanel(DeepNullTier.DIAMOND);
    public static final DeferredItem<Item> EMERALD_PANEL = registerPanel(DeepNullTier.EMERALD);

    public static final DeferredItem<Item> FILTER = ITEMS.register("filter", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UPGRADE_CORE = ITEMS.register("upgrade_core", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ENDER_UPGRADE_CORE = ITEMS.register("ender_upgrade_core", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SYNCHRONIZER = ITEMS.register("synchronizer", () -> new SynchronizerItem(new Item.Properties()));
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
    public static final DeferredItem<Item> BALLOON_UPGRADE = registerUpgrade(DeepNullUpgradeType.BALLOON);
    public static final DeferredItem<Item> GAS_UPGRADE = registerUpgrade(DeepNullUpgradeType.GAS);
    public static final DeferredItem<Item> ENDER_UPGRADE = registerUpgrade(DeepNullUpgradeType.ENDER);
    public static final DeferredItem<Item> DEEP_NULL_DOCK = ITEMS.register("deepnull_dock", () -> new BlockItem(ModBlocks.DEEP_NULL_DOCK.get(), new Item.Properties()));
    public static final DeferredItem<Item> NULL_WORKBENCH = ITEMS.register("null_workbench", () -> new BlockItem(ModBlocks.NULL_WORKBENCH.get(), new Item.Properties()));
    public static final DeferredItem<Item> DRIP_STAND = ITEMS.register("drip_stand", () -> new BlockItem(ModBlocks.DRIP_STAND.get(), new Item.Properties()));

    private ModItems() {
    }

    private static DeferredItem<Item> registerDeepNull(DeepNullTier tier) {
        return ITEMS.register(tier.deepNullId(), () -> new DeepNullItem(tier, new Item.Properties()));
    }

    private static DeferredItem<Item> registerDampNull(DeepNullTier tier) {
        return ITEMS.register(tier.dampNullId(), () -> new DampNullItem(tier, new Item.Properties()));
    }

    private static DeferredItem<Item> registerDenNull(DeepNullTier tier) {
        return ITEMS.register(tier.denNullId(), () -> new DenNullItem(tier, new Item.Properties()));
    }

    private static DeferredItem<Item> registerDripNull(DeepNullTier tier) {
        return ITEMS.register(tier.dripNullId(), () -> new DripNullItem(tier, new Item.Properties()));
    }

    private static DeferredItem<Item> registerDenUpgrade(dev.deepdaddyttv.deepnullreforged.dennull.DenNullUpgradeType type) {
        return ITEMS.register(type.itemId(), () -> new DenNullUpgradeItem(type, new Item.Properties()));
    }

    private static DeferredItem<Item> registerPanel(DeepNullTier tier) {
        return ITEMS.register(tier.panelId(), () -> new DeepNullPanelItem(tier, new Item.Properties()));
    }

    private static DeferredItem<Item> registerUpgrade(DeepNullUpgradeType type) {
        if (type == DeepNullUpgradeType.ENDER) {
            return ITEMS.register(type.itemId(), () -> new EnderUpgradeItem(new Item.Properties()));
        }
        return ITEMS.register(type.itemId(), () -> new DeepNullUpgradeItem(type, new Item.Properties()));
    }
}
