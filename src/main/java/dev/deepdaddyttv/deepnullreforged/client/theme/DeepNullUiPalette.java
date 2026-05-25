package dev.deepdaddyttv.deepnullreforged.client.theme;

public record DeepNullUiPalette(
        int panelBodyRgb,
        int panelHeaderRgb,
        int panelOutlineRgb,
        int panelTitleTextColor,
        int panelBodyTextColor,
        int screenTitleTextColor,
        int screenBodyTextColor,
        int screenAccentTextColor,
        int hoverOutlineColor,
        int hoverFillColor,
        int slotFrameColor,
        int slotPreviewOverlayColor,
        int slotUnavailableOverlayColor,
        int ghostSlotOverlayColor
) {
    private static final DeepNullUiPalette VANILLA = new DeepNullUiPalette(
            0xC4B88E,
            0x8F7448,
            0x5D492D,
            0xFF20150A,
            0xFF2D2113,
            0xFFF7F2D8,
            0xFFF1EAD0,
            0xFFE7D6A2,
            0xFF9F8B5B,
            0x33FFF5C3,
            0xFF7A6A44,
            0x77000000,
            0x884C1D14,
            0x55000000
    );
    private static final DeepNullUiPalette MINECRAFT_DARK = new DeepNullUiPalette(
            0x121720,
            0x1A2230,
            0x697487,
            0xFFFFFFFF,
            0xFFE8EDF5,
            0xFFFFFFFF,
            0xFFE8EDF5,
            0xFF9DB4D8,
            0xFFD8DCE5,
            0x22FFFFFF,
            0xFF8C8C8C,
            0x88000000,
            0xAA220000,
            0x55000000
    );
    private static final DeepNullUiPalette LEGACY = new DeepNullUiPalette(
            0x1A2231,
            0x2A3A54,
            0x66AAC8,
            0xFFFFFFFF,
            0xFFDDF0FF,
            0xFFF4FBFF,
            0xFFDDF0FF,
            0xFF88D8F2,
            0xFF88D8F2,
            0x224CC9F0,
            0xFF4A90A4,
            0x77000000,
            0x8838222A,
            0x5526404F
    );

    public static DeepNullUiPalette of(DeepNullUiTheme theme) {
        return switch (theme) {
            case VANILLA -> VANILLA;
            case MINECRAFT_DARK -> MINECRAFT_DARK;
            case LEGACY -> LEGACY;
        };
    }

    public int panelBody(float opacity) {
        return withOpacity(panelBodyRgb, opacity);
    }

    public int panelHeader(float opacity) {
        return withOpacity(panelHeaderRgb, opacity);
    }

    public int panelOutline(float opacity) {
        return withOpacity(panelOutlineRgb, opacity);
    }

    private static int withOpacity(int rgb, float opacity) {
        int alpha = Math.max(0, Math.min(255, Math.round(opacity * 255.0F)));
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }
}
