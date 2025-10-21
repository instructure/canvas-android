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
package com.instructure.horizon.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

val gson = Gson()

/**
 * Deserializes the dynamic list of maps into a list of a specific data class using Gson.
 */
inline fun <reified T : Any> List<Any>.deserializeDynamicList(): List<T> {
    val targetType = object : TypeToken<T>() {}.type

    return this.mapNotNull { rawItem ->
        if (rawItem is Map<*, *>) {
            try {
                return@mapNotNull gson.fromJson<T>(gson.toJsonTree(rawItem), targetType)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
}