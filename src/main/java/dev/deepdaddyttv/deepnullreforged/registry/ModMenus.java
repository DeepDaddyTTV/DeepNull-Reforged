package dev.deepdaddyttv.deepnullreforged.registry;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, DeepNullReforged.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<DeepNullMenu>> DEEP_NULL_MENU = MENUS.register(
            "deep_null",
            () -> IMenuTypeExtension.create(DeepNullMenu::new)
    );

    private ModMenus() {
    }
}
