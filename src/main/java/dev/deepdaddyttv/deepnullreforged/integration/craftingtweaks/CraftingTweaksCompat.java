package dev.deepdaddyttv.deepnullreforged.integration.craftingtweaks;

import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.integration.jei.DeepNullCraftingTransferSupport;
import dev.deepdaddyttv.deepnullreforged.integration.jei.ServerDeepNullJeiSession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public final class CraftingTweaksCompat {
    private static final String API_CLASS = "net.blay09.mods.craftingtweaks.api.CraftingTweaksAPI";
    private static final String PROVIDER_INTERFACE = "net.blay09.mods.craftingtweaks.api.CraftingGridProvider";
    private static final String CLEAR_HANDLER_INTERFACE = "net.blay09.mods.craftingtweaks.api.GridClearHandler";
    private static final String BUILDER_INTERFACE = "net.blay09.mods.craftingtweaks.api.CraftingGridBuilder";
    private static final String DECORATOR_INTERFACE = "net.blay09.mods.craftingtweaks.api.CraftingGridDecorator";
    private static final String BUTTON_ALIGNMENT_CLASS = "net.blay09.mods.craftingtweaks.api.ButtonAlignment";
    private static final String DEFAULT_CLEAR_HANDLER_CLASS = "net.blay09.mods.craftingtweaks.api.impl.DefaultGridClearHandler";
    private static final String VANILLA_PROVIDER_CLASS = "net.blay09.mods.craftingtweaks.compat.VanillaCraftingGridProvider";
    private static final String PROVIDER_MANAGER_CLASS = "net.blay09.mods.craftingtweaks.CraftingTweaksProviderManager";
    private static final String PROVIDER_LIST_FIELD = "craftingGridProviders";

    private static boolean installed;

    private CraftingTweaksCompat() {
    }

    public static void initialize() {
        if (installed) {
            return;
        }

        try {
            ClassLoader classLoader = CraftingTweaksCompat.class.getClassLoader();
            Class<?> providerInterface = Class.forName(PROVIDER_INTERFACE, false, classLoader);
            Class<?> clearHandlerInterface = Class.forName(CLEAR_HANDLER_INTERFACE, false, classLoader);
            unregisterVanillaProvider(classLoader);

            Object defaultClearHandler = Class.forName(DEFAULT_CLEAR_HANDLER_CLASS, false, classLoader)
                    .getConstructor()
                    .newInstance();
            Method defaultClearMethod = clearHandlerInterface.getMethod(
                    "clearGrid",
                    Class.forName("net.blay09.mods.craftingtweaks.api.CraftingGrid", false, classLoader),
                    Player.class,
                    AbstractContainerMenu.class,
                    boolean.class
            );

            Object clearHandler = Proxy.newProxyInstance(
                    classLoader,
                    new Class<?>[]{clearHandlerInterface},
                    clearHandlerInvocation(defaultClearHandler, defaultClearMethod)
            );
            Object provider = Proxy.newProxyInstance(
                    classLoader,
                    new Class<?>[]{providerInterface},
                    providerInvocation(classLoader, clearHandler)
            );

            Class<?> apiClass = Class.forName(API_CLASS, false, classLoader);
            apiClass.getMethod("registerCraftingGridProvider", providerInterface).invoke(null, provider);
            installed = true;
            DeepNullReforged.LOGGER.info("Registered Crafting Tweaks DeepNull clear-grid compatibility");
        } catch (ReflectiveOperationException exception) {
            DeepNullReforged.LOGGER.warn("Failed to register Crafting Tweaks compatibility", exception);
        }
    }

    public static boolean handleJeiAwareClear(AbstractContainerMenu menu, Player player) {
        if (!DeepNullConfig.enableCraftingTweaksReturnIntegration()) {
            return false;
        }
        if (!ServerDeepNullJeiSession.shouldReturn(player, menu)) {
            return false;
        }

        boolean handled = DeepNullCraftingTransferSupport.returnCurrentCraftingContents(menu, player);
        if (handled) {
            ServerDeepNullJeiSession.clear(player);
        }
        return handled;
    }

    private static InvocationHandler clearHandlerInvocation(Object defaultClearHandler, Method defaultClearMethod) {
        return (proxy, method, args) -> {
            String methodName = method.getName();
            if (methodName.equals("clearGrid")) {
                Object grid = args[0];
                Player player = (Player) args[1];
                AbstractContainerMenu menu = (AbstractContainerMenu) args[2];
                boolean drop = (Boolean) args[3];
                boolean handled = handleJeiAwareClear(menu, player);
                if (handled && hasCraftingContents(menu)) {
                    defaultClearMethod.invoke(defaultClearHandler, grid, player, menu, drop);
                } else if (!handled) {
                    defaultClearMethod.invoke(defaultClearHandler, grid, player, menu, drop);
                }
                return null;
            }
            return handleProxyObjectMethod(proxy, method, args, "DeepNullCraftingTweaksClearHandler");
        };
    }

    @SuppressWarnings("unchecked")
    private static InvocationHandler providerInvocation(ClassLoader classLoader, Object clearHandler) throws ReflectiveOperationException {
        Class<?> builderInterface = Class.forName(BUILDER_INTERFACE, false, classLoader);
        Class<?> decoratorInterface = Class.forName(DECORATOR_INTERFACE, false, classLoader);
        Method addGrid = builderInterface.getMethod("addGrid", int.class, int.class);
        Method setButtonAlignment = decoratorInterface.getMethod("setButtonAlignment", Class.forName(BUTTON_ALIGNMENT_CLASS, false, classLoader));
        Method hideAllTweakButtons = decoratorInterface.getMethod("hideAllTweakButtons");
        Method setClearHandler = decoratorInterface.getMethod("clearHandler", Class.forName(CLEAR_HANDLER_INTERFACE, false, classLoader));
        Object leftAlignment = Enum.valueOf((Class<Enum>) Class.forName(BUTTON_ALIGNMENT_CLASS, false, classLoader), "LEFT");

        return (proxy, method, args) -> switch (method.getName()) {
            case "getModId" -> "minecraft";
            case "handles" -> {
                AbstractContainerMenu menu = (AbstractContainerMenu) args[0];
                yield menu instanceof CraftingMenu || menu instanceof InventoryMenu;
            }
            case "buildCraftingGrids" -> {
                Object builder = args[0];
                AbstractContainerMenu menu = (AbstractContainerMenu) args[1];
                Object decorator;
                if (menu instanceof CraftingMenu) {
                    decorator = addGrid.invoke(builder, 1, 9);
                    setButtonAlignment.invoke(decorator, leftAlignment);
                } else if (menu instanceof InventoryMenu) {
                    decorator = addGrid.invoke(builder, 1, 4);
                    hideAllTweakButtons.invoke(decorator);
                } else {
                    yield null;
                }
                setClearHandler.invoke(decorator, clearHandler);
                yield null;
            }
            case "onInitialize" -> null;
            case "requiresServerSide" -> false;
            default -> handleProxyObjectMethod(proxy, method, args, "DeepNullCraftingTweaksProvider");
        };
    }

    private static Object handleProxyObjectMethod(Object proxy, Method method, Object[] args, String name) {
        return switch (method.getName()) {
            case "toString" -> name;
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> null;
        };
    }

    @SuppressWarnings("unchecked")
    private static void unregisterVanillaProvider(ClassLoader classLoader) throws ReflectiveOperationException {
        Class<?> providerManagerClass = Class.forName(PROVIDER_MANAGER_CLASS, false, classLoader);
        Field providersField = providerManagerClass.getDeclaredField(PROVIDER_LIST_FIELD);
        providersField.setAccessible(true);
        List<Object> providers = (List<Object>) providersField.get(null);
        providers.removeIf(provider -> {
            String className = provider.getClass().getName();
            return className.equals(VANILLA_PROVIDER_CLASS) || className.equals("$Proxy") || className.contains("DeepNullCraftingTweaksProvider");
        });
    }

    private static boolean hasCraftingContents(AbstractContainerMenu menu) {
        DeepNullCraftingTransferSupport.CraftingContext context = DeepNullCraftingTransferSupport.resolveContext(menu);
        if (context == null) {
            return false;
        }

        for (int slotIndex : context.craftSlotIndices()) {
            Slot slot = menu.getSlot(slotIndex);
            if (!slot.getItem().isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
