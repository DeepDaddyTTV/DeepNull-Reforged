package dev.deepdaddyttv.deepnullreforged.integration.craftingtweaks;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CraftingTweaksCompatTest {
    @Test
    void addGridFallbackSupportsTwoArgumentOverloads() throws Exception {
        Method findAddGridMethod = CraftingTweaksCompat.class.getDeclaredMethod("findAddGridMethod", Class.class, Class.class);
        findAddGridMethod.setAccessible(true);
        Method invokeAddGrid = CraftingTweaksCompat.class.getDeclaredMethod("invokeAddGrid", Method.class, Object.class, int.class, int.class);
        invokeAddGrid.setAccessible(true);

        Method addGrid = (Method) findAddGridMethod.invoke(null, TwoArgBuilder.class, FakeDecorator.class);
        TwoArgBuilderImpl builder = new TwoArgBuilderImpl();
        Object decorator = invokeAddGrid.invoke(null, addGrid, builder, 1, 9);

        assertInstanceOf(FakeDecoratorImpl.class, decorator);
        assertEquals(1, builder.width);
        assertEquals(9, builder.height);
    }

    @Test
    void addGridFallbackSupportsNamedThreeArgumentOverloads() throws Exception {
        Method findAddGridMethod = CraftingTweaksCompat.class.getDeclaredMethod("findAddGridMethod", Class.class, Class.class);
        findAddGridMethod.setAccessible(true);
        Method invokeAddGrid = CraftingTweaksCompat.class.getDeclaredMethod("invokeAddGrid", Method.class, Object.class, int.class, int.class);
        invokeAddGrid.setAccessible(true);

        Method addGrid = (Method) findAddGridMethod.invoke(null, ThreeArgBuilder.class, FakeDecorator.class);
        ThreeArgBuilderImpl builder = new ThreeArgBuilderImpl();
        Object decorator = invokeAddGrid.invoke(null, addGrid, builder, 1, 4);

        assertInstanceOf(FakeDecoratorImpl.class, decorator);
        assertEquals("deepnullreforged", builder.gridId);
        assertEquals(1, builder.width);
        assertEquals(4, builder.height);
    }

    private interface FakeDecorator {
    }

    private static final class FakeDecoratorImpl implements FakeDecorator {
    }

    private interface TwoArgBuilder {
        FakeDecorator addGrid(int width, int height);
    }

    private static final class TwoArgBuilderImpl implements TwoArgBuilder {
        private int width;
        private int height;

        @Override
        public FakeDecorator addGrid(int width, int height) {
            this.width = width;
            this.height = height;
            return new FakeDecoratorImpl();
        }
    }

    private interface ThreeArgBuilder {
        FakeDecorator addGrid(String gridId, int width, int height);
    }

    private static final class ThreeArgBuilderImpl implements ThreeArgBuilder {
        private String gridId;
        private int width;
        private int height;

        @Override
        public FakeDecorator addGrid(String gridId, int width, int height) {
            this.gridId = gridId;
            this.width = width;
            this.height = height;
            return new FakeDecoratorImpl();
        }
    }
}
