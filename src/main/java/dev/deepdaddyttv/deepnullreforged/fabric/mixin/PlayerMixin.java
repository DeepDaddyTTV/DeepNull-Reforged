package dev.deepdaddyttv.deepnullreforged.fabric.mixin;

import dev.deepdaddyttv.deepnullreforged.event.CommonEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
abstract class PlayerMixin {
    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("RETURN"))
    private void deepnullreforged$markTossedItems(ItemStack itemStack, boolean thrownFromHand, CallbackInfoReturnable<@Nullable ItemEntity> cir) {
        if (!thrownFromHand) {
            return;
        }

        Player self = (Player) (Object) this;
        if (self.level().isClientSide()) {
            return;
        }

        ItemEntity itemEntity = cir.getReturnValue();
        if (itemEntity != null) {
            CommonEvents.markPlayerTossedItem(self, itemEntity);
        }
    }
}
