/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.features.calendar

import com.instructure.pandautils.utils.FeatureFlagProvider
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StudentCalendarBehaviorTest {

    private val featureFlagProvider: FeatureFlagProvider = mockk()
    private val behavior = StudentCalendarBehavior(featureFlagProvider)

    @Test
    fun `shouldShowAddEventButton returns false when restrict_student_access flag is enabled`() = runTest {
        coEvery { featureFlagProvider.checkEnvironmentFeatureFlag("restrict_student_access") } returns true

        val result = behavior.shouldShowAddEventButton()

        assertFalse(result)
    }

    @Test
    fun `shouldShowAddEventButton returns true when restrict_student_access flag is disabled`() = runTest {
        coEvery { featureFlagProvider.checkEnvironmentFeatureFlag("restrict_student_access") } returns false

        val result = behavior.shouldShowAddEventButton()

        assertTrue(result)
    }

    @Test
    fun `shouldShowAddEventButton returns true when flag check throws exception`() = runTest {
        coEvery { featureFlagProvider.checkEnvironmentFeatureFlag("restrict_student_access") } throws RuntimeException("Network error")

        val result = behavior.shouldShowAddEventButton()

        assertTrue(result)
    }
}
