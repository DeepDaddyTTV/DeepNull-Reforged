package dev.deepdaddyttv.deepnullreforged.gametest;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullData;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullEntry;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullDampResourceKind;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullData;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullResourceSummary;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullSnapshot;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullStationRef;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullStationSnapshot;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullStationStatus;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullStationNullType;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlocks;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

@GameTestHolder(DeepNullReforged.MODID)
@PrefixGameTestTemplate(false)
public final class HubNullRegressionGameTests {
    private HubNullRegressionGameTests() {
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void hubnull_snapshot_aggregates_two_loaded_deepnull_docks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        DeepNullDockBlockEntity first = placeDock(helper, new BlockPos(1, 1, 1));
        DeepNullDockBlockEntity second = placeDock(helper, new BlockPos(3, 1, 1));
        first.setStoredDeepNull(configuredNull(helper, DeepNullTier.REDSTONE, new ItemStack(Items.IRON_INGOT, 12), new ItemStack(Items.DIAMOND, 2)));
        second.setStoredDeepNull(configuredNull(helper, DeepNullTier.IRON, new ItemStack(Items.IRON_INGOT, 20), new ItemStack(Items.EMERALD, 4)));

        HubNullData data = new HubNullData(List.of(ref(level, first), ref(level, second)));
        HubNullSnapshot snapshot = HubNullSnapshot.build(level.getServer(), data);

        helper.assertValueEqual(snapshot.countStatus(HubNullStationStatus.ONLINE), 2, "HubNull should see both loaded DeepNull docks online");
        helper.assertValueEqual(count(snapshot, Items.IRON_INGOT), 32L, "HubNull should aggregate iron across stations");
        helper.assertValueEqual(count(snapshot, Items.DIAMOND), 2L, "HubNull should include first station diamond count");
        helper.assertValueEqual(count(snapshot, Items.EMERALD), 4L, "HubNull should include second station emerald count");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void hubnull_snapshot_marks_empty_supported_and_missing_stations(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        DeepNullDockBlockEntity empty = placeDock(helper, new BlockPos(1, 1, 1));
        DeepNullDockBlockEntity damp = placeDock(helper, new BlockPos(3, 1, 1));
        DeepNullDockBlockEntity den = placeDock(helper, new BlockPos(5, 1, 1));
        ItemStack dampStack = DeepNullGameTestSupport.dampNullStack(DeepNullTier.REDSTONE);
        DeepNullInventory dampInventory = new DeepNullInventory(DeepNullTier.REDSTONE, dampStack, level.registryAccess(), null);
        dampInventory.fillFluid(0, new FluidStack(Fluids.WATER, 1000), false);
        damp.setStoredDeepNull(dampStack);
        ItemStack denStack = DeepNullGameTestSupport.denNullStack(DeepNullTier.REDSTONE);
        DenNullData.set(denStack, new DenNullData(0, List.of(
                new DenNullEntry(ResourceLocation.withDefaultNamespace("cow"), 3, "minecraft:cow", new CompoundTag(), "Cow", "")
        )));
        den.setStoredDeepNull(denStack);
        HubNullStationRef missing = new HubNullStationRef(level.dimension().location(), helper.absolutePos(new BlockPos(7, 1, 1)), "Missing");

        HubNullSnapshot snapshot = HubNullSnapshot.build(level.getServer(), new HubNullData(List.of(ref(level, empty), ref(level, damp), ref(level, den), missing)));

        helper.assertValueEqual(snapshot.countStatus(HubNullStationStatus.EMPTY), 1, "Empty dock status");
        helper.assertValueEqual(snapshot.countStatus(HubNullStationStatus.ONLINE), 2, "DampNull and DenNull should be supported HubNull stations");
        helper.assertValueEqual(snapshot.countStatus(HubNullStationStatus.UNSUPPORTED), 0, "DampNull and DenNull should no longer be unsupported");
        helper.assertValueEqual(snapshot.countStatus(HubNullStationStatus.MISSING), 1, "Missing dock status");
        helper.assertValueEqual(fluidAmount(snapshot, ResourceLocation.withDefaultNamespace("water")), 1000L, "HubNull should aggregate DampNull water");
        helper.assertValueEqual(count(snapshot.denResources(), ResourceLocation.withDefaultNamespace("cow")), 3L, "HubNull should aggregate DenNull entity totals");
        HubNullStationSnapshot dampSnapshot = snapshot.stations().stream()
                .filter(station -> station.ref().equals(ref(level, damp)))
                .findFirst()
                .orElseThrow();
        helper.assertValueEqual(dampSnapshot.nullType(), HubNullStationNullType.DAMP, "DampNull station type");
        helper.assertValueEqual(dampSnapshot.displayName(), dampStack.getHoverName().getString(), "DampNull station should show the docked Null name");
        helper.assertTrue(dampSnapshot.accentColor() != 0, "DampNull station should carry a Null style accent color");
        HubNullStationSnapshot denSnapshot = snapshot.stations().stream()
                .filter(station -> station.ref().equals(ref(level, den)))
                .findFirst()
                .orElseThrow();
        helper.assertValueEqual(denSnapshot.nullType(), HubNullStationNullType.DEN, "DenNull station type");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void remote_deepnull_menu_requires_registered_hubnull_station(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        DeepNullDockBlockEntity dock = placeDock(helper, new BlockPos(1, 1, 1));
        dock.setStoredDeepNull(configuredNull(helper, DeepNullTier.REDSTONE, new ItemStack(Items.COBBLESTONE, 8)));
        HubNullStationRef ref = ref(level, dock);
        var player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack hubNull = new ItemStack(ModItems.HUB_NULL.get());
        HubNullData.set(hubNull, new HubNullData(List.of(ref)));
        player.getInventory().setItem(0, hubNull);

        DeepNullMenu menu = DeepNullMenu.forRemoteDock(1, player.getInventory(), dock, ref.dimension(), 0, DeepNullMenu.ViewMode.MAIN);
        helper.assertTrue(menu.stillValid(player), "Remote DeepNull menu should remain valid while HubNull owns the registered station");
        DeepNullMenu upgrades = DeepNullMenu.forRemoteDock(2, player.getInventory(), dock, ref.dimension(), 0, DeepNullMenu.ViewMode.UPGRADES);
        helper.assertValueEqual(upgrades.getViewMode(), DeepNullMenu.ViewMode.UPGRADES, "Remote DeepNull menu should preserve requested upgrade view");

        HubNullData.set(hubNull, HubNullData.EMPTY);
        helper.assertFalse(menu.stillValid(player), "Remote DeepNull menu should become invalid when the station is removed from HubNull");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void remote_dampnull_client_menu_uses_fluid_inventory(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        DeepNullDockBlockEntity dock = placeDock(helper, new BlockPos(1, 1, 1));
        ItemStack dampStack = DeepNullGameTestSupport.dampNullStack(DeepNullTier.EMERALD);
        dock.setStoredDeepNull(dampStack);
        HubNullStationRef ref = ref(level, dock);
        var player = DeepNullGameTestSupport.fakePlayer(helper);
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
        buffer.writeVarInt(DeepNullMenu.SourceType.REMOTE_DOCK.ordinal());
        buffer.writeVarInt(DeepNullMenu.ViewMode.FLUID.ordinal());
        buffer.writeVarInt(DeepNullTier.EMERALD.ordinalId());
        buffer.writeVarInt(-1);
        buffer.writeBlockPos(dock.getBlockPos());
        buffer.writeVarInt(0);
        buffer.writeVarInt(0);
        buffer.writeBoolean(false);
        buffer.writeResourceLocation(ref.dimension());
        buffer.writeVarInt(0);
        buffer.writeBoolean(true);

        DeepNullMenu menu = new DeepNullMenu(3, player.getInventory(), buffer);

        helper.assertValueEqual(menu.getViewMode(), DeepNullMenu.ViewMode.FLUID, "Remote DampNull client menu should keep the fluid view");
        helper.assertTrue(menu.getDankInventory().supportsFluidStorage(), "Remote DampNull client menu should use a fluid-capable client inventory");
        helper.assertValueEqual(menu.getStorageSlotCount(), DeepNullTier.EMERALD.dampNullTankCount(), "Remote DampNull client menu should expose DampNull tank slots");
        DeepNullPayloads.FluidContentsPayload payload = new DeepNullPayloads.FluidContentsPayload(
                menu.containerId,
                List.of(new FluidStack(Fluids.WATER, 1000)),
                List.of()
        );
        RegistryFriendlyByteBuf syncBuffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
        DeepNullPayloads.FluidContentsPayload.STREAM_CODEC.encode(syncBuffer, payload);
        DeepNullPayloads.FluidContentsPayload decoded = DeepNullPayloads.FluidContentsPayload.STREAM_CODEC.decode(syncBuffer);
        menu.acceptFluidContents(decoded.fluids(), decoded.chemicals());
        helper.assertValueEqual(menu.getDankInventory().getFluidInSlot(0).getFluid(), Fluids.WATER, "Remote DampNull client menu should receive tank fluid identity");
        helper.assertValueEqual(menu.getDankInventory().getFluidInSlot(0).getAmount(), 1000, "Remote DampNull client menu should receive tank fluid amount");
        helper.succeed();
    }

    private static DeepNullDockBlockEntity placeDock(GameTestHelper helper, BlockPos relativePos) {
        helper.setBlock(relativePos, ModBlocks.DEEP_NULL_DOCK.get());
        if (!(helper.getLevel().getBlockEntity(helper.absolutePos(relativePos)) instanceof DeepNullDockBlockEntity dock)) {
            helper.fail("Expected DeepNull Dock at " + relativePos);
            throw new IllegalStateException("Unreachable");
        }
        return dock;
    }

    private static ItemStack configuredNull(GameTestHelper helper, DeepNullTier tier, ItemStack... stacks) {
        ItemStack nullStack = DeepNullGameTestSupport.deepNullStack(tier);
        DeepNullInventory inventory = new DeepNullInventory(tier, nullStack, helper.getLevel().registryAccess(), null);
        for (int i = 0; i < stacks.length; i++) {
            inventory.setStackInSlot(i, stacks[i]);
        }
        return nullStack;
    }

    private static HubNullStationRef ref(ServerLevel level, DeepNullDockBlockEntity dock) {
        BlockPos pos = dock.getBlockPos();
        return new HubNullStationRef(level.dimension().location(), pos, "Dock " + pos.toShortString());
    }

    private static long count(HubNullSnapshot snapshot, net.minecraft.world.item.Item item) {
        return count(snapshot.resources(), net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item));
    }

    private static long count(List<HubNullResourceSummary> resources, ResourceLocation itemId) {
        return resources.stream()
                .filter(summary -> summary.itemId().equals(itemId))
                .mapToLong(HubNullResourceSummary::count)
                .sum();
    }

    private static long fluidAmount(HubNullSnapshot snapshot, ResourceLocation fluidId) {
        return snapshot.dampResources().stream()
                .filter(summary -> summary.kind() == HubNullDampResourceKind.FLUID)
                .filter(summary -> summary.resourceId().equals(fluidId))
                .mapToLong(summary -> summary.amount())
                .sum();
    }
}
