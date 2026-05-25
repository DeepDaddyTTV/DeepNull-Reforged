package dev.deepdaddyttv.deepnullreforged.dennull;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record DenNullBufferEntry(ResourceLocation itemId, int count) {
    private static final String ITEM_TAG = "Item";
    private static final String COUNT_TAG = "Count";

    public DenNullBufferEntry {
        itemId = itemId == null ? ResourceLocation.withDefaultNamespace("air") : itemId;
        count = Math.max(0, count);
    }

    public boolean isEmpty() {
        return count <= 0 || itemId.equals(ResourceLocation.withDefaultNamespace("air"));
    }

    public Item item() {
        return BuiltInRegistries.ITEM.get(itemId);
    }

    public ItemStack stack() {
        return isEmpty() ? ItemStack.EMPTY : new ItemStack(item(), count);
    }

    public DenNullBufferEntry withCount(int count) {
        return new DenNullBufferEntry(itemId, count);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(ITEM_TAG, itemId.toString());
        tag.putInt(COUNT_TAG, count);
        return tag;
    }

    public static DenNullBufferEntry load(CompoundTag tag) {
        ResourceLocation itemId = ResourceLocation.tryParse(tag.getString(ITEM_TAG));
        return new DenNullBufferEntry(itemId, tag.getInt(COUNT_TAG));
    }

    public static void write(RegistryFriendlyByteBuf buffer, DenNullBufferEntry entry) {
        buffer.writeResourceLocation(entry.itemId);
        buffer.writeVarInt(entry.count);
    }

    public static DenNullBufferEntry read(RegistryFriendlyByteBuf buffer) {
        return new DenNullBufferEntry(buffer.readResourceLocation(), buffer.readVarInt());
    }

    public static DenNullBufferEntry fromStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return new DenNullBufferEntry(ResourceLocation.withDefaultNamespace("air"), 0);
        }
        return new DenNullBufferEntry(BuiltInRegistries.ITEM.getKey(stack.getItem()), stack.getCount());
    }
}
