package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.inventory.TransferDirectionMode;
import dev.deepdaddyttv.deepnullreforged.inventory.TransferOutputMode;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ClientUiTextTest {
    @Test
    void upgradeScreenTitleUsesDampNullKeyForFluidScreens() {
        assertEquals("dn.dampnull_upgrades_screen.desc", translationKey(ClientUiText.upgradeScreenTitle(true)));
        assertEquals("dn.upgrades_screen.desc", translationKey(ClientUiText.upgradeScreenTitle(false)));
    }

    @Test
    void upgradeKindTextDistinguishesDeepNullAndDampNullModes() {
        assertEquals("upgrade.kind.dampnull", translationKey(ClientUiText.upgradeKindText(true, DeepNullUpgradeType.SPONGE)));
        assertEquals("upgrade.kind.anynull", translationKey(ClientUiText.upgradeKindText(true, DeepNullUpgradeType.ENDER)));
        assertEquals("upgrade.kind.dampnull", translationKey(ClientUiText.upgradeKindText(false, DeepNullUpgradeType.GAS)));
        assertEquals("upgrade.kind.deepnull", translationKey(ClientUiText.upgradeKindText(false, DeepNullUpgradeType.FILTER)));
    }

    @Test
    void supportedTierTextMatchesUpgradeTypeRules() {
        assertEquals("upgrade.tiers.iron_plus", translationKey(ClientUiText.supportedTierText(DeepNullUpgradeType.FILTER)));
        assertEquals("upgrade.tiers.diamond_plus", translationKey(ClientUiText.supportedTierText(DeepNullUpgradeType.ENERGY)));
        assertEquals("upgrade.tiers.emerald_only", translationKey(ClientUiText.supportedTierText(DeepNullUpgradeType.DEEP_ENERGY)));
        assertEquals("upgrade.tiers.every_tier", translationKey(ClientUiText.supportedTierText(DeepNullUpgradeType.SPONGE)));
    }

    @Test
    void transferOutputModeMessageUsesItemAndFluidPrefixes() {
        Component itemMessage = ClientUiText.transferOutputModeMessage(false, TransferOutputMode.MATCHING);
        Component fluidMessage = ClientUiText.transferOutputModeMessage(true, TransferOutputMode.LOCKED);

        assertEquals("dn.item_output_mode.desc", translationKey(itemMessage));
        assertEquals("dn.transfer_matching.desc", translationKey(itemMessage.getSiblings().get(1)));
        assertEquals("dn.fluid_output_mode.desc", translationKey(fluidMessage));
        assertEquals("dn.transfer_locked.desc", translationKey(fluidMessage.getSiblings().get(1)));
    }

    @Test
    void transferDirectionModeMessageUsesItemAndFluidPrefixes() {
        Component itemMessage = ClientUiText.transferDirectionModeMessage(false, TransferDirectionMode.INSERT);
        Component fluidMessage = ClientUiText.transferDirectionModeMessage(true, TransferDirectionMode.EXTRACT);

        assertEquals("dn.item_transfer_direction.desc", translationKey(itemMessage));
        assertEquals("dn.transfer_direction_insert.desc", translationKey(itemMessage.getSiblings().get(1)));
        assertEquals("dn.fluid_transfer_direction.desc", translationKey(fluidMessage));
        assertEquals("dn.transfer_direction_extract.desc", translationKey(fluidMessage.getSiblings().get(1)));
    }

    private static String translationKey(Component component) {
        return assertInstanceOf(TranslatableContents.class, component.getContents()).getKey();
    }
}
