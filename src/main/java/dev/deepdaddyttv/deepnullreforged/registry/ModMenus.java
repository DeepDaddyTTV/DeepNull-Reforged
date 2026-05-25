package dev.deepdaddyttv.deepnullreforged.registry;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.menu.DenNullMenu;
import dev.deepdaddyttv.deepnullreforged.menu.DripNullMenu;
import dev.deepdaddyttv.deepnullreforged.menu.DumpNullMenu;
import dev.deepdaddyttv.deepnullreforged.menu.HubNullMenu;
import dev.deepdaddyttv.deepnullreforged.menu.NullWorkbenchMenu;
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

    public static final DeferredHolder<MenuType<?>, MenuType<DumpNullMenu>> DUMP_NULL_MENU = MENUS.register(
            "dump_null",
            () -> IMenuTypeExtension.create(DumpNullMenu::new)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<DenNullMenu>> DEN_NULL_MENU = MENUS.register(
            "den_null",
            () -> IMenuTypeExtension.create(DenNullMenu::new)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<DripNullMenu>> DRIP_NULL_MENU = MENUS.register(
            "drip_null",
            () -> IMenuTypeExtension.create(DripNullMenu::new)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<HubNullMenu>> HUB_NULL_MENU = MENUS.register(
            "hub_null",
            () -> IMenuTypeExtension.create(HubNullMenu::new)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<NullWorkbenchMenu>> NULL_WORKBENCH_MENU = MENUS.register(
            "null_workbench",
            () -> IMenuTypeExtension.create((containerId, inventory, buf) -> new NullWorkbenchMenu(containerId, inventory, buf.readBlockPos()))
    );

    private ModMenus() {
    }
}
