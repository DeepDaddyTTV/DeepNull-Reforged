package net.neoforged.neoforge.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class ModConfigSpec {
    private ModConfigSpec() {
    }

    public static final class Builder {
        public Builder comment(String... comments) {
            return this;
        }

        public Builder push(String path) {
            return this;
        }

        public Builder pop() {
            return this;
        }

        public BooleanValue define(String name, boolean defaultValue) {
            return new BooleanValue(defaultValue);
        }

        public IntValue defineInRange(String name, int defaultValue, int min, int max) {
            return new IntValue(defaultValue, min, max);
        }

        public DoubleValue defineInRange(String name, double defaultValue, double min, double max) {
            return new DoubleValue(defaultValue, min, max);
        }

        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(String name, List<? extends T> defaultValue, Supplier<T> supplier, Predicate<Object> validator) {
            return new ConfigValue<>(List.copyOf(new ArrayList<>(defaultValue)));
        }

        public ModConfigSpec build() {
            return new ModConfigSpec();
        }
    }

    public static class ConfigValue<T> {
        private T value;

        public ConfigValue(T defaultValue) {
            this.value = defaultValue;
        }

        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }
    }

    public static final class BooleanValue extends ConfigValue<Boolean> {
        public BooleanValue(boolean defaultValue) {
            super(defaultValue);
        }

        public boolean getAsBoolean() {
            return get();
        }
    }

    public static final class IntValue extends ConfigValue<Integer> {
        private final int min;
        private final int max;

        public IntValue(int defaultValue, int min, int max) {
            super(clamp(defaultValue, min, max));
            this.min = min;
            this.max = max;
        }

        public int getAsInt() {
            return get();
        }

        @Override
        public void set(Integer value) {
            super.set(clamp(value == null ? min : value, min, max));
        }

        private static int clamp(int value, int min, int max) {
            return Math.max(min, Math.min(max, value));
        }
    }

    public static final class DoubleValue extends ConfigValue<Double> {
        private final double min;
        private final double max;

        public DoubleValue(double defaultValue, double min, double max) {
            super(clamp(defaultValue, min, max));
            this.min = min;
            this.max = max;
        }

        @Override
        public void set(Double value) {
            super.set(clamp(value == null ? min : value, min, max));
        }

        private static double clamp(double value, double min, double max) {
            return Math.max(min, Math.min(max, value));
        }
    }
}
