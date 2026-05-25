package dev.deepdaddyttv.deepnullreforged.dumpnull;

import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.item.DumpNullItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class DumpNullAutomation {
    private static final int WORK_PER_TICK = 4;

    private DumpNullAutomation() {
    }

    public static IItemHandler createHandler(DeepNullDockBlockEntity dock, @Nullable Direction side) {
        return new Handler(dock, side);
    }

    public static void tickDock(Level level, BlockPos pos, DeepNullDockBlockEntity dock) {
        DumpNullData original = data(dock);
        DumpNullData updated = processAndPush(dock, original);
        if (!updated.equals(original)) {
            setData(dock, updated, false);
        }
    }

    private static DumpNullData processAndPush(DeepNullDockBlockEntity dock, DumpNullData data) {
        DumpNullData working = data;
        for (int work = 0; work < WORK_PER_TICK && !working.inputBuffer().isEmpty(); work++) {
            DumpNullBufferEntry entry = working.inputBuffer().getFirst();
            ItemStack stack = entry.stack();
            List<DumpNullBufferEntry> remainingInput = new ArrayList<>(working.inputBuffer());
            remainingInput.removeFirst();
            working = working.withInputBuffer(remainingInput);
            RouteResult result = routeStack(working, stack);
            working = result.data();
            if (!result.remainder().isEmpty()) {
                working = working.withInputBuffer(prepend(result.remainder(), working.inputBuffer(), DumpNullData.INPUT_BUFFER_SLOTS));
                break;
            }
        }
        return pushBuffers(dock, working);
    }

    private static RouteResult routeStack(DumpNullData data, ItemStack stack) {
        if (stack.isEmpty()) {
            return new RouteResult(data, ItemStack.EMPTY);
        }
        DumpNullRuleAction action = DumpNullRuleMatcher.actionFor(data, stack);
        if (action != DumpNullRuleAction.VOID) {
            DumpNullData.BufferMutation mutation = data.insertOutput(stack);
            return new RouteResult(mutation.data(), mutation.remainder());
        }
        return switch (data.dockDiscardMode()) {
            case VOID -> new RouteResult(data, ItemStack.EMPTY);
            case EXPORT_DISCARD_LANE -> {
                DumpNullData.BufferMutation mutation = data.insertDiscard(stack);
                yield new RouteResult(mutation.data(), mutation.remainder());
            }
            case GENERATE_FE -> generateEnergy(data, stack);
        };
    }

    private static RouteResult generateEnergy(DumpNullData data, ItemStack stack) {
        if (!data.hasUpgrade(DumpNullUpgradeType.POWER)) {
            return new RouteResult(data, stack);
        }
        int reward = DumpNullEnergyRewardResolver.rewardFor(stack);
        DumpNullData.EnergyMutation mutation = data.generateEnergy(reward, false);
        if (mutation.amount() <= 0) {
            return new RouteResult(data, stack);
        }
        return new RouteResult(mutation.data(), ItemStack.EMPTY);
    }

    private static DumpNullData pushBuffers(DeepNullDockBlockEntity dock, DumpNullData data) {
        Level level = dock.getLevel();
        if (level == null) {
            return data;
        }
        DumpNullData working = data;
        for (Direction direction : Direction.values()) {
            if (working.sideMode(direction).output() && !working.isDiscardExportSide(direction)) {
                IItemHandler target = target(level, dock.getBlockPos(), direction);
                if (target != null) {
                    working = pushOutput(working, target);
                }
            }
            if (working.isDiscardExportSide(direction)) {
                IItemHandler target = target(level, dock.getBlockPos(), direction);
                if (target != null) {
                    working = pushDiscard(working, target);
                }
            }
        }
        return working;
    }

    private static DumpNullData pushOutput(DumpNullData data, IItemHandler target) {
        DumpNullData working = data;
        int slot = 0;
        while (slot < working.outputBuffer().size()) {
            ItemStack stack = working.outputBuffer().get(slot).stack();
            ItemStack remainder = ItemHandlerHelper.insertItem(target, stack, false);
            int moved = stack.getCount() - remainder.getCount();
            if (moved <= 0) {
                slot++;
                continue;
            }
            working = working.extractOutput(slot, moved).data();
            if (!remainder.isEmpty()) {
                slot++;
            }
        }
        return working;
    }

    private static DumpNullData pushDiscard(DumpNullData data, IItemHandler target) {
        DumpNullData working = data;
        int slot = 0;
        while (slot < working.discardBuffer().size()) {
            ItemStack stack = working.discardBuffer().get(slot).stack();
            ItemStack remainder = ItemHandlerHelper.insertItem(target, stack, false);
            int moved = stack.getCount() - remainder.getCount();
            if (moved <= 0) {
                slot++;
                continue;
            }
            working = working.extractDiscard(slot, moved).data();
            if (!remainder.isEmpty()) {
                slot++;
            }
        }
        return working;
    }

    private static IItemHandler target(Level level, BlockPos pos, Direction direction) {
        IItemHandler target = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.relative(direction), direction.getOpposite());
        if (target == null) {
            target = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.relative(direction), null);
        }
        return target;
    }

    private static List<DumpNullBufferEntry> prepend(ItemStack stack, List<DumpNullBufferEntry> entries, int maxSlots) {
        List<DumpNullBufferEntry> next = new ArrayList<>();
        next.add(DumpNullBufferEntry.fromStack(stack));
        next.addAll(entries);
        if (next.size() > maxSlots) {
            return List.copyOf(next.subList(0, maxSlots));
        }
        return List.copyOf(next);
    }

    private static DumpNullData data(DeepNullDockBlockEntity dock) {
        return DumpNullData.get(dock.getStoredDeepNull());
    }

    private static void setData(DeepNullDockBlockEntity dock, DumpNullData data, boolean invalidateCapabilities) {
        DumpNullData.set(dock.getStoredDeepNull(), data);
        dock.markStoredDeepNullChanged(invalidateCapabilities);
    }

    private record RouteResult(DumpNullData data, ItemStack remainder) {
    }

    private static final class Handler implements IItemHandler {
        private final DeepNullDockBlockEntity dock;
        private final @Nullable Direction side;

        private Handler(DeepNullDockBlockEntity dock, @Nullable Direction side) {
            this.dock = dock;
            this.side = side;
        }

        @Override
        public int getSlots() {
            return DumpNullData.OUTPUT_BUFFER_SLOTS;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            DumpNullData data = data(dock);
            if (isDiscardLane(data)) {
                return stackAt(data.discardBuffer(), slot);
            }
            if (allowsOutput(data)) {
                return stackAt(data.outputBuffer(), slot);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot < 0 || slot >= getSlots() || stack.isEmpty() || !(dock.getStoredDeepNull().getItem() instanceof DumpNullItem)) {
                return stack;
            }
            DumpNullData current = data(dock);
            if (!allowsInput(current)) {
                return stack;
            }
            RouteResult routed = routeStack(current, stack);
            if (!simulate) {
                DumpNullData updated = processAndPush(dock, routed.data());
                if (!updated.equals(current)) {
                    setData(dock, updated, false);
                }
            }
            return routed.remainder();
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            DumpNullData current = data(dock);
            if (amount <= 0 || slot < 0 || slot >= getSlots()) {
                return ItemStack.EMPTY;
            }
            if (isDiscardLane(current)) {
                if (simulate) {
                    return stackAt(current.discardBuffer(), slot).copyWithCount(Math.min(amount, stackAt(current.discardBuffer(), slot).getCount()));
                }
                DumpNullData.ExtractionMutation mutation = current.extractDiscard(slot, amount);
                if (!mutation.extracted().isEmpty()) {
                    setData(dock, mutation.data(), false);
                }
                return mutation.extracted();
            }
            if (!allowsOutput(current)) {
                return ItemStack.EMPTY;
            }
            if (simulate) {
                return stackAt(current.outputBuffer(), slot).copyWithCount(Math.min(amount, stackAt(current.outputBuffer(), slot).getCount()));
            }
            DumpNullData.ExtractionMutation mutation = current.extractOutput(slot, amount);
            if (!mutation.extracted().isEmpty()) {
                setData(dock, mutation.data(), false);
            }
            return mutation.extracted();
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= 0 && slot < getSlots() ? DumpNullData.MAX_BUFFER_STACK_SIZE : 0;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot >= 0 && slot < getSlots() && !stack.isEmpty() && allowsInput(data(dock));
        }

        private boolean allowsInput(DumpNullData data) {
            return !isDiscardLane(data) && (side == null || data.sideMode(side).input());
        }

        private boolean allowsOutput(DumpNullData data) {
            return !isDiscardLane(data) && (side == null || data.sideMode(side).output());
        }

        private boolean isDiscardLane(DumpNullData data) {
            return side != null && data.isDiscardExportSide(side);
        }

        private static ItemStack stackAt(List<DumpNullBufferEntry> entries, int slot) {
            return slot >= 0 && slot < entries.size() ? entries.get(slot).stack() : ItemStack.EMPTY;
        }
    }
}
