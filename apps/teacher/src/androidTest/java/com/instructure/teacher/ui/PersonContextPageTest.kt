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

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addSubmissionsForAssignment
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeAssignmentDetailsManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeCommentLibraryManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeInboxSettingsManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeStudentContextManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionContentManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionGradeManager
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.di.GraphQlApiModule
import com.instructure.canvasapi2.managers.graphql.AssignmentDetailsManager
import com.instructure.canvasapi2.managers.CommentLibraryManager
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.managers.StudentContextManager
import com.instructure.canvasapi2.managers.graphql.SubmissionContentManager
import com.instructure.canvasapi2.managers.graphql.SubmissionGradeManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.models.User
import com.instructure.teacher.ui.pages.PersonContextPage
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Test

@UninstallModules(GraphQlApiModule::class)
@HiltAndroidTest
class PersonContextPageTest : TeacherTest() {

    @BindValue
    @JvmField
    val personContextManager: StudentContextManager = FakeStudentContextManager()

    @BindValue
    @JvmField
    val commentLibraryManager: CommentLibraryManager = FakeCommentLibraryManager()

    @BindValue
    @JvmField
    val inboxSettingsManager: InboxSettingsManager = FakeInboxSettingsManager()

    @BindValue
    @JvmField
    val assignmentDetailsManager: AssignmentDetailsManager = FakeAssignmentDetailsManager()

    @BindValue
    @JvmField
    val submissionContentManager: SubmissionContentManager = FakeSubmissionContentManager()

    @BindValue
    @JvmField
    val submissionGradeManager: SubmissionGradeManager = FakeSubmissionGradeManager()

    @Test
    override fun displaysPageObjects() {
        getToPersonContextPage(PersonContextPage.UserRole.TEACHER)
        personContextPage.assertPageObjects()
    }

    @Test
    fun displaysStudentContextPageObjects() {
        getToPersonContextPage(PersonContextPage.UserRole.STUDENT)
        studentContextPage.assertPageObjects()
    }

    @Test
    fun displaysTeacherInfo() {
        val (teacher, course) = getToPersonContextPage(PersonContextPage.UserRole.TEACHER)
        personContextPage.assertDisplaysCourseInfo(course.name)
        personContextPage.assertPersonNameIsDisplayed(teacher.shortName!!)
    }

    @Test
    fun displaysStudentInfo() {
        val (student, course) = getToPersonContextPage(PersonContextPage.UserRole.STUDENT)
        personContextPage.assertDisplaysCourseInfo(course.name)
        personContextPage.assertSectionNameView(PersonContextPage.UserRole.STUDENT)
        personContextPage.assertPersonNameIsDisplayed(student.shortName!!)

        studentContextPage.assertDisplaysCourseInfo(course.name)
        studentContextPage.assertDisplaysStudentInfo(student.shortName!!, student.loginId!!)
    }

    private fun getToPersonContextPage(userRole: PersonContextPage.UserRole? = null): Pair<User, Course> {
        val data = MockCanvas.init(
            teacherCount = 1,
            studentCount = 1,
            courseCount = 1,
            favoriteCourseCount = 1,
        )

        val course = data.courses.values.first()
        val teacher = data.teachers[0]
        val student = data.students[0]
        val assignment = data.addAssignment(
            courseId = course.id,
            submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        )

        data.addSubmissionsForAssignment(
            assignmentId = assignment.id,
            userId = student.id,
            types = listOf("online_text_entry"),
            grade = "100%",
            score = 10.0,
        )
        val peopleTab = Tab(position = 1, label = "People", visibility = "public", tabId = Tab.PEOPLE_ID)
        data.courseTabs += course.id to mutableListOf(peopleTab)


        data.addCoursePermissions(
            course.id,
            CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        tokenLogin(data.domain, data.tokenFor(teacher)!!, teacher)

        dashboardPage.openCourse(course)
        courseBrowserPage.openPeopleTab()

        if(userRole == PersonContextPage.UserRole.TEACHER) {
            peopleListPage.clickPerson(teacher.name)
            return Pair(teacher, course)
        }
        else if(userRole == PersonContextPage.UserRole.STUDENT) {
            peopleListPage.clickPerson(student.name)
            return Pair(student, course)
        }

        return Pair(teacher, course)
    }

}
