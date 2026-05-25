package dev.deepdaddyttv.deepnullreforged.gametest;

import dev.deepdaddyttv.deepnullreforged.dennull.DenNullData;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullEntry;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.item.DenNullItem;
import dev.deepdaddyttv.deepnullreforged.menu.DenNullMenu;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.item.ItemStack;

public final class DenNullRegressionGameTests {
    private DenNullRegressionGameTests() {
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void capture_requires_bait_and_leaves_entity_intact(GameTestHelper helper) {
        ServerPlayer player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack denStack = DeepNullGameTestSupport.denNullStack(DeepNullTier.REDSTONE);
        player.getInventory().setItem(0, denStack);

        Cow cow = cow(helper, new BlockPos(2, 1, 2));
        DenNullItem item = (DenNullItem) denStack.getItem();

        helper.assertFalse(item.tryCapture(player, denStack, cow), "Capture should fail without Bait");
        helper.assertFalse(cow.isRemoved(), "Cow should remain when capture has no Bait");
        helper.assertValueEqual(DenNullData.get(denStack).entries().size(), 0, "DenNull should remain empty");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void live_capture_mutates_authoritative_hand_stack(GameTestHelper helper) {
        ServerPlayer player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack denStack = DeepNullGameTestSupport.denNullStack(DeepNullTier.REDSTONE);
        player.getInventory().selected = 0;
        player.setItemInHand(InteractionHand.MAIN_HAND, denStack);
        player.getInventory().setItem(1, new ItemStack(ModItems.BAIT.get()));

        Cow cow = cow(helper, new BlockPos(2, 1, 2));
        DenNullItem item = (DenNullItem) denStack.getItem();
        ItemStack copiedInteractionStack = denStack.copy();

        InteractionResult result = item.interactLivingEntity(copiedInteractionStack, player, cow, InteractionHand.MAIN_HAND);

        helper.assertTrue(result == InteractionResult.SUCCESS, "Live capture should succeed");
        helper.assertTrue(cow.isRemoved(), "Captured cow should be removed after data is saved");
        helper.assertValueEqual(DenNullData.get(player.getItemInHand(InteractionHand.MAIN_HAND)).totalCount(), 1,
                "Authoritative held DenNull should store one capture");
        helper.assertValueEqual(DenNullData.get(copiedInteractionStack).totalCount(), 0,
                "Copied interaction stack should not be the saved capture source");
        helper.assertTrue(player.getInventory().getItem(1).isEmpty(), "Live capture should consume one Bait");

        DenNullMenu menu = DenNullMenu.forItem(7, player.getInventory(), player.getInventory().selected, DeepNullTier.REDSTONE);
        helper.assertValueEqual(menu.getData().totalCount(), 1, "DenNull menu data should read the saved capture");

        item.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(DenNullData.get(player.getItemInHand(InteractionHand.MAIN_HAND)).totalCount(), 0,
                "Release should decrement the saved held capture");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void offhand_live_capture_mutates_authoritative_offhand_stack(GameTestHelper helper) {
        ServerPlayer player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack denStack = DeepNullGameTestSupport.denNullStack(DeepNullTier.REDSTONE);
        player.setItemInHand(InteractionHand.OFF_HAND, denStack);
        player.getInventory().setItem(1, new ItemStack(ModItems.BAIT.get()));

        Cow cow = cow(helper, new BlockPos(2, 1, 2));
        DenNullItem item = (DenNullItem) denStack.getItem();
        ItemStack copiedInteractionStack = denStack.copy();

        InteractionResult result = item.interactLivingEntity(copiedInteractionStack, player, cow, InteractionHand.OFF_HAND);

        helper.assertTrue(result == InteractionResult.SUCCESS, "Offhand live capture should succeed");
        helper.assertTrue(cow.isRemoved(), "Offhand captured cow should be removed");
        helper.assertValueEqual(DenNullData.get(player.getOffhandItem()).totalCount(), 1,
                "Authoritative offhand DenNull should store one capture");
        helper.assertValueEqual(DenNullData.get(copiedInteractionStack).totalCount(), 0,
                "Copied offhand interaction stack should not be the saved capture source");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void bait_capture_stores_entry_and_release_decrements(GameTestHelper helper) {
        ServerPlayer player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack denStack = DeepNullGameTestSupport.denNullStack(DeepNullTier.REDSTONE);
        player.getInventory().setItem(0, denStack);
        player.getInventory().setItem(1, new ItemStack(ModItems.BAIT.get()));

        Cow cow = cow(helper, new BlockPos(2, 1, 2));
        DenNullItem item = (DenNullItem) denStack.getItem();

        helper.assertTrue(item.tryCapture(player, denStack, cow), "Capture should succeed with Bait");
        helper.assertTrue(cow.isRemoved(), "Captured cow should be removed");
        helper.assertValueEqual(DenNullData.get(denStack).totalCount(), 1, "DenNull should store one capture");
        helper.assertTrue(player.getInventory().getItem(1).isEmpty(), "Capture should consume one Bait");

        player.setItemInHand(InteractionHand.MAIN_HAND, denStack);
        item.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(DenNullData.get(denStack).totalCount(), 0, "Release should decrement the stored capture");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void clone_upgrade_release_does_not_decrement(GameTestHelper helper) {
        ServerPlayer player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack denStack = DeepNullGameTestSupport.denNullStack(DeepNullTier.EMERALD);
        DenNullEntry cowEntry = new DenNullEntry(ResourceLocation.withDefaultNamespace("cow"), 1, "cow|standard", new CompoundTag(), "Cow", "standard data");
        DenNullData.set(denStack, new DenNullData(0, java.util.List.of(cowEntry)).withUpgrades(DenNullData.EMPTY.upgrades().withInstalled(DenNullUpgradeType.CLONE)));
        player.setItemInHand(InteractionHand.MAIN_HAND, denStack);

        ((DenNullItem) denStack.getItem()).use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

        helper.assertValueEqual(DenNullData.get(denStack).totalCount(), 1, "Clone upgrade should keep the stored cow available");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void four_foxes_stack_and_release_leaves_visible_count(GameTestHelper helper) {
        ServerPlayer player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack denStack = DeepNullGameTestSupport.denNullStack(DeepNullTier.REDSTONE);
        player.getInventory().setItem(0, denStack);
        player.getInventory().setItem(1, new ItemStack(ModItems.BAIT.get(), 4));
        DenNullItem item = (DenNullItem) denStack.getItem();

        for (int index = 0; index < 4; index++) {
            Fox fox = fox(helper, new BlockPos(2 + index, 1, 2));
            helper.assertTrue(item.tryCapture(player, denStack, fox), "Fox capture " + index + " should succeed with Bait");
        }

        DenNullData captured = DenNullData.get(denStack);
        helper.assertValueEqual(captured.entries().size(), 1, "Matching fox captures should share one entry");
        helper.assertValueEqual(captured.entries().get(0).count(), 4, "Fox entry should show four captures");
        helper.assertTrue(player.getInventory().getItem(1).isEmpty(), "Four captures should consume four Bait");

        player.setItemInHand(InteractionHand.MAIN_HAND, denStack);
        item.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        DenNullData afterRelease = DenNullData.get(denStack);
        helper.assertValueEqual(afterRelease.entries().size(), 1, "Released stacked fox entry should remain visible");
        helper.assertValueEqual(afterRelease.entries().get(0).count(), 3, "Release should decrement stacked fox count");
        helper.succeed();
    }

    private static Cow cow(GameTestHelper helper, BlockPos pos) {
        Cow cow = EntityType.COW.create(helper.getLevel());
        if (cow == null) {
            helper.fail("Cow entity could not be created");
            throw new IllegalStateException("Unreachable");
        }
        BlockPos absolute = helper.absolutePos(pos);
        cow.moveTo(absolute.getX() + 0.5D, absolute.getY(), absolute.getZ() + 0.5D, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(cow);
        return cow;
    }

    private static Fox fox(GameTestHelper helper, BlockPos pos) {
        Fox fox = EntityType.FOX.create(helper.getLevel());
        if (fox == null) {
            helper.fail("Fox entity could not be created");
            throw new IllegalStateException("Unreachable");
        }
        BlockPos absolute = helper.absolutePos(pos);
        fox.moveTo(absolute.getX() + 0.5D, absolute.getY(), absolute.getZ() + 0.5D, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(fox);
        return fox;
    }
}
