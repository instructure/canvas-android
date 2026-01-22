/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.pandautils.features.dashboard.widget.courses

import com.google.gson.Gson
import com.instructure.pandautils.features.dashboard.widget.SettingDefinition
import com.instructure.pandautils.features.dashboard.widget.SettingType
import com.instructure.pandautils.features.dashboard.widget.WidgetConfig
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata

data class CoursesConfig(
    override val widgetId: String = WidgetMetadata.WIDGET_ID_COURSES,
    val showGrades: Boolean = false,
    val showColorOverlay: Boolean = false
) : WidgetConfig {
    override fun toJson(): String = Gson().toJson(this)

    override fun getSettingDefinitions() = listOf(
        SettingDefinition(
            key = KEY_SHOW_GRADES,
            type = SettingType.BOOLEAN
        ),
        SettingDefinition(
            key = KEY_SHOW_COLOR_OVERLAY,
            type = SettingType.BOOLEAN
        )
    )

    companion object {
        const val KEY_SHOW_GRADES = "showGrades"
        const val KEY_SHOW_COLOR_OVERLAY = "showColorOverlay"

        fun fromJson(jsonString: String): CoursesConfig {
            return Gson().fromJson(jsonString, CoursesConfig::class.java)
        }
    }
}
