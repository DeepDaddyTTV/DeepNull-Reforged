package dev.deepdaddyttv.deepnullreforged.integration.jei;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import dev.deepdaddyttv.deepnullreforged.testutil.MinecraftBootstrap;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeepNullJeiPluginRegistrationTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftBootstrap.ensureBootstrapped();
    }

    @Test
    void pluginRegistersExactMenuHandlersForCarriedDeepNulls() {
        RecordingTransferRegistration registration = new RecordingTransferRegistration();

        new DeepNullJeiPlugin().registerRecipeTransferHandlers(registration);

        List<DeepNullCraftingTransferHandler> handlers = registration.specificHandlers.stream()
                .map(SpecificRegistration::handler)
                .filter(DeepNullCraftingTransferHandler.class::isInstance)
                .map(DeepNullCraftingTransferHandler.class::cast)
                .toList();

        assertEquals(2, handlers.size(), "JEI should register exactly two carried DeepNull crafting handlers");

        DeepNullCraftingTransferHandler inventoryHandler = handlers.stream()
                .filter(handler -> handler.getContainerClass() == InventoryMenu.class)
                .findFirst()
                .orElse(null);
        assertTrue(inventoryHandler != null, "JEI should register a carried DeepNull handler for the player 2x2 inventory menu");
        assertTrue(inventoryHandler != null && inventoryHandler.getMenuType().isEmpty(), "InventoryMenu JEI handler should not require a menu type");

        DeepNullCraftingTransferHandler craftingHandler = handlers.stream()
                .filter(handler -> handler.getContainerClass() == CraftingMenu.class)
                .findFirst()
                .orElse(null);
        assertTrue(craftingHandler != null, "JEI should register a carried DeepNull handler for the crafting table menu");
        assertTrue(craftingHandler != null && craftingHandler.getMenuType().equals(Optional.of(MenuType.CRAFTING)), "CraftingMenu JEI handler should require MenuType.CRAFTING");

        assertEquals(3, registration.specificHandlers.size(), "The two carried DeepNull handlers plus the Null Workbench handler should be registered");
        assertInstanceOf(NullWorkbenchRecipeTransferHandler.class, registration.specificHandlers.getLast().handler());
    }

    @Test
    void helperBuildsTheSameExactMenuHandlers() throws ReflectiveOperationException {
        Method method = DeepNullJeiPlugin.class.getDeclaredMethod("createCraftingTransferHandlers", IRecipeTransferHandlerHelper.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<DeepNullCraftingTransferHandler> handlers = (List<DeepNullCraftingTransferHandler>) method.invoke(null, proxy(IRecipeTransferHandlerHelper.class));

        assertEquals(2, handlers.size());
        assertTrue(handlers.stream().anyMatch(handler -> handler.getContainerClass() == InventoryMenu.class && handler.getMenuType().isEmpty()));
        assertTrue(handlers.stream().anyMatch(handler -> handler.getContainerClass() == CraftingMenu.class && handler.getMenuType().equals(Optional.of(MenuType.CRAFTING))));
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> null);
    }

    private static final class RecordingTransferRegistration implements IRecipeTransferRegistration {
        private final IJeiHelpers jeiHelpers = proxy(IJeiHelpers.class);
        private final IRecipeTransferHandlerHelper transferHelper = proxy(IRecipeTransferHandlerHelper.class);
        private final List<SpecificRegistration> specificHandlers = new ArrayList<>();

        @Override
        public IJeiHelpers getJeiHelpers() {
            return jeiHelpers;
        }

        @Override
        public IRecipeTransferHandlerHelper getTransferHelper() {
            return transferHelper;
        }

        @Override
        public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(Class<? extends C> containerClass, MenuType<C> menuType, RecipeType<R> recipeType, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount) {
        }

        @Override
        public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(IRecipeTransferInfo<C, R> recipeTransferInfo) {
        }

        @Override
        public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(IRecipeTransferHandler<C, R> recipeTransferHandler, RecipeType<R> recipeType) {
            specificHandlers.add(new SpecificRegistration(recipeTransferHandler, recipeType));
        }

        @Override
        public <C extends AbstractContainerMenu> void addUniversalRecipeTransferHandler(IUniversalRecipeTransferHandler<C> recipeTransferHandler) {
        }

        @Override
        public <C extends AbstractContainerMenu, R> void addUniversalRecipeTransferHandler(IRecipeTransferHandler<C, R> recipeTransferHandler) {
        }
    }

    private record SpecificRegistration(IRecipeTransferHandler<?, ?> handler, RecipeType<?> recipeType) {
    }
}
