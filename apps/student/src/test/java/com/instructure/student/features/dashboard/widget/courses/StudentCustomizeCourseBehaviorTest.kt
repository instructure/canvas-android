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

package com.instructure.student.features.dashboard.widget.courses

import com.instructure.student.util.StudentPrefs
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StudentCustomizeCourseBehaviorTest {

    private val studentPrefs: StudentPrefs = mockk()
    private val behavior = StudentCustomizeCourseBehavior(studentPrefs)

    @Test
    fun `shouldShowColorOverlay returns true when hideCourseColorOverlay is false`() {
        every { studentPrefs.hideCourseColorOverlay } returns false

        val result = behavior.shouldShowColorOverlay()

        assertTrue(result)
    }

    @Test
    fun `shouldShowColorOverlay returns false when hideCourseColorOverlay is true`() {
        every { studentPrefs.hideCourseColorOverlay } returns true

        val result = behavior.shouldShowColorOverlay()

        assertFalse(result)
    }
}
