package dev.deepdaddyttv.deepnullreforged.gametest;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.integration.mekanism.MekanismCompat;
import dev.deepdaddyttv.deepnullreforged.integration.mekanism.MekanismTransferCompat;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.inventory.StoredChemical;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

@GameTestHolder(DeepNullReforged.MODID)
@PrefixGameTestTemplate(false)
public final class MekanismRegressionGameTests {
    private MekanismRegressionGameTests() {
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void mekanism_chemical_transfer_pushes_and_refills_only_matching_types(GameTestHelper helper) throws ReflectiveOperationException {
        if (!ModList.get().isLoaded("mekanism")) {
            helper.succeed();
            return;
        }

        Class<?> mekanismApiClass = Class.forName("mekanism.api.MekanismAPI");
        Class<?> chemicalClass = Class.forName("mekanism.api.chemical.Chemical");
        Class<?> chemicalStackClass = Class.forName("mekanism.api.chemical.ChemicalStack");
        Class<?> chemicalHandlerClass = Class.forName("mekanism.api.chemical.IChemicalHandler");
        @SuppressWarnings("unchecked")
        ResourceKey<? extends Registry<?>> chemicalRegistryKey = (ResourceKey<? extends Registry<?>>) mekanismApiClass.getField("CHEMICAL_REGISTRY_NAME").get(null);
        ResourceKey<?> emptyChemicalKey = (ResourceKey<?>) mekanismApiClass.getField("EMPTY_CHEMICAL_KEY").get(null);

        List<Holder.Reference<?>> chemicals = new ArrayList<>(2);
        @SuppressWarnings({"rawtypes", "unchecked"})
        Iterable<?> chemicalElements = ((Iterable<?>) helper.getLevel().registryAccess()
                .lookupOrThrow((ResourceKey) chemicalRegistryKey)
                .listElements()::iterator);
        for (Object rawHolder : chemicalElements) {
            Holder.Reference<?> holder = (Holder.Reference<?>) rawHolder;
            if (holder.key().equals(emptyChemicalKey)) {
                continue;
            }
            chemicals.add(holder);
            if (chemicals.size() >= 2) {
                break;
            }
        }
        helper.assertTrue(chemicals.size() >= 2, "Mekanism chemical registry should expose at least two non-empty chemicals for integration tests");

        Constructor<?> chemicalStackCtor = chemicalStackClass.getConstructor(Holder.class, long.class);
        Object firstChemical = chemicalStackCtor.newInstance(chemicals.get(0), 1_000L);
        Object secondChemical = chemicalStackCtor.newInstance(chemicals.get(1), 800L);
        StoredChemical storedFirst = (StoredChemical) MekanismCompat.class.getMethod("fromChemicalStack", chemicalStackClass).invoke(null, firstChemical);
        StoredChemical storedSecond = (StoredChemical) MekanismCompat.class.getMethod("fromChemicalStack", chemicalStackClass).invoke(null, secondChemical);

        DeepNullInventory pushInventory = DeepNullGameTestSupport.dampNullInventory(helper, DeepNullTier.REDSTONE);
        pushInventory.getUpgradeHandler().setStackInSlot(DeepNullUpgradeType.GAS.slot(), DeepNullGameTestSupport.upgradeStack(DeepNullUpgradeType.GAS));
        pushInventory.fillChemical(storedFirst, false);

        ReflectiveChemicalHandler pushTarget = new ReflectiveChemicalHandler(chemicalHandlerClass, chemicalStackClass, 2, 10_000L);
        Method moveTo = MekanismTransferCompat.class.getMethod("moveChemicalsToTarget", DeepNullInventory.class, chemicalHandlerClass);
        helper.assertTrue((Boolean) moveTo.invoke(null, pushInventory, pushTarget.proxy()), "Mekanism push should move stored chemicals into the target");
        helper.assertValueEqual(pushTarget.amount(0), 1_000L, "Chemical target should receive the stored chemical");

        DeepNullInventory refillInventory = DeepNullGameTestSupport.dampNullInventory(helper, DeepNullTier.REDSTONE);
        refillInventory.getUpgradeHandler().setStackInSlot(DeepNullUpgradeType.GAS.slot(), DeepNullGameTestSupport.upgradeStack(DeepNullUpgradeType.GAS));
        refillInventory.fillChemical(storedFirst.copyWithAmount(500L), false);

        ReflectiveChemicalHandler refillTarget = new ReflectiveChemicalHandler(chemicalHandlerClass, chemicalStackClass, 2, 10_000L);
        refillTarget.set(0, secondChemical);
        Method copyWithAmount = chemicalStackClass.getMethod("copyWithAmount", long.class);
        refillTarget.set(1, copyWithAmount.invoke(firstChemical, 600L));

        Method moveFrom = MekanismTransferCompat.class.getMethod("moveChemicalsFromTarget", DeepNullInventory.class, chemicalHandlerClass);
        helper.assertTrue((Boolean) moveFrom.invoke(null, refillInventory, refillTarget.proxy()), "Mekanism refill should pull matching stored chemicals");
        helper.assertValueEqual(refillInventory.getChemicalInSlot(0).amount(), 1_100L, "Matching chemical should be refilled into the DampNull");
        helper.assertValueEqual(refillTarget.amount(0), 800L, "Non-matching chemical should remain in the source handler");
        helper.assertValueEqual(refillTarget.amount(1), 0L, "Matching chemical should be drained from the source handler");
        helper.assertTrue(refillInventory.findMatchingChemicalSlot(storedSecond) < 0, "Non-matching chemicals should not create new stored slots during refill");
        helper.succeed();
    }

    private static final class ReflectiveChemicalHandler implements InvocationHandler {
        private final List<Object> contents;
        private final long capacity;
        private final Object emptyStack;
        private final Method copyMethod;
        private final Method copyWithAmountMethod;
        private final Method getAmountMethod;
        private final Method isEmptyMethod;
        private final Method executeMethod;
        private final Method isSameChemicalMethod;
        private final Object proxy;

        private ReflectiveChemicalHandler(Class<?> chemicalHandlerClass, Class<?> chemicalStackClass, int tanks, long capacity) throws ReflectiveOperationException {
            this.capacity = capacity;
            this.emptyStack = chemicalStackClass.getField("EMPTY").get(null);
            this.copyMethod = chemicalStackClass.getMethod("copy");
            this.copyWithAmountMethod = chemicalStackClass.getMethod("copyWithAmount", long.class);
            this.getAmountMethod = chemicalStackClass.getMethod("getAmount");
            this.isEmptyMethod = chemicalStackClass.getMethod("isEmpty");
            this.executeMethod = Class.forName("mekanism.api.Action").getMethod("execute");
            this.isSameChemicalMethod = chemicalStackClass.getMethod("isSameChemical", chemicalStackClass, chemicalStackClass);
            this.contents = new ArrayList<>(tanks);
            for (int i = 0; i < tanks; i++) {
                this.contents.add(emptyStack);
            }
            this.proxy = Proxy.newProxyInstance(chemicalHandlerClass.getClassLoader(), new Class<?>[]{chemicalHandlerClass}, this);
        }

        private Object proxy() {
            return proxy;
        }

        private void set(int tank, Object stack) throws ReflectiveOperationException {
            contents.set(tank, copyMethod.invoke(stack));
        }

        private long amount(int tank) throws ReflectiveOperationException {
            return (long) getAmountMethod.invoke(contents.get(tank));
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return switch (method.getName()) {
                case "getChemicalTanks" -> contents.size();
                case "getChemicalInTank" -> copyMethod.invoke(contents.get((int) args[0]));
                case "setChemicalInTank" -> {
                    contents.set((int) args[0], copyMethod.invoke(args[1]));
                    yield null;
                }
                case "getChemicalTankCapacity" -> capacity;
                case "isValid" -> !((boolean) isEmptyMethod.invoke(args[1]));
                case "insertChemical" -> {
                    if (args.length == 3 && args[0] instanceof Integer tank) {
                        yield insert(tank, args[1], args[2]);
                    }
                    yield insertAcrossTanks(args[0], args[1]);
                }
                case "extractChemical" -> {
                    if (args.length == 3 && args[0] instanceof Integer tank) {
                        yield extract(tank, ((Number) args[1]).longValue(), args[2]);
                    }
                    if (args.length == 2 && args[0] instanceof Number amount) {
                        yield extractAcrossTanks(amount.longValue(), args[1]);
                    }
                    yield extractMatching(args[0], args[1]);
                }
                default -> throw new UnsupportedOperationException("Unsupported reflective chemical handler method: " + method.getName());
            };
        }

        private Object insertAcrossTanks(Object stack, Object action) throws ReflectiveOperationException {
            Object remainder = copyMethod.invoke(stack);
            for (int tank = 0; tank < contents.size(); tank++) {
                if ((boolean) isEmptyMethod.invoke(remainder)) {
                    break;
                }
                remainder = insert(tank, remainder, action);
            }
            return remainder;
        }

        private Object insert(int tank, Object stack, Object action) throws ReflectiveOperationException {
            if ((boolean) isEmptyMethod.invoke(stack)) {
                return emptyStack;
            }
            Object existing = contents.get(tank);
            if ((boolean) isEmptyMethod.invoke(existing)) {
                long accepted = Math.min(capacity, (long) getAmountMethod.invoke(stack));
                if ((boolean) executeMethod.invoke(action) && accepted > 0L) {
                    contents.set(tank, copyWithAmountMethod.invoke(stack, accepted));
                }
                return accepted >= (long) getAmountMethod.invoke(stack) ? emptyStack : copyWithAmountMethod.invoke(stack, (long) getAmountMethod.invoke(stack) - accepted);
            }
            if (!(boolean) isSameChemicalMethod.invoke(null, existing, stack)) {
                return copyMethod.invoke(stack);
            }
            long space = capacity - (long) getAmountMethod.invoke(existing);
            long accepted = Math.min(space, (long) getAmountMethod.invoke(stack));
            if ((boolean) executeMethod.invoke(action) && accepted > 0L) {
                contents.set(tank, copyWithAmountMethod.invoke(existing, (long) getAmountMethod.invoke(existing) + accepted));
            }
            return accepted >= (long) getAmountMethod.invoke(stack) ? emptyStack : copyWithAmountMethod.invoke(stack, (long) getAmountMethod.invoke(stack) - accepted);
        }

        private Object extractAcrossTanks(long amount, Object action) throws ReflectiveOperationException {
            long remaining = amount;
            Object extracted = emptyStack;
            for (int tank = 0; tank < contents.size() && remaining > 0L; tank++) {
                Object part = extract(tank, remaining, action);
                long partAmount = (long) getAmountMethod.invoke(part);
                if (partAmount <= 0L) {
                    continue;
                }
                remaining -= partAmount;
                if ((boolean) isEmptyMethod.invoke(extracted)) {
                    extracted = copyMethod.invoke(part);
                } else {
                    extracted = copyWithAmountMethod.invoke(extracted, (long) getAmountMethod.invoke(extracted) + partAmount);
                }
            }
            return extracted;
        }

        private Object extractMatching(Object stack, Object action) throws ReflectiveOperationException {
            long requested = (long) getAmountMethod.invoke(stack);
            if (requested <= 0L) {
                return emptyStack;
            }
            for (int tank = 0; tank < contents.size(); tank++) {
                Object existing = contents.get(tank);
                if ((boolean) isEmptyMethod.invoke(existing)) {
                    continue;
                }
                if ((boolean) isSameChemicalMethod.invoke(null, existing, stack)) {
                    return extract(tank, requested, action);
                }
            }
            return emptyStack;
        }

        private Object extract(int tank, long amount, Object action) throws ReflectiveOperationException {
            Object existing = contents.get(tank);
            long extracted = Math.min(amount, (long) getAmountMethod.invoke(existing));
            if (extracted <= 0L) {
                return emptyStack;
            }
            if ((boolean) executeMethod.invoke(action)) {
                long remaining = (long) getAmountMethod.invoke(existing) - extracted;
                contents.set(tank, remaining <= 0L ? emptyStack : copyWithAmountMethod.invoke(existing, remaining));
            }
            return copyWithAmountMethod.invoke(existing, extracted);
        }
    }
}
