package dev.deepdaddyttv.deepnullreforged.integration.mekanism;

import dev.deepdaddyttv.deepnullreforged.inventory.StoredChemical;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.Nullable;

public final class MekanismClientCompat {
    private MekanismClientCompat() {
    }

    public static @Nullable TextureAtlasSprite getChemicalSprite(StoredChemical storedChemical) {
        return null;
    }
}
