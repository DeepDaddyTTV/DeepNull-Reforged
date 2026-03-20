package dev.deepdaddyttv.deepnullreforged.integration.mekanism;

import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.StoredChemical;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlockEntities;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlocks;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import mekanism.api.MekanismAPITags;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

public final class MekanismCompat {
    private MekanismCompat() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.CHEMICAL.item(),
                (stack, context) -> createItemChemicalHandler(stack),
                ModItems.REDSTONE_DAMP_NULL.get(),
                ModItems.LAPIS_DAMP_NULL.get(),
                ModItems.IRON_DAMP_NULL.get(),
                ModItems.GOLD_DAMP_NULL.get(),
                ModItems.DIAMOND_DAMP_NULL.get(),
                ModItems.EMERALD_DAMP_NULL.get(),
                ModItems.CREATIVE_DAMP_NULL.get()
        );
        event.registerBlockEntity(
                Capabilities.CHEMICAL.block(),
                ModBlockEntities.DEEP_NULL_DOCK.get(),
                MekanismCompat::createDockEntityChemicalHandler
        );
        event.registerBlock(
                Capabilities.CHEMICAL.block(),
                MekanismCompat::createDockChemicalHandler,
                ModBlocks.DEEP_NULL_DOCK.get()
        );
    }

    public static @Nullable IChemicalHandler getBlockChemicalHandler(UseOnContext context) {
        IChemicalHandler target = context.getLevel().getCapability(Capabilities.CHEMICAL.block(), context.getClickedPos(), context.getClickedFace());
        if (target == null) {
            target = context.getLevel().getCapability(Capabilities.CHEMICAL.block(), context.getClickedPos(), null);
        }
        return target;
    }

    public static StoredChemical fromChemicalStack(ChemicalStack stack) {
        if (stack.isEmpty()) {
            return StoredChemical.EMPTY;
        }
        Chemical chemical = stack.getChemical();
        ResourceLocation icon = chemical.getIcon();
        return new StoredChemical(
                stack.getTypeRegistryName().toString(),
                stack.getAmount(),
                icon == null ? "" : icon.toString(),
                chemical.getTint(),
                chemical.getTranslationKey(),
                chemical.is(MekanismAPITags.Chemicals.GASEOUS)
        );
    }

    public static ChemicalStack toChemicalStack(DeepNullInventory inventory, StoredChemical stored) {
        if (stored.isEmpty()) {
            return ChemicalStack.EMPTY;
        }
        HolderLookup.Provider registries = currentRegistries();
        if (registries == null) {
            return ChemicalStack.EMPTY;
        }
        Holder<Chemical> holder = Chemical.parseOptionalHolder(registries, stored.chemicalId());
        if (holder.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
            return ChemicalStack.EMPTY;
        }
        return new ChemicalStack(holder, stored.amount());
    }

    private static @Nullable IChemicalHandler createItemChemicalHandler(ItemStack stack) {
        DeepNullInventory inventory = createInventory(stack);
        return inventory == null || !inventory.supportsChemicalStorage() ? null : new DeepNullChemicalHandler(inventory, stack);
    }

    private static @Nullable IChemicalHandler createDockEntityChemicalHandler(DeepNullDockBlockEntity dock, @Nullable Direction side) {
        DeepNullInventory inventory = dock.createInventory();
        if (inventory == null || !inventory.supportsChemicalStorage()) {
            return null;
        }
        return new DeepNullChemicalHandler(inventory, dock.getStoredDeepNull());
    }

    private static @Nullable IChemicalHandler createDockChemicalHandler(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, @Nullable Direction side) {
        if (blockEntity instanceof DeepNullDockBlockEntity dock) {
            return createDockEntityChemicalHandler(dock, side);
        }
        if (level.getBlockEntity(pos) instanceof DeepNullDockBlockEntity dock) {
            return createDockEntityChemicalHandler(dock, side);
        }
        return null;
    }

    private static @Nullable DeepNullInventory createInventory(ItemStack stack) {
        if (!(stack.getItem() instanceof DeepNullItem deepNullItem)) {
            return null;
        }
        return new DeepNullInventory(deepNullItem.tier(), stack, MekanismCompat::currentRegistries, null);
    }

    private static @Nullable HolderLookup.Provider currentRegistries() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.registryAccess();
        }

        try {
            Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
            Object minecraft = minecraftClass.getMethod("getInstance").invoke(null);
            Object level = minecraftClass.getField("level").get(minecraft);
            if (level == null) {
                return null;
            }
            return (HolderLookup.Provider) level.getClass().getMethod("registryAccess").invoke(level);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }
}
