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

const val REGULAR_FONT_KEY = "sans-serif"
const val MEDIUM_FONT_KEY = "sans-serif-medium"

abstract class TypefaceBehavior {

    abstract val typefaceMap: Map<String, String>

    fun overrideFont(context: Context) {
        try {
            val fontMap = typefaceMap.mapValues { Typeface.createFromAsset(context.assets, it.value) }
            val staticField: Field = Typeface::class.java
                    .getDeclaredField("sSystemFontMap")
            staticField.isAccessible = true
            val systemMap: MutableMap<String, Typeface> = staticField.get(null) as MutableMap<String, Typeface>
            val updatedSystemMap = mutableMapOf<String, Typeface>()
            updatedSystemMap.putAll(systemMap)
            updatedSystemMap.putAll(fontMap)
            staticField.set(null, updatedSystemMap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
