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
import java.util.Arrays;
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
            Method defaultClearMethod = clearHandlerInterface.getMethod("clearGrid", Class.forName("net.blay09.mods.craftingtweaks.api.CraftingGrid", false, classLoader), Player.class, AbstractContainerMenu.class, boolean.class);

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

    private static InvocationHandler providerInvocation(ClassLoader classLoader, Object clearHandler) throws ReflectiveOperationException {
        Class<?> builderInterface = Class.forName(BUILDER_INTERFACE, false, classLoader);
        Class<?> decoratorInterface = Class.forName(DECORATOR_INTERFACE, false, classLoader);
        Class<?> clearHandlerInterface = Class.forName(CLEAR_HANDLER_INTERFACE, false, classLoader);
        Class<?> buttonAlignmentClass = Class.forName(BUTTON_ALIGNMENT_CLASS, false, classLoader);
        Method addGrid = findAddGridMethod(builderInterface, decoratorInterface);
        Method setButtonAlignment = findOptionalMethod(decoratorInterface, "setButtonAlignment", buttonAlignmentClass);
        Method hideAllTweakButtons = findOptionalMethod(decoratorInterface, "hideAllTweakButtons");
        Method setClearHandler = findRequiredMethod(decoratorInterface, clearHandlerInterface, "clearHandler", "setClearHandler");
        Object leftAlignment = Enum.valueOf((Class<Enum>) buttonAlignmentClass, "LEFT");

        return (proxy, method, args) -> {
            String methodName = method.getName();
            return switch (methodName) {
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
                        decorator = invokeAddGrid(addGrid, builder, 1, 9);
                        if (setButtonAlignment != null) {
                            setButtonAlignment.invoke(decorator, leftAlignment);
                        }
                    } else if (menu instanceof InventoryMenu) {
                        decorator = invokeAddGrid(addGrid, builder, 1, 4);
                        if (hideAllTweakButtons != null) {
                            hideAllTweakButtons.invoke(decorator);
                        }
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
        };
    }

    private static Method findAddGridMethod(Class<?> builderInterface, Class<?> decoratorInterface) throws NoSuchMethodException {
        return Arrays.stream(builderInterface.getMethods())
                .filter(method -> method.getName().equals("addGrid"))
                .filter(method -> decoratorInterface.isAssignableFrom(method.getReturnType()))
                .filter(method -> {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    return parameterTypes.length == 2
                            && parameterTypes[0] == int.class
                            && parameterTypes[1] == int.class
                            || parameterTypes.length == 3
                            && parameterTypes[0] == String.class
                            && parameterTypes[1] == int.class
                            && parameterTypes[2] == int.class;
                })
                .findFirst()
                .orElseThrow(() -> new NoSuchMethodException("No compatible addGrid overload found on " + builderInterface.getName()));
    }

    private static Object invokeAddGrid(Method addGrid, Object builder, int width, int height) throws ReflectiveOperationException {
        return switch (addGrid.getParameterCount()) {
            case 2 -> addGrid.invoke(builder, width, height);
            case 3 -> addGrid.invoke(builder, "deepnullreforged", width, height);
            default -> throw new NoSuchMethodException("Unsupported addGrid overload: " + addGrid);
        };
    }

    private static Method findRequiredMethod(Class<?> type, Class<?> parameterType, String... names) throws NoSuchMethodException {
        Method method = findOptionalMethod(type, parameterType, names);
        if (method != null) {
            return method;
        }
        throw new NoSuchMethodException("No compatible method found on " + type.getName() + " for " + String.join(", ", names));
    }

    private static Method findOptionalMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        try {
            return type.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static Method findOptionalMethod(Class<?> type, Class<?> parameterType, String... names) {
        for (String name : names) {
            Method method = Arrays.stream(type.getMethods())
                    .filter(candidate -> candidate.getName().equals(name))
                    .filter(candidate -> candidate.getParameterCount() == 1)
                    .filter(candidate -> candidate.getParameterTypes()[0].isAssignableFrom(parameterType)
                            || parameterType.isAssignableFrom(candidate.getParameterTypes()[0]))
                    .findFirst()
                    .orElse(null);
            if (method != null) {
                return method;
            }
        }
        return null;
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
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
