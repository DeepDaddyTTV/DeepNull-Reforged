package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.inventory.TransferDirectionMode;
import dev.deepdaddyttv.deepnullreforged.inventory.TransferOutputMode;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.client.KeyMapping;

import java.util.Locale;

final class ClientUiText {
    private ClientUiText() {
    }

    static Component transferOutputModeMessage(boolean fluidOnly, TransferOutputMode mode) {
        return Component.translatable(fluidOnly ? "dn.fluid_output_mode.desc" : "dn.item_output_mode.desc")
                .append(": ")
                .append(Component.translatable(mode.translationKey()));
    }

    static Component transferDirectionModeMessage(boolean fluidOnly, TransferDirectionMode mode) {
        return Component.translatable(fluidOnly ? "dn.fluid_transfer_direction.desc" : "dn.item_transfer_direction.desc")
                .append(": ")
                .append(Component.translatable(mode.translationKey()));
    }

    static Component upgradeScreenTitle(boolean fluidOnly) {
        return fluidOnly
                ? Component.translatable("dn.dampnull_upgrades_screen.desc")
                : Component.translatable("dn.upgrades_screen.desc");
    }

    static MutableComponent upgradeKindText(boolean fluidOnly, DeepNullUpgradeType type) {
        if (fluidOnly) {
            return type == DeepNullUpgradeType.ENDER
                    ? Component.translatable("upgrade.kind.anynull")
                    : Component.translatable("upgrade.kind.dampnull");
        }
        return switch (type) {
            case STONE_GENERATOR, OBSIDIAN_GENERATOR, SPONGE, GAS -> Component.translatable("upgrade.kind.dampnull");
            case ENDER -> Component.translatable("upgrade.kind.anynull");
            default -> Component.translatable("upgrade.kind.deepnull");
        };
    }

    static Component supportedTierText(DeepNullUpgradeType type) {
        return switch (type) {
            case FILTER -> Component.translatable("upgrade.tiers.iron_plus");
            case FLUID, AUTO_FEEDING, AUTO_SMELTING, BASIC_COMPRESSION, ADVANCED_COMPRESSION, STONEWORKS, STONE_GENERATOR, OBSIDIAN_GENERATOR, SPONGE, GAS, ENDER ->
                    Component.translatable("upgrade.tiers.every_tier");
            case ENERGY -> Component.translatable("upgrade.tiers.diamond_plus");
            case DEEP_ENERGY -> Component.translatable("upgrade.tiers.emerald_only");
        };
    }

    static String shortcutKeyToken(KeyMapping keyMapping) {
        return keyMapping == null ? "?" : shortcutKeyToken(keyMapping.getTranslatedKeyMessage().getString());
    }

    static String shortcutKeyToken(String rawKeyName) {
        if (rawKeyName == null || rawKeyName.trim().isEmpty()) {
            return "?";
        }
        String normalized = rawKeyName.trim();
        return switch (normalized.toLowerCase(Locale.ROOT)) {
            case "grave accent" -> "`";
            case "delete" -> "Del";
            case "escape" -> "Esc";
            case "backspace" -> "Bksp";
            case "left control", "right control", "control" -> "Ctrl";
            case "left alt", "right alt", "alt" -> "Alt";
            case "left shift", "right shift", "shift" -> "Shift";
            case "page up" -> "PgUp";
            case "page down" -> "PgDn";
            case "caps lock" -> "Caps";
            case "insert" -> "Ins";
            default -> normalized.length() == 1 ? normalized.toUpperCase(Locale.ROOT) : normalized;
        };
    }
}
