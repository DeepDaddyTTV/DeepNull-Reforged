package dev.deepdaddyttv.deepnullreforged.data;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullItemHandler;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.registry.ModContent;
import dev.deepdaddyttv.deepnullreforged.capability.DampNullFluidHandler;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Hand;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

/** Registry-dependent regression checks run by Forge's headless data launcher. */
public final class RegressionValidationProvider implements IDataProvider {
    @Override
    public void run(DirectoryCache cache) {
        for (DeepNullTier tier : DeepNullTier.values()) {
            ItemStack owner = new ItemStack(ModContent.DEEP_NULLS[tier.id()].get());
            DeepNullItemHandler inventory = new DeepNullItemHandler(owner, tier);
            int last = tier.slots() - 1;
            int count = tier.creative() ? 64 : Math.min(512, tier.itemCapacity());
            inventory.insertItem(last, new ItemStack(Items.STONE, count), false);
            inventory.insertItem(0, new ItemStack(Items.DIRT, 3), false);
            DeepNullItemHandler restored = new DeepNullItemHandler(owner.copy(), tier);
            require(restored.getStackInSlot(last).getCount() == count, "extended count persistence " + tier);
            require(restored.getStackInSlot(1).isEmpty(), "sparse slot persistence " + tier);
            require(restored.getStackInSlot(0).getItem() == Items.DIRT, "slot identity " + tier);
            restored.extractItem(last, 32, true);
            require(restored.getStackInSlot(last).getCount() == count, "simulated extraction mutated storage");
            restored.extractItem(last, 32, false);
            require(restored.getStackInSlot(last).getCount() == (tier.creative() ? count : count - 32), "extraction conservation");
            require(!restored.insertItem(1, owner, false).isEmpty(), "nested Null accepted");
            DeepNullMenu menu = new DeepNullMenu(1, new PlayerInventory(null), inventory, Hand.MAIN_HAND, tier);
            require(menu.slots.size() == tier.slots() + 36, "menu slot count " + tier);
            for (int i = 0; i < tier.slots(); i++) {
                checkSlot(menu.slots.get(i), 9 + i % 9 * 21, 19 + i / 9 * 21);
                require(menu.slots.get(i).getItem().getItem() == inventory.getStackInSlot(i).getItem(), "menu storage mapping");
            }
            for (int i = 0; i < 27; i++) checkSlot(menu.slots.get(tier.slots() + i), 9 + i % 9 * 21, 28 + tier.rows() * 21 + i / 9 * 21);
            for (int i = 0; i < 9; i++) checkSlot(menu.slots.get(tier.slots() + 27 + i), 9 + i * 21, 95 + tier.rows() * 21);

            ItemStack damp = new ItemStack(ModContent.DAMP_NULLS[tier.id()].get());
            DampNullFluidHandler fluids = new DampNullFluidHandler(damp, tier);
            require(fluids.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.SIMULATE) == 1000, "fluid simulation result");
            require(fluids.getFluidInTank(0).isEmpty(), "fluid simulation mutated storage");
            fluids.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE);
            fluids.fill(new FluidStack(Fluids.LAVA, 1000), FluidAction.EXECUTE);
            DampNullFluidHandler loaded = new DampNullFluidHandler(damp.copy(), tier);
            require(loaded.getFluidInTank(0).getFluid() == Fluids.WATER, "water persistence");
            require(loaded.getFluidInTank(1).getFluid() == Fluids.LAVA, "lava persistence");
            loaded.drain(250, FluidAction.EXECUTE);
            require(loaded.getFluidInTank(0).getAmount() == (tier.creative() ? 1000 : 750), "fluid conservation");
        }
    }
    private static void checkSlot(Slot slot, int x, int y) {
        require(slot.x == x && slot.y == y, "menu hitbox mismatch: expected " + x + "," + y + " got " + slot.x + "," + slot.y);
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
    @Override public String getName() { return "DeepNull storage, menu layout and fluid regressions (all seven tiers)"; }
}
