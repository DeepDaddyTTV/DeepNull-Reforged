package dev.deepdaddyttv.deepnullreforged.menu;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullItemHandler;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModContent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullNetwork;
import dev.deepdaddyttv.deepnullreforged.network.ExtendedSlotPacket;
import net.minecraftforge.items.SlotItemHandler;

public class DeepNullMenu extends Container {
    private final DeepNullItemHandler handler;
    private final DeepNullTier tier;
    private final Hand hand;
    private final PlayerEntity menuPlayer;
    private final NonNullList<ItemStack> lastExtended;

    public static DeepNullMenu fromNetwork(int id, PlayerInventory inventory, PacketBuffer buffer) {
        Hand hand = buffer.readUnsignedByte() == 0 ? Hand.MAIN_HAND : Hand.OFF_HAND;
        DeepNullTier tier = DeepNullTier.byId(buffer.readUnsignedByte());
        ItemStack held = inventory.player.getItemInHand(hand);
        DeepNullItemHandler handler;
        if (held.getItem() instanceof DeepNullItem) {
            handler = ((DeepNullItem) held.getItem()).createHandler(held);
        } else {
            handler = new DeepNullItemHandler(new ItemStack(ModContent.DEEP_NULLS[tier.id()].get()), tier);
        }
        return new DeepNullMenu(id, inventory, handler, hand, tier);
    }

    public DeepNullMenu(int id, PlayerInventory inventory, DeepNullItemHandler handler, Hand hand, DeepNullTier tier) {
        super(ModContent.DEEP_NULL_MENU.get(), id);
        this.handler = handler;
        this.tier = tier;
        this.hand = hand;
        this.menuPlayer = inventory.player;
        this.lastExtended = NonNullList.withSize(tier.slots(), ItemStack.EMPTY);
        addStorageSlots();
        addPlayerSlots(inventory);
    }

    private void addStorageSlots() {
        int slot = 0;
        for (int row = 0; row < tier.rows(); row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new SlotItemHandler(handler, slot++, 8 + col * 18, 18 + row * 18));
            }
        }
    }

    private void addPlayerSlots(PlayerInventory inventory) {
        int startY = 32 + tier.rows() * 18;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, startY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            final int playerSlot = col;
            addSlot(new Slot(inventory, col, 8 + col * 18, startY + 58) {
                @Override
                public boolean mayPickup(PlayerEntity player) {
                    return !(hand == Hand.MAIN_HAND && inventory.selected == playerSlot);
                }
            });
        }
    }

    public DeepNullTier getTier() {
        return tier;
    }

    @Override
    public void addSlotListener(IContainerListener listener) {
        super.addSlotListener(listener);
        if (listener instanceof ServerPlayerEntity) syncExtended((ServerPlayerEntity) listener, true);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (menuPlayer instanceof ServerPlayerEntity) syncExtended((ServerPlayerEntity) menuPlayer, false);
    }

    private void syncExtended(ServerPlayerEntity player, boolean force) {
        for (int slot = 0; slot < tier.slots(); slot++) {
            ItemStack current = handler.getStackInSlot(slot);
            if (force || !ItemStack.matches(lastExtended.get(slot), current)) {
                lastExtended.set(slot, current.copy());
                DeepNullNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new ExtendedSlotPacket(containerId, slot, current));
            }
        }
    }

    @Override
    public ItemStack clicked(int slotId, int button, ClickType clickType, PlayerEntity player) {
        if (slotId >= 0 && slotId < tier.slots()) {
            if (clickType == ClickType.PICKUP) {
                ItemStack carried = player.inventory.getCarried();
                ItemStack stored = handler.getStackInSlot(slotId);
                if (carried.isEmpty()) {
                    int limit = stored.isEmpty() ? 0 : stored.getMaxStackSize();
                    int amount = button == 0 ? limit : (limit + 1) / 2;
                    player.inventory.setCarried(handler.extractItem(slotId, amount, false));
                } else {
                    int amount = button == 0 ? carried.getCount() : 1;
                    ItemStack offered = carried.copy();
                    offered.setCount(amount);
                    ItemStack remainder = handler.insertItem(slotId, offered, false);
                    int inserted = amount - remainder.getCount();
                    if (inserted > 0) carried.shrink(inserted);
                }
                return ItemStack.EMPTY;
            }
            if (clickType == ClickType.THROW) {
                ItemStack dropped = handler.extractItem(slotId, button == 0 ? 1 : 64, false);
                if (!dropped.isEmpty()) player.drop(dropped, true);
                return ItemStack.EMPTY;
            }
            if (clickType == ClickType.SWAP || clickType == ClickType.QUICK_CRAFT || clickType == ClickType.PICKUP_ALL) {
                return ItemStack.EMPTY;
            }
        }
        return super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        ItemStack held = player.getItemInHand(hand);
        return held.getItem() instanceof DeepNullItem && ((DeepNullItem) held.getItem()).getTier() == tier;
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return result;
        ItemStack source = slot.getItem();
        result = source.copy();
        int storageSlots = tier.slots();
        if (index < storageSlots) {
            if (!moveItemStackTo(source, storageSlots, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(source, 0, storageSlots, false)) return ItemStack.EMPTY;
        }
        if (source.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return result;
    }
}
