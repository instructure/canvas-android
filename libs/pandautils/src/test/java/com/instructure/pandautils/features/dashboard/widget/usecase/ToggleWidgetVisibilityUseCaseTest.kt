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

import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.repository.WidgetMetadataRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ToggleWidgetVisibilityUseCaseTest {

    private val updateWidgetPositionUseCase: UpdateWidgetPositionUseCase = mockk(relaxed = true)
    private val updateWidgetVisibilityUseCase: UpdateWidgetVisibilityUseCase = mockk(relaxed = true)
    private val widgetMetadataRepository: WidgetMetadataRepository = mockk(relaxed = true)

    private lateinit var useCase: ToggleWidgetVisibilityUseCase

    @Before
    fun setUp() {
        useCase = ToggleWidgetVisibilityUseCase(
            updateWidgetPositionUseCase,
            updateWidgetVisibilityUseCase,
            widgetMetadataRepository
        )
    }

    @Test
    fun `disabling widget moves it to first invisible position`() = runTest {
        // 4 non-editable widgets (positions 0-3)
        val nonEditableWidgets = listOf(
            WidgetMetadata(id = "progress", position = 0, isVisible = true, isEditable = false),
            WidgetMetadata(id = "conferences", position = 1, isVisible = true, isEditable = false),
            WidgetMetadata(id = "invitations", position = 2, isVisible = true, isEditable = false),
            WidgetMetadata(id = "announcements", position = 3, isVisible = true, isEditable = false)
        )

        // 3 visible editable widgets (positions 4-6) and 1 invisible (position 7)
        val editableWidgets = listOf(
            WidgetMetadata(id = "widget1", position = 4, isVisible = true, isEditable = true),
            WidgetMetadata(id = "widget2", position = 5, isVisible = true, isEditable = true),
            WidgetMetadata(id = "widget3", position = 6, isVisible = true, isEditable = true),
            WidgetMetadata(id = "widget4", position = 7, isVisible = false, isEditable = true)
        )

        coEvery { widgetMetadataRepository.observeAllMetadata() } returns flowOf(nonEditableWidgets + editableWidgets)

        val params = ToggleWidgetVisibilityUseCase.Params("widget2", editableWidgets)
        useCase(params)

        // widget2 is at position 5, should move to position 6 (first invisible position for editable widgets)
        // widget3 should shift up from 6 to 5
        coVerify { updateWidgetPositionUseCase(UpdateWidgetPositionUseCase.Params("widget3", 5)) }
        coVerify { updateWidgetPositionUseCase(UpdateWidgetPositionUseCase.Params("widget2", 6)) }
        coVerify { updateWidgetVisibilityUseCase(UpdateWidgetVisibilityUseCase.Params("widget2", false)) }
    }

    @Test
    fun `enabling widget moves it to end of visible widgets`() = runTest {
        // 4 non-editable widgets
        val nonEditableWidgets = listOf(
            WidgetMetadata(id = "progress", position = 0, isVisible = true, isEditable = false),
            WidgetMetadata(id = "conferences", position = 1, isVisible = true, isEditable = false),
            WidgetMetadata(id = "invitations", position = 2, isVisible = true, isEditable = false),
            WidgetMetadata(id = "announcements", position = 3, isVisible = true, isEditable = false)
        )

        // 2 visible editable widgets and 2 invisible
        val editableWidgets = listOf(
            WidgetMetadata(id = "widget1", position = 4, isVisible = true, isEditable = true),
            WidgetMetadata(id = "widget2", position = 5, isVisible = true, isEditable = true),
            WidgetMetadata(id = "widget3", position = 6, isVisible = false, isEditable = true),
            WidgetMetadata(id = "widget4", position = 7, isVisible = false, isEditable = true)
        )

        coEvery { widgetMetadataRepository.observeAllMetadata() } returns flowOf(nonEditableWidgets + editableWidgets)

        val params = ToggleWidgetVisibilityUseCase.Params("widget3", editableWidgets)
        useCase(params)

        // widget3 is at position 6, should move to position 6 (end of visible = 4 + 2)
        // No widgets need to shift since it's moving backward
        coVerify { updateWidgetPositionUseCase(UpdateWidgetPositionUseCase.Params("widget3", 6)) }
        coVerify { updateWidgetVisibilityUseCase(UpdateWidgetVisibilityUseCase.Params("widget3", true)) }
    }

    @Test
    fun `enabling first invisible widget shifts other invisible widgets down`() = runTest {
        // 4 non-editable widgets
        val nonEditableWidgets = listOf(
            WidgetMetadata(id = "progress", position = 0, isVisible = true, isEditable = false),
            WidgetMetadata(id = "conferences", position = 1, isVisible = true, isEditable = false),
            WidgetMetadata(id = "invitations", position = 2, isVisible = true, isEditable = false),
            WidgetMetadata(id = "announcements", position = 3, isVisible = true, isEditable = false)
        )

        // 2 visible and 3 invisible editable widgets
        val editableWidgets = listOf(
            WidgetMetadata(id = "widget1", position = 4, isVisible = true, isEditable = true),
            WidgetMetadata(id = "widget2", position = 5, isVisible = true, isEditable = true),
            WidgetMetadata(id = "widget3", position = 6, isVisible = false, isEditable = true),
            WidgetMetadata(id = "widget4", position = 7, isVisible = false, isEditable = true),
            WidgetMetadata(id = "widget5", position = 8, isVisible = false, isEditable = true)
        )

        coEvery { widgetMetadataRepository.observeAllMetadata() } returns flowOf(nonEditableWidgets + editableWidgets)

        val params = ToggleWidgetVisibilityUseCase.Params("widget3", editableWidgets)
        useCase(params)

        // widget3 should move to position 6 (end of visible = 4 + 2)
        // No shift needed as it's staying in same position
        coVerify { updateWidgetPositionUseCase(UpdateWidgetPositionUseCase.Params("widget3", 6)) }
        coVerify { updateWidgetVisibilityUseCase(UpdateWidgetVisibilityUseCase.Params("widget3", true)) }
    }

    @Test
    fun `disabling last visible widget moves it to first invisible position`() = runTest {
        // 4 non-editable widgets
        val nonEditableWidgets = listOf(
            WidgetMetadata(id = "progress", position = 0, isVisible = true, isEditable = false),
            WidgetMetadata(id = "conferences", position = 1, isVisible = true, isEditable = false),
            WidgetMetadata(id = "invitations", position = 2, isVisible = true, isEditable = false),
            WidgetMetadata(id = "announcements", position = 3, isVisible = true, isEditable = false)
        )

        // 3 visible editable widgets
        val editableWidgets = listOf(
            WidgetMetadata(id = "widget1", position = 4, isVisible = true, isEditable = true),
            WidgetMetadata(id = "widget2", position = 5, isVisible = true, isEditable = true),
            WidgetMetadata(id = "widget3", position = 6, isVisible = true, isEditable = true)
        )

        coEvery { widgetMetadataRepository.observeAllMetadata() } returns flowOf(nonEditableWidgets + editableWidgets)

        val params = ToggleWidgetVisibilityUseCase.Params("widget3", editableWidgets)
        useCase(params)

        // widget3 is at position 6, should move to position 6 (first invisible = 4 + 3 - 1)
        // No shift needed since it's the last one
        coVerify { updateWidgetPositionUseCase(UpdateWidgetPositionUseCase.Params("widget3", 6)) }
        coVerify { updateWidgetVisibilityUseCase(UpdateWidgetVisibilityUseCase.Params("widget3", false)) }
    }

    @Test
    fun `disabling first visible widget shifts others up`() = runTest {
        // 4 non-editable widgets
        val nonEditableWidgets = listOf(
            WidgetMetadata(id = "progress", position = 0, isVisible = true, isEditable = false),
            WidgetMetadata(id = "conferences", position = 1, isVisible = true, isEditable = false),
            WidgetMetadata(id = "invitations", position = 2, isVisible = true, isEditable = false),
            WidgetMetadata(id = "announcements", position = 3, isVisible = true, isEditable = false)
        )

        // 3 visible editable widgets
        val editableWidgets = listOf(
            WidgetMetadata(id = "widget1", position = 4, isVisible = true, isEditable = true),
            WidgetMetadata(id = "widget2", position = 5, isVisible = true, isEditable = true),
            WidgetMetadata(id = "widget3", position = 6, isVisible = true, isEditable = true)
        )

        coEvery { widgetMetadataRepository.observeAllMetadata() } returns flowOf(nonEditableWidgets + editableWidgets)

        val params = ToggleWidgetVisibilityUseCase.Params("widget1", editableWidgets)
        useCase(params)

        // widget1 is at position 4, should move to position 6 (first invisible = 4 + 3 - 1)
        // widget2 and widget3 should shift up
        coVerify { updateWidgetPositionUseCase(UpdateWidgetPositionUseCase.Params("widget2", 4)) }
        coVerify { updateWidgetPositionUseCase(UpdateWidgetPositionUseCase.Params("widget3", 5)) }
        coVerify { updateWidgetPositionUseCase(UpdateWidgetPositionUseCase.Params("widget1", 6)) }
        coVerify { updateWidgetVisibilityUseCase(UpdateWidgetVisibilityUseCase.Params("widget1", false)) }
    }

    @Test
    fun `non-editable widgets are never repositioned`() = runTest {
        // 4 non-editable widgets
        val nonEditableWidgets = listOf(
            WidgetMetadata(id = "progress", position = 0, isVisible = true, isEditable = false),
            WidgetMetadata(id = "conferences", position = 1, isVisible = true, isEditable = false),
            WidgetMetadata(id = "invitations", position = 2, isVisible = true, isEditable = false),
            WidgetMetadata(id = "announcements", position = 3, isVisible = true, isEditable = false)
        )

        // 2 visible editable widgets
        val editableWidgets = listOf(
            WidgetMetadata(id = "widget1", position = 4, isVisible = true, isEditable = true),
            WidgetMetadata(id = "widget2", position = 5, isVisible = true, isEditable = true)
        )

        coEvery { widgetMetadataRepository.observeAllMetadata() } returns flowOf(nonEditableWidgets + editableWidgets)

        val params = ToggleWidgetVisibilityUseCase.Params("widget1", editableWidgets)
        useCase(params)

        // Verify only editable widgets were repositioned (widget2 shifts up, widget1 moves to end)
        coVerify { updateWidgetPositionUseCase(UpdateWidgetPositionUseCase.Params("widget2", 4)) }
        coVerify { updateWidgetPositionUseCase(UpdateWidgetPositionUseCase.Params("widget1", 5)) }
        coVerify { updateWidgetVisibilityUseCase(UpdateWidgetVisibilityUseCase.Params("widget1", false)) }

        // Verify updateWidgetPositionUseCase was only called twice (for the 2 editable widgets)
        coVerify(exactly = 2) { updateWidgetPositionUseCase(any()) }
    }

    @Test
    fun `enabling widget when no invisible widgets exist`() = runTest {
        // 4 non-editable widgets
        val nonEditableWidgets = listOf(
            WidgetMetadata(id = "progress", position = 0, isVisible = true, isEditable = false),
            WidgetMetadata(id = "conferences", position = 1, isVisible = true, isEditable = false),
            WidgetMetadata(id = "invitations", position = 2, isVisible = true, isEditable = false),
            WidgetMetadata(id = "announcements", position = 3, isVisible = true, isEditable = false)
        )

        // All editable widgets are visible
        val editableWidgets = listOf(
            WidgetMetadata(id = "widget1", position = 4, isVisible = true, isEditable = true),
            WidgetMetadata(id = "widget2", position = 5, isVisible = true, isEditable = true)
        )

        coEvery { widgetMetadataRepository.observeAllMetadata() } returns flowOf(nonEditableWidgets + editableWidgets)

        val params = ToggleWidgetVisibilityUseCase.Params("widget1", editableWidgets)
        useCase(params)

        // Toggling an already visible widget should disable it
        coVerify { updateWidgetVisibilityUseCase(UpdateWidgetVisibilityUseCase.Params("widget1", false)) }
    }
}