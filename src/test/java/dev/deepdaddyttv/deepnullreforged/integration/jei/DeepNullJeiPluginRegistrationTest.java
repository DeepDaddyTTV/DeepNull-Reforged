package dev.deepdaddyttv.deepnullreforged.integration.jei;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class DeepNullJeiPluginRegistrationTest {
    @Test
    void pluginRegistersUniversalCraftingTransferHandlerForCarriedDeepNulls() {
        RecordingTransferRegistration registration = new RecordingTransferRegistration();

        new DeepNullJeiPlugin().registerRecipeTransferHandlers(registration);

        assertEquals(1, registration.universalHandlers.size(), "Carried DeepNull crafting should be exposed through a universal JEI transfer handler");
        IRecipeTransferHandler<?, ?> universalHandler = registration.universalHandlers.getFirst();
        DeepNullCraftingTransferHandler craftingHandler = assertInstanceOf(DeepNullCraftingTransferHandler.class, universalHandler);
        assertSame(AbstractContainerMenu.class, craftingHandler.getContainerClass(), "Universal carried crafting handler should match any crafting-capable menu");

        assertEquals(1, registration.specificHandlers.size(), "Only the Null Workbench should remain on a specific recipe-transfer registration");
        assertInstanceOf(NullWorkbenchRecipeTransferHandler.class, registration.specificHandlers.getFirst().handler());
    }

    private static final class RecordingTransferRegistration implements IRecipeTransferRegistration {
        private final IJeiHelpers jeiHelpers = proxy(IJeiHelpers.class);
        private final IRecipeTransferHandlerHelper transferHelper = proxy(IRecipeTransferHandlerHelper.class);
        private final List<IRecipeTransferHandler<?, ?>> universalHandlers = new ArrayList<>();
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
            universalHandlers.add(recipeTransferHandler);
        }

        @SuppressWarnings("unchecked")
        private static <T> T proxy(Class<T> type) {
            return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> null);
        }
    }

    private record SpecificRegistration(IRecipeTransferHandler<?, ?> handler, RecipeType<?> recipeType) {
    }
}
