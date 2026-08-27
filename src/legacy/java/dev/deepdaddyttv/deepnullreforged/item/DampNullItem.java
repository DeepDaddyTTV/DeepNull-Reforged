package dev.deepdaddyttv.deepnullreforged.item;

import dev.deepdaddyttv.deepnullreforged.capability.DampNullFluidHandler;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullData;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class DampNullItem extends Item {
    private final DeepNullTier tier;

    public DampNullItem(DeepNullTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public DeepNullTier getTier() { return tier; }

    public DampNullFluidHandler createHandler(ItemStack stack) {
        return new DampNullFluidHandler(stack, tier);
    }

    @Override
    public ICapabilityProvider initCapabilities(final ItemStack stack, @Nullable CompoundNBT nbt) {
        final LazyOptional<DampNullFluidHandler> handler = LazyOptional.of(() -> createHandler(stack));
        return new ICapabilityProvider() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
                if (capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY
                        || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return handler.cast();
                return LazyOptional.empty();
            }
        };
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player != null && FluidUtil.interactWithFluidHandler(player, context.getHand(), context.getLevel(),
                context.getClickedPos(), context.getClickedFace())) {
            return ActionResultType.sidedSuccess(context.getLevel().isClientSide);
        }
        return ActionResultType.PASS;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockRayTraceResult hit = getPlayerPOVHitResult(world, player, RayTraceContext.FluidMode.SOURCE_ONLY);
        if (!world.isClientSide) {
            if (dev.deepdaddyttv.deepnullreforged.inventory.DeepNullData.hasUpgrade(stack, "sponge_upgrade")) {
                int absorbed = absorbSources(world, hit.getBlockPos(), createHandler(stack));
                if (absorbed > 0) return ActionResult.success(stack);
            }
            FluidActionResult pickedUp = FluidUtil.tryPickUpFluid(stack, player, world, hit.getBlockPos(), hit.getDirection());
            if (pickedUp.isSuccess()) {
                player.setItemInHand(hand, pickedUp.getResult());
                return ActionResult.success(pickedUp.getResult());
            }

            DampNullFluidHandler handler = createHandler(stack);
            FluidStack selected = handler.drain(1000, net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE);
            if (!selected.isEmpty() && FluidUtil.tryPlaceFluid(player, world, hand, hit.getBlockPos().relative(hit.getDirection()), handler, selected)) {
                return ActionResult.success(stack);
            }
        }
        return ActionResult.sidedSuccess(stack, world.isClientSide);
    }

    private int absorbSources(World world, net.minecraft.util.math.BlockPos origin, DampNullFluidHandler handler) {
        Queue<net.minecraft.util.math.BlockPos> queue = new ArrayDeque<net.minecraft.util.math.BlockPos>();
        Set<Long> visited = new HashSet<Long>();
        queue.add(origin);
        int absorbed = 0;
        while (!queue.isEmpty() && absorbed < tier.spongeLimit()) {
            net.minecraft.util.math.BlockPos pos = queue.remove();
            if (!visited.add(pos.asLong()) || pos.distManhattan(origin) > 8) continue;
            FluidState state = world.getFluidState(pos);
            if (!state.isEmpty() && state.isSource()) {
                FluidStack fluid = new FluidStack(state.getType(), 1000);
                if (handler.fill(fluid, net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE) == 1000) {
                    handler.fill(fluid, net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                    world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    absorbed++;
                }
            }
            for (Direction direction : Direction.values()) queue.add(pos.relative(direction));
        }
        return absorbed;
    }

    @Override
    public net.minecraft.item.Rarity getRarity(ItemStack stack) { return tier.rarity(); }

    @Override
    public boolean isFoil(ItemStack stack) { return tier.creative(); }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        DampNullFluidHandler handler = createHandler(stack);
        tooltip.add(new StringTextComponent(handler.getTanks() + " ").append(new TranslationTextComponent("dn.number_of_tanks.desc")).withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent(tier.creative() ? "Infinite" : (tier.fluidCapacity() / 1000) + " buckets per tank").withStyle(TextFormatting.DARK_GRAY));
        if (!Screen.hasShiftDown()) {
            tooltip.add(new StringTextComponent("Hold Shift for fluids").withStyle(TextFormatting.YELLOW));
            return;
        }
        int shown = 0;
        for (int i = 0; i < handler.getTanks() && shown < 8; i++) {
            FluidStack fluid = handler.getFluidInTank(i);
            if (!fluid.isEmpty()) {
                tooltip.add(new StringTextComponent(fluid.getAmount() + " mB ").append(fluid.getDisplayName()).withStyle(TextFormatting.AQUA));
                shown++;
            }
        }
        if (shown == 0) tooltip.add(new TranslationTextComponent("dn.empty.desc").withStyle(TextFormatting.ITALIC));
        net.minecraft.nbt.ListNBT upgrades = DeepNullData.upgrades(stack);
        if (!upgrades.isEmpty()) {
            tooltip.add(new StringTextComponent("Upgrades:").withStyle(TextFormatting.GOLD));
            for (int i = 0; i < upgrades.size(); i++) {
                tooltip.add(new StringTextComponent("  " + upgrades.getString(i)).withStyle(TextFormatting.GRAY));
            }
        }
    }
}
