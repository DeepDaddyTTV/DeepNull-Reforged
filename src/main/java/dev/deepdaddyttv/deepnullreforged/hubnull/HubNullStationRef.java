package dev.deepdaddyttv.deepnullreforged.hubnull;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record HubNullStationRef(ResourceLocation dimension, BlockPos pos, String name) {
    private static final String DIMENSION_TAG = "Dimension";
    private static final String POS_TAG = "Pos";
    private static final String NAME_TAG = "Name";

    public HubNullStationRef {
        dimension = dimension == null ? ResourceLocation.withDefaultNamespace("overworld") : dimension;
        pos = pos == null ? BlockPos.ZERO : pos.immutable();
        name = name == null || name.isBlank() ? defaultName(pos) : name;
    }

    public boolean sameStation(HubNullStationRef other) {
        return other != null && dimension.equals(other.dimension) && pos.equals(other.pos);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(DIMENSION_TAG, dimension.toString());
        tag.putLong(POS_TAG, pos.asLong());
        tag.putString(NAME_TAG, name);
        return tag;
    }

    public static HubNullStationRef load(CompoundTag tag) {
        ResourceLocation dimension = ResourceLocation.tryParse(tag.getString(DIMENSION_TAG));
        if (dimension == null) {
            dimension = ResourceLocation.withDefaultNamespace("overworld");
        }
        BlockPos pos = tag.contains(POS_TAG, Tag.TAG_ANY_NUMERIC) ? BlockPos.of(tag.getLong(POS_TAG)) : BlockPos.ZERO;
        String name = tag.getString(NAME_TAG);
        return new HubNullStationRef(dimension, pos, name);
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeResourceLocation(dimension);
        buffer.writeBlockPos(pos);
        buffer.writeUtf(name);
    }

    public static HubNullStationRef read(RegistryFriendlyByteBuf buffer) {
        return new HubNullStationRef(buffer.readResourceLocation(), buffer.readBlockPos(), buffer.readUtf());
    }

    public String key() {
        return dimension + "@" + pos.toShortString();
    }

    private static String defaultName(BlockPos pos) {
        return "Dock " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof HubNullStationRef other)) {
            return false;
        }
        return dimension.equals(other.dimension) && pos.equals(other.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension, pos);
    }
}
