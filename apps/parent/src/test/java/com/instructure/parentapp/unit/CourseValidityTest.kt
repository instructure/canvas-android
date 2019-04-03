/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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
package com.instructure.parentapp.unit

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.Term
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.parentapp.models.isValidForParent
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*
import java.util.concurrent.TimeUnit

class CourseValidityTest : Assert() {

    private lateinit var baseCourse: Course

    @Before
    fun setup() {
        baseCourse = Course(
            id = 100,
            name = "test"
        )
    }

    @Test
    fun `Returns false if course is restricted by date`() {
        val course = baseCourse.copy(accessRestrictedByDate = true)
        assertFalse(course.isValidForParent())
    }

    @Test
    fun `Returns false if course has invalid name`() {
        val course = baseCourse.copy(name = "")
        assertFalse(course.isValidForParent())
    }

    @Test
    fun `Returns false if course has ended`() {
        val yesterday = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)).toApiString()
        val course = baseCourse.copy(endAt = yesterday)
        assertFalse(course.isValidForParent())
    }

    @Test
    fun `Returns false if term has ended and course has not`() {
        val tomorrow = Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)).toApiString()
        val yesterday = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)).toApiString()
        val course = baseCourse.copy(
            endAt = tomorrow,
            term = Term(endAt = yesterday)
        )
        assertFalse(course.isValidForParent())
    }

    @Test
    fun `Returns false if all sections have ended`() {
        val tomorrow = Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)).toApiString()
        val yesterday = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)).toApiString()
        val course = baseCourse.copy(
            endAt = tomorrow,
            sections = listOf(
                Section(endAt = yesterday),
                Section(endAt = yesterday)
            )
        )
        assertFalse(course.isValidForParent())
    }

    @Test
    fun `Returns true if any section has not ended`() {
        val tomorrow = Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)).toApiString()
        val yesterday = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)).toApiString()
        val course = baseCourse.copy(
            endAt = tomorrow,
            sections = listOf(
                Section(endAt = yesterday),
                Section(endAt = tomorrow)
            )
        )
        assertTrue(course.isValidForParent())
    }

    @Test
    fun `Returns true if restricted to course dates even if term has ended`() {
        val tomorrow = Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)).toApiString()
        val yesterday = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)).toApiString()
        val course = baseCourse.copy(
            endAt = tomorrow,
            term = Term(endAt = yesterday),
            restrictEnrollmentsToCourseDate = true
        )
        assertTrue(course.isValidForParent())
    }

    @Test
    fun `Returns true if no end date specified`() {
        assertTrue(baseCourse.isValidForParent())
    }

}
