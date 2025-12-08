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

import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.pandautils.features.dashboard.widget.repository.WidgetMetadataRepository
import javax.inject.Inject

class SwapWidgetPositionsUseCase @Inject constructor(
    private val repository: WidgetMetadataRepository
) : BaseUseCase<SwapWidgetPositionsUseCase.Params, Unit>() {

    override suspend fun execute(params: Params) {
        repository.swapPositions(params.widgetId1, params.widgetId2)
    }

    data class Params(
        val widgetId1: String,
        val widgetId2: String
    )
}