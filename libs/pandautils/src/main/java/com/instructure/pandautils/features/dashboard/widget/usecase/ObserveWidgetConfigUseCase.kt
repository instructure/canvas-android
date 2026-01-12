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

package com.instructure.pandautils.features.dashboard.widget.usecase

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.instructure.pandautils.features.dashboard.customize.WidgetSettingItem
import com.instructure.pandautils.features.dashboard.widget.SettingType
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.repository.WidgetConfigDataRepository
import com.instructure.pandautils.features.dashboard.widget.welcome.WelcomeConfig
import com.instructure.pandautils.features.dashboard.widget.forecast.ForecastConfig
import com.instructure.pandautils.domain.usecase.BaseFlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveWidgetConfigUseCase @Inject constructor(
    private val repository: WidgetConfigDataRepository,
    private val gson: Gson
) : BaseFlowUseCase<String, List<WidgetSettingItem>>() {

    override fun execute(params: String): Flow<List<WidgetSettingItem>> {
        return repository.observeConfigJson(params).map { json ->
            val config = json?.let { deserializeConfig(params, it) } ?: getDefaultConfig(params)
            config?.let { parseConfigToSettings(it) } ?: emptyList()
        }
    }

    private fun getDefaultConfig(widgetId: String): com.instructure.pandautils.features.dashboard.widget.WidgetConfig? {
        return when (widgetId) {
            WidgetMetadata.WIDGET_ID_WELCOME -> WelcomeConfig()
            WidgetMetadata.WIDGET_ID_FORECAST -> ForecastConfig()
            else -> null
        }
    }

    private fun deserializeConfig(widgetId: String, json: String): com.instructure.pandautils.features.dashboard.widget.WidgetConfig? {
        return when (widgetId) {
            WidgetMetadata.WIDGET_ID_WELCOME -> try {
                gson.fromJson(json, WelcomeConfig::class.java)
            } catch (e: Exception) {
                null
            }
            WidgetMetadata.WIDGET_ID_FORECAST -> try {
                gson.fromJson(json, ForecastConfig::class.java)
            } catch (e: Exception) {
                null
            }
            else -> null
        }
    }

    private fun parseConfigToSettings(config: com.instructure.pandautils.features.dashboard.widget.WidgetConfig): List<WidgetSettingItem> {
        val settingDefinitions = config.getSettingDefinitions()
        if (settingDefinitions.isEmpty()) return emptyList()

        val jsonString = config.toJson()
        val jsonObject = gson.fromJson(jsonString, JsonObject::class.java)

        return settingDefinitions.mapNotNull { definition ->
            val jsonElement = jsonObject.get(definition.key)
            val value = when (definition.type) {
                SettingType.BOOLEAN -> jsonElement?.asBoolean
                SettingType.COLOR -> jsonElement?.asString
            }

            value?.let {
                WidgetSettingItem(
                    key = definition.key,
                    type = definition.type,
                    value = it
                )
            }
        }
    }
}