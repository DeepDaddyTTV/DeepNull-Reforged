package dev.deepdaddyttv.deepnullreforged.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum StoneGeneratorVariant {
    COBBLESTONE("cobblestone", Items.COBBLESTONE),
    DIORITE("diorite", Items.DIORITE),
    ANDESITE("andesite", Items.ANDESITE),
    GRANITE("granite", Items.GRANITE),
    COBBLED_DEEPSLATE("cobbled_deepslate", Items.COBBLED_DEEPSLATE),
    TUFF("tuff", Items.TUFF);

    private final String id;
    private final Item item;

    StoneGeneratorVariant(String id, Item item) {
        this.id = id;
        this.item = item;
    }

    public String id() {
        return id;
    }

    public Item item() {
        return item;
    }

    public ItemStack stack() {
        return new ItemStack(item);
    }

    public ItemStack stack(int count) {
        return item == null || count <= 0 ? ItemStack.EMPTY : new ItemStack(item, count);
    }

    public Component displayName() {
        return item.getDescription();
    }

    public StoneGeneratorVariant cycle(boolean forward) {
        StoneGeneratorVariant[] values = values();
        int nextIndex = Math.floorMod(ordinal() + (forward ? 1 : -1), values.length);
        return values[nextIndex];
    }

    public static StoneGeneratorVariant byId(int id) {
        StoneGeneratorVariant[] values = values();
        if (id < 0 || id >= values.length) {
            return COBBLESTONE;
        }
        return values[id];
    }
}
