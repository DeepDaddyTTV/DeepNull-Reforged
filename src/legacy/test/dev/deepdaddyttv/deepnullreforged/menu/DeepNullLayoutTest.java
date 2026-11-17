package dev.deepdaddyttv.deepnullreforged.menu;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

class DeepNullLayoutTest {
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6})
    void everySlotMatchesShippedTexture(int tier) throws Exception {
        int rows = Math.min(tier + 1, 6);
        BufferedImage texture = ImageIO.read(Paths.get("src/main/resources/assets/deepnullreforged/textures/gui/deepnullscreen" + tier + ".png").toFile());
        assertEquals(256, texture.getWidth());
        assertEquals(256, texture.getHeight());
        assertEquals(202, DeepNullLayout.WIDTH);
        assertEquals(141 + (rows - 1) * 21, DeepNullLayout.height(rows));
        for (int col = 0; col < 9; col++) {
            assertEquals(9 + col * 21, DeepNullLayout.x(col));
            for (int row = 0; row < rows; row++) {
                assertEquals(19 + row * 21, DeepNullLayout.storageY(row));
                assertSlotFrame(texture, DeepNullLayout.x(col), DeepNullLayout.storageY(row));
            }
            for (int row = 0; row < 3; row++) {
                assertEquals(49 + (rows - 1) * 21 + row * 21, DeepNullLayout.playerY(rows, row));
                assertSlotFrame(texture, DeepNullLayout.x(col), DeepNullLayout.playerY(rows, row));
            }
            assertEquals(116 + (rows - 1) * 21, DeepNullLayout.hotbarY(rows));
            assertSlotFrame(texture, DeepNullLayout.x(col), DeepNullLayout.hotbarY(rows));
        }
    }

    private void assertSlotFrame(BufferedImage texture, int x, int y) {
        // The bevel is brighter than the recessed interior; sample both axes.
        // Comparing colors exactly would incorrectly reject the atlas shading gradient.
        int interior = brightness(texture.getRGB(x + 7, y + 7));
        assertTrue(brightness(texture.getRGB(x + 7, y - 1)) > interior + 25,
                "Top frame mismatch at " + x + "," + y);
        assertTrue(brightness(texture.getRGB(x - 1, y + 7)) > interior + 25,
                "Left frame mismatch at " + x + "," + y);
    }

    private int brightness(int rgb) {
        return Math.max((rgb >> 16) & 255, Math.max((rgb >> 8) & 255, rgb & 255));
    }
}
