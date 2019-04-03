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

package com.instructure.canvasapi2.utils

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

object NumberHelper {

    /**
     * Formats the a double as a String percentage, limiting decimal places to the specified length
     * @param number The number to be formatted.
     * @param maxFractionDigits The maximum number of decimal places
     * @return The formatted String
     */
    @JvmOverloads
    @JvmStatic
    fun doubleToPercentage(number: Double?, maxFractionDigits: Int = 2): String {
        val f = NumberFormat.getPercentInstance(Locale.getDefault())
        f.maximumFractionDigits = maxFractionDigits
        return f.format(number!! / 100)
    }

    @JvmStatic
    fun formatInt(number: Int?): String = NumberFormat.getIntegerInstance().format(number) ?: ""

    @JvmStatic
    fun formatInt(number: Long?): String = NumberFormat.getIntegerInstance().format(number) ?: ""

    /**
     * Formats a double value using the current locale settings
     * @param number The number to be formatted
     * @param decimalPlaces The number of decimal places to be printed
     * @param trimZero Whether to include decimal places if everything after the decimal would be zero.
     * @return The formatted string
     */
    @JvmStatic
    fun formatDecimal(number: Double, decimalPlaces: Int, trimZero: Boolean): String {
        val format = DecimalFormat()
        format.maximumFractionDigits = decimalPlaces
        if (!trimZero) {
            format.minimumFractionDigits = decimalPlaces
            format.isDecimalSeparatorAlwaysShown = false
        }
        if (decimalPlaces <= 0) {
            format.roundingMode = RoundingMode.FLOOR
        }
        return format.format(number)
    }
}

val Int.localized get() = NumberHelper.formatInt(this)

val Long.localized get() = NumberHelper.formatInt(this)
