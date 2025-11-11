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

package com.instructure.student.features.dashboard.widget.usecase

import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.student.features.dashboard.widget.WidgetMetadata
import com.instructure.student.features.dashboard.widget.repository.WidgetMetadataRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class EnsureDefaultWidgetsUseCase @Inject constructor(
    private val repository: WidgetMetadataRepository
) : BaseUseCase<Unit, Unit>() {

    override suspend fun execute(params: Unit) {
        val existingWidgets = repository.observeAllMetadata().first()
        val existingWidgetIds = existingWidgets.map { it.id }.toSet()

        val defaultWidgets = listOf(
            WidgetMetadata(
                id = "welcome",
                title = "Welcome",
                position = 0,
                isVisible = true
            )
        )

        defaultWidgets.forEach { widget ->
            if (widget.id !in existingWidgetIds) {
                repository.saveMetadata(widget)
            }
        }
    }
}