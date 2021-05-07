/*
 * Copyright (C) 2021 - present Instructure, Inc.
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

package com.instructure.pandautils.typeface

import android.content.Context
import android.graphics.Typeface
import java.lang.reflect.Field
import java.lang.reflect.Type

const val REGULAR_FONT_KEY = "sans-serif"
const val MEDIUM_FONT_KEY = "sans-serif-medium"

class TypefaceBehavior(private val context: Context) {

    private val typefaceMap: Map<String, String> = mapOf(
        REGULAR_FONT_KEY to "fonts/balsamiq_regular.ttf",
        MEDIUM_FONT_KEY to "fonts/balsamiq_regular.ttf"
    )

    private var systemDefaults = emptyMap<String, Typeface>()

    fun overrideFont() {
        try {
            val fontMap = typefaceMap.mapValues { Typeface.createFromAsset(context.assets, it.value) }
            val staticField: Field = Typeface::class.java
                    .getDeclaredField("sSystemFontMap")
            staticField.isAccessible = true
            val systemMap: MutableMap<String, Typeface> = staticField.get(null) as MutableMap<String, Typeface>
            val updatedSystemMap = mutableMapOf<String, Typeface>()
            updatedSystemMap.putAll(systemMap)
            systemDefaults = typefaceMap.mapValues { systemMap[it.key] as Typeface }
            updatedSystemMap.putAll(fontMap)
            staticField.set(null, updatedSystemMap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetFonts() {
        try {
            val staticField: Field = Typeface::class.java
                .getDeclaredField("sSystemFontMap")
            staticField.isAccessible = true
            val systemMap: MutableMap<String, Typeface> = staticField.get(null) as MutableMap<String, Typeface>
            val updatedSystemMap = mutableMapOf<String, Typeface>()
            updatedSystemMap.putAll(systemMap)
            updatedSystemMap.putAll(systemDefaults)
            staticField.set(null, updatedSystemMap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
