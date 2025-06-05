/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.student.widget.grades.list

import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemedColor
import com.instructure.student.widget.glance.WidgetState
import com.instructure.student.widget.grades.GradesWidgetRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GradesWidgetUpdaterTest {
    private val repository: GradesWidgetRepository = mockk()
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val glanceManager: GlanceAppWidgetManager = mockk()
    private val glanceId: GlanceId = mockk()
    private lateinit var updater: GradesWidgetUpdater

    @Before
    fun setup() {
        ContextKeeper.appContext = mockk(relaxed = true)
        every { apiPrefs.user } returns mockk()
        mockkConstructor(GlanceAppWidgetManager::class)
        mockkObject(ColorKeeper)
        every { ColorKeeper.getOrGenerateColor(any()) } returns mockk()
        every { glanceManager.getGlanceIdBy(any<Int>()) } returns glanceId
        updater = GradesWidgetUpdater(repository, apiPrefs, glanceManager)
    }

    @Test
    fun `emits NotLoggedIn when user is null`() = runTest {
        every { apiPrefs.user } returns null
        updater.updateData(listOf(1))
        val state = updater.uiState.first().second
        assertEquals(WidgetState.NotLoggedIn, state.state)
    }

    @Test
    fun `emits Empty when repository returns empty list`() = runTest {
        coEvery { repository.getCoursesWithGradingScheme(any()) } returns emptyList()

        updater.updateData(listOf(1))
        val state = updater.uiState.first().second
        assertEquals(WidgetState.Empty, state.state)
    }

    @Test
    fun `emits Content when repository returns courses`() = runTest {
        every { apiPrefs.fullDomain } returns ""
        val themedColor = mockk<ThemedColor>(relaxed = true)
        every { themedColor.light } returns 0
        every { themedColor.dark } returns 0
        mockkObject(ColorKeeper)
        every { ColorKeeper.getOrGenerateColor(any()) } returns themedColor
        val course = mockk<Course>(relaxed = true)
        coEvery { repository.getCoursesWithGradingScheme(any()) } returns listOf(course)

        updater.updateData(listOf(1))
        val state = updater.uiState.first().second
        assertEquals(WidgetState.Content, state.state)
        assertEquals(1, state.courses.size)
    }

    @Test
    fun `emits Error when repository throws`() = runTest {
        coEvery { repository.getCoursesWithGradingScheme(any()) } throws RuntimeException("fail")

        updater.updateData(listOf(1))
        val state = updater.uiState.first().second
        assertEquals(WidgetState.Error, state.state)
    }
}
