/*
 * Copyright (C) 2021 - present Instructure, Inc.
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

package com.emeritus.student.mobius.settings.help

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.HelpLink
import com.instructure.pandautils.features.help.HelpLinkFilter
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StudentHelpLinkFilterTest {

    private val helpLinkFilter: HelpLinkFilter = StudentHelpLinkFilter()

    @Test
    fun `Don't allow links that are only available for teachers`() {
        val linkAllowed = helpLinkFilter.isLinkAllowed(createHelpLink(listOf("teacher"), "Teacher link"), emptyList())

        assertFalse(linkAllowed)
    }

    @Test
    fun `Allow links that are only available for students`() {
        val linkAllowed = helpLinkFilter.isLinkAllowed(createHelpLink(listOf("student"), "Student link"), emptyList())

        assertTrue(linkAllowed)
    }

    @Test
    fun `Allow links that are available for all users`() {
        val linkAllowed = helpLinkFilter.isLinkAllowed(createHelpLink(listOf("user"), "User link"), emptyList())

        assertTrue(linkAllowed)
    }

    @Test
    fun `Allow links that are available for students and teachers`() {
        val linkAllowed = helpLinkFilter.isLinkAllowed(createHelpLink(listOf("student", "teacher"), "Student and Teacher link"), emptyList())

        assertTrue(linkAllowed)
    }

    @Test
    fun `Don't allow teacher feedback link if user has no favorite courses as student`() {
        val link = createHelpLink(listOf("user"), "Student and Teacher link", url = "#teacher_feedback")
        val linkAllowed = helpLinkFilter.isLinkAllowed(link, emptyList())

        assertFalse(linkAllowed)
    }

    @Test
    fun `Allow teacher feedback link if user has favorite courses as student`() {
        val link = createHelpLink(listOf("user"), "Student and Teacher link", url = "#teacher_feedback")
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        val linkAllowed = helpLinkFilter.isLinkAllowed(link, listOf(course))

        assertTrue(linkAllowed)
    }

    private fun createHelpLink(availableTo: List<String>, text: String, id: String = "", url: String = ""): HelpLink {
        return HelpLink(id, "", availableTo, url, text, "")
    }
}