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
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = DeepNullReforged.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    public static final KeyMapping NEXT_ITEM = new KeyMapping("key.next_item.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "key.categories." + DeepNullReforged.MODID);
    public static final KeyMapping PREVIOUS_ITEM = new KeyMapping("key.previous_item.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "key.categories." + DeepNullReforged.MODID);
    public static final KeyMapping OPEN_DEEP_NULL = new KeyMapping("key.open_deepnull.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "key.categories." + DeepNullReforged.MODID);
    public static final KeyMapping TOGGLE_TRANSFER_LOCK = new KeyMapping("key.toggle_transfer_lock.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "key.categories." + DeepNullReforged.MODID);
    public static final KeyMapping TOGGLE_HUD = new KeyMapping("key.toggle_hud.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "key.categories." + DeepNullReforged.MODID);
    public static final KeyMapping TOGGLE_AUTO_PICKUP = new KeyMapping("key.toggle_auto_pickup.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "key.categories." + DeepNullReforged.MODID);
    public static final KeyMapping TOGGLE_AUTO_FEEDING = new KeyMapping("key.toggle_auto_feeding.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "key.categories." + DeepNullReforged.MODID);
    public static final KeyMapping TOGGLE_AUTO_SMELTING = new KeyMapping("key.toggle_auto_smelting.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "key.categories." + DeepNullReforged.MODID);
    public static final KeyMapping CYCLE_STONE_GENERATOR = new KeyMapping("key.cycle_stone_generator.desc", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "key.categories." + DeepNullReforged.MODID);

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

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
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
                ModItems.CREATIVE_DAMP_NULL.get());
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
