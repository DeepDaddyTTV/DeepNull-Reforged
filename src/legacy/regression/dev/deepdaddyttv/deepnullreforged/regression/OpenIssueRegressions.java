package dev.deepdaddyttv.deepnullreforged.regression;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.capability.DampNullFluidHandler;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullItemHandler;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullData;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DampNullItem;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.registry.ModContent;
import net.minecraft.block.Blocks;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.mockito.ArgumentCaptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@Mod.EventBusSubscriber(modid = DeepNullReforged.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class OpenIssueRegressions implements IDataProvider {
    @SubscribeEvent public static void gather(GatherDataEvent event) {
        if (event.includeServer()) event.getGenerator().addProvider(new OpenIssueRegressions());
    }
    @Override public String getName() { return "Forge 1.16.5 open-issue regressions"; }
    @Override public void run(DirectoryCache cache) throws java.io.IOException {
        List<String> failures = new ArrayList<>();
        check(failures, "22/large and low-stack drops", this::drops);
        check(failures, "22/cursor and creative drops", this::dropBoundaries);
        check(failures, "15/upgraded MK IV reinsertion and pickup", this::upgradedPickup);
        check(failures, "19/item capability and GUI coherence", this::itemCoherence);
        check(failures, "19/replaced item and fluid tags", this::replacedTags);
        check(failures, "14/fluid capability and direct-use coherence", this::fluidCoherence);
        check(failures, "14/creative tank transfer conservation", () -> tankTransfer(true, false));
        check(failures, "14/survival tank transfer conservation", () -> tankTransfer(false, false));
        check(failures, "14/client tank interaction", () -> tankTransfer(false, true));
        check(failures, "14/partial, empty and full tank transfers", this::transferBoundaries);
        check(failures, "24/matching tank capacity and overflow", this::fluidOverflow);
        check(failures, "14/fluid simulation, tags and capacity", this::fluidBoundaries);
        check(failures, "25/dock break preserves stored Null", this::dockBreak);
        if (!failures.isEmpty()) throw new IllegalStateException(String.join("; ", failures));
        java.nio.file.Path report = java.nio.file.Paths.get(System.getProperty("deepnull.regressionReport"));
        java.nio.file.Files.createDirectories(report.getParent());
        java.nio.file.Files.write(report, "All Forge issue regressions passed. See run-data/logs/latest.log for cases.\n"
                .getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
    private static void check(List<String> failures, String name, Runnable action) {
        try { action.run(); System.out.println("PASS issue " + name); }
        catch (Throwable error) { failures.add(name + ": " + error); System.out.println("FAIL issue " + name + ": " + error); error.printStackTrace(); }
    }
    private static void require(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
    private static void field(Object target, Class<?> owner, String name, Object value) {
        try { Field f = owner.getDeclaredField(name); f.setAccessible(true); f.set(target, value); }
        catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }
    private static PlayerEntity player() {
        PlayerEntity player = mock(PlayerEntity.class);
        field(player, PlayerEntity.class, "inventory", new PlayerInventory(player));
        field(player, PlayerEntity.class, "abilities", new PlayerAbilities());
        field(player, Entity.class, "position", net.minecraft.util.math.vector.Vector3d.ZERO);
        return player;
    }
    private void drops() {
        for (Item item : new Item[]{Items.ENDER_PEARL, Items.CLAY, Items.DIAMOND_PICKAXE}) {
            for (int button : new int[]{1, 0, 2}) {
                PlayerEntity player = player();
                ItemStack bag = new ItemStack(ModContent.DEEP_NULLS[5].get());
                DeepNullItemHandler handler = ((DeepNullItem) bag.getItem()).createHandler(bag);
                handler.insertItem(0, new ItemStack(item, 500000), false);
                DeepNullMenu menu = new DeepNullMenu(1, player.inventory, handler, Hand.MAIN_HAND, DeepNullTier.EMERALD);
                menu.clicked(0, button, ClickType.THROW, player);
                int amount = button == 0 ? 1 : button == 1 ? new ItemStack(item).getMaxStackSize() : 0;
                require(handler.getStackInSlot(0).getCount() == 500000 - amount, "drop changed wrong amount for " + item + " button " + button);
                if (amount == 0) verify(player, never()).drop(any(ItemStack.class), anyBoolean());
                else {
                    ArgumentCaptor<ItemStack> drop = ArgumentCaptor.forClass(ItemStack.class);
                    verify(player, times(1)).drop(drop.capture(), eq(true));
                    require(drop.getValue().getCount() == amount, "oversized drop");
                }
            }
        }
    }
    private void itemCoherence() {
        ItemStack bag = new ItemStack(ModContent.DEEP_NULLS[5].get());
        IItemHandler cached = bag.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(AssertionError::new);
        DeepNullItemHandler gui = ((DeepNullItem) bag.getItem()).createHandler(bag);
        gui.insertItem(8, new ItemStack(Items.CLAY, 500000), false);
        cached.insertItem(0, new ItemStack(Items.DIRT, 3), false);
        require(cached.getStackInSlot(8).getCount() == 500000, "cached capability lost GUI contents");
        gui.extractItem(8, 64, false);
        ItemStack saved = ItemStack.of(bag.save(new CompoundNBT()));
        IItemHandler restored = saved.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(AssertionError::new);
        require(restored.getStackInSlot(0).getCount() == 3, "GUI overwrote capability mutation");
        require(restored.getStackInSlot(8).getCount() == 499936, "reloaded count incorrect");
    }
    private void dropBoundaries() {
        PlayerEntity player = player();
        ItemStack bag = new ItemStack(ModContent.DEEP_NULLS[6].get());
        DeepNullItemHandler handler = ((DeepNullItem) bag.getItem()).createHandler(bag);
        handler.insertItem(0, new ItemStack(Items.ENDER_PEARL, 16), false);
        DeepNullMenu menu = new DeepNullMenu(1, player.inventory, handler, Hand.MAIN_HAND, DeepNullTier.CREATIVE);
        player.inventory.setCarried(new ItemStack(Items.DIRT));
        menu.clicked(0, 1, ClickType.THROW, player);
        verify(player, never()).drop(any(ItemStack.class), anyBoolean());
        player.inventory.setCarried(ItemStack.EMPTY);
        menu.clicked(0, 1, ClickType.THROW, player);
        ArgumentCaptor<ItemStack> drop = ArgumentCaptor.forClass(ItemStack.class);
        verify(player, times(1)).drop(drop.capture(), eq(true));
        require(drop.getValue().getCount() == 16 && handler.getStackInSlot(0).getCount() == 16,
                "creative drop consumed seed or exceeded stack size");
    }
    private void upgradedPickup() {
        PlayerEntity player = player();
        World world = mock(World.class);
        player.level = world;
        net.minecraft.item.crafting.RecipeManager recipes = mock(net.minecraft.item.crafting.RecipeManager.class);
        when(world.getRecipeManager()).thenReturn(recipes);
        when(recipes.getRecipeFor(any(), any(), eq(world))).thenReturn(java.util.Optional.empty());
        ItemStack bag = new ItemStack(ModContent.DEEP_NULLS[3].get());
        DeepNullData.addUpgrade(bag, "basic_compression_upgrade");
        DeepNullData.addUpgrade(bag, "advanced_compression_upgrade");
        player.inventory.setItem(0, bag);
        DeepNullItemHandler handler = ((DeepNullItem) bag.getItem()).createHandler(bag);
        handler.insertItem(0, new ItemStack(Items.CLAY, 128), false);
        DeepNullMenu menu = new DeepNullMenu(1, player.inventory, handler, Hand.MAIN_HAND, DeepNullTier.GOLD);
        menu.clicked(0, 0, ClickType.PICKUP, player);
        require(player.inventory.getCarried().getCount() == 64, "withdraw failed");
        menu.clicked(0, 0, ClickType.PICKUP, player);
        require(player.inventory.getCarried().isEmpty() && handler.getStackInSlot(0).getCount() == 128, "reinsertion failed");
        ItemEntity entity = mock(ItemEntity.class);
        when(entity.getItem()).thenReturn(new ItemStack(Items.CLAY, 64));
        net.minecraftforge.event.entity.player.EntityItemPickupEvent event =
                new net.minecraftforge.event.entity.player.EntityItemPickupEvent(player, entity);
        new dev.deepdaddyttv.deepnullreforged.DeepNullEvents().onPickup(event);
        require(event.isCanceled() && handler.getStackInSlot(0).getCount() == 192, "matching pickup failed");
        verify(entity).remove();
    }
    private void fluidCoherence() {
        ItemStack bag = new ItemStack(ModContent.DAMP_NULLS[0].get());
        IFluidHandler cached = bag.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).orElseThrow(AssertionError::new);
        DampNullFluidHandler direct = ((DampNullItem) bag.getItem()).createHandler(bag);
        direct.fill(new FluidStack(Fluids.WATER, 1000), EXECUTE);
        cached.fill(new FluidStack(Fluids.LAVA, 1000), EXECUTE);
        direct.drain(new FluidStack(Fluids.WATER, 250), EXECUTE);
        ItemStack saved = ItemStack.of(bag.save(new CompoundNBT()));
        IFluidHandler restored = saved.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).orElseThrow(AssertionError::new);
        require(restored.drain(new FluidStack(Fluids.WATER, 1000), SIMULATE).getAmount() == 750, "water lost");
        require(restored.drain(new FluidStack(Fluids.LAVA, 1000), SIMULATE).getAmount() == 1000, "lava lost");
    }
    private void replacedTags() {
        ItemStack bag = new ItemStack(ModContent.DEEP_NULLS[5].get());
        DeepNullItemHandler cached = ((DeepNullItem) bag.getItem()).createHandler(bag);
        cached.insertItem(0, new ItemStack(Items.CLAY, 500000), false);
        ItemStack changed = bag.copy();
        ((DeepNullItem) changed.getItem()).createHandler(changed).extractItem(0, 64, false);
        bag.setTag(changed.getTag().copy());
        require(cached.getStackInSlot(0).getCount() == 499936, "item capability ignored replaced tag");
        cached.insertItem(1, new ItemStack(Items.DIRT, 1), false);
        require(((DeepNullItem) bag.copy().getItem()).createHandler(bag.copy()).getStackInSlot(0).getCount() == 499936,
                "item mutation overwrote replaced tag");
        ItemStack damp = new ItemStack(ModContent.DAMP_NULLS[0].get());
        DampNullFluidHandler fluids = ((DampNullItem) damp.getItem()).createHandler(damp);
        fluids.fill(new FluidStack(Fluids.WATER, 1000), EXECUTE);
        ItemStack updated = damp.copy();
        ((DampNullItem) updated.getItem()).createHandler(updated).drain(250, EXECUTE);
        damp.setTag(updated.getTag().copy());
        require(fluids.drain(1000, SIMULATE).getAmount() == 750, "fluid capability ignored replaced tag");
        damp.setTag(new CompoundNBT());
        require(fluids.drain(1000, SIMULATE).isEmpty(), "cleared tag retained fluid");
    }
    private void fluidBoundaries() {
        ItemStack bag = new ItemStack(ModContent.DAMP_NULLS[0].get());
        DampNullFluidHandler fluids = ((DampNullItem) bag.getItem()).createHandler(bag);
        FluidStack tagged = new FluidStack(Fluids.WATER, 17000);
        tagged.getOrCreateTag().putString("variant", "test");
        require(fluids.fill(tagged, SIMULATE) == 17000, "simulated fill rejected");
        require(fluids.drain(Integer.MAX_VALUE, SIMULATE).isEmpty(), "simulation changed storage");
        require(fluids.fill(tagged, EXECUTE) == 17000 && tagged.getAmount() == 17000, "fill changed caller fluid");
        require(fluids.drain(new FluidStack(Fluids.WATER, 1000), EXECUTE).isEmpty(), "mixed distinct fluid tags");
        fluids.getFluidInTank(0).setAmount(1);
        require(fluids.drain(Integer.MAX_VALUE, SIMULATE).getAmount() == 17000, "getter exposed mutable storage");
        int capacity = fluids.getTanks() * fluids.getTankCapacity(0);
        FluidStack large = tagged.copy();
        large.setAmount(Integer.MAX_VALUE);
        require(fluids.fill(large, SIMULATE) == capacity - 17000, "simulated capacity incorrect");
        require(fluids.fill(large, EXECUTE) == capacity - 17000, "fill capacity incorrect");
        require(fluids.fill(tagged, EXECUTE) == 0, "full tank accepted fluid");
        require(fluids.drain(0, EXECUTE).isEmpty(), "zero drain changed fluid");
        require(fluids.drain(Integer.MAX_VALUE, EXECUTE).getAmount() == capacity, "full drain incorrect");
        ItemStack creative = new ItemStack(ModContent.DAMP_NULLS[6].get());
        DampNullFluidHandler infinite = ((DampNullItem) creative.getItem()).createHandler(creative);
        infinite.fill(tagged, EXECUTE);
        require(infinite.drain(Integer.MAX_VALUE, EXECUTE).getAmount() == Integer.MAX_VALUE, "creative output capped");
        require(infinite.drain(1000, SIMULATE).getAmount() == 1000, "creative seed consumed");
    }
    private void tankTransfer(boolean creative, boolean client) {
        tankTransfer(creative, client, 0, 4000, client ? 4000 : 0);
    }
    private void transferBoundaries() {
        for (boolean creative : new boolean[]{false, true}) {
            tankTransfer(creative, false, 143500, 4000, 3500);
            tankTransfer(creative, false, 1000, 0, 1000);
            tankTransfer(creative, false, 144000, 10000, 10000);
        }
    }
    private void tankTransfer(boolean creative, boolean client, int initialStored, int initialSource, int finalSource) {
        PlayerEntity player = player();
        player.abilities.instabuild = creative;
        World world = mock(World.class);
        field(world, World.class, "isClientSide", client);
        player.level = world;
        ItemStack bag = new ItemStack(ModContent.DAMP_NULLS[0].get());
        ((DampNullItem) bag.getItem()).createHandler(bag).fill(new FluidStack(Fluids.WATER, initialStored), EXECUTE);
        when(player.getItemInHand(Hand.MAIN_HAND)).thenReturn(bag);
        when(player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)).thenReturn(LazyOptional.of(() -> new net.minecraftforge.items.wrapper.PlayerInvWrapper(player.inventory)));
        FluidTank source = new FluidTank(10000);
        source.fill(new FluidStack(Fluids.WATER, initialSource), EXECUTE);
        TileEntity tile = mock(TileEntity.class);
        when(tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.UP)).thenReturn(LazyOptional.of(() -> source));
        when(world.getBlockState(BlockPos.ZERO)).thenReturn(ModContent.DEEP_NULL_DOCK.get().defaultBlockState());
        when(world.getBlockEntity(BlockPos.ZERO)).thenReturn(tile);
        ItemUseContext context = mock(ItemUseContext.class);
        when(context.getPlayer()).thenReturn(player);
        when(context.getLevel()).thenReturn(world);
        when(context.getItemInHand()).thenReturn(bag);
        when(context.getHand()).thenReturn(Hand.MAIN_HAND);
        when(context.getClickedPos()).thenReturn(BlockPos.ZERO);
        when(context.getClickedFace()).thenReturn(Direction.UP);
        bag.getItem().useOn(context);
        // If the interaction replaced the held stack, inspect that actual result.
        ItemStack held = bag;
        if (!mockingDetails(player).getInvocations().isEmpty()) {
            for (org.mockito.invocation.Invocation invocation : mockingDetails(player).getInvocations()) {
                if (invocation.getMethod().getName().equals("setItemInHand")) held = (ItemStack) invocation.getArgument(1);
            }
        }
        int stored = ((DampNullItem) held.getItem()).createHandler(held).drain(Integer.MAX_VALUE, SIMULATE).getAmount();
        require(stored + source.getFluidAmount() == initialStored + initialSource, "tank interaction destroyed fluid");
        require(source.getFluidAmount() == finalSource, "wrong logical-side transfer or amount");
    }
    private void fluidOverflow() {
        ItemStack bag = new ItemStack(ModContent.DAMP_NULLS[0].get());
        DampNullFluidHandler handler = ((DampNullItem) bag.getItem()).createHandler(bag);
        for (int i = 0; i < 17; i++) require(handler.fill(new FluidStack(Fluids.WATER, 1000), EXECUTE) == 1000, "pickup " + i + " refused");
        require(handler.getFluidInTank(0).getAmount() == 16000 && handler.getFluidInTank(1).getAmount() == 1000, "matching tank packing");
        require(handler.drain(new FluidStack(Fluids.WATER, 17000), SIMULATE).getAmount() == 17000, "cross-tank simulated drain");
        require(handler.drain(new FluidStack(Fluids.WATER, 17000), EXECUTE).getAmount() == 17000, "cross-tank drain");
        require(handler.drain(1, SIMULATE).isEmpty(), "drain left phantom fluid");
    }
    private void dockBreak() {
        World world = mock(World.class);
        field(world, World.class, "random", new Random(1));
        when(world.getGameRules()).thenReturn(new GameRules());
        for (boolean damp : new boolean[]{false, true}) {
            DeepNullDockBlockEntity dock = new DeepNullDockBlockEntity();
            ItemStack bag = new ItemStack(damp ? ModContent.DAMP_NULLS[5].get() : ModContent.DEEP_NULLS[5].get());
            bag.getOrCreateTag().putString("RegressionMarker", "keep me");
            if (damp) ((DampNullItem) bag.getItem()).createHandler(bag).fill(new FluidStack(Fluids.WATER, 500000), EXECUTE);
            else ((DeepNullItem) bag.getItem()).createHandler(bag).insertItem(0, new ItemStack(Items.CLAY, 500000), false);
            ItemStack expected = bag.copy();
            require(dock.insertNull(bag), "dock insertion failed");
            require(bag.isEmpty(), "dock duplicated inserted Null");
            CompoundNBT saved = dock.save(new CompoundNBT());
            DeepNullDockBlockEntity loaded = new DeepNullDockBlockEntity();
            loaded.load(ModContent.DEEP_NULL_DOCK.get().defaultBlockState(), saved);
            when(world.getBlockEntity(BlockPos.ZERO)).thenReturn(loaded);
            clearInvocations(world);
            ModContent.DEEP_NULL_DOCK.get().onRemove(ModContent.DEEP_NULL_DOCK.get().defaultBlockState(), world, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), false);
            ArgumentCaptor<Entity> drop = ArgumentCaptor.forClass(Entity.class);
            verify(world, times(1)).addFreshEntity(drop.capture());
            require(ItemStack.matches(expected, ((ItemEntity) drop.getValue()).getItem()), "dock dropped a different Null");
            require(loaded.getNull().isEmpty(), "dock did not consume drop");
            ModContent.DEEP_NULL_DOCK.get().onRemove(ModContent.DEEP_NULL_DOCK.get().defaultBlockState(), world, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), false);
            verify(world, times(1)).addFreshEntity(any(Entity.class));
        }
    }
}
