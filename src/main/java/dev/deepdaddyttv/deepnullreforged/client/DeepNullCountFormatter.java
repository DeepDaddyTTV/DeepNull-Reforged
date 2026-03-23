package dev.deepdaddyttv.deepnullreforged.client;

public final class DeepNullCountFormatter {
    private static final String[] SUFFIXES = {"", "K", "M", "B", "T"};

    private DeepNullCountFormatter() {
    }

    public static String formatCompact(int count) {
        long value = Math.max(0L, count);
        if (value < 1_000L) {
            return Long.toString(value);
        }

        int suffixIndex = 0;
        double scaled = value;
        while (scaled >= 1_000.0D && suffixIndex < SUFFIXES.length - 1) {
            scaled /= 1_000.0D;
            suffixIndex++;
        }

        if (scaled < 10.0D) {
            long tenths = (long) Math.floor((scaled * 10.0D) + 1.0E-9D);
            long whole = tenths / 10L;
            long fraction = tenths % 10L;
            return fraction == 0L
                    ? whole + SUFFIXES[suffixIndex]
                    : whole + "." + fraction + SUFFIXES[suffixIndex];
        }

        return (long) Math.floor(scaled) + SUFFIXES[suffixIndex];
    }

    public static String formatExact(int count) {
        long value = Math.max(0L, count);
        String digits = Long.toString(value);
        if (digits.length() <= 3) {
            return digits;
        }

        int lead = digits.length() % 3;
        if (lead == 0) {
            lead = 3;
        }

        StringBuilder builder = new StringBuilder(digits.length() + (digits.length() / 3));
        builder.append(digits, 0, lead);
        for (int index = lead; index < digits.length(); index += 3) {
            builder.append(',').append(digits, index, index + 3);
        }
        return builder.toString();
    }
}
