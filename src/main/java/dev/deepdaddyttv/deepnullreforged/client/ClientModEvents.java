package dev.deepdaddyttv.deepnullreforged.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.client.render.DeepNullDockRenderer;
import dev.deepdaddyttv.deepnullreforged.client.render.DeepNullItemRendering;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlockEntities;
import dev.deepdaddyttv.deepnullreforged.registry.ModMenus;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = DeepNullReforged.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    public static final KeyMapping NEXT_ITEM = new KeyMapping("key.next_item.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "key.categories." + DeepNullReforged.MODID);
    public static final KeyMapping PREVIOUS_ITEM = new KeyMapping("key.previous_item.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "key.categories." + DeepNullReforged.MODID);
    public static final KeyMapping OPEN_DEEP_NULL = new KeyMapping("key.open_deepnull.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "key.categories." + DeepNullReforged.MODID);
    public static final KeyMapping TOGGLE_TRANSFER_LOCK = new KeyMapping("key.toggle_transfer_lock.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "key.categories." + DeepNullReforged.MODID);

    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.DEEP_NULL_MENU.get(), ClientModEvents::createDeepNullScreen);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(NEXT_ITEM);
        event.register(PREVIOUS_ITEM);
        event.register(OPEN_DEEP_NULL);
        event.register(TOGGLE_TRANSFER_LOCK);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        DeepNullItemRendering.registerClientExtensions(event);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        DeepNullItemRendering.registerAdditionalModels(event);
    }

    @SubscribeEvent
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
}
