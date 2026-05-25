package dev.deepdaddyttv.deepnullreforged.dumpnull;

import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record DumpNullData(
        List<ResourceLocation> selectedMobs,
        List<DumpNullRule> rules,
        boolean active,
        DumpNullHeldDiscardMode heldDiscardMode,
        DumpNullDockDiscardMode dockDiscardMode,
        Map<Direction, DumpNullSideMode> sideModes,
        Set<Direction> discardExportSides,
        List<DumpNullBufferEntry> inputBuffer,
        List<DumpNullBufferEntry> outputBuffer,
        List<DumpNullBufferEntry> discardBuffer,
        Set<DumpNullUpgradeType> upgrades,
        int storedEnergy
) {
    public static final int INPUT_BUFFER_SLOTS = 9;
    public static final int OUTPUT_BUFFER_SLOTS = 9;
    public static final int DISCARD_BUFFER_SLOTS = 9;
    public static final int MAX_BUFFER_STACK_SIZE = 64;
    public static final DumpNullData EMPTY = new DumpNullData(List.of(), List.of());

    private static final String ROOT_TAG = "DumpNull";
    private static final String SELECTED_MOBS_TAG = "SelectedMobs";
    private static final String RULES_TAG = "Rules";
    private static final String ACTIVE_TAG = "Active";
    private static final String HELD_DISCARD_MODE_TAG = "HeldDiscardMode";
    private static final String DOCK_DISCARD_MODE_TAG = "DockDiscardMode";
    private static final String SIDE_MODES_TAG = "SideModes";
    private static final String DISCARD_EXPORT_SIDES_TAG = "DiscardExportSides";
    private static final String INPUT_BUFFER_TAG = "InputBuffer";
    private static final String OUTPUT_BUFFER_TAG = "OutputBuffer";
    private static final String DISCARD_BUFFER_TAG = "DiscardBuffer";
    private static final String UPGRADES_TAG = "Upgrades";
    private static final String ENERGY_TAG = "Energy";
    private static final String SIDE_TAG = "Side";
    private static final String MODE_TAG = "Mode";

    public DumpNullData(List<ResourceLocation> selectedMobs, List<DumpNullRule> rules) {
        this(
                selectedMobs,
                rules,
                false,
                DumpNullHeldDiscardMode.BLOCK_PICKUP,
                DumpNullDockDiscardMode.VOID,
                defaultSideModes(),
                Set.of(),
                List.of(),
                List.of(),
                List.of(),
                Set.of(),
                0
        );
    }

    public DumpNullData {
        selectedMobs = List.copyOf(selectedMobs == null ? List.of() : selectedMobs);
        rules = List.copyOf(rules == null ? List.of() : rules);
        heldDiscardMode = heldDiscardMode == null ? DumpNullHeldDiscardMode.BLOCK_PICKUP : heldDiscardMode;
        dockDiscardMode = dockDiscardMode == null ? DumpNullDockDiscardMode.VOID : dockDiscardMode;
        sideModes = Map.copyOf(normalizeSideModes(sideModes));
        discardExportSides = Set.copyOf(normalizeDirectionSet(discardExportSides));
        inputBuffer = normalizeBuffer(inputBuffer, INPUT_BUFFER_SLOTS);
        outputBuffer = normalizeBuffer(outputBuffer, OUTPUT_BUFFER_SLOTS);
        discardBuffer = normalizeBuffer(discardBuffer, DISCARD_BUFFER_SLOTS);
        upgrades = Set.copyOf(upgrades == null ? Set.of() : upgrades);
        storedEnergy = Math.max(0, Math.min(storedEnergy, energyCapacity(upgrades)));
    }

    public static DumpNullData get(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return EMPTY;
        }
        CompoundTag tag = customData.copyTag();
        if (!tag.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            return EMPTY;
        }
        return load(tag.getCompound(ROOT_TAG));
    }

    public static void set(ItemStack stack, DumpNullData data) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put(ROOT_TAG, (data == null ? EMPTY : data).save()));
    }

    public static DumpNullData load(CompoundTag root) {
        List<ResourceLocation> mobs = new ArrayList<>();
        if (root.contains(SELECTED_MOBS_TAG, Tag.TAG_LIST)) {
            ListTag mobList = root.getList(SELECTED_MOBS_TAG, Tag.TAG_STRING);
            for (int i = 0; i < mobList.size(); i++) {
                ResourceLocation mobId = ResourceLocation.tryParse(mobList.getString(i));
                if (mobId != null) {
                    mobs.add(mobId);
                }
            }
        }

        List<DumpNullRule> rules = new ArrayList<>();
        if (root.contains(RULES_TAG, Tag.TAG_LIST)) {
            ListTag ruleList = root.getList(RULES_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < ruleList.size(); i++) {
                rules.add(DumpNullRule.load(ruleList.getCompound(i)));
            }
        }

        Map<Direction, DumpNullSideMode> sideModes = defaultSideModes();
        if (root.contains(SIDE_MODES_TAG, Tag.TAG_LIST)) {
            ListTag sideList = root.getList(SIDE_MODES_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < sideList.size(); i++) {
                CompoundTag entry = sideList.getCompound(i);
                Direction direction = Direction.byName(entry.getString(SIDE_TAG));
                if (direction != null) {
                    sideModes.put(direction, DumpNullSideMode.byName(entry.getString(MODE_TAG)));
                }
            }
        }

        Set<Direction> discardExportSides = new LinkedHashSet<>();
        if (root.contains(DISCARD_EXPORT_SIDES_TAG, Tag.TAG_LIST)) {
            ListTag sideList = root.getList(DISCARD_EXPORT_SIDES_TAG, Tag.TAG_STRING);
            for (int i = 0; i < sideList.size(); i++) {
                Direction direction = Direction.byName(sideList.getString(i));
                if (direction != null) {
                    discardExportSides.add(direction);
                }
            }
        }

        Set<DumpNullUpgradeType> upgrades = EnumSet.noneOf(DumpNullUpgradeType.class);
        if (root.contains(UPGRADES_TAG, Tag.TAG_LIST)) {
            ListTag upgradeList = root.getList(UPGRADES_TAG, Tag.TAG_STRING);
            for (int i = 0; i < upgradeList.size(); i++) {
                DumpNullUpgradeType type = DumpNullUpgradeType.byItemId(upgradeList.getString(i));
                if (type != null) {
                    upgrades.add(type);
                }
            }
        }

        return new DumpNullData(
                mobs,
                rules,
                root.getBoolean(ACTIVE_TAG),
                DumpNullHeldDiscardMode.byName(root.getString(HELD_DISCARD_MODE_TAG)),
                DumpNullDockDiscardMode.byName(root.getString(DOCK_DISCARD_MODE_TAG)),
                sideModes,
                discardExportSides,
                readBuffer(root, INPUT_BUFFER_TAG, INPUT_BUFFER_SLOTS),
                readBuffer(root, OUTPUT_BUFFER_TAG, OUTPUT_BUFFER_SLOTS),
                readBuffer(root, DISCARD_BUFFER_TAG, DISCARD_BUFFER_SLOTS),
                upgrades,
                root.getInt(ENERGY_TAG)
        );
    }

    public CompoundTag save() {
        CompoundTag root = new CompoundTag();
        ListTag mobList = new ListTag();
        for (ResourceLocation mob : selectedMobs) {
            mobList.add(StringTag.valueOf(mob.toString()));
        }
        root.put(SELECTED_MOBS_TAG, mobList);

        ListTag ruleList = new ListTag();
        for (DumpNullRule rule : rules) {
            ruleList.add(rule.save());
        }
        root.put(RULES_TAG, ruleList);
        root.putBoolean(ACTIVE_TAG, active);
        root.putString(HELD_DISCARD_MODE_TAG, heldDiscardMode.name());
        root.putString(DOCK_DISCARD_MODE_TAG, dockDiscardMode.name());

        ListTag sideList = new ListTag();
        for (Direction direction : Direction.values()) {
            CompoundTag entry = new CompoundTag();
            entry.putString(SIDE_TAG, direction.getName());
            entry.putString(MODE_TAG, sideMode(direction).name());
            sideList.add(entry);
        }
        root.put(SIDE_MODES_TAG, sideList);

        ListTag discardSideList = new ListTag();
        for (Direction direction : Direction.values()) {
            if (discardExportSides.contains(direction)) {
                discardSideList.add(StringTag.valueOf(direction.getName()));
            }
        }
        root.put(DISCARD_EXPORT_SIDES_TAG, discardSideList);
        writeBuffer(root, INPUT_BUFFER_TAG, inputBuffer);
        writeBuffer(root, OUTPUT_BUFFER_TAG, outputBuffer);
        writeBuffer(root, DISCARD_BUFFER_TAG, discardBuffer);

        ListTag upgradeList = new ListTag();
        for (DumpNullUpgradeType type : upgrades) {
            upgradeList.add(StringTag.valueOf(type.itemId()));
        }
        root.put(UPGRADES_TAG, upgradeList);
        root.putInt(ENERGY_TAG, storedEnergy);
        return root;
    }

    public static void write(RegistryFriendlyByteBuf buffer, DumpNullData data) {
        DumpNullData value = data == null ? EMPTY : data;
        buffer.writeVarInt(value.selectedMobs.size());
        for (ResourceLocation mob : value.selectedMobs) {
            buffer.writeResourceLocation(mob);
        }
        buffer.writeVarInt(value.rules.size());
        for (DumpNullRule rule : value.rules) {
            buffer.writeNbt(rule.save());
        }
        buffer.writeBoolean(value.active);
        buffer.writeVarInt(value.heldDiscardMode.ordinal());
        buffer.writeVarInt(value.dockDiscardMode.ordinal());
        for (Direction direction : Direction.values()) {
            buffer.writeVarInt(value.sideMode(direction).ordinal());
            buffer.writeBoolean(value.discardExportSides.contains(direction));
        }
        writeBuffer(buffer, value.inputBuffer);
        writeBuffer(buffer, value.outputBuffer);
        writeBuffer(buffer, value.discardBuffer);
        buffer.writeVarInt(value.upgrades.size());
        for (DumpNullUpgradeType type : value.upgrades) {
            buffer.writeVarInt(type.ordinal());
        }
        buffer.writeVarInt(value.storedEnergy);
    }

    public static DumpNullData read(RegistryFriendlyByteBuf buffer) {
        int mobCount = Math.max(0, Math.min(512, buffer.readVarInt()));
        List<ResourceLocation> mobs = new ArrayList<>(mobCount);
        for (int i = 0; i < mobCount; i++) {
            mobs.add(buffer.readResourceLocation());
        }
        int ruleCount = Math.max(0, Math.min(512, buffer.readVarInt()));
        List<DumpNullRule> rules = new ArrayList<>(ruleCount);
        for (int i = 0; i < ruleCount; i++) {
            CompoundTag tag = buffer.readNbt();
            if (tag != null) {
                rules.add(DumpNullRule.load(tag));
            }
        }
        boolean active = buffer.readBoolean();
        DumpNullHeldDiscardMode heldMode = DumpNullHeldDiscardMode.byId(buffer.readVarInt());
        DumpNullDockDiscardMode dockMode = DumpNullDockDiscardMode.byId(buffer.readVarInt());
        Map<Direction, DumpNullSideMode> sideModes = defaultSideModes();
        Set<Direction> discardExportSides = new LinkedHashSet<>();
        for (Direction direction : Direction.values()) {
            sideModes.put(direction, DumpNullSideMode.byId(buffer.readVarInt()));
            if (buffer.readBoolean()) {
                discardExportSides.add(direction);
            }
        }
        List<DumpNullBufferEntry> input = readBuffer(buffer, INPUT_BUFFER_SLOTS);
        List<DumpNullBufferEntry> output = readBuffer(buffer, OUTPUT_BUFFER_SLOTS);
        List<DumpNullBufferEntry> discard = readBuffer(buffer, DISCARD_BUFFER_SLOTS);
        int upgradeCount = Math.max(0, Math.min(DumpNullUpgradeType.values().length, buffer.readVarInt()));
        Set<DumpNullUpgradeType> upgrades = EnumSet.noneOf(DumpNullUpgradeType.class);
        for (int i = 0; i < upgradeCount; i++) {
            upgrades.add(DumpNullUpgradeType.byId(buffer.readVarInt()));
        }
        return new DumpNullData(mobs, rules, active, heldMode, dockMode, sideModes, discardExportSides, input, output, discard, upgrades, buffer.readVarInt());
    }

    public DumpNullSideMode sideMode(Direction direction) {
        return sideModes.getOrDefault(direction, DumpNullSideMode.BOTH);
    }

    public boolean isDiscardExportSide(Direction direction) {
        return direction != null && discardExportSides.contains(direction);
    }

    public boolean hasUpgrade(DumpNullUpgradeType type) {
        return upgrades.contains(type);
    }

    public int energyCapacity() {
        return energyCapacity(upgrades);
    }

    public int energyTransferRate() {
        return hasUpgrade(DumpNullUpgradeType.POWER) ? DeepNullConfig.getDumpNullEnergyTransferRate() : 0;
    }

    public DumpNullData withActive(boolean active) {
        return rebuild(selectedMobs, rules, active, heldDiscardMode, dockDiscardMode, sideModes, discardExportSides, inputBuffer, outputBuffer, discardBuffer, upgrades, storedEnergy);
    }

    public DumpNullData toggleActive() {
        return withActive(!active);
    }

    public DumpNullData withHeldDiscardMode(DumpNullHeldDiscardMode mode) {
        return rebuild(selectedMobs, rules, active, mode, dockDiscardMode, sideModes, discardExportSides, inputBuffer, outputBuffer, discardBuffer, upgrades, storedEnergy);
    }

    public DumpNullData withDockDiscardMode(DumpNullDockDiscardMode mode) {
        return rebuild(selectedMobs, rules, active, heldDiscardMode, mode, sideModes, discardExportSides, inputBuffer, outputBuffer, discardBuffer, upgrades, storedEnergy);
    }

    public DumpNullData withSideMode(Direction direction, DumpNullSideMode mode) {
        if (direction == null) {
            return this;
        }
        Map<Direction, DumpNullSideMode> next = new EnumMap<>(Direction.class);
        next.putAll(sideModes);
        next.put(direction, mode == null ? DumpNullSideMode.BOTH : mode);
        return rebuild(selectedMobs, rules, active, heldDiscardMode, dockDiscardMode, next, discardExportSides, inputBuffer, outputBuffer, discardBuffer, upgrades, storedEnergy);
    }

    public DumpNullData withDiscardExportSide(Direction direction, boolean enabled) {
        if (direction == null) {
            return this;
        }
        Set<Direction> next = EnumSet.noneOf(Direction.class);
        next.addAll(discardExportSides);
        if (enabled) {
            next.add(direction);
        } else {
            next.remove(direction);
        }
        return rebuild(selectedMobs, rules, active, heldDiscardMode, dockDiscardMode, sideModes, next, inputBuffer, outputBuffer, discardBuffer, upgrades, storedEnergy);
    }

    public DumpNullData withUpgrade(DumpNullUpgradeType type) {
        if (type == null || upgrades.contains(type)) {
            return this;
        }
        Set<DumpNullUpgradeType> next = EnumSet.noneOf(DumpNullUpgradeType.class);
        next.addAll(upgrades);
        next.add(type);
        return rebuild(selectedMobs, rules, active, heldDiscardMode, dockDiscardMode, sideModes, discardExportSides, inputBuffer, outputBuffer, discardBuffer, next, storedEnergy);
    }

    public EnergyMutation receiveEnergy(int maxReceive, boolean simulate) {
        if (!hasUpgrade(DumpNullUpgradeType.POWER) || maxReceive <= 0) {
            return new EnergyMutation(this, 0);
        }
        int accepted = Math.min(Math.min(maxReceive, energyTransferRate()), energyCapacity() - storedEnergy);
        if (accepted <= 0 || simulate) {
            return new EnergyMutation(this, Math.max(0, accepted));
        }
        return new EnergyMutation(withStoredEnergy(storedEnergy + accepted), accepted);
    }

    public EnergyMutation extractEnergy(int maxExtract, boolean simulate) {
        if (!hasUpgrade(DumpNullUpgradeType.POWER) || maxExtract <= 0) {
            return new EnergyMutation(this, 0);
        }
        int extracted = Math.min(Math.min(maxExtract, energyTransferRate()), storedEnergy);
        if (extracted <= 0 || simulate) {
            return new EnergyMutation(this, Math.max(0, extracted));
        }
        return new EnergyMutation(withStoredEnergy(storedEnergy - extracted), extracted);
    }

    public EnergyMutation generateEnergy(int amount, boolean simulate) {
        if (!hasUpgrade(DumpNullUpgradeType.POWER) || amount <= 0) {
            return new EnergyMutation(this, 0);
        }
        int accepted = Math.min(amount, energyCapacity() - storedEnergy);
        if (accepted <= 0 || simulate) {
            return new EnergyMutation(this, Math.max(0, accepted));
        }
        return new EnergyMutation(withStoredEnergy(storedEnergy + accepted), accepted);
    }

    public DumpNullData withStoredEnergy(int energy) {
        return rebuild(selectedMobs, rules, active, heldDiscardMode, dockDiscardMode, sideModes, discardExportSides, inputBuffer, outputBuffer, discardBuffer, upgrades, energy);
    }

    public BufferMutation insertInput(ItemStack stack) {
        BufferMutation mutation = insertIntoBuffer(inputBuffer, stack, INPUT_BUFFER_SLOTS);
        return new BufferMutation(withInputBuffer(mutation.buffer()), mutation.remainder(), mutation.buffer());
    }

    public BufferMutation insertOutput(ItemStack stack) {
        BufferMutation mutation = insertIntoBuffer(outputBuffer, stack, OUTPUT_BUFFER_SLOTS);
        return new BufferMutation(withOutputBuffer(mutation.buffer()), mutation.remainder(), mutation.buffer());
    }

    public BufferMutation insertDiscard(ItemStack stack) {
        BufferMutation mutation = insertIntoBuffer(discardBuffer, stack, DISCARD_BUFFER_SLOTS);
        return new BufferMutation(withDiscardBuffer(mutation.buffer()), mutation.remainder(), mutation.buffer());
    }

    public ExtractionMutation extractOutput(int slot, int amount) {
        ExtractionMutation mutation = extractFromBuffer(outputBuffer, slot, amount, OUTPUT_BUFFER_SLOTS);
        return new ExtractionMutation(withOutputBuffer(mutation.buffer()), mutation.extracted(), mutation.buffer());
    }

    public ExtractionMutation extractDiscard(int slot, int amount) {
        ExtractionMutation mutation = extractFromBuffer(discardBuffer, slot, amount, DISCARD_BUFFER_SLOTS);
        return new ExtractionMutation(withDiscardBuffer(mutation.buffer()), mutation.extracted(), mutation.buffer());
    }

    public DumpNullData withInputBuffer(List<DumpNullBufferEntry> buffer) {
        return rebuild(selectedMobs, rules, active, heldDiscardMode, dockDiscardMode, sideModes, discardExportSides, buffer, outputBuffer, discardBuffer, upgrades, storedEnergy);
    }

    public DumpNullData withOutputBuffer(List<DumpNullBufferEntry> buffer) {
        return rebuild(selectedMobs, rules, active, heldDiscardMode, dockDiscardMode, sideModes, discardExportSides, inputBuffer, buffer, discardBuffer, upgrades, storedEnergy);
    }

    public DumpNullData withDiscardBuffer(List<DumpNullBufferEntry> buffer) {
        return rebuild(selectedMobs, rules, active, heldDiscardMode, dockDiscardMode, sideModes, discardExportSides, inputBuffer, outputBuffer, buffer, upgrades, storedEnergy);
    }

    public DumpNullData withMob(ResourceLocation mobId, boolean selected) {
        List<ResourceLocation> mobs = new ArrayList<>(selectedMobs);
        if (selected) {
            if (!mobs.contains(mobId)) {
                mobs.add(mobId);
            }
        } else {
            mobs.remove(mobId);
        }
        return withCore(mobs, rules);
    }

    public DumpNullData clearMobs() {
        return withCore(List.of(), rules);
    }

    public DumpNullData withSelectedMobs(List<ResourceLocation> mobs) {
        return withCore(mobs, rules);
    }

    public DumpNullData addRule(DumpNullRule rule) {
        List<DumpNullRule> updated = new ArrayList<>(rules);
        updated.add(rule);
        return withCore(selectedMobs, updated);
    }

    public DumpNullData removeRule(int index) {
        if (index < 0 || index >= rules.size()) {
            return this;
        }
        List<DumpNullRule> updated = new ArrayList<>(rules);
        updated.remove(index);
        return withCore(selectedMobs, updated);
    }

    public DumpNullData moveRule(int index, int delta) {
        int target = index + delta;
        if (index < 0 || index >= rules.size() || target < 0 || target >= rules.size()) {
            return this;
        }
        List<DumpNullRule> updated = new ArrayList<>(rules);
        DumpNullRule rule = updated.remove(index);
        updated.add(target, rule);
        return withCore(selectedMobs, updated);
    }

    public DumpNullData applyPreset(DumpNullPresetResult preset) {
        List<DumpNullRule> updatedRules = new ArrayList<>();
        for (DumpNullRule rule : rules) {
            if (!rule.isPresetGenerated()) {
                updatedRules.add(rule);
            }
        }
        updatedRules.addAll(preset.presetRules());
        return withCore(preset.selectedMobs(), updatedRules);
    }

    public DumpNullData withItemStatus(ResourceLocation itemId, DumpNullItemStatus status) {
        if (itemId == null) {
            return this;
        }
        return withItemStatuses(List.of(itemId), status);
    }

    public DumpNullData withItemStatuses(List<ResourceLocation> itemIds, DumpNullItemStatus status) {
        if (itemIds == null || itemIds.isEmpty()) {
            return this;
        }
        List<ResourceLocation> normalizedItemIds = new ArrayList<>();
        for (ResourceLocation itemId : itemIds) {
            if (itemId != null && !normalizedItemIds.contains(itemId)) {
                normalizedItemIds.add(itemId);
            }
        }
        if (normalizedItemIds.isEmpty()) {
            return this;
        }
        DumpNullItemStatus normalizedStatus = status == null ? DumpNullItemStatus.NEUTRAL : status;
        List<DumpNullRule> updated = new ArrayList<>();
        for (DumpNullRule rule : rules) {
            if (!normalizedItemIds.contains(rule.itemId()) || !rule.isSimpleStatusRuleFor(rule.itemId())) {
                updated.add(rule);
            }
        }
        if (normalizedStatus != DumpNullItemStatus.NEUTRAL) {
            for (ResourceLocation itemId : normalizedItemIds) {
                updated.add(DumpNullRule.basic(normalizedStatus.toRuleAction(), itemId, 0, List.of(), List.of()));
            }
        }
        return withCore(selectedMobs, updated);
    }

    public DumpNullData withItemFilter(
            ResourceLocation itemId,
            DumpNullItemStatus status,
            int minDurabilityPercent,
            List<ResourceLocation> requiredEnchantments,
            List<ResourceLocation> forbiddenEnchantments
    ) {
        if (itemId == null) {
            return this;
        }
        DumpNullItemStatus normalizedStatus = status == null ? DumpNullItemStatus.NEUTRAL : status;
        List<DumpNullRule> updated = rules.stream()
                .filter(rule -> !rule.itemId().equals(itemId))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        if (normalizedStatus == DumpNullItemStatus.NEUTRAL) {
            return withCore(selectedMobs, updated);
        }

        int normalizedDurability = Math.max(0, Math.min(100, minDurabilityPercent));
        List<ResourceLocation> required = List.copyOf(requiredEnchantments == null ? List.of() : requiredEnchantments);
        List<ResourceLocation> forbidden = List.copyOf(forbiddenEnchantments == null ? List.of() : forbiddenEnchantments);
        boolean advanced = normalizedDurability > 0 || !required.isEmpty() || !forbidden.isEmpty();
        DumpNullRuleAction action = normalizedStatus.toRuleAction();
        updated.add(DumpNullRule.basic(action, itemId, normalizedDurability, required, forbidden));
        if (advanced) {
            DumpNullRuleAction fallback = action == DumpNullRuleAction.PASS ? DumpNullRuleAction.VOID : DumpNullRuleAction.PASS;
            updated.add(DumpNullRule.basic(fallback, itemId, 0, List.of(), List.of()));
        }
        return withCore(selectedMobs, updated);
    }

    public DumpNullData clearItemFilter(ResourceLocation itemId) {
        if (itemId == null) {
            return this;
        }
        return clearItemFilters(List.of(itemId));
    }

    public DumpNullData clearItemFilters(List<ResourceLocation> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return this;
        }
        List<ResourceLocation> normalizedItemIds = new ArrayList<>();
        for (ResourceLocation itemId : itemIds) {
            if (itemId != null && !normalizedItemIds.contains(itemId)) {
                normalizedItemIds.add(itemId);
            }
        }
        if (normalizedItemIds.isEmpty()) {
            return this;
        }
        List<DumpNullRule> updated = rules.stream()
                .filter(rule -> !normalizedItemIds.contains(rule.itemId()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        return withCore(selectedMobs, updated);
    }

    public DumpNullItemStatus statusFor(ResourceLocation itemId) {
        DumpNullItemStatusSummary summary = itemStatusSummaryMap().get(itemId);
        return summary == null ? DumpNullItemStatus.NEUTRAL : summary.status();
    }

    public List<DumpNullItemStatusSummary> itemStatusSummaries() {
        return itemStatusSummaryMap().values().stream()
                .sorted(Comparator.comparing(summary -> summary.itemId().toString()))
                .toList();
    }

    private DumpNullData withCore(List<ResourceLocation> selectedMobs, List<DumpNullRule> rules) {
        return rebuild(selectedMobs, rules, active, heldDiscardMode, dockDiscardMode, sideModes, discardExportSides, inputBuffer, outputBuffer, discardBuffer, upgrades, storedEnergy);
    }

    private DumpNullData rebuild(
            List<ResourceLocation> selectedMobs,
            List<DumpNullRule> rules,
            boolean active,
            DumpNullHeldDiscardMode heldDiscardMode,
            DumpNullDockDiscardMode dockDiscardMode,
            Map<Direction, DumpNullSideMode> sideModes,
            Set<Direction> discardExportSides,
            List<DumpNullBufferEntry> inputBuffer,
            List<DumpNullBufferEntry> outputBuffer,
            List<DumpNullBufferEntry> discardBuffer,
            Set<DumpNullUpgradeType> upgrades,
            int storedEnergy
    ) {
        return new DumpNullData(selectedMobs, rules, active, heldDiscardMode, dockDiscardMode, sideModes, discardExportSides,
                inputBuffer, outputBuffer, discardBuffer, upgrades, storedEnergy);
    }

    private Map<ResourceLocation, DumpNullItemStatusSummary> itemStatusSummaryMap() {
        Map<ResourceLocation, MutableStatusSummary> summaries = new LinkedHashMap<>();
        for (DumpNullRule rule : rules) {
            MutableStatusSummary summary = summaries.computeIfAbsent(rule.itemId(), ignored -> new MutableStatusSummary(rule.itemId()));
            summary.presetGenerated |= rule.isPresetGenerated();
            if (rule.isAdvanced()) {
                summary.advanced = true;
                if (!summary.advancedStatusSet) {
                    summary.status = DumpNullItemStatus.fromAction(rule.action());
                    summary.advancedStatusSet = true;
                }
            } else if (!summary.advancedStatusSet && summary.status == DumpNullItemStatus.NEUTRAL) {
                summary.status = DumpNullItemStatus.fromAction(rule.action());
            }
        }

        Map<ResourceLocation, DumpNullItemStatusSummary> result = new LinkedHashMap<>();
        for (MutableStatusSummary summary : summaries.values()) {
            if (summary.status != DumpNullItemStatus.NEUTRAL) {
                result.put(summary.itemId, summary.toSummary());
            }
        }
        return result;
    }

    private static int energyCapacity(Set<DumpNullUpgradeType> upgrades) {
        return upgrades != null && upgrades.contains(DumpNullUpgradeType.POWER) ? DeepNullConfig.getDumpNullEnergyCapacity() : 0;
    }

    private static Map<Direction, DumpNullSideMode> defaultSideModes() {
        EnumMap<Direction, DumpNullSideMode> modes = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            modes.put(direction, DumpNullSideMode.BOTH);
        }
        return modes;
    }

    private static Map<Direction, DumpNullSideMode> normalizeSideModes(Map<Direction, DumpNullSideMode> modes) {
        EnumMap<Direction, DumpNullSideMode> normalized = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            DumpNullSideMode mode = modes == null ? null : modes.get(direction);
            normalized.put(direction, mode == null ? DumpNullSideMode.BOTH : mode);
        }
        return normalized;
    }

    private static Set<Direction> normalizeDirectionSet(Set<Direction> directions) {
        EnumSet<Direction> normalized = EnumSet.noneOf(Direction.class);
        if (directions != null) {
            for (Direction direction : directions) {
                if (direction != null) {
                    normalized.add(direction);
                }
            }
        }
        return normalized;
    }

    private static List<DumpNullBufferEntry> normalizeBuffer(List<DumpNullBufferEntry> entries, int maxSlots) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<DumpNullBufferEntry> normalized = new ArrayList<>();
        for (DumpNullBufferEntry entry : entries) {
            if (entry == null || entry.isEmpty()) {
                continue;
            }
            int remaining = entry.count();
            while (remaining > 0 && normalized.size() < maxSlots) {
                int count = Math.min(MAX_BUFFER_STACK_SIZE, remaining);
                normalized.add(entry.withCount(count));
                remaining -= count;
            }
            if (normalized.size() >= maxSlots) {
                break;
            }
        }
        return List.copyOf(normalized);
    }

    private static BufferMutation insertIntoBuffer(List<DumpNullBufferEntry> buffer, ItemStack stack, int maxSlots) {
        if (stack == null || stack.isEmpty()) {
            return new BufferMutation(EMPTY, ItemStack.EMPTY, normalizeBuffer(buffer, maxSlots));
        }
        List<DumpNullBufferEntry> updated = new ArrayList<>(normalizeBuffer(buffer, maxSlots));
        int remaining = stack.getCount();
        for (int i = 0; i < updated.size() && remaining > 0; i++) {
            DumpNullBufferEntry entry = updated.get(i);
            if (!entry.sameItem(stack) || entry.count() >= MAX_BUFFER_STACK_SIZE) {
                continue;
            }
            int inserted = Math.min(MAX_BUFFER_STACK_SIZE - entry.count(), remaining);
            updated.set(i, entry.withCount(entry.count() + inserted));
            remaining -= inserted;
        }
        while (remaining > 0 && updated.size() < maxSlots) {
            int inserted = Math.min(MAX_BUFFER_STACK_SIZE, remaining);
            updated.add(DumpNullBufferEntry.fromStack(stack.copyWithCount(inserted)));
            remaining -= inserted;
        }
        ItemStack remainder = remaining <= 0 ? ItemStack.EMPTY : stack.copyWithCount(remaining);
        return new BufferMutation(EMPTY, remainder, normalizeBuffer(updated, maxSlots));
    }

    private static ExtractionMutation extractFromBuffer(List<DumpNullBufferEntry> buffer, int slot, int amount, int maxSlots) {
        List<DumpNullBufferEntry> updated = new ArrayList<>(normalizeBuffer(buffer, maxSlots));
        if (slot < 0 || slot >= updated.size() || amount <= 0) {
            return new ExtractionMutation(EMPTY, ItemStack.EMPTY, updated);
        }
        DumpNullBufferEntry entry = updated.get(slot);
        int extracted = Math.min(amount, entry.count());
        ItemStack result = entry.stack().copyWithCount(extracted);
        if (extracted >= entry.count()) {
            updated.remove(slot);
        } else {
            updated.set(slot, entry.withCount(entry.count() - extracted));
        }
        return new ExtractionMutation(EMPTY, result, normalizeBuffer(updated, maxSlots));
    }

    private static List<DumpNullBufferEntry> readBuffer(CompoundTag root, String key, int maxSlots) {
        List<DumpNullBufferEntry> entries = new ArrayList<>();
        if (root.contains(key, Tag.TAG_LIST)) {
            ListTag list = root.getList(key, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                entries.add(DumpNullBufferEntry.load(list.getCompound(i)));
            }
        }
        return normalizeBuffer(entries, maxSlots);
    }

    private static void writeBuffer(CompoundTag root, String key, List<DumpNullBufferEntry> entries) {
        ListTag list = new ListTag();
        for (DumpNullBufferEntry entry : entries) {
            list.add(entry.save());
        }
        root.put(key, list);
    }

    private static void writeBuffer(RegistryFriendlyByteBuf buffer, List<DumpNullBufferEntry> entries) {
        buffer.writeVarInt(entries.size());
        for (DumpNullBufferEntry entry : entries) {
            DumpNullBufferEntry.write(buffer, entry);
        }
    }

    private static List<DumpNullBufferEntry> readBuffer(RegistryFriendlyByteBuf buffer, int maxSlots) {
        int count = Math.max(0, Math.min(maxSlots, buffer.readVarInt()));
        List<DumpNullBufferEntry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(DumpNullBufferEntry.read(buffer));
        }
        return normalizeBuffer(entries, maxSlots);
    }

    public record BufferMutation(DumpNullData data, ItemStack remainder, List<DumpNullBufferEntry> buffer) {
    }

    public record ExtractionMutation(DumpNullData data, ItemStack extracted, List<DumpNullBufferEntry> buffer) {
    }

    public record EnergyMutation(DumpNullData data, int amount) {
    }

    private static final class MutableStatusSummary {
        private final ResourceLocation itemId;
        private DumpNullItemStatus status = DumpNullItemStatus.NEUTRAL;
        private boolean advanced;
        private boolean advancedStatusSet;
        private boolean presetGenerated;

        private MutableStatusSummary(ResourceLocation itemId) {
            this.itemId = itemId;
        }

        private DumpNullItemStatusSummary toSummary() {
            return new DumpNullItemStatusSummary(itemId, status, advanced, presetGenerated);
        }
    }
}
