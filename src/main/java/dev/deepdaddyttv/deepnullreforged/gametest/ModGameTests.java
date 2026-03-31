package dev.deepdaddyttv.deepnullreforged.gametest;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public final class ModGameTests {
    private static final Identifier EMPTY_STRUCTURE = DeepNullReforged.id("empty");
    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, DeepNullReforged.MODID);
    private static final List<RegisteredGameTest> REGISTERED_TESTS = new ArrayList<>();

    static {
        registerSuite("deepnull", DeepNullRegressionGameTests.class);
        registerSuite("workbench", NullWorkbenchRegressionGameTests.class);
        registerSuite("crafting_transfer", CraftingTransferRegressionGameTests.class);
        registerSuite("ae2", Ae2RegressionGameTests.class);
        registerSuite("mekanism", MekanismRegressionGameTests.class);
    }

    private ModGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        TEST_FUNCTIONS.register(modEventBus);
        modEventBus.addListener(ModGameTests::registerGameTests);
    }

    private static void registerSuite(String suiteName, Class<?> testClass) {
        List<Method> testMethods = java.util.Arrays.stream(testClass.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .filter(method -> method.getReturnType() == Void.TYPE)
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getParameterTypes()[0] == GameTestHelper.class)
                .sorted(Comparator.comparing(Method::getName))
                .toList();
        if (testMethods.isEmpty()) {
            throw new IllegalStateException("No GameTest methods found in " + testClass.getName());
        }

        for (Method method : testMethods) {
            String path = suiteName + "/" + method.getName();
            Identifier id = DeepNullReforged.id(path);
            TEST_FUNCTIONS.register(path, () -> helper -> invoke(method, helper));
            REGISTERED_TESTS.add(new RegisteredGameTest(id, ResourceKey.create(Registries.TEST_FUNCTION, id)));
        }
    }

    private static void registerGameTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                DeepNullReforged.id("default_gametest_env"),
                new TestEnvironmentDefinition.AllOf()
        );

        for (RegisteredGameTest test : REGISTERED_TESTS) {
            TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
                    environment,
                    EMPTY_STRUCTURE,
                    400,
                    0,
                    true,
                    Rotation.NONE,
                    false,
                    1,
                    1,
                    false,
                    0
            );
            event.registerTest(test.id(), testData -> new FunctionGameTestInstance(test.functionKey(), testData), data);
        }
    }

    private static void invoke(Method method, GameTestHelper helper) {
        try {
            method.invoke(null, helper);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new RuntimeException("Failed to run GameTest " + method.getDeclaringClass().getSimpleName() + "#" + method.getName(), cause);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException("Failed to invoke GameTest " + method.getDeclaringClass().getSimpleName() + "#" + method.getName(), exception);
        }
    }

    private record RegisteredGameTest(Identifier id, ResourceKey<Consumer<GameTestHelper>> functionKey) {
    }
}
