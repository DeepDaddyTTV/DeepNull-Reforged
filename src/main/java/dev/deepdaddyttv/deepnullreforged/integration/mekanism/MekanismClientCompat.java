package dev.deepdaddyttv.deepnullreforged.integration.mekanism;

import dev.deepdaddyttv.deepnullreforged.inventory.StoredChemical;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public final class MekanismClientCompat {
    private static volatile @Nullable Method chemicalSpriteResolver;
    private static volatile boolean chemicalSpriteResolverInitialized;

    private MekanismClientCompat() {
    }

    public static @Nullable TextureAtlasSprite getChemicalSprite(StoredChemical storedChemical) {
        if (storedChemical.isEmpty() || !ModList.get().isLoaded("mekanism")) {
            return null;
        }

        ResourceLocation icon = storedChemical.iconLocation();
        if (icon == null) {
            return null;
        }

        Method resolver = chemicalSpriteResolver();
        if (resolver == null) {
            return null;
        }

        try {
            Object sprite = resolver.invoke(null, icon);
            return sprite instanceof TextureAtlasSprite textureAtlasSprite ? textureAtlasSprite : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static @Nullable Method chemicalSpriteResolver() {
        if (chemicalSpriteResolverInitialized) {
            return chemicalSpriteResolver;
        }

        synchronized (MekanismClientCompat.class) {
            if (chemicalSpriteResolverInitialized) {
                return chemicalSpriteResolver;
            }

            try {
                Class<?> rendererClass = Class.forName("mekanism.client.render.MekanismRenderer");
                chemicalSpriteResolver = rendererClass.getMethod("getSprite", ResourceLocation.class);
            } catch (ReflectiveOperationException | LinkageError ignored) {
                chemicalSpriteResolver = null;
            }

            chemicalSpriteResolverInitialized = true;
            return chemicalSpriteResolver;
        }
    }
}
