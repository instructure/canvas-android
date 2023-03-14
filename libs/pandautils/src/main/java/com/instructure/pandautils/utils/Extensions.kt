/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.utils

import com.google.gson.Gson
import java.util.*
import kotlin.math.ln
import kotlin.math.pow

fun Any.toJson(): String {
    return Gson().toJson(this)
}

inline fun <reified T> String.fromJson(): T {
    return Gson().fromJson<T>(this, T::class.java)
}

fun Long.humanReadableByteCount(): String {
    val unit = 1024
    if (this < unit) return "$this B"
    val exp = (ln(this.toDouble()) / ln(unit.toDouble())).toInt()
    val pre = "KMGTPE"[exp - 1].toString()
    return String.format(Locale.getDefault(), "%.1f %sB", this / unit.toDouble().pow(exp.toDouble()), pre)
}

fun Long?.orDefault(default: Long = 0): Long {
    return this ?: default
}

fun Int?.orDefault(default: Int = 0): Int {
    return this ?: default
}

fun Boolean?.orDefault(default: Boolean = false): Boolean {
    return this ?: default
}

fun Double?.orDefault(default: Double = 0.0): Double {
    return this ?: default
}
