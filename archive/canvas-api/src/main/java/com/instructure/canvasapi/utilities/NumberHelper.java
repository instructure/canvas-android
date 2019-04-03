/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.canvasapi.utilities;

import java.text.NumberFormat;
import java.util.Locale;

public class NumberHelper {

    /**
     * Formats the a double as a String percentage, limited to two decimal places
     * @param number The number to be formatted
     * @return The formatted String
     */
    public static String doubleToPercentage(Double number) {
        return doubleToPercentage(number, 2);
    }

    /**
     * Formats the a double as a String percentage, limiting decimal places to the specified length
     * @param number The number to be formatted.
     * @return The formatted String
     */
    public static String doubleToPercentage(Double number, int maxFractionDigits) {
        NumberFormat f = NumberFormat.getPercentInstance(Locale.getDefault());
        f.setMaximumFractionDigits(maxFractionDigits);
        return f.format(number/100);
    }

    /**
     * Formats an integer value with the current locale, using correct separators and numerals.
     * For example, some languages use non-european digits, e.g. in arabic "12345" becomes "١٢,٣٤٥"
     * and some languages separate every third digit with a blank space, e.g. "12345" becomes "12 345"
     * @param number The number to be formatted
     * @return The formatted number as a String
     */
    public static String formatInt(long number) {
        return NumberFormat.getIntegerInstance().format(number);
    }
}
