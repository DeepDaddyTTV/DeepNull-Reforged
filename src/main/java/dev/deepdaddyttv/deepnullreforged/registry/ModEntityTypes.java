package dev.deepdaddyttv.deepnullreforged.registry;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.entity.DampNullBalloonProjectile;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, DeepNullReforged.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<DampNullBalloonProjectile>> DAMPNULL_BALLOON_PROJECTILE = ENTITY_TYPES.register(
            "dampnull_balloon_projectile",
            () -> EntityType.Builder.<DampNullBalloonProjectile>of(DampNullBalloonProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("dampnull_balloon_projectile")
    );

    private ModEntityTypes() {
    }
}
