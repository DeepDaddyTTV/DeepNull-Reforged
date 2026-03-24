package dev.deepdaddyttv.deepnullreforged.inventory;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum StyleGlassVariant {
    DEFAULT("default"),
    CREEPER("creeper"),
    PICKAXE("pickaxe"),
    FISH("fish"),
    FISHING_ROD("fishing_rod");

    private final String id;

    StyleGlassVariant(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public boolean supports(boolean fluidOnly) {
        return this == DEFAULT
                || (fluidOnly && (this == FISH || this == FISHING_ROD))
                || (!fluidOnly && (this == CREEPER || this == PICKAXE));
    }

    public static boolean isSupportedModifier(ItemStack stack) {
        return !fromModifier(false, stack).equals(DEFAULT) || !fromModifier(true, stack).equals(DEFAULT);
    }

    public static StyleGlassVariant fromModifier(ItemStack nullStack, ItemStack modifier) {
        return fromModifier(!nullStack.isEmpty() && nullStack.getItem() instanceof dev.deepdaddyttv.deepnullreforged.item.DampNullItem, modifier);
    }

    public static StyleGlassVariant fromModifier(boolean fluidOnly, ItemStack modifier) {
        if (modifier.isEmpty()) {
            return DEFAULT;
        }
        if (!fluidOnly) {
            if (modifier.is(Items.CREEPER_HEAD)) {
                return CREEPER;
            }
            if (modifier.is(ItemTags.PICKAXES)) {
                return PICKAXE;
            }
            return DEFAULT;
        }
        if (modifier.is(ItemTags.FISHES)) {
            return FISH;
        }
        if (modifier.is(Items.FISHING_ROD)) {
            return FISHING_ROD;
        }
        return DEFAULT;
    }

    public static StyleGlassVariant byId(String id) {
        for (StyleGlassVariant variant : values()) {
            if (variant.id.equals(id)) {
                return variant;
            }
        }
        return DEFAULT;
    }
}
