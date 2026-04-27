package dev.deepdaddyttv.deepnullreforged.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.client.render.DeepNullDockRenderer;
import dev.deepdaddyttv.deepnullreforged.client.render.DeepNullItemRendering;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.item.DampNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlockEntities;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import dev.deepdaddyttv.deepnullreforged.registry.ModMenus;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

public final class ClientModEvents {
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(DeepNullReforged.id("keybindings"));
    public static final KeyMapping NEXT_ITEM = new KeyMapping("key.next_item.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static final KeyMapping PREVIOUS_ITEM = new KeyMapping("key.previous_item.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static final KeyMapping OPEN_DEEP_NULL = new KeyMapping("key.open_deepnull.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static final KeyMapping TOGGLE_TRANSFER_LOCK = new KeyMapping("key.toggle_transfer_lock.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static final KeyMapping TOGGLE_TRANSFER_DIRECTION = new KeyMapping("key.toggle_transfer_direction.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static final KeyMapping TOGGLE_SPONGE = new KeyMapping("key.toggle_sponge.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static final KeyMapping TOGGLE_HUD = new KeyMapping("key.toggle_hud.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static final KeyMapping TOGGLE_AUTO_PICKUP = new KeyMapping("key.toggle_auto_pickup.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static final KeyMapping TOGGLE_GLOBAL_AUTO_PICKUP = new KeyMapping("key.toggle_global_auto_pickup.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static final KeyMapping TOGGLE_AUTO_FEEDING = new KeyMapping("key.toggle_auto_feeding.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static final KeyMapping TOGGLE_AUTO_SMELTING = new KeyMapping("key.toggle_auto_smelting.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static final KeyMapping CYCLE_STONE_GENERATOR = new KeyMapping("key.cycle_stone_generator.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static final KeyMapping GUI_PRIMARY_ACTION = new KeyMapping("key.gui_primary_action.desc", InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_LEFT, KEY_CATEGORY);
    public static final KeyMapping GUI_SECONDARY_ACTION = new KeyMapping("key.gui_secondary_action.desc", InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_RIGHT, KEY_CATEGORY);
    public static final KeyMapping GUI_TERTIARY_ACTION = new KeyMapping("key.gui_tertiary_action.desc", InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_MIDDLE, KEY_CATEGORY);
    private static boolean initialized;

    private ClientModEvents() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        DeepNullItemRendering.initialize();

        MenuScreens.register(ModMenus.DEEP_NULL_MENU.get(), ClientModEvents::createDeepNullScreen);
        MenuScreens.register(ModMenus.NULL_WORKBENCH_MENU.get(), NullWorkbenchScreen::new);

        KeyMappingHelper.registerKeyMapping(NEXT_ITEM);
        KeyMappingHelper.registerKeyMapping(PREVIOUS_ITEM);
        KeyMappingHelper.registerKeyMapping(OPEN_DEEP_NULL);
        KeyMappingHelper.registerKeyMapping(TOGGLE_TRANSFER_LOCK);
        KeyMappingHelper.registerKeyMapping(TOGGLE_TRANSFER_DIRECTION);
        KeyMappingHelper.registerKeyMapping(TOGGLE_SPONGE);
        KeyMappingHelper.registerKeyMapping(TOGGLE_HUD);
        KeyMappingHelper.registerKeyMapping(TOGGLE_AUTO_PICKUP);
        KeyMappingHelper.registerKeyMapping(TOGGLE_GLOBAL_AUTO_PICKUP);
        KeyMappingHelper.registerKeyMapping(TOGGLE_AUTO_FEEDING);
        KeyMappingHelper.registerKeyMapping(TOGGLE_AUTO_SMELTING);
        KeyMappingHelper.registerKeyMapping(CYCLE_STONE_GENERATOR);
        KeyMappingHelper.registerKeyMapping(GUI_PRIMARY_ACTION);
        KeyMappingHelper.registerKeyMapping(GUI_SECONDARY_ACTION);
        KeyMappingHelper.registerKeyMapping(GUI_TERTIARY_ACTION);

        BlockEntityRendererRegistry.register(ModBlockEntities.DEEP_NULL_DOCK.get(), DeepNullDockRenderer::new);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
                    if (!(stack.getItem() instanceof DeepNullItem deepNullItem) || tintIndex < 0 || tintIndex > 1) {
                        return 0xFFFFFF;
                    }
                    DeepNullInventory.StyleRenderData style = DeepNullInventory.readStyleRenderData(
                            stack,
                            deepNullItem.tier(),
                            stack.getItem() instanceof DampNullItem
                    );
                    if (!style.hasCustomStyle()) {
                        return 0xFFFFFFFF;
                    }
                    int rgb = tintIndex == 0 ? style.frameColor() : style.glassColor();
                    return 0xFF000000 | (rgb & 0xFFFFFF);
                },
                ModItems.REDSTONE_DEEP_NULL.get(),
                ModItems.LAPIS_DEEP_NULL.get(),
                ModItems.IRON_DEEP_NULL.get(),
                ModItems.GOLD_DEEP_NULL.get(),
                ModItems.DIAMOND_DEEP_NULL.get(),
                ModItems.EMERALD_DEEP_NULL.get(),
                ModItems.CREATIVE_DEEP_NULL.get(),
                ModItems.REDSTONE_DAMP_NULL.get(),
                ModItems.LAPIS_DAMP_NULL.get(),
                ModItems.IRON_DAMP_NULL.get(),
                ModItems.GOLD_DAMP_NULL.get(),
                ModItems.DIAMOND_DAMP_NULL.get(),
                ModItems.EMERALD_DAMP_NULL.get(),
                ModItems.CREATIVE_DAMP_NULL.get()
        );
    }

    private static AbstractContainerScreen<DeepNullMenu> createDeepNullScreen(DeepNullMenu menu, Inventory inventory, Component title) {
        return switch (menu.getViewMode()) {
            case MAIN -> new DeepNullScreen(menu, inventory, title);
            case FLUID -> new DeepNullFluidScreen(menu, inventory, title);
            case UPGRADES -> new DeepNullUpgradeScreen(menu, inventory, title);
            case FILTER, AUTO_SMELT_FILTER -> new DeepNullFilterScreen(menu, inventory, title);
        };
    }

    public static boolean isPrimaryGuiButton(int button) {
        return GUI_PRIMARY_ACTION.matchesMouse(button);
    }

    public static boolean isSecondaryGuiButton(int button) {
        return GUI_SECONDARY_ACTION.matchesMouse(button);
    }

    public static boolean isTertiaryGuiButton(int button) {
        return GUI_TERTIARY_ACTION.matchesMouse(button);
    }
}
