package dev.deepdaddyttv.deepnullreforged.nullseed;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record NullSeedPreset(
        String presetId,
        String displayName,
        NullSeedPresetSource source,
        String namespace,
        List<NullSeedKind> supportedKinds,
        List<NullSeedEntry> entries
) {
    private static final String ID_TAG = "Id";
    private static final String NAME_TAG = "Name";
    private static final String SOURCE_TAG = "Source";
    private static final String NAMESPACE_TAG = "Namespace";
    private static final String SUPPORTED_TAG = "Supported";
    private static final String ENTRIES_TAG = "Entries";
    private static final String KIND_TAG = "Kind";
    private static final String TARGET_TAG = "Target";
    private static final String ENTRY_ID_TAG = "EntryId";
    private static final String AMOUNT_TAG = "Amount";
    private static final String DUMP_STATUS_TAG = "DumpStatus";

    public NullSeedPreset {
        presetId = presetId == null ? "" : presetId;
        displayName = displayName == null || displayName.isBlank() ? presetId : displayName;
        source = source == null ? NullSeedPresetSource.BUILT_IN : source;
        namespace = namespace == null ? "" : namespace;
        supportedKinds = List.copyOf(supportedKinds == null ? List.of() : supportedKinds.stream().filter(kind -> kind != null).distinct().toList());
        entries = List.copyOf(entries == null ? List.of() : entries.stream().filter(entry -> entry != null).toList());
    }

    public static NullSeedPreset empty(String presetId) {
        return new NullSeedPreset(presetId, presetId, NullSeedPresetSource.BUILT_IN, "", List.of(), List.of());
    }

    public NullSeedPresetSummary summary() {
        return new NullSeedPresetSummary(presetId, displayName, source, namespace, entries.size());
    }

    public boolean supports(NullSeedKind targetKind) {
        return targetKind != null && supportedKinds.contains(targetKind);
    }

    public NullSeedPreset withIdAndName(String presetId, String displayName) {
        return new NullSeedPreset(presetId, displayName, source, namespace, supportedKinds, entries);
    }

    public NullSeedPreset asUserPreset() {
        return new NullSeedPreset(presetId, displayName, NullSeedPresetSource.USER, namespace, supportedKinds, entries);
    }

    public static void write(RegistryFriendlyByteBuf buffer, NullSeedPreset preset) {
        NullSeedPreset value = preset == null ? empty("") : preset;
        buffer.writeUtf(value.presetId());
        buffer.writeUtf(value.displayName());
        buffer.writeVarInt(value.source().ordinal());
        buffer.writeUtf(value.namespace());
        buffer.writeVarInt(value.supportedKinds().size());
        for (NullSeedKind kind : value.supportedKinds()) {
            buffer.writeVarInt(kind.ordinal());
        }
        NullSeedEntry.writeList(buffer, value.entries());
    }

    public static NullSeedPreset read(RegistryFriendlyByteBuf buffer) {
        String id = buffer.readUtf();
        String name = buffer.readUtf();
        NullSeedPresetSource source = NullSeedPresetSource.byId(buffer.readVarInt());
        String namespace = buffer.readUtf();
        int kindCount = Math.max(0, Math.min(buffer.readVarInt(), NullSeedKind.values().length));
        List<NullSeedKind> supportedKinds = new ArrayList<>(kindCount);
        for (int i = 0; i < kindCount; i++) {
            supportedKinds.add(NullSeedKind.byId(buffer.readVarInt()));
        }
        return new NullSeedPreset(id, name, source, namespace, supportedKinds, NullSeedEntry.readList(buffer));
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(ID_TAG, presetId);
        tag.putString(NAME_TAG, displayName);
        tag.putInt(SOURCE_TAG, source.ordinal());
        tag.putString(NAMESPACE_TAG, namespace);
        ListTag supported = new ListTag();
        for (NullSeedKind kind : supportedKinds) {
            CompoundTag entry = new CompoundTag();
            entry.putInt(KIND_TAG, kind.ordinal());
            supported.add(entry);
        }
        tag.put(SUPPORTED_TAG, supported);
        ListTag entryList = new ListTag();
        for (NullSeedEntry seedEntry : entries) {
            CompoundTag entry = new CompoundTag();
            entry.putInt(KIND_TAG, seedEntry.kind().ordinal());
            entry.putInt(TARGET_TAG, seedEntry.targetIndex());
            entry.putString(ENTRY_ID_TAG, seedEntry.id().toString());
            entry.putInt(AMOUNT_TAG, seedEntry.amount());
            entry.putInt(DUMP_STATUS_TAG, seedEntry.dumpStatus().ordinal());
            entryList.add(entry);
        }
        tag.put(ENTRIES_TAG, entryList);
        return tag;
    }

    public static NullSeedPreset load(CompoundTag tag) {
        List<NullSeedKind> supportedKinds = new ArrayList<>();
        if (tag.contains(SUPPORTED_TAG, Tag.TAG_LIST)) {
            ListTag list = tag.getList(SUPPORTED_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                supportedKinds.add(NullSeedKind.byId(list.getCompound(i).getInt(KIND_TAG)));
            }
        }
        List<NullSeedEntry> entries = new ArrayList<>();
        if (tag.contains(ENTRIES_TAG, Tag.TAG_LIST)) {
            ListTag list = tag.getList(ENTRIES_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                ResourceLocation id = ResourceLocation.tryParse(entry.getString(ENTRY_ID_TAG));
                if (id == null) {
                    continue;
                }
                entries.add(new NullSeedEntry(
                        NullSeedKind.byId(entry.getInt(KIND_TAG)),
                        entry.getInt(TARGET_TAG),
                        id,
                        entry.getInt(AMOUNT_TAG),
                        dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemStatus.byId(entry.getInt(DUMP_STATUS_TAG))
                ));
            }
        }
        return new NullSeedPreset(
                tag.getString(ID_TAG),
                tag.getString(NAME_TAG),
                NullSeedPresetSource.byId(tag.getInt(SOURCE_TAG)),
                tag.getString(NAMESPACE_TAG),
                supportedKinds,
                entries
        ).asUserPreset();
    }
}
