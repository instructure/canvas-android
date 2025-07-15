/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.pandautils.utils

import java.math.RoundingMode
import java.text.DecimalFormat

fun Double.toFormattedString(maxDecimals: Int? = 2): String {
    return if (this % 1 == 0.0) {
        this.toInt().toString()
    } else {
        if (maxDecimals != null) {
            this.toBigDecimal().setScale(maxDecimals, RoundingMode.HALF_DOWN).toString()
        } else {
            this.toString()
        }
    }
}

val Double.stringValueWithoutTrailingZeros: String
    get() {
        val format = DecimalFormat("0.#")
        return format.format(this)
    }
