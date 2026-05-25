package dev.deepdaddyttv.deepnullreforged.registry;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.devmode.DeepNullDevModeSupport;
import dev.deepdaddyttv.deepnullreforged.nullseed.NullTestingPresets;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DeepNullReforged.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DEEP_NULL_TAB = CREATIVE_MODE_TABS.register(DeepNullReforged.MODID, () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + DeepNullReforged.MODID))
            .icon(() -> ModItems.REDSTONE_DEEP_NULL.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.REDSTONE_DEEP_NULL.get());
                output.accept(ModItems.LAPIS_DEEP_NULL.get());
                output.accept(ModItems.IRON_DEEP_NULL.get());
                output.accept(ModItems.GOLD_DEEP_NULL.get());
                output.accept(ModItems.DIAMOND_DEEP_NULL.get());
                output.accept(ModItems.EMERALD_DEEP_NULL.get());
                output.accept(ModItems.CREATIVE_DEEP_NULL.get());
                output.accept(ModItems.REDSTONE_DAMP_NULL.get());
                output.accept(ModItems.LAPIS_DAMP_NULL.get());
                output.accept(ModItems.IRON_DAMP_NULL.get());
                output.accept(ModItems.GOLD_DAMP_NULL.get());
                output.accept(ModItems.DIAMOND_DAMP_NULL.get());
                output.accept(ModItems.EMERALD_DAMP_NULL.get());
                output.accept(ModItems.CREATIVE_DAMP_NULL.get());
                output.accept(ModItems.HUB_NULL.get());
                output.accept(ModItems.REDSTONE_DEN_NULL.get());
                output.accept(ModItems.LAPIS_DEN_NULL.get());
                output.accept(ModItems.IRON_DEN_NULL.get());
                output.accept(ModItems.GOLD_DEN_NULL.get());
                output.accept(ModItems.DIAMOND_DEN_NULL.get());
                output.accept(ModItems.EMERALD_DEN_NULL.get());
                output.accept(ModItems.CREATIVE_DEN_NULL.get());
                output.accept(ModItems.BAIT.get());
                output.accept(ModItems.REDSTONE_DRIP_NULL.get());
                output.accept(ModItems.LAPIS_DRIP_NULL.get());
                output.accept(ModItems.IRON_DRIP_NULL.get());
                output.accept(ModItems.GOLD_DRIP_NULL.get());
                output.accept(ModItems.DIAMOND_DRIP_NULL.get());
                output.accept(ModItems.EMERALD_DRIP_NULL.get());
                output.accept(ModItems.CREATIVE_DRIP_NULL.get());
                output.accept(ModItems.DRIP_MEND_UPGRADE.get());
                output.accept(ModItems.DEN_BREEDING_UPGRADE.get());
                output.accept(ModItems.DEN_CLONE_UPGRADE.get());
                output.accept(ModItems.DEN_DYE_UPGRADE.get());
                output.accept(ModItems.DEN_MILK_UPGRADE.get());
                output.accept(ModItems.DEN_SHEAR_UPGRADE.get());
                output.accept(ModItems.DEN_BABY_UPGRADE.get());
                output.accept(ModItems.DEN_TAG_UPGRADE.get());
                output.accept(ModItems.DEN_CAPTURE_UPGRADE.get());
                output.accept(ModItems.DEN_SPAWNER_UPGRADE.get());
                output.accept(ModItems.DEN_FARM_UPGRADE.get());
                if (DeepNullDevModeSupport.shouldShowCreativePresets()) {
                    for (var presetStack : NullTestingPresets.creativeStacks(parameters.holders())) {
                        output.accept(presetStack);
                    }
                }
                output.accept(ModItems.REDSTONE_PANEL.get());
                output.accept(ModItems.LAPIS_PANEL.get());
                output.accept(ModItems.IRON_PANEL.get());
                output.accept(ModItems.GOLD_PANEL.get());
                output.accept(ModItems.DIAMOND_PANEL.get());
                output.accept(ModItems.EMERALD_PANEL.get());
                output.accept(ModItems.FILTER.get());
                output.accept(ModItems.UPGRADE_CORE.get());
                output.accept(ModItems.ENDER_UPGRADE_CORE.get());
                output.accept(ModItems.SYNCHRONIZER.get());
                output.accept(ModItems.FILTER_UPGRADE.get());
                output.accept(ModItems.FLUID_UPGRADE.get());
                output.accept(ModItems.ENERGY_UPGRADE.get());
                output.accept(ModItems.DEEP_ENERGY_UPGRADE.get());
                output.accept(ModItems.AUTO_FEEDING_UPGRADE.get());
                output.accept(ModItems.AUTO_SMELTING_UPGRADE.get());
                output.accept(ModItems.BASIC_COMPRESSION_UPGRADE.get());
                output.accept(ModItems.ADVANCED_COMPRESSION_UPGRADE.get());
                output.accept(ModItems.STONEWORKS_UPGRADE.get());
                output.accept(ModItems.STONE_GENERATOR_UPGRADE.get());
                output.accept(ModItems.OBSIDIAN_GENERATOR_UPGRADE.get());
                output.accept(ModItems.SPONGE_UPGRADE.get());
                output.accept(ModItems.BALLOON_UPGRADE.get());
                if (ModList.get().isLoaded("mekanism")) {
                    output.accept(ModItems.GAS_UPGRADE.get());
                }
                output.accept(ModItems.ENDER_UPGRADE.get());
                output.accept(ModItems.DEEP_NULL_DOCK.get());
                output.accept(ModItems.NULL_WORKBENCH.get());
                output.accept(ModItems.DRIP_STAND.get());
            })
            .build());

    private ModCreativeTabs() {
    }
}
