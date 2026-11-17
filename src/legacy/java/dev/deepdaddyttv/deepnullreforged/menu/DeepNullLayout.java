package dev.deepdaddyttv.deepnullreforged.menu;

/** Coordinates of the slot interiors in the shipped 256x256 GUI atlases. */
public final class DeepNullLayout {
    public static final int WIDTH = 202;
    private DeepNullLayout() { }
    public static int x(int column) { return 9 + column * 21; }
    public static int storageY(int row) { return 19 + row * 21; }
    public static int playerY(int rows, int row) { return 28 + rows * 21 + row * 21; }
    public static int hotbarY(int rows) { return 95 + rows * 21; }
    public static int height(int rows) { return 120 + rows * 21; }
}
