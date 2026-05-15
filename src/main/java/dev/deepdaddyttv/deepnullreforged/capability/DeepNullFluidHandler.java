package dev.deepdaddyttv.deepnullreforged.capability;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class DeepNullFluidHandler implements IFluidHandlerItem {
    private final Supplier<@Nullable DeepNullInventory> inventorySupplier;
    private final Supplier<ItemStack> containerSupplier;
    private final int fixedTank;
    private final boolean singleSelectedTankView;

    public DeepNullFluidHandler(DeepNullInventory inventory, ItemStack container) {
        this(() -> inventory, () -> container, -1, false);
    }

    public DeepNullFluidHandler(DeepNullInventory inventory, ItemStack container, int fixedTank) {
        this(() -> inventory, () -> container, fixedTank, false);
    }

    public DeepNullFluidHandler(DeepNullInventory inventory, ItemStack container, boolean singleSelectedTankView) {
        this(() -> inventory, () -> container, -1, singleSelectedTankView);
    }

    public DeepNullFluidHandler(DeepNullInventory inventory, ItemStack container, int fixedTank, boolean singleSelectedTankView) {
        this(() -> inventory, () -> container, fixedTank, singleSelectedTankView);
    }

    public DeepNullFluidHandler(Supplier<@Nullable DeepNullInventory> inventorySupplier, Supplier<ItemStack> containerSupplier) {
        this(inventorySupplier, containerSupplier, -1, false);
    }

    public DeepNullFluidHandler(Supplier<@Nullable DeepNullInventory> inventorySupplier, Supplier<ItemStack> containerSupplier, boolean singleSelectedTankView) {
        this(inventorySupplier, containerSupplier, -1, singleSelectedTankView);
    }

    public DeepNullFluidHandler(Supplier<@Nullable DeepNullInventory> inventorySupplier, Supplier<ItemStack> containerSupplier, int fixedTank, boolean singleSelectedTankView) {
        this.inventorySupplier = inventorySupplier;
        this.containerSupplier = containerSupplier;
        this.fixedTank = fixedTank;
        this.singleSelectedTankView = singleSelectedTankView;
    }

    @Override
    public int getTanks() {
        DeepNullInventory inventory = currentInventory();
        if (inventory == null) {
            return 0;
        }
        if (fixedTank >= 0) {
            return 1;
        }
        if (singleSelectedTankView) {
            return 1;
        }
        return inventory.getFluidSlotCount();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        DeepNullInventory inventory = currentInventory();
        if (inventory == null) {
            return FluidStack.EMPTY;
        }
        if (fixedTank >= 0) {
            return tank == 0 ? inventory.getFluidInSlot(fixedTank) : FluidStack.EMPTY;
        }
        if (singleSelectedTankView) {
            int selectedTank = selectedTank(inventory);
            return tank == 0 && selectedTank >= 0 ? inventory.getFluidInSlot(selectedTank) : FluidStack.EMPTY;
        }
        return tank >= 0 && tank < inventory.getFluidSlotCount() ? inventory.getFluidInSlot(tank) : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        DeepNullInventory inventory = currentInventory();
        if (inventory == null) {
            return 0;
        }
        if (fixedTank >= 0) {
            return tank == 0 ? inventory.getFluidCapacity() : 0;
        }
        if (singleSelectedTankView) {
            return tank == 0 ? inventory.getFluidCapacity() : 0;
        }
        return tank >= 0 && tank < inventory.getFluidSlotCount() ? inventory.getFluidCapacity() : 0;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        DeepNullInventory inventory = currentInventory();
        if (inventory == null) {
            return false;
        }
        if (fixedTank >= 0) {
            return tank == 0 && inventory.acceptsNormalFluids() && !stack.isEmpty();
        }
        if (singleSelectedTankView) {
            return tank == 0 && inventory.acceptsNormalFluids() && !stack.isEmpty();
        }
        return tank >= 0 && tank < inventory.getFluidSlotCount() && inventory.acceptsNormalFluids() && !stack.isEmpty();
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        DeepNullInventory inventory = currentInventory();
        if (inventory == null) {
            return 0;
        }
        if (fixedTank >= 0) {
            return inventory.fillFluid(fixedTank, resource, action.simulate());
        }
        if (singleSelectedTankView) {
            int selectedTank = selectedTank(inventory);
            return selectedTank >= 0 ? inventory.fillFluid(selectedTank, resource, action.simulate()) : 0;
        }
        return inventory.fillFluid(resource, action.simulate());
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        DeepNullInventory inventory = currentInventory();
        if (inventory == null) {
            return FluidStack.EMPTY;
        }
        if (fixedTank >= 0) {
            FluidStack existing = inventory.getFluidInSlot(fixedTank);
            if (existing.isEmpty() || !FluidStack.isSameFluidSameComponents(existing, resource)) {
                return FluidStack.EMPTY;
            }
            return inventory.drainFluid(fixedTank, resource.getAmount(), action.simulate());
        }
        if (singleSelectedTankView) {
            int selectedTank = selectedTank(inventory);
            if (selectedTank < 0) {
                return FluidStack.EMPTY;
            }
            FluidStack existing = inventory.getFluidInSlot(selectedTank);
            if (existing.isEmpty() || !FluidStack.isSameFluidSameComponents(existing, resource)) {
                return FluidStack.EMPTY;
            }
            return inventory.drainFluid(selectedTank, resource.getAmount(), action.simulate());
        }
        return inventory.drainFluid(resource, action.simulate());
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        DeepNullInventory inventory = currentInventory();
        if (inventory == null) {
            return FluidStack.EMPTY;
        }
        if (fixedTank >= 0) {
            return inventory.drainFluid(fixedTank, maxDrain, action.simulate());
        }
        if (singleSelectedTankView) {
            int selectedTank = selectedTank(inventory);
            return selectedTank >= 0 ? inventory.drainFluid(selectedTank, maxDrain, action.simulate()) : FluidStack.EMPTY;
        }
        return inventory.drainFluid(maxDrain, action.simulate());
    }

    @Override
    public ItemStack getContainer() {
        return containerSupplier.get();
    }

    private @Nullable DeepNullInventory currentInventory() {
        DeepNullInventory inventory = inventorySupplier.get();
        return inventory != null && inventory.supportsFluidStorage() ? inventory : null;
    }

    private int selectedTank(DeepNullInventory inventory) {
        int selectedSlot = inventory.getSelectedSlot();
        if (selectedSlot >= 0 && selectedSlot < inventory.getFluidSlotCount()) {
            return selectedSlot;
        }
        return inventory.getFluidSlotCount() > 0 ? 0 : -1;
    }
}
