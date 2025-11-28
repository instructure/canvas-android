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
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class EnsureDefaultWidgetsUseCaseTest {

    private val repository: WidgetMetadataRepository = mockk(relaxed = true)
    private lateinit var useCase: EnsureDefaultWidgetsUseCase

    @Before
    fun setup() {
        useCase = EnsureDefaultWidgetsUseCase(repository)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `execute creates default widget when database is empty`() = runTest {
        coEvery { repository.observeAllMetadata() } returns flowOf(emptyList())

        useCase(Unit)

        coVerify {
            repository.saveMetadata(
                match {
                    it.id == "course_invitations" && it.position == 0 && it.isVisible && !it.isEditable
                }
            )
        }
        coVerify {
            repository.saveMetadata(
                match {
                    it.id == "institutional_announcements" && it.position == 1 && it.isVisible && !it.isEditable
                }
            )
        }
        coVerify {
            repository.saveMetadata(
                match {
                    it.id == "welcome" && it.position == 2 && it.isVisible
                }
            )
        }
    }

    @Test
    fun `execute does not create widget if it already exists`() = runTest {
        val existingMetadata = listOf(
            WidgetMetadata("course_invitations", 0, true, false),
            WidgetMetadata("institutional_announcements", 1, true, false),
            WidgetMetadata("welcome", 2, true)
        )
        coEvery { repository.observeAllMetadata() } returns flowOf(existingMetadata)

        useCase(Unit)

        coVerify(exactly = 0) { repository.saveMetadata(any()) }
    }

    @Test
    fun `execute creates only missing widgets`() = runTest {
        val existingMetadata = listOf(
            WidgetMetadata("other-widget", 0, true)
        )
        coEvery { repository.observeAllMetadata() } returns flowOf(existingMetadata)

        useCase(Unit)

        coVerify(exactly = 1) {
            repository.saveMetadata(
                match { it.id == "course_invitations" }
            )
        }
        coVerify(exactly = 1) {
            repository.saveMetadata(
                match { it.id == "institutional_announcements" }
            )
        }
        coVerify(exactly = 1) {
            repository.saveMetadata(
                match { it.id == "welcome" }
            )
        }
        coVerify(exactly = 0) {
            repository.saveMetadata(
                match { it.id == "other-widget" }
            )
        }
    }

    @Test
    fun `execute creates all default widgets when database is empty`() = runTest {
        coEvery { repository.observeAllMetadata() } returns flowOf(emptyList())

        useCase(Unit)

        coVerify(atLeast = 1) { repository.saveMetadata(any()) }
    }
}