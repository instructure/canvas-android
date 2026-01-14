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

package com.instructure.pandautils.features.dashboard.widget.forecast

import com.google.gson.Gson
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.repository.WidgetConfigDataRepository
import com.instructure.pandautils.domain.usecase.BaseFlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveForecastConfigUseCase @Inject constructor(
    private val repository: WidgetConfigDataRepository,
    private val gson: Gson
) : BaseFlowUseCase<Unit, ForecastConfig>() {

    override fun execute(params: Unit): Flow<ForecastConfig> {
        return repository.observeConfigJson(WidgetMetadata.WIDGET_ID_FORECAST).map { json ->
            json?.let {
                try {
                    gson.fromJson(it, ForecastConfig::class.java)
                } catch (e: Exception) {
                    ForecastConfig()
                }
            } ?: ForecastConfig()
        }
    }
}