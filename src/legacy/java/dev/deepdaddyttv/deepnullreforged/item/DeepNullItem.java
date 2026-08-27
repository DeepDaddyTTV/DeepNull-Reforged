package dev.deepdaddyttv.deepnullreforged.item;

import dev.deepdaddyttv.deepnullreforged.capability.DeepNullEnergyStorage;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullItemHandler;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullData;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenuProvider;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class DeepNullItem extends Item {
    public static final String SELECTED_SLOT_TAG = "SelectedSlot";
    private final DeepNullTier tier;

    public DeepNullItem(DeepNullTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public DeepNullTier getTier() {
        return tier;
    }

    public DeepNullItemHandler createHandler(ItemStack stack) {
        return new DeepNullItemHandler(stack, tier);
    }

    @Override
    public ICapabilityProvider initCapabilities(final ItemStack stack, @Nullable CompoundNBT nbt) {
        final LazyOptional<DeepNullItemHandler> handler = LazyOptional.of(() -> createHandler(stack));
        final LazyOptional<DeepNullEnergyStorage> energy = LazyOptional.of(() -> new DeepNullEnergyStorage(stack, tier));
        return new ICapabilityProvider() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
                if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return handler.cast();
                if (capability == CapabilityEnergy.ENERGY) return energy.cast();
                return LazyOptional.empty();
            }
        };
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isSecondaryUseActive()) {
            DeepNullItemHandler handler = createHandler(stack);
            int selected = Math.max(0, Math.min(handler.getSlots() - 1,
                    stack.getOrCreateTag().getInt(SELECTED_SLOT_TAG)));
            ItemStack stored = handler.getStackInSlot(selected);
            if (!stored.isEmpty()) {
                ItemStack usable = stored.copy();
                usable.setCount(Math.min(stored.getCount(), stored.getMaxStackSize()));
                int before = usable.getCount();
                player.setItemInHand(hand, usable);
                ActionResult<ItemStack> delegated = usable.getItem().use(world, player, hand);
                ItemStack resultStack = delegated.getObject();
                player.setItemInHand(hand, stack);
                if (delegated.getResult().consumesAction()) {
                    if (ItemStack.isSame(usable, resultStack) && ItemStack.tagMatches(usable, resultStack)) {
                        int consumed = Math.max(0, before - resultStack.getCount());
                        if (consumed > 0) handler.extractItem(selected, consumed, false);
                    } else {
                        handler.extractItem(selected, 1, false);
                        ItemStack remainder = handler.insertMatching(resultStack, true);
                        if (!remainder.isEmpty() && !player.addItem(remainder)) player.drop(remainder, false);
                    }
                    return new ActionResult<ItemStack>(delegated.getResult(), stack);
                }
            }
        }
        if (!world.isClientSide && player instanceof ServerPlayerEntity) {
            NetworkHooks.openGui((ServerPlayerEntity) player, new DeepNullMenuProvider(stack, hand, tier), buffer -> {
                buffer.writeByte(hand == Hand.MAIN_HAND ? 0 : 1);
                buffer.writeByte(tier.id());
            });
        }
        return ActionResult.sidedSuccess(stack, world.isClientSide);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null || player.isSecondaryUseActive()) return ActionResultType.PASS;

        ItemStack bag = context.getItemInHand();
        DeepNullItemHandler handler = createHandler(bag);
        int selected = Math.max(0, Math.min(handler.getSlots() - 1, bag.getOrCreateTag().getInt(SELECTED_SLOT_TAG)));
        ItemStack stored = handler.getStackInSlot(selected);
        if (stored.isEmpty()) return ActionResultType.PASS;

        ItemStack usable = stored.copy();
        usable.setCount(Math.min(stored.getCount(), stored.getMaxStackSize()));
        int before = usable.getCount();
        net.minecraft.util.math.BlockRayTraceResult hit = new net.minecraft.util.math.BlockRayTraceResult(
                context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
        ItemUseContext delegated = new ItemUseContext(context.getLevel(), player, context.getHand(), usable, hit);
        ActionResultType result = usable.getItem().useOn(delegated);
        int consumed = Math.max(0, before - usable.getCount());
        if (result.consumesAction() && consumed > 0 && !context.getLevel().isClientSide) {
            handler.extractItem(selected, consumed, false);
        }
        return result;
    }

    @Override
    public net.minecraft.item.Rarity getRarity(ItemStack stack) {
        return tier.rarity();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return tier.creative() || stack.getOrCreateTag().getBoolean("EnderLinked");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent(tier.slots() + " ").append(new TranslationTextComponent("dn.number_of_slots.desc")).withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent(tier.creative() ? "Infinite" : Integer.toString(tier.itemCapacity()))
                .append(" ").append(new TranslationTextComponent("dn.items_per_slot.desc")).withStyle(TextFormatting.DARK_GRAY));
        if (!Screen.hasShiftDown()) {
            tooltip.add(new StringTextComponent("Hold Shift for contents").withStyle(TextFormatting.YELLOW));
            return;
        }
        DeepNullItemHandler handler = createHandler(stack);
        int shown = 0;
        for (int i = 0; i < handler.getSlots() && shown < 8; i++) {
            ItemStack stored = handler.getStackInSlot(i);
            if (!stored.isEmpty()) {
                tooltip.add(new StringTextComponent(stored.getCount() + " x ").append(stored.getHoverName()).withStyle(TextFormatting.AQUA));
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
