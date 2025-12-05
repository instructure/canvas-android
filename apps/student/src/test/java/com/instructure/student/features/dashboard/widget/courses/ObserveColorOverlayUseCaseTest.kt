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

package com.instructure.student.features.dashboard.widget.courses

import android.content.Context
import android.content.SharedPreferences
import com.instructure.student.util.StudentPrefs
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ObserveColorOverlayUseCaseTest {

    private val context: Context = mockk()
    private val sharedPreferences: SharedPreferences = mockk(relaxed = true)

    private lateinit var useCase: ObserveColorOverlayUseCase

    @Before
    fun setup() {
        mockkObject(StudentPrefs)
        every { context.getSharedPreferences(any(), any()) } returns sharedPreferences
        useCase = ObserveColorOverlayUseCase(context)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `emits true when hideCourseColorOverlay is false`() = runTest {
        every { StudentPrefs.hideCourseColorOverlay } returns false

        val result = useCase(Unit).first()

        assertTrue(result)
    }

    @Test
    fun `emits false when hideCourseColorOverlay is true`() = runTest {
        every { StudentPrefs.hideCourseColorOverlay } returns true

        val result = useCase(Unit).first()

        assertFalse(result)
    }

    @Test
    fun `registers preference change listener`() = runTest {
        every { StudentPrefs.hideCourseColorOverlay } returns false

        useCase(Unit).first()

        verify { sharedPreferences.registerOnSharedPreferenceChangeListener(any()) }
    }
}