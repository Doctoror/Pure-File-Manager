package com.docd.purefm.utils;

public final class ArrayUtils {
    private ArrayUtils() {}

    @SuppressWarnings("unchecked")
    public static <INPUT, OUTPUT> void copyArrayAndCast(final INPUT[] input, final OUTPUT[] output) {
        if (input == null || output == null) {
            throw new IllegalArgumentException("input and output must not be null");
        }
        if (input.length != output.length) {
            throw new IllegalArgumentException("input and output lengths differ");
        }
        for (int i = 0; i < input.length; i++) {
            output[i] = (OUTPUT) input[i];
        }
    }
}
