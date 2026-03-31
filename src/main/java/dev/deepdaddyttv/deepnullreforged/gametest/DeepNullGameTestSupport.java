package dev.deepdaddyttv.deepnullreforged.gametest;

import dev.deepdaddyttv.deepnullreforged.block.entity.NullWorkbenchBlockEntity;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlocks;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

final class DeepNullGameTestSupport {
    static final String EMPTY_TEMPLATE_NAMESPACE = "fabric-gametest-api-v1";
    static final String EMPTY_TEMPLATE = FabricGameTest.EMPTY_STRUCTURE;

    private DeepNullGameTestSupport() {
    }

    static DeepNullInventory deepNullInventory(GameTestHelper helper, DeepNullTier tier) {
        return new DeepNullInventory(tier, deepNullStack(tier), helper.getLevel().registryAccess(), null);
    }

    static DeepNullInventory dampNullInventory(GameTestHelper helper, DeepNullTier tier) {
        return new DeepNullInventory(tier, dampNullStack(tier), helper.getLevel().registryAccess(), null);
    }

    static ItemStack deepNullStack(DeepNullTier tier) {
        return new ItemStack(switch (tier) {
            case REDSTONE -> ModItems.REDSTONE_DEEP_NULL.get();
            case LAPIS -> ModItems.LAPIS_DEEP_NULL.get();
            case IRON -> ModItems.IRON_DEEP_NULL.get();
            case GOLD -> ModItems.GOLD_DEEP_NULL.get();
            case DIAMOND -> ModItems.DIAMOND_DEEP_NULL.get();
            case EMERALD -> ModItems.EMERALD_DEEP_NULL.get();
            case CREATIVE -> ModItems.CREATIVE_DEEP_NULL.get();
        });
    }

    static ItemStack dampNullStack(DeepNullTier tier) {
        return new ItemStack(switch (tier) {
            case REDSTONE -> ModItems.REDSTONE_DAMP_NULL.get();
            case LAPIS -> ModItems.LAPIS_DAMP_NULL.get();
            case IRON -> ModItems.IRON_DAMP_NULL.get();
            case GOLD -> ModItems.GOLD_DAMP_NULL.get();
            case DIAMOND -> ModItems.DIAMOND_DAMP_NULL.get();
            case EMERALD -> ModItems.EMERALD_DAMP_NULL.get();
            case CREATIVE -> ModItems.CREATIVE_DAMP_NULL.get();
        });
    }

    static ItemStack panelStack(DeepNullTier tier, int count) {
        ItemStack stack = new ItemStack(switch (tier) {
            case REDSTONE -> ModItems.REDSTONE_PANEL.get();
            case LAPIS -> ModItems.LAPIS_PANEL.get();
            case IRON -> ModItems.IRON_PANEL.get();
            case GOLD -> ModItems.GOLD_PANEL.get();
            case DIAMOND -> ModItems.DIAMOND_PANEL.get();
            case EMERALD -> ModItems.EMERALD_PANEL.get();
            case CREATIVE -> throw new IllegalArgumentException("Creative tier has no craftable panel recipe");
        });
        stack.setCount(count);
        return stack;
    }

    static ItemStack upgradeStack(DeepNullUpgradeType type) {
        return new ItemStack(switch (type) {
            case FILTER -> ModItems.FILTER_UPGRADE.get();
            case FLUID -> ModItems.FLUID_UPGRADE.get();
            case ENERGY -> ModItems.ENERGY_UPGRADE.get();
            case DEEP_ENERGY -> ModItems.DEEP_ENERGY_UPGRADE.get();
            case AUTO_FEEDING -> ModItems.AUTO_FEEDING_UPGRADE.get();
            case AUTO_SMELTING -> ModItems.AUTO_SMELTING_UPGRADE.get();
            case BASIC_COMPRESSION -> ModItems.BASIC_COMPRESSION_UPGRADE.get();
            case ADVANCED_COMPRESSION -> ModItems.ADVANCED_COMPRESSION_UPGRADE.get();
            case STONE_GENERATOR -> ModItems.STONE_GENERATOR_UPGRADE.get();
            case OBSIDIAN_GENERATOR -> ModItems.OBSIDIAN_GENERATOR_UPGRADE.get();
            case SPONGE -> ModItems.SPONGE_UPGRADE.get();
            case GAS -> ModItems.GAS_UPGRADE.get();
            case STONEWORKS -> ModItems.STONEWORKS_UPGRADE.get();
            case ENDER -> ModItems.ENDER_UPGRADE.get();
        });
    }

    static int storedItemCount(DeepNullInventory inventory, Item item) {
        int total = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    static ServerPlayer fakePlayer(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getInventory().clearContent();
        player.containerMenu = player.inventoryMenu;
        player.moveTo(
                helper.absolutePos(new BlockPos(1, 1, 1)).getX() + 0.5D,
                helper.absolutePos(new BlockPos(1, 1, 1)).getY(),
                helper.absolutePos(new BlockPos(1, 1, 1)).getZ() + 0.5D
        );
        return player;
    }

    static NullWorkbenchBlockEntity placeWorkbench(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, ModBlocks.NULL_WORKBENCH.get());
        ServerLevel level = helper.getLevel();
        if (!(level.getBlockEntity(helper.absolutePos(pos)) instanceof NullWorkbenchBlockEntity workbench)) {
            helper.fail("Null Workbench block entity was not created at " + pos);
            throw new IllegalStateException("Unreachable");
        }
        return workbench;
    }

    static void tickWorkbench(GameTestHelper helper, BlockPos pos, int ticks) {
        ServerLevel level = helper.getLevel();
        BlockPos absolutePos = helper.absolutePos(pos);
        if (!(level.getBlockEntity(absolutePos) instanceof NullWorkbenchBlockEntity workbench)) {
            helper.fail("Expected Null Workbench block entity at " + pos);
            return;
        }
        for (int i = 0; i < ticks; i++) {
            NullWorkbenchBlockEntity.serverTick(level, absolutePos, level.getBlockState(absolutePos), workbench);
        }
    }
}
