package dev.deepdaddyttv.deepnullreforged.registry;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.menu.NullWorkbenchMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import dev.deepdaddyttv.deepnullreforged.compat.registries.DeferredHolder;
import dev.deepdaddyttv.deepnullreforged.compat.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, DeepNullReforged.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<DeepNullMenu>> DEEP_NULL_MENU = MENUS.register(
            "deep_null",
            () -> new ExtendedMenuType<>(DeepNullMenu::new, DeepNullMenu.OpenData.STREAM_CODEC)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<NullWorkbenchMenu>> NULL_WORKBENCH_MENU = MENUS.register(
            "null_workbench",
            () -> new ExtendedMenuType<>(NullWorkbenchMenu::new, BlockPos.STREAM_CODEC)
    );

    private ModMenus() {
    }

    public static void register() {
        MENUS.register();
    }
}
