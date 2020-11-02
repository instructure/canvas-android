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
import kotlin.math.ln
import kotlin.math.pow

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

    /**
     * Returns a user-friendly file size, such as "12.7 MB" or "967 KB", closely matching web's behavior.
     *
     * @param context An Android [Context], used to access the string resources for file size units
     * @param size The file size in bytes
     */
    fun readableFileSize(context: Context, size: Long): String {
        val units = context.resources.getStringArray(R.array.file_size_units)
        return readableFileSize(units, size)
    }

    /**
     * Returns a user-friendly file size, closely matching web's behavior.
     *
     * @param units An array of human-readable file size units, ordered from smallest to largest. This *must* be
     *              consecutive and must start with a representation for the 'bytes' unit. For example:
     *              `["Bytes", "KiloBytes", "Megabytes"]` or `["B", "KB", "MB", "GB", "TB"]`
     * @param fileSize The file size in bytes
     */
    fun readableFileSize(units: Array<String>, fileSize: Long): String {
        // To match web, we use metric (1000) rather than binary (1024) as the unit step size
        val unitStepSize = 1000.0

        // Ensure the file size is at least zero (in rare cases we've seen negative values)
        val bytes = fileSize.coerceAtLeast(0L).toDouble()

        // Find the index of the unit prefix, capping at the max unit (in rare cases we've seen as high as Long.MAX_VALUE)
        val unitIndex = floor(ln(bytes) / ln(unitStepSize)).toInt().coerceIn(0, units.lastIndex)

        // Calculate the new value base on the chosen unit
        val value = (bytes / unitStepSize.pow(unitIndex))

        // To match web, we don't display decimals for units smaller than MB
        val format = if (unitIndex < 2) "#,###" else "#,##0.#"
        val displayValue = DecimalFormat(format).format(value)

        return "$displayValue ${units[unitIndex]}"
    }
}

val Int.localized get() = NumberHelper.formatInt(this)

val Long.localized get() = NumberHelper.formatInt(this)
