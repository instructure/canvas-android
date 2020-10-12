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

import android.content.Context
import com.instructure.canvasapi2.R
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

object NumberHelper {

    /**
     * Formats the a double as a String percentage, limiting decimal places to the specified length
     * @param number The number to be formatted.
     * @param maxFractionDigits The maximum number of decimal places
     * @return The formatted String
     */
    @JvmOverloads
    fun doubleToPercentage(number: Double?, maxFractionDigits: Int = 2): String {
        val f = NumberFormat.getPercentInstance(Locale.getDefault())
        f.maximumFractionDigits = maxFractionDigits
        return f.format(number!! / 100)
    }

    fun formatInt(number: Int?): String = NumberFormat.getIntegerInstance().format(number) ?: ""

    fun formatInt(number: Long?): String = NumberFormat.getIntegerInstance().format(number) ?: ""

    /**
     * Formats a double value using the current locale settings
     * @param number The number to be formatted
     * @param decimalPlaces The number of decimal places to be printed
     * @param trimZero Whether to include decimal places if everything after the decimal would be zero.
     * @return The formatted string
     */
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

    fun readableFileSize(context: Context, size: Long): String {
        val units = context.resources.getStringArray(R.array.file_size_units)
        return readableFileSize(units, size)
    }

    fun readableFileSize(units: Array<String>, fileSize: Long): String {
        val size = fileSize.coerceAtLeast(0L)
        var digitGroups = 0
        if (size > 0) digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt().coerceIn(0, units.size-1)
        val byteSize = floor(size / 1024.0.pow(digitGroups.toDouble()) * 10) / 10
        val displaySize = DecimalFormat("#,##0.#").format(byteSize)

        return "$displaySize ${units[digitGroups]}"
    }
}

val Int.localized get() = NumberHelper.formatInt(this)

val Long.localized get() = NumberHelper.formatInt(this)
