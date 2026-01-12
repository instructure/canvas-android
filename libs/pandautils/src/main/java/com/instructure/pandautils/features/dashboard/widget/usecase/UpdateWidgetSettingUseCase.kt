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
import com.instructure.pandautils.features.dashboard.widget.WidgetConfig
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.forecast.ForecastConfig
import com.instructure.pandautils.features.dashboard.widget.repository.WidgetConfigDataRepository
import com.instructure.pandautils.features.dashboard.widget.welcome.WelcomeConfig
import com.instructure.pandautils.domain.usecase.BaseUseCase
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

class UpdateWidgetSettingUseCase @Inject constructor(
    private val repository: WidgetConfigDataRepository,
    private val gson: Gson
) : BaseUseCase<UpdateWidgetSettingUseCase.Params, Unit>() {

    data class Params(
        val widgetId: String,
        val key: String,
        val value: Any
    )

    override suspend fun execute(params: Params) {
        val json = repository.getConfigJson(params.widgetId)
        val config = json?.let { deserializeConfig(params.widgetId, it) } ?: getDefaultConfig(params.widgetId) ?: return

        val jsonString = config.toJson()
        val jsonObject = gson.fromJson(jsonString, JsonObject::class.java)

        when (params.value) {
            is Boolean -> jsonObject.addProperty(params.key, params.value)
            is String -> jsonObject.addProperty(params.key, params.value)
            is Int -> jsonObject.addProperty(params.key, params.value)
            is Number -> jsonObject.addProperty(params.key, params.value)
        }

        val updatedJsonString = gson.toJson(jsonObject)
        repository.saveConfigJson(params.widgetId, updatedJsonString)
    }

    private fun getDefaultConfig(widgetId: String): WidgetConfig? {
        return when (widgetId) {
            WidgetMetadata.WIDGET_ID_WELCOME -> WelcomeConfig()
            WidgetMetadata.WIDGET_ID_FORECAST -> ForecastConfig()
            else -> null
        }
    }

    private fun deserializeConfig(widgetId: String, json: String): WidgetConfig? {
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
}