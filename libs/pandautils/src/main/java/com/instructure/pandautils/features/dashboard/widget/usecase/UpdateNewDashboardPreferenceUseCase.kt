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

package com.instructure.pandautils.features.dashboard.widget.usecase

import com.google.gson.Gson
import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.pandautils.features.dashboard.widget.GlobalConfig
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.repository.WidgetConfigDataRepository
import javax.inject.Inject

class UpdateNewDashboardPreferenceUseCase @Inject constructor(
    private val repository: WidgetConfigDataRepository,
    private val gson: Gson
) : BaseUseCase<UpdateNewDashboardPreferenceUseCase.Params, Unit>() {

    data class Params(val enabled: Boolean)

    override suspend fun execute(params: Params) {
        val json = repository.getConfigJson(WidgetMetadata.WIDGET_ID_GLOBAL)
        val config = json?.let {
            try {
                gson.fromJson(it, GlobalConfig::class.java)
            } catch (e: Exception) {
                GlobalConfig()
            }
        } ?: GlobalConfig()

        val updated = config.copy(newDashboardEnabled = params.enabled)
        repository.saveConfigJson(WidgetMetadata.WIDGET_ID_GLOBAL, updated.toJson())
    }
}