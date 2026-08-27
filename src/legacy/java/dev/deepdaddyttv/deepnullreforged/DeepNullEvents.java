package dev.deepdaddyttv.deepnullreforged;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullData;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullItemHandler;
import dev.deepdaddyttv.deepnullreforged.item.DampNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullUpgradeItem;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import dev.deepdaddyttv.deepnullreforged.capability.DampNullFluidHandler;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import dev.deepdaddyttv.deepnullreforged.registry.ModContent;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DeepNullEvents {
    @SubscribeEvent
    public void onPickup(EntityItemPickupEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player.level.isClientSide) return;
        ItemEntity entity = event.getItem();
        ItemStack incoming = entity.getItem();
        if (incoming.isEmpty()) return;

        ItemStack remainder = incoming;
        for (int slot = 0; slot < player.inventory.getContainerSize() && !remainder.isEmpty(); slot++) {
            ItemStack carried = player.inventory.getItem(slot);
            if (!(carried.getItem() instanceof DeepNullItem)) continue;
            if (!carried.getOrCreateTag().getBoolean("AutoPickupInitialized")) {
                carried.getOrCreateTag().putBoolean("AutoPickupInitialized", true);
                carried.getOrCreateTag().putBoolean("AutoPickup", true);
            }
            if (!carried.getOrCreateTag().getBoolean("AutoPickup")) continue;
            DeepNullItemHandler handler = ((DeepNullItem) carried.getItem()).createHandler(carried);
            remainder = applyAutoSmelting(player, carried, handler, remainder);
            remainder = applyCompression(player, carried, handler, remainder, 3, "advanced_compression_upgrade");
            remainder = applyCompression(player, carried, handler, remainder, 2, "basic_compression_upgrade");
            if (handler.containsMatching(remainder)) remainder = handler.insertMatching(remainder, false);
        }

        if (remainder.isEmpty()) {
            entity.remove();
            event.setCanceled(true);
        } else if (remainder.getCount() != incoming.getCount()) {
            entity.setItem(remainder);
        }
    }

    private ItemStack applyAutoSmelting(PlayerEntity player, ItemStack carried, DeepNullItemHandler handler, ItemStack incoming) {
        if (incoming.isEmpty() || !DeepNullData.hasUpgrade(carried, "auto_smelting_upgrade")) return incoming;
        Inventory input = new Inventory(incoming.copy());
        java.util.Optional<FurnaceRecipe> recipe = player.level.getRecipeManager().getRecipeFor(IRecipeType.SMELTING, input, player.level);
        if (!recipe.isPresent()) return incoming;
        ItemStack output = recipe.get().getResultItem().copy();
        if (output.isEmpty() || !handler.containsMatching(output)) return incoming;
        long total = (long) output.getCount() * incoming.getCount();
        output.setCount((int) Math.min(Integer.MAX_VALUE, total));
        ItemStack remainder = handler.insertMatching(output, false);
        int inserted = output.getCount() - remainder.getCount();
        int consumed = inserted / Math.max(1, recipe.get().getResultItem().getCount());
        ItemStack inputRemainder = incoming.copy();
        inputRemainder.shrink(Math.min(consumed, incoming.getCount()));
        return inputRemainder;
    }

    private ItemStack applyCompression(PlayerEntity player, ItemStack carried, DeepNullItemHandler handler,
                                       ItemStack incoming, int size, String upgrade) {
        int ingredients = size * size;
        if (incoming.isEmpty() || incoming.getCount() < ingredients || !DeepNullData.hasUpgrade(carried, upgrade)) return incoming;
        CraftingInventory crafting = new CraftingInventory(new Container(null, -1) {
            @Override
            public boolean stillValid(PlayerEntity player) { return false; }
        }, size, size);
        for (int slot = 0; slot < ingredients; slot++) {
            ItemStack one = incoming.copy();
            one.setCount(1);
            crafting.setItem(slot, one);
        }
        java.util.Optional<ICraftingRecipe> recipe = player.level.getRecipeManager().getRecipeFor(IRecipeType.CRAFTING, crafting, player.level);
        if (!recipe.isPresent()) return incoming;
        ItemStack singleResult = recipe.get().assemble(crafting);
        if (singleResult.isEmpty() || !handler.containsMatching(singleResult)) return incoming;
        int groups = incoming.getCount() / ingredients;
        long wanted = (long) groups * singleResult.getCount();
        ItemStack output = singleResult.copy();
        output.setCount((int) Math.min(Integer.MAX_VALUE, wanted));
        ItemStack outputRemainder = handler.insertMatching(output, false);
        int inserted = output.getCount() - outputRemainder.getCount();
        int completedGroups = inserted / Math.max(1, singleResult.getCount());
        ItemStack result = incoming.copy();
        result.shrink(completedGroups * ingredients);
        return result;
    }

    @SubscribeEvent
    public void onInstallUpgrade(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() != Hand.MAIN_HAND || event.getWorld().isClientSide) return;
        PlayerEntity player = event.getPlayer();
        ItemStack nullStack = player.getMainHandItem();
        ItemStack upgradeStack = player.getOffhandItem();
        if (!(nullStack.getItem() instanceof DeepNullItem || nullStack.getItem() instanceof DampNullItem)) return;
        if (upgradeStack.getItem() == ModContent.SYNCHRONIZER.get()) {
            synchronize(nullStack, upgradeStack, player);
            event.setCanceled(true);
            return;
        }
        if (!(upgradeStack.getItem() instanceof DeepNullUpgradeItem)) return;

        String id = ((DeepNullUpgradeItem) upgradeStack.getItem()).getUpgradeId();
        if (DeepNullData.addUpgrade(nullStack, id)) {
            if (!player.abilities.instabuild) upgradeStack.shrink(1);
            player.displayClientMessage(new StringTextComponent("Installed ").append(upgradeStack.getHoverName()).withStyle(TextFormatting.GREEN), false);
            event.setCanceled(true);
        }
    }

    private void synchronize(ItemStack nullStack, ItemStack synchronizer, PlayerEntity player) {
        CompoundNBT synchronizerTag = synchronizer.getOrCreateTag();
        if (!synchronizerTag.contains("StoredConfig", net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT source = nullStack.getOrCreateTag();
            CompoundNBT config = new CompoundNBT();
            copyConfigKey(source, config, "DeepNullUpgrades");
            copyConfigKey(source, config, "SelectedSlot");
            copyConfigKey(source, config, "AutoPickup");
            copyConfigKey(source, config, "AutoPickupInitialized");
            copyConfigKey(source, config, "Style");
            copyConfigKey(source, config, "FrameColor");
            copyConfigKey(source, config, "GlassColor");
            synchronizerTag.put("StoredConfig", config);
            player.displayClientMessage(new StringTextComponent("Null configuration saved").withStyle(TextFormatting.AQUA), false);
        } else {
            CompoundNBT target = nullStack.getOrCreateTag();
            CompoundNBT config = synchronizerTag.getCompound("StoredConfig");
            for (String key : config.getAllKeys()) target.put(key, config.get(key).copy());
            player.displayClientMessage(new StringTextComponent("Null configuration restored").withStyle(TextFormatting.AQUA), false);
        }
    }

    private void copyConfigKey(CompoundNBT source, CompoundNBT destination, String key) {
        if (source.contains(key)) destination.put(key, source.get(key).copy());
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide || event.player.tickCount % 20 != 0) return;
        PlayerEntity player = event.player;
        runDampNullGenerators(player);
        if (!player.canEat(false)) return;

        for (int slot = 0; slot < player.inventory.getContainerSize(); slot++) {
            ItemStack carried = player.inventory.getItem(slot);
            if (!(carried.getItem() instanceof DeepNullItem) || !DeepNullData.hasUpgrade(carried, "auto_feeding_upgrade")) continue;
            DeepNullItemHandler handler = ((DeepNullItem) carried.getItem()).createHandler(carried);
            for (int storedSlot = 0; storedSlot < handler.getSlots(); storedSlot++) {
                ItemStack food = handler.getStackInSlot(storedSlot);
                if (!food.isEmpty() && food.isEdible()) {
                    ItemStack serving = food.copy();
                    serving.setCount(1);
                    player.eat(player.level, serving);
                    handler.extractItem(storedSlot, 1, false);
                    return;
                }
            }
        }
    }

    private void runDampNullGenerators(PlayerEntity player) {
        for (int slot = 0; slot < player.inventory.getContainerSize(); slot++) {
            ItemStack dampStack = player.inventory.getItem(slot);
            if (!(dampStack.getItem() instanceof DampNullItem)) continue;
            DampNullItem dampItem = (DampNullItem) dampStack.getItem();
            DampNullFluidHandler fluids = dampItem.createHandler(dampStack);
            if (!hasFluid(fluids, Fluids.WATER) || !hasFluid(fluids, Fluids.LAVA)) continue;

            if (DeepNullData.hasUpgrade(dampStack, "stone_generator_upgrade")) {
                routeGenerated(player, new ItemStack(Items.COBBLESTONE, dampItem.getTier().stoneRate()));
            }
            if (player.tickCount % 100 == 0 && DeepNullData.hasUpgrade(dampStack, "obsidian_generator_upgrade")) {
                ItemStack obsidian = new ItemStack(Items.OBSIDIAN);
                if (canRouteGenerated(player, obsidian)) {
                    fluids.drain(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
                    fluids.drain(new FluidStack(Fluids.LAVA, 1000), IFluidHandler.FluidAction.EXECUTE);
                    routeGenerated(player, obsidian);
                }
            }
        }
    }

    private boolean hasFluid(DampNullFluidHandler handler, net.minecraft.fluid.Fluid fluid) {
        return handler.drain(new FluidStack(fluid, 1000), IFluidHandler.FluidAction.SIMULATE).getAmount() >= 1000;
    }

    private boolean canRouteGenerated(PlayerEntity player, ItemStack output) {
        for (int slot = 0; slot < player.inventory.getContainerSize(); slot++) {
            ItemStack carried = player.inventory.getItem(slot);
            if (carried.getItem() instanceof DeepNullItem) {
                DeepNullItemHandler handler = ((DeepNullItem) carried.getItem()).createHandler(carried);
                if (handler.containsMatching(output) && handler.canAcceptMatching(output)) return true;
            }
        }
        return false;
    }

    private void routeGenerated(PlayerEntity player, ItemStack output) {
        for (int slot = 0; slot < player.inventory.getContainerSize() && !output.isEmpty(); slot++) {
            ItemStack carried = player.inventory.getItem(slot);
            if (carried.getItem() instanceof DeepNullItem) {
                DeepNullItemHandler handler = ((DeepNullItem) carried.getItem()).createHandler(carried);
                if (handler.containsMatching(output)) output = handler.insertMatching(output, false);
            }
        }
    }
}
