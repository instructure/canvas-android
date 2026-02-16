/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.pandautils.features.dashboard.widget

import com.google.gson.Gson

data class GlobalConfig(
    override val widgetId: String = WidgetMetadata.WIDGET_ID_GLOBAL,
    val backgroundColor: Int = DEFAULT_COLOR
) : WidgetConfig {
    override fun toJson(): String = Gson().toJson(this)

    override fun getSettingDefinitions() = listOf(
        SettingDefinition(
            key = "backgroundColor",
            type = SettingType.COLOR
        )
    )

    companion object {
        fun fromJson(jsonString: String): GlobalConfig {
            return Gson().fromJson(jsonString, GlobalConfig::class.java)
        }

        const val DEFAULT_COLOR = 0xFF2573DF.toInt()
    }
}
