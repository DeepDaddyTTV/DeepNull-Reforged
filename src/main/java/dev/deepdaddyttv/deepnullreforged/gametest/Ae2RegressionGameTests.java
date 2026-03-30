package dev.deepdaddyttv.deepnullreforged.gametest;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static net.minecraft.world.item.Items.COBBLESTONE;
import static net.minecraft.world.item.Items.DIRT;
import static net.minecraft.world.item.Items.STICK;

@GameTestHolder(DeepNullReforged.MODID)
@PrefixGameTestTemplate(false)
public final class Ae2RegressionGameTests {
    private Ae2RegressionGameTests() {
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void ae2_item_transfer_pushes_and_refills_only_matching_types(GameTestHelper helper) throws ReflectiveOperationException {
        if (!classPresent("appeng.api.storage.MEStorage")) {
            helper.succeed();
            return;
        }

        Class<?> ae2TransferClass = Class.forName("dev.deepdaddyttv.deepnullreforged.integration.ae2.Ae2TransferCompat");
        Class<?> actionableClass = Class.forName("appeng.api.config.Actionable");
        Class<?> actionSourceClass = Class.forName("appeng.api.networking.security.IActionSource");
        Class<?> meStorageClass = Class.forName("appeng.api.storage.MEStorage");
        Class<?> aeItemKeyClass = Class.forName("appeng.api.stacks.AEItemKey");

        Object actionableModulate = Enum.valueOf((Class<Enum>) actionableClass.asSubclass(Enum.class), "MODULATE");
        Object actionSource = actionSourceClass.getMethod("empty").invoke(null);
        Method aeItemKeyOf = aeItemKeyClass.getMethod("of", ItemStack.class);

        DeepNullInventory pushInventory = DeepNullGameTestSupport.deepNullInventory(helper, DeepNullTier.REDSTONE);
        pushInventory.setStackInSlot(0, new ItemStack(COBBLESTONE, 8));
        pushInventory.setStackInSlot(1, new ItemStack(DIRT, 5));
        pushInventory.setCustomExtractionMinimum(0, 0);
        pushInventory.setCustomExtractionMinimum(1, 0);

        ReflectiveMeStorage pushStorage = new ReflectiveMeStorage(meStorageClass, actionableClass, actionSourceClass);
        Method moveItemsTo = ae2TransferClass.getMethod("moveItemsToStorage", DeepNullInventory.class, meStorageClass, actionSourceClass);
        helper.assertTrue((Boolean) moveItemsTo.invoke(null, pushInventory, pushStorage.proxy(), actionSource), "AE2 item push should move stored items into ME storage");
        helper.assertValueEqual(pushStorage.amount(aeItemKeyOf.invoke(null, new ItemStack(COBBLESTONE))), 8L, "AE2 storage should receive cobblestone");
        helper.assertValueEqual(pushStorage.amount(aeItemKeyOf.invoke(null, new ItemStack(DIRT))), 5L, "AE2 storage should receive dirt");

        DeepNullInventory refillInventory = DeepNullGameTestSupport.deepNullInventory(helper, DeepNullTier.REDSTONE);
        refillInventory.setStackInSlot(0, new ItemStack(COBBLESTONE, 1));

        ReflectiveMeStorage refillStorage = new ReflectiveMeStorage(meStorageClass, actionableClass, actionSourceClass);
        refillStorage.put(aeItemKeyOf.invoke(null, new ItemStack(COBBLESTONE)), 6L);
        refillStorage.put(aeItemKeyOf.invoke(null, new ItemStack(STICK)), 9L);

        Method moveItemsFrom = ae2TransferClass.getMethod("moveItemsFromStorage", DeepNullInventory.class, meStorageClass, actionSourceClass);
        helper.assertTrue((Boolean) moveItemsFrom.invoke(null, refillInventory, refillStorage.proxy(), actionSource), "AE2 refill should pull matching stored items");
        helper.assertValueEqual(refillInventory.getStackInSlot(0).getCount(), 7, "Matching stored items should be refilled from AE2");
        helper.assertTrue(refillInventory.getStackInSlot(1).isEmpty(), "Non-matching AE2 items should not create new DeepNull stacks");
        helper.assertValueEqual(refillStorage.amount(aeItemKeyOf.invoke(null, new ItemStack(STICK))), 9L, "Non-matching AE2 items should remain in storage");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void ae2_fluid_transfer_pushes_and_refills_only_matching_types(GameTestHelper helper) throws ReflectiveOperationException {
        if (!classPresent("appeng.api.storage.MEStorage")) {
            helper.succeed();
            return;
        }

        Class<?> ae2TransferClass = Class.forName("dev.deepdaddyttv.deepnullreforged.integration.ae2.Ae2TransferCompat");
        Class<?> actionableClass = Class.forName("appeng.api.config.Actionable");
        Class<?> actionSourceClass = Class.forName("appeng.api.networking.security.IActionSource");
        Class<?> meStorageClass = Class.forName("appeng.api.storage.MEStorage");
        Class<?> aeFluidKeyClass = Class.forName("appeng.api.stacks.AEFluidKey");

        Object actionSource = actionSourceClass.getMethod("empty").invoke(null);
        Method aeFluidKeyOf = aeFluidKeyClass.getMethod("of", FluidStack.class);

        DeepNullInventory pushInventory = DeepNullGameTestSupport.dampNullInventory(helper, DeepNullTier.REDSTONE);
        pushInventory.fillFluid(new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME * 2), false);
        pushInventory.fillFluid(new FluidStack(Fluids.LAVA, FluidType.BUCKET_VOLUME), false);

        ReflectiveMeStorage pushStorage = new ReflectiveMeStorage(meStorageClass, actionableClass, actionSourceClass);
        Method moveFluidsTo = ae2TransferClass.getMethod("moveFluidsToStorage", DeepNullInventory.class, meStorageClass, actionSourceClass);
        helper.assertTrue((Boolean) moveFluidsTo.invoke(null, pushInventory, pushStorage.proxy(), actionSource), "AE2 fluid push should move stored fluids into ME storage");
        helper.assertValueEqual(pushStorage.amount(aeFluidKeyOf.invoke(null, new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME))), (long) FluidType.BUCKET_VOLUME * 2, "AE2 storage should receive water");
        helper.assertValueEqual(pushStorage.amount(aeFluidKeyOf.invoke(null, new FluidStack(Fluids.LAVA, FluidType.BUCKET_VOLUME))), (long) FluidType.BUCKET_VOLUME, "AE2 storage should receive lava");

        DeepNullInventory refillInventory = DeepNullGameTestSupport.dampNullInventory(helper, DeepNullTier.REDSTONE);
        refillInventory.fillFluid(new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME), false);

        ReflectiveMeStorage refillStorage = new ReflectiveMeStorage(meStorageClass, actionableClass, actionSourceClass);
        refillStorage.put(aeFluidKeyOf.invoke(null, new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME)), FluidType.BUCKET_VOLUME * 3L);
        refillStorage.put(aeFluidKeyOf.invoke(null, new FluidStack(Fluids.LAVA, FluidType.BUCKET_VOLUME)), FluidType.BUCKET_VOLUME * 4L);

        Method moveFluidsFrom = ae2TransferClass.getMethod("moveFluidsFromStorage", DeepNullInventory.class, meStorageClass, actionSourceClass);
        helper.assertTrue((Boolean) moveFluidsFrom.invoke(null, refillInventory, refillStorage.proxy(), actionSource), "AE2 refill should pull matching stored fluids");
        helper.assertValueEqual(refillInventory.getFluidInSlot(0).getAmount(), FluidType.BUCKET_VOLUME * 4, "Matching stored fluids should be refilled from AE2");
        helper.assertValueEqual(refillStorage.amount(aeFluidKeyOf.invoke(null, new FluidStack(Fluids.LAVA, FluidType.BUCKET_VOLUME))), FluidType.BUCKET_VOLUME * 4L, "Non-matching AE2 fluids should remain in storage");
        helper.succeed();
    }

    private static boolean classPresent(String className) {
        try {
            Class.forName(className, false, Ae2RegressionGameTests.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError exception) {
            return false;
        }
    }

    private static final class ReflectiveMeStorage implements InvocationHandler {
        private final Map<Object, Long> contents = new HashMap<>();
        private final Object actionableModulate;
        private final Object proxy;

        private ReflectiveMeStorage(Class<?> meStorageClass, Class<?> actionableClass, Class<?> actionSourceClass) {
            this.actionableModulate = Enum.valueOf((Class<Enum>) actionableClass.asSubclass(Enum.class), "MODULATE");
            this.proxy = Proxy.newProxyInstance(meStorageClass.getClassLoader(), new Class<?>[]{meStorageClass}, this);
        }

        private Object proxy() {
            return proxy;
        }

        private long amount(Object key) {
            return contents.getOrDefault(key, 0L);
        }

        private void put(Object key, long amount) {
            contents.put(key, amount);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "insert" -> insert(args[0], ((Number) args[1]).longValue(), args[2]);
                case "extract" -> extract(args[0], ((Number) args[1]).longValue(), args[2]);
                case "getDescription" -> Component.literal("Fake ME Storage");
                default -> throw new UnsupportedOperationException("Unsupported reflective AE2 storage method: " + method.getName());
            };
        }

        private long insert(Object key, long amount, Object actionMode) {
            if (amount <= 0L) {
                return 0L;
            }
            if (actionableModulate.equals(actionMode)) {
                contents.merge(key, amount, Long::sum);
            }
            return amount;
        }

        private long extract(Object key, long amount, Object actionMode) {
            long available = contents.getOrDefault(key, 0L);
            long extracted = Math.min(amount, available);
            if (actionableModulate.equals(actionMode) && extracted > 0L) {
                long remaining = available - extracted;
                if (remaining <= 0L) {
                    contents.remove(key);
                } else {
                    contents.put(key, remaining);
                }
            }
            return extracted;
        }
    }
}
