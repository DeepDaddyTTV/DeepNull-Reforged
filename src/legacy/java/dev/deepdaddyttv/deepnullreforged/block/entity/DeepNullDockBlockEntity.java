package dev.deepdaddyttv.deepnullreforged.block.entity;

import dev.deepdaddyttv.deepnullreforged.item.DampNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModContent;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DeepNullDockBlockEntity extends TileEntity {
    private final ItemStackHandler holder = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() instanceof DeepNullItem || stack.getItem() instanceof DampNullItem;
        }

        @Override
        public int getSlotLimit(int slot) { return 1; }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    };
    private final LazyOptional<ItemStackHandler> holderCapability = LazyOptional.of(() -> holder);

    public DeepNullDockBlockEntity() {
        super(ModContent.DEEP_NULL_DOCK_TILE.get());
    }

    public ItemStack getNull() { return holder.getStackInSlot(0); }

    public boolean insertNull(ItemStack stack) {
        if (!getNull().isEmpty() || !holder.isItemValid(0, stack)) return false;
        ItemStack one = stack.copy();
        one.setCount(1);
        holder.setStackInSlot(0, one);
        stack.shrink(1);
        return true;
    }

    public ItemStack removeNull() {
        return holder.extractItem(0, 1, false);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        ItemStack nested = getNull();
        if (!nested.isEmpty()) {
            LazyOptional<T> nestedCapability = nested.getCapability(capability, side);
            if (nestedCapability.isPresent()) return nestedCapability;
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return holderCapability.cast();
        return super.getCapability(capability, side);
    }

    @Override
    public CompoundNBT save(CompoundNBT root) {
        super.save(root);
        root.put("Null", holder.serializeNBT());
        return root;
    }

    @Override
    public void load(BlockState state, CompoundNBT root) {
        super.load(state, root);
        holder.deserializeNBT(root.getCompound("Null"));
    }

    @Override
    public CompoundNBT getUpdateTag() { return save(new CompoundNBT()); }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() { return new SUpdateTileEntityPacket(worldPosition, 0, getUpdateTag()); }

    @Override
    public void onDataPacket(NetworkManager network, SUpdateTileEntityPacket packet) {
        load(getBlockState(), packet.getTag());
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        holderCapability.invalidate();
    }
}
