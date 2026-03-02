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

class ToggleWidgetVisibilityUseCase @Inject constructor(
    private val updateWidgetPositionUseCase: UpdateWidgetPositionUseCase,
    private val updateWidgetVisibilityUseCase: UpdateWidgetVisibilityUseCase,
    private val widgetMetadataRepository: WidgetMetadataRepository
) : BaseUseCase<ToggleWidgetVisibilityUseCase.Params, Unit>() {

    override suspend fun execute(params: Params) {
        // Get all widgets to determine non-editable count
        val allWidgets = widgetMetadataRepository.observeAllMetadata().first()
        val nonEditableCount = allWidgets.count { !it.isEditable }

        // Only work with editable widgets
        val editableWidgets = params.widgets.filter { it.isEditable }

        val widgetItem = editableWidgets.firstOrNull { it.id == params.widgetId } ?: return
        val newVisibility = !widgetItem.isVisible
        val currentPosition = widgetItem.position

        if (newVisibility) {
            // Enabling: Move to the end of visible editable widgets
            val visibleEditableCount = editableWidgets.count { it.isVisible }
            val newPosition = nonEditableCount + visibleEditableCount

            // Shift widgets based on direction of movement
            when {
                currentPosition > newPosition -> {
                    // Moving forward: shift widgets from [newPosition, currentPosition) down by 1
                    editableWidgets
                        .filter { it.id != params.widgetId && it.position >= newPosition && it.position < currentPosition }
                        .sortedByDescending { it.position }
                        .forEach { widget ->
                            updateWidgetPositionUseCase(
                                UpdateWidgetPositionUseCase.Params(widget.id, widget.position + 1)
                            )
                        }
                }
                currentPosition < newPosition -> {
                    // Moving backward: shift widgets from (currentPosition, newPosition] up by 1
                    editableWidgets
                        .filter { it.id != params.widgetId && it.position > currentPosition && it.position <= newPosition }
                        .sortedBy { it.position }
                        .forEach { widget ->
                            updateWidgetPositionUseCase(
                                UpdateWidgetPositionUseCase.Params(widget.id, widget.position - 1)
                            )
                        }
                }
            }

            // Move the enabled widget to the end of visible widgets
            updateWidgetPositionUseCase(
                UpdateWidgetPositionUseCase.Params(params.widgetId, newPosition)
            )
        } else {
            // Disabling: Move to the first position after visible editable widgets
            val visibleEditableCount = editableWidgets.count { it.isVisible }
            val newPosition = nonEditableCount + visibleEditableCount - 1

            // Shift widgets based on direction of movement
            when {
                currentPosition > newPosition -> {
                    // Moving forward: shift widgets from [newPosition, currentPosition) down by 1
                    editableWidgets
                        .filter { it.id != params.widgetId && it.position >= newPosition && it.position < currentPosition }
                        .sortedByDescending { it.position }
                        .forEach { widget ->
                            updateWidgetPositionUseCase(
                                UpdateWidgetPositionUseCase.Params(widget.id, widget.position + 1)
                            )
                        }
                }
                currentPosition < newPosition -> {
                    // Moving backward: shift widgets from (currentPosition, newPosition] up by 1
                    editableWidgets
                        .filter { it.id != params.widgetId && it.position > currentPosition && it.position <= newPosition }
                        .sortedBy { it.position }
                        .forEach { widget ->
                            updateWidgetPositionUseCase(
                                UpdateWidgetPositionUseCase.Params(widget.id, widget.position - 1)
                            )
                        }
                }
            }

            // Move the disabled widget to the first invisible position
            updateWidgetPositionUseCase(
                UpdateWidgetPositionUseCase.Params(params.widgetId, newPosition)
            )
        }

        // Finally update visibility
        updateWidgetVisibilityUseCase(
            UpdateWidgetVisibilityUseCase.Params(params.widgetId, newVisibility)
        )
    }

    data class Params(
        val widgetId: String,
        val widgets: List<WidgetMetadata>
    )
}