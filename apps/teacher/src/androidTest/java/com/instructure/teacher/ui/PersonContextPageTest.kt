/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.teacher.ui

import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.teacher.ui.pages.PersonContextPage
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedAssignments
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class PersonContextPageTest : TeacherTest() {

    @Test
    override fun displaysPageObjects() {
        getToPersonContextPage()
        personContextPage.assertPageObjects()
    }

    @Test
    fun displaysTeacherInfo() {
        val (teacher, course) = getToPersonContextPage()
        personContextPage.assertDisplaysCourseInfo(course)
        personContextPage.assertSectionNameView(PersonContextPage.UserRole.TEACHER)
        personContextPage.assertPersonNameIsDisplayed(teacher.shortName)
    }

    private fun getToPersonContextPage(assignmentCount: Int = 1): Triple<CanvasUserApiModel, CourseApiModel, List<AssignmentApiModel>> {
        val data = seedData(teachers = 1, courses = 1, favoriteCourses = 1, students = 1)
        val course = data.coursesList[0]
        val teacher = data.teachersList[0]
        val assignments = seedAssignments(
            courseId = course.id,
            assignments = assignmentCount,
            teacherToken = teacher.token
        )
        tokenLogin(teacher)
        coursesListPage.openCourse(course)
        courseBrowserPage.openPeopleTab()
        peopleListPage.clickPerson(teacher)
        return Triple(teacher, course, assignments)
    }

}
