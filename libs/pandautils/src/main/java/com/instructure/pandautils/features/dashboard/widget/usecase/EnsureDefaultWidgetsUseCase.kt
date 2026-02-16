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
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.repository.WidgetMetadataRepository
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
                id = WidgetMetadata.WIDGET_ID_PROGRESS,
                position = 0,
                isVisible = true,
                isEditable = false
            ),
            WidgetMetadata(
                id = WidgetMetadata.WIDGET_ID_CONFERENCES,
                position = 1,
                isVisible = true,
                isEditable = false
            ),
            WidgetMetadata(
                id = WidgetMetadata.WIDGET_ID_COURSE_INVITATIONS,
                position = 2,
                isVisible = true,
                isEditable = false
            ),
            WidgetMetadata(
                id = WidgetMetadata.WIDGET_ID_INSTITUTIONAL_ANNOUNCEMENTS,
                position = 3,
                isVisible = true,
                isEditable = false
            ),
            WidgetMetadata(
                id = WidgetMetadata.WIDGET_ID_WELCOME,
                position = 4,
                isVisible = true
            ),
            WidgetMetadata(
                id = WidgetMetadata.WIDGET_ID_COURSES,
                position = 5,
                isVisible = true
            ),
            WidgetMetadata(
                id = WidgetMetadata.WIDGET_ID_FORECAST,
                position = 6,
                isVisible = true
            ),
            WidgetMetadata(
                id = WidgetMetadata.WIDGET_ID_TODO,
                position = 7,
                isVisible = true,
            )
        )

        defaultWidgets.forEach { widget ->
            if (widget.id !in existingWidgetIds) {
                repository.saveMetadata(widget)
            }
        }
    }
}