/*
 * Copyright 2014 Yaroslav Mytkalyk
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.docd.purefm.utils;

import android.support.annotation.NonNull;

public final class ArrayUtils {

    private ArrayUtils() {}

    @SuppressWarnings("unchecked")
    public static <INPUT, OUTPUT> void copyArrayAndCast(@NonNull final INPUT[] input, @NonNull final OUTPUT[] output) {
        if (input.length != output.length) {
            throw new IllegalArgumentException("input and output lengths differ");
        }
        for (int i = 0; i < input.length; i++) {
            output[i] = (OUTPUT) input[i];
        }
    }
}
