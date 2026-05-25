package dev.deepdaddyttv.deepnullreforged.block.entity;

import dev.deepdaddyttv.deepnullreforged.item.DripNullItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DripStandBlockEntity extends BlockEntity {
    private static final String STORED_DRIP_TAG = "StoredDripNull";
    private ItemStack storedDripNull = ItemStack.EMPTY;

    public DripStandBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DRIP_STAND.get(), pos, blockState);
    }

    public boolean hasStoredDripNull() {
        return !storedDripNull.isEmpty() && storedDripNull.getItem() instanceof DripNullItem;
    }

    public ItemStack getStoredDripNull() {
        return storedDripNull;
    }

    public void setStoredDripNull(ItemStack stack) {
        storedDripNull = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        setChangedAndSync();
    }

    public void setStoredDripNullClient(ItemStack stack) {
        storedDripNull = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        Level level = getLevel();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    public ItemStack removeStoredDripNull() {
        ItemStack result = storedDripNull.copy();
        storedDripNull = ItemStack.EMPTY;
        setChangedAndSync();
        return result;
    }

    public void markStoredDripNullChanged() {
        setChangedAndSync();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (hasStoredDripNull()) {
            tag.put(STORED_DRIP_TAG, storedDripNull.saveOptional(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        storedDripNull = tag.contains(STORED_DRIP_TAG, Tag.TAG_COMPOUND)
                ? ItemStack.parseOptional(registries, tag.getCompound(STORED_DRIP_TAG))
                : ItemStack.EMPTY;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void setChangedAndSync() {
        setChanged();
        Level level = getLevel();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }
}
