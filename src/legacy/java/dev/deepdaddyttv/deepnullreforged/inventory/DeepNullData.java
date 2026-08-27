package dev.deepdaddyttv.deepnullreforged.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.Constants;

public final class DeepNullData {
    private static final String UPGRADES = "DeepNullUpgrades";

    private DeepNullData() {
    }

    public static boolean hasUpgrade(ItemStack stack, String id) {
        CompoundNBT tag = stack.getTag();
        if (tag == null) return false;
        ListNBT upgrades = tag.getList(UPGRADES, Constants.NBT.TAG_STRING);
        for (int i = 0; i < upgrades.size(); i++) {
            if (id.equals(upgrades.getString(i))) return true;
        }
        return false;
    }

    public static boolean addUpgrade(ItemStack stack, String id) {
        if (hasUpgrade(stack, id)) return false;
        ListNBT upgrades = stack.getOrCreateTag().getList(UPGRADES, Constants.NBT.TAG_STRING);
        upgrades.add(StringNBT.valueOf(id));
        stack.getOrCreateTag().put(UPGRADES, upgrades);
        return true;
    }

    public static ListNBT upgrades(ItemStack stack) {
        return stack.getOrCreateTag().getList(UPGRADES, Constants.NBT.TAG_STRING);
    }
}
