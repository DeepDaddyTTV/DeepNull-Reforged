package dev.deepdaddyttv.deepnullreforged.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public final class NbtCodecs {
    private NbtCodecs() {
    }

    public static CompoundTag encodeItemStack(HolderLookup.Provider registries, ItemStack stack) {
        return ItemStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack)
                .result()
                .flatMap(Tag::asCompound)
                .orElseGet(CompoundTag::new);
    }

    public static ItemStack decodeItemStack(HolderLookup.Provider registries, CompoundTag tag) {
        return ItemStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag)
                .result()
                .orElse(ItemStack.EMPTY);
    }
}
