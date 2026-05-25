package dev.deepdaddyttv.deepnullreforged.dumpnull;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record DumpNullBufferEntry(ResourceLocation itemId, int count) {
    private static final String ITEM_TAG = "Item";
    private static final String COUNT_TAG = "Count";
    private static final ResourceLocation AIR = ResourceLocation.withDefaultNamespace("air");

    public DumpNullBufferEntry {
        itemId = itemId == null ? AIR : itemId;
        count = Math.max(0, count);
    }

    public boolean isEmpty() {
        return count <= 0 || itemId.equals(AIR);
    }

    public boolean sameItem(ItemStack stack) {
        return !stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(itemId);
    }

    public Item item() {
        return BuiltInRegistries.ITEM.get(itemId);
    }

    public ItemStack stack() {
        return isEmpty() ? ItemStack.EMPTY : new ItemStack(item(), count);
    }

    public DumpNullBufferEntry withCount(int count) {
        return new DumpNullBufferEntry(itemId, count);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(ITEM_TAG, itemId.toString());
        tag.putInt(COUNT_TAG, count);
        return tag;
    }

    public static DumpNullBufferEntry load(CompoundTag tag) {
        ResourceLocation itemId = ResourceLocation.tryParse(tag.getString(ITEM_TAG));
        return new DumpNullBufferEntry(itemId, tag.getInt(COUNT_TAG));
    }

    public static void write(RegistryFriendlyByteBuf buffer, DumpNullBufferEntry entry) {
        buffer.writeResourceLocation(entry.itemId);
        buffer.writeVarInt(entry.count);
    }

    public static DumpNullBufferEntry read(RegistryFriendlyByteBuf buffer) {
        return new DumpNullBufferEntry(buffer.readResourceLocation(), buffer.readVarInt());
    }

    public static DumpNullBufferEntry fromStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return new DumpNullBufferEntry(AIR, 0);
        }
        return new DumpNullBufferEntry(BuiltInRegistries.ITEM.getKey(stack.getItem()), stack.getCount());
    }
}
