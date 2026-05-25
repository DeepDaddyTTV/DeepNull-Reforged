package dev.deepdaddyttv.deepnullreforged.gametest;

import dev.deepdaddyttv.deepnullreforged.block.entity.DripStandBlockEntity;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripMending;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripNullData;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripProfile;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripSlotAssignment;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripSlotRef;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripSwapEngine;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.item.DripNullItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Map;

public final class DripNullRegressionGameTests {
    private DripNullRegressionGameTests() {
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void capture_clears_scoped_slots_and_vaults_items(GameTestHelper helper) {
        ServerPlayer player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack drip = DeepNullGameTestSupport.dripNullStack(DeepNullTier.REDSTONE);
        player.getInventory().setItem(8, drip);
        player.getInventory().setItem(0, new ItemStack(Items.IRON_PICKAXE));
        player.getInventory().armor.set(0, new ItemStack(Items.IRON_BOOTS));

        DripSwapEngine.Result result = DripSwapEngine.run(player, drip, DeepNullTier.REDSTONE, 8);

        helper.assertTrue(result.success(), "Initial DripNull capture should succeed");
        helper.assertTrue(player.getInventory().getItem(0).isEmpty(), "Captured hotbar slot should be cleared");
        helper.assertTrue(player.getInventory().armor.get(0).isEmpty(), "Captured armor slot should be cleared");
        DripNullData data = DripNullData.get(drip, DeepNullTier.REDSTONE, helper.getLevel().registryAccess());
        helper.assertValueEqual(data.profile(0).slots().size(), 2, "Profile should reference two captured slots");
        helper.assertValueEqual(data.vaultItems().size(), 2, "Vault should hold two captured items");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void farmer_to_miner_swap_is_lossless(GameTestHelper helper) {
        ServerPlayer player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack drip = DeepNullGameTestSupport.dripNullStack(DeepNullTier.LAPIS);
        player.getInventory().setItem(8, drip);
        player.getInventory().setItem(0, new ItemStack(Items.WOODEN_HOE));
        DripSwapEngine.run(player, drip, DeepNullTier.LAPIS, 8);

        player.getInventory().setItem(0, new ItemStack(Items.IRON_PICKAXE));
        DripSwapEngine.selectProfile(player, drip, DeepNullTier.LAPIS, 1);
        DripSwapEngine.run(player, drip, DeepNullTier.LAPIS, 8);

        DripSwapEngine.selectProfile(player, drip, DeepNullTier.LAPIS, 0);
        DripSwapEngine.Result result = DripSwapEngine.run(player, drip, DeepNullTier.LAPIS, 8);

        helper.assertTrue(result.success(), "Swapping back to Farmer should succeed");
        helper.assertTrue(player.getInventory().getItem(0).is(Items.WOODEN_HOE), "Farmer hoe should equip");
        DripNullData data = DripNullData.get(drip, DeepNullTier.LAPIS, helper.getLevel().registryAccess());
        helper.assertTrue(data.vaultItems().values().stream().anyMatch(stack -> stack.is(Items.IRON_PICKAXE)), "Miner pickaxe should return to the vault");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void shared_ref_moves_without_duplicate(GameTestHelper helper) {
        ServerPlayer player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack drip = DeepNullGameTestSupport.dripNullStack(DeepNullTier.LAPIS);
        player.getInventory().setItem(8, drip);
        DripProfile first = DripProfile.defaultProfile(0).withSlots(List.of(new DripSlotAssignment(DripSlotRef.inventory(0), "ref1")));
        DripProfile second = DripProfile.defaultProfile(1).withSlots(List.of(new DripSlotAssignment(DripSlotRef.inventory(1), "ref1")));
        DripNullData.set(drip, new DripNullData(0, -1, List.of(first, second), Map.of("ref1", new ItemStack(Items.DIAMOND_SWORD)), List.of(), DripNullData.empty(DeepNullTier.LAPIS).upgrades(), 0L, 0L, 2), DeepNullTier.LAPIS, helper.getLevel().registryAccess());

        DripSwapEngine.run(player, drip, DeepNullTier.LAPIS, 8);
        DripSwapEngine.selectProfile(player, drip, DeepNullTier.LAPIS, 1);
        DripSwapEngine.Result result = DripSwapEngine.run(player, drip, DeepNullTier.LAPIS, 8);

        helper.assertTrue(result.success(), "Shared ref swap should succeed");
        helper.assertTrue(player.getInventory().getItem(0).isEmpty(), "Original slot should be cleared");
        helper.assertTrue(player.getInventory().getItem(1).is(Items.DIAMOND_SWORD), "Shared sword should move to target slot");
        helper.assertValueEqual(countPlayerItems(player, Items.DIAMOND_SWORD), 1, "Shared sword should not duplicate");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void same_profile_use_stows_equipped_items(GameTestHelper helper) {
        ServerPlayer player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack drip = DeepNullGameTestSupport.dripNullStack(DeepNullTier.REDSTONE);
        player.getInventory().setItem(8, drip);
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND_SWORD));
        DripProfile profile = DripProfile.defaultProfile(0).withSlots(List.of(new DripSlotAssignment(DripSlotRef.inventory(0), "ref1")));
        DripNullData.set(drip, new DripNullData(0, 0, List.of(profile), Map.of(), List.of(), DripNullData.empty(DeepNullTier.REDSTONE).upgrades(), 0L, 0L, 2), DeepNullTier.REDSTONE, helper.getLevel().registryAccess());

        DripSwapEngine.Result result = DripSwapEngine.run(player, drip, DeepNullTier.REDSTONE, 8);

        helper.assertTrue(result.success(), "Using an already equipped profile should stow it");
        helper.assertTrue(player.getInventory().getItem(0).isEmpty(), "Stowed player slot should be cleared");
        DripNullData data = DripNullData.get(drip, DeepNullTier.REDSTONE, helper.getLevel().registryAccess());
        helper.assertValueEqual(data.equippedProfile(), -1, "Profile should no longer be equipped");
        helper.assertTrue(data.vaultItems().get("ref1").is(Items.DIAMOND_SWORD), "Current equipped item should return to the vault");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void clear_equipped_profile_moves_items_to_recovery(GameTestHelper helper) {
        ServerPlayer player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack drip = DeepNullGameTestSupport.dripNullStack(DeepNullTier.REDSTONE);
        player.getInventory().setItem(8, drip);
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND_CHESTPLATE));
        DripProfile profile = DripProfile.defaultProfile(0).withSlots(List.of(new DripSlotAssignment(DripSlotRef.inventory(0), "ref1")));
        DripNullData.set(drip, new DripNullData(0, 0, List.of(profile), Map.of(), List.of(), DripNullData.empty(DeepNullTier.REDSTONE).upgrades(), 0L, 0L, 2), DeepNullTier.REDSTONE, helper.getLevel().registryAccess());

        DripSwapEngine.Result result = DripSwapEngine.clearSelectedProfile(player, drip, DeepNullTier.REDSTONE);

        helper.assertTrue(result.success(), "Clearing an equipped profile should stow before clearing");
        helper.assertTrue(player.getInventory().getItem(0).isEmpty(), "Cleared equipped profile should empty player slots");
        DripNullData data = DripNullData.get(drip, DeepNullTier.REDSTONE, helper.getLevel().registryAccess());
        helper.assertTrue(data.profile(0).slots().isEmpty(), "Cleared profile should have no assignments");
        helper.assertTrue(data.vaultItems().isEmpty(), "Unshared cleared refs should leave the vault");
        helper.assertTrue(data.looseItems().stream().anyMatch(stack -> stack.is(Items.DIAMOND_CHESTPLATE)), "Cleared items should move to recovery inventory");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void missing_vault_item_aborts_without_mutation(GameTestHelper helper) {
        ServerPlayer player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack drip = DeepNullGameTestSupport.dripNullStack(DeepNullTier.REDSTONE);
        player.getInventory().setItem(8, drip);
        player.getInventory().setItem(0, new ItemStack(Items.STONE));
        DripProfile profile = DripProfile.defaultProfile(0).withSlots(List.of(new DripSlotAssignment(DripSlotRef.inventory(0), "missing")));
        DripNullData.set(drip, new DripNullData(0, -1, List.of(profile, DripProfile.defaultProfile(1)), Map.of(), List.of(), DripNullData.empty(DeepNullTier.REDSTONE).upgrades(), 0L, 0L, 2), DeepNullTier.REDSTONE, helper.getLevel().registryAccess());

        DripSwapEngine.Result result = DripSwapEngine.run(player, drip, DeepNullTier.REDSTONE, 8);

        helper.assertFalse(result.success(), "Missing vault item should abort");
        helper.assertTrue(player.getInventory().getItem(0).is(Items.STONE), "Player slot should remain unchanged");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void drip_stand_stores_and_removes_dripnull(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.DRIP_STAND.get());
        DripStandBlockEntity stand = (DripStandBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        ItemStack drip = DeepNullGameTestSupport.dripNullStack(DeepNullTier.REDSTONE);

        stand.setStoredDripNull(drip);
        helper.assertTrue(stand.hasStoredDripNull(), "Drip Stand should store one DripNull");
        ItemStack removed = stand.removeStoredDripNull();
        helper.assertTrue(removed.getItem() instanceof DripNullItem, "Removed stack should be the stored DripNull");
        helper.assertFalse(stand.hasStoredDripNull(), "Drip Stand should be empty after remove");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void xp_pickup_repairs_damaged_vault_item(GameTestHelper helper) {
        ServerPlayer player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack drip = DeepNullGameTestSupport.dripNullStack(DeepNullTier.REDSTONE);
        ItemStack damaged = new ItemStack(Items.IRON_PICKAXE);
        damaged.setDamageValue(10);
        DripProfile profile = DripProfile.defaultProfile(0).withSlots(List.of(new DripSlotAssignment(DripSlotRef.inventory(0), "ref1")));
        DripNullData data = new DripNullData(0, -1, List.of(profile, DripProfile.defaultProfile(1)), Map.of("ref1", damaged), List.of(),
                DripNullData.empty(DeepNullTier.REDSTONE).upgrades().withInstalled(DripNullUpgradeType.MEND), 0L, 0L, 2);
        DripNullData.set(drip, data, DeepNullTier.REDSTONE, helper.getLevel().registryAccess());
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, drip);
        ExperienceOrb orb = new ExperienceOrb(helper.getLevel(), player.getX(), player.getY(), player.getZ(), 3);

        helper.assertTrue(DripMending.handlePickup(player, orb), "Mend Upgrade should consume XP");
        DripNullData updated = DripNullData.get(player.getMainHandItem(), DeepNullTier.REDSTONE, helper.getLevel().registryAccess());
        helper.assertTrue(updated.vaultItems().get("ref1").getDamageValue() < 10, "Stored damaged item should be repaired");
        helper.succeed();
    }

    private static int countPlayerItems(ServerPlayer player, net.minecraft.world.item.Item item) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }
}
