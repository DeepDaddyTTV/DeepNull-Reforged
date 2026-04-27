package dev.deepdaddyttv.deepnullreforged.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.client.render.DeepNullDockRenderer;
import dev.deepdaddyttv.deepnullreforged.client.render.DeepNullSelectedItemModel;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.item.DampNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlockEntities;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import dev.deepdaddyttv.deepnullreforged.registry.ModMenus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;

public final class ClientModEvents {
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(DeepNullReforged.MODID, "general"));
    private static final @SuppressWarnings("rawtypes") Class ITEM_COLOR_EVENT_CLASS = loadItemColorEventClass();
    private static final Method ITEM_COLOR_REGISTER_METHOD = findItemColorRegisterMethod();

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

    private ClientModEvents() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ClientModEvents::registerScreens);
        modEventBus.addListener(ClientModEvents::registerKeyMappings);
        modEventBus.addListener(ClientModEvents::registerItemModels);
        modEventBus.addListener(ClientModEvents::registerRenderers);
        registerItemColorListener(modEventBus);
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.DEEP_NULL_MENU.get(), ClientModEvents::createDeepNullScreen);
        event.register(ModMenus.NULL_WORKBENCH_MENU.get(), NullWorkbenchScreen::new);
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(NEXT_ITEM);
        event.register(PREVIOUS_ITEM);
        event.register(OPEN_DEEP_NULL);
        event.register(TOGGLE_TRANSFER_LOCK);
        event.register(TOGGLE_TRANSFER_DIRECTION);
        event.register(TOGGLE_SPONGE);
        event.register(TOGGLE_HUD);
        event.register(TOGGLE_AUTO_PICKUP);
        event.register(TOGGLE_GLOBAL_AUTO_PICKUP);
        event.register(TOGGLE_AUTO_FEEDING);
        event.register(TOGGLE_AUTO_SMELTING);
        event.register(CYCLE_STONE_GENERATOR);
        event.register(GUI_PRIMARY_ACTION);
        event.register(GUI_SECONDARY_ACTION);
        event.register(GUI_TERTIARY_ACTION);
    }

    public static boolean isPrimaryGuiButton(int button) {
        return GUI_PRIMARY_ACTION.getKey().getType() == InputConstants.Type.MOUSE
                && GUI_PRIMARY_ACTION.getKey().getValue() == button;
    }

    public static boolean isSecondaryGuiButton(int button) {
        return GUI_SECONDARY_ACTION.getKey().getType() == InputConstants.Type.MOUSE
                && GUI_SECONDARY_ACTION.getKey().getValue() == button;
    }

    public static boolean isTertiaryGuiButton(int button) {
        return GUI_TERTIARY_ACTION.getKey().getType() == InputConstants.Type.MOUSE
                && GUI_TERTIARY_ACTION.getKey().getValue() == button;
    }

    public static void registerItemModels(RegisterItemModelsEvent event) {
        event.register(DeepNullReforged.id("selected_item"), DeepNullSelectedItemModel.Unbaked.MAP_CODEC);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.DEEP_NULL_DOCK.get(), DeepNullDockRenderer::new);
    }

    private static AbstractContainerScreen<DeepNullMenu> createDeepNullScreen(DeepNullMenu menu, Inventory inventory, Component title) {
        return switch (menu.getViewMode()) {
            case MAIN -> new DeepNullScreen(menu, inventory, title);
            case FLUID -> new DeepNullFluidScreen(menu, inventory, title);
            case UPGRADES -> new DeepNullUpgradeScreen(menu, inventory, title);
            case FILTER, AUTO_SMELT_FILTER -> new DeepNullFilterScreen(menu, inventory, title);
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerItemColorListener(IEventBus modEventBus) {
        if (ITEM_COLOR_EVENT_CLASS != null) {
            modEventBus.addListener(ITEM_COLOR_EVENT_CLASS, (Consumer) ClientModEvents::registerItemColorsReflective);
        }
    }

    private static void registerItemColorsReflective(Object event) {
        if (ITEM_COLOR_REGISTER_METHOD == null) {
            return;
        }
        try {
            ITEM_COLOR_REGISTER_METHOD.invoke(event, createItemColorProxy(), deepNullColorItems());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to register DeepNull item colors", exception);
        }
    }

    private static int deepNullTint(ItemStack stack, int tintIndex) {
        if (!(stack.getItem() instanceof DeepNullItem deepNullItem) || tintIndex < 0 || tintIndex > 1) {
            return 0xFFFFFF;
        }
        DeepNullInventory.StyleRenderData style = DeepNullInventory.readStyleRenderData(
                stack,
                deepNullItem.tier(),
                stack.getItem() instanceof DampNullItem
        );
        if (!style.hasColorOverrides()) {
            return 0xFFFFFFFF;
        }
        int rgb = tintIndex == 0 ? style.frameColor() : style.glassColor();
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    private static ItemLike[] deepNullColorItems() {
        return new ItemLike[]{
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
        };
    }

    @SuppressWarnings("rawtypes")
    private static Class loadItemColorEventClass() {
        try {
            return Class.forName("net.neoforged.neoforge.client.event.RegisterColorHandlersEvent$Item");
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Method findItemColorRegisterMethod() {
        if (ITEM_COLOR_EVENT_CLASS == null) {
            return null;
        }
        for (Method method : ITEM_COLOR_EVENT_CLASS.getMethods()) {
            if ("register".equals(method.getName()) && method.getParameterCount() == 2) {
                return method;
            }
        }
        return null;
    }

    private static Object createItemColorProxy() {
        Class<?> itemColorType = ITEM_COLOR_REGISTER_METHOD.getParameterTypes()[0];
        return Proxy.newProxyInstance(itemColorType.getClassLoader(), new Class<?>[]{itemColorType}, (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "DeepNullItemColorProxy";
                    default -> null;
                };
            }
            return deepNullTint((ItemStack) args[0], (int) args[1]);
        });
    }
}
