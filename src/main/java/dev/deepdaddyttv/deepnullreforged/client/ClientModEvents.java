package dev.deepdaddyttv.deepnullreforged.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.registry.ModMenus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = DeepNullReforged.MODID, value = Dist.CLIENT)
public final class ClientModEvents {
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(DeepNullReforged.MODID, "general"));

    public static final KeyMapping NEXT_ITEM = new KeyMapping("key.next_item.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static final KeyMapping PREVIOUS_ITEM = new KeyMapping("key.previous_item.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static final KeyMapping OPEN_DEEP_NULL = new KeyMapping("key.open_deepnull.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static final KeyMapping TOGGLE_TRANSFER_LOCK = new KeyMapping("key.toggle_transfer_lock.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static final KeyMapping TOGGLE_HUD = new KeyMapping("key.toggle_hud.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static final KeyMapping TOGGLE_AUTO_PICKUP = new KeyMapping("key.toggle_auto_pickup.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static final KeyMapping TOGGLE_AUTO_FEEDING = new KeyMapping("key.toggle_auto_feeding.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static final KeyMapping TOGGLE_AUTO_SMELTING = new KeyMapping("key.toggle_auto_smelting.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static final KeyMapping CYCLE_STONE_GENERATOR = new KeyMapping("key.cycle_stone_generator.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);

    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.DEEP_NULL_MENU.get(), ClientModEvents::createDeepNullScreen);
        event.register(ModMenus.NULL_WORKBENCH_MENU.get(), NullWorkbenchScreen::new);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(NEXT_ITEM);
        event.register(PREVIOUS_ITEM);
        event.register(OPEN_DEEP_NULL);
        event.register(TOGGLE_TRANSFER_LOCK);
        event.register(TOGGLE_HUD);
        event.register(TOGGLE_AUTO_PICKUP);
        event.register(TOGGLE_AUTO_FEEDING);
        event.register(TOGGLE_AUTO_SMELTING);
        event.register(CYCLE_STONE_GENERATOR);
    }

    private static AbstractContainerScreen<DeepNullMenu> createDeepNullScreen(DeepNullMenu menu, Inventory inventory, Component title) {
        return switch (menu.getViewMode()) {
            case MAIN -> new DeepNullScreen(menu, inventory, title);
            case FLUID -> new DeepNullFluidScreen(menu, inventory, title);
            case UPGRADES -> new DeepNullUpgradeScreen(menu, inventory, title);
            case FILTER, AUTO_SMELT_FILTER -> new DeepNullFilterScreen(menu, inventory, title);
        };
    }
}
