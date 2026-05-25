package dev.deepdaddyttv.deepnullreforged.client.theme;

public enum DeepNullUiTheme {
    VANILLA("dn.ui_theme.vanilla"),
    MINECRAFT_DARK("dn.ui_theme.minecraft_dark"),
    LEGACY("dn.ui_theme.legacy");

    private final String translationKey;

    DeepNullUiTheme(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return translationKey;
    }
}
