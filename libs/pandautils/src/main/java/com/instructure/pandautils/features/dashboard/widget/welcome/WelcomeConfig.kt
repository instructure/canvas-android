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

package com.instructure.pandautils.features.dashboard.widget.welcome

import com.google.gson.Gson
import com.instructure.pandautils.features.dashboard.widget.SettingDefinition
import com.instructure.pandautils.features.dashboard.widget.SettingType
import com.instructure.pandautils.features.dashboard.widget.WidgetConfig
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata

data class WelcomeConfig(
    override val widgetId: String = WidgetMetadata.WIDGET_ID_WELCOME,
    val showGreeting: Boolean = true,
    val backgroundColor: Int = 0x2573DF
) : WidgetConfig {
    override fun toJson(): String = Gson().toJson(this)

    override fun getSettingDefinitions() = listOf(
        SettingDefinition(
            key = "showGreeting",
            type = SettingType.BOOLEAN
        ),
        SettingDefinition(
            key = "backgroundColor",
            type = SettingType.COLOR
        )
    )

    companion object {
        fun fromJson(jsonString: String): WelcomeConfig {
            return Gson().fromJson(jsonString, WelcomeConfig::class.java)
        }
    }
}