/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 *
 */
package com.instructure.teacher.ui.interaction

import androidx.test.espresso.Espresso.closeSoftKeyboard
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.teacher.ui.pages.classic.PeopleListPage
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.lang.Thread.sleep

@HiltAndroidTest
class PeopleListInteractionTest : TeacherTest() {

    @Test
    override fun displaysPageObjects() {
        goToPeopleList()
        peopleListPage.assertPageObjects()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.PEOPLE, TestCategory.INTERACTION)
    fun displaysAllEnrolledUsers() {
        val data = goToPeopleList(studentCount = 2, teacherCount = 1)
        val teacher = data.teachers[0]

        peopleListPage.assertPersonListed(teacher)
        data.students.forEach { peopleListPage.assertPersonListed(it) }
        peopleListPage.assertSearchResultCount(data.students.size + 1)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.PEOPLE, TestCategory.INTERACTION)
    fun displaysCorrectPersonRoles() {
        val data = goToPeopleList(studentCount = 1, teacherCount = 1)
        val teacher = data.teachers[0]
        val student = data.students[0]

        peopleListPage.assertPersonRole(teacher.name, PeopleListPage.UserRole.TEACHER)
        peopleListPage.assertPersonRole(student.name, PeopleListPage.UserRole.STUDENT)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.PEOPLE, TestCategory.INTERACTION)
    fun searchByName_narrowsTheList() {
        val data = goToPeopleList(studentCount = 2, teacherCount = 1)
        val target = data.teachers[0]

        peopleListPage.searchable.clickOnSearchButton()
        peopleListPage.searchable.typeToSearchBar(target.name)

        peopleListPage.assertSearchResultCount(1)
        peopleListPage.assertPersonListed(target.name)
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.PEOPLE, TestCategory.INTERACTION)
    fun searchByName_noMatch_showsEmptyView() {
        goToPeopleList(studentCount = 1, teacherCount = 1)

        peopleListPage.searchable.clickOnSearchButton()
        peopleListPage.searchable.typeToSearchBar("NonMatchingQuery_${System.currentTimeMillis()}")

        peopleListPage.assertEmptyViewIsDisplayed()
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.PEOPLE, TestCategory.INTERACTION)
    fun clearSearch_restoresFullList() {
        val data = goToPeopleList(studentCount = 2, teacherCount = 1)
        val totalPeople = data.students.size + data.teachers.size
        val target = data.teachers[0]

        peopleListPage.searchable.clickOnSearchButton()
        peopleListPage.searchable.typeToSearchBar(target.name)
        peopleListPage.assertSearchResultCount(1)
        peopleListPage.assertPersonListed(target.name)

        peopleListPage.searchable.clickOnClearSearchButton()
        sleep(1000)
        closeSoftKeyboard()
        peopleListPage.assertSearchResultCount(totalPeople)
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.PEOPLE, TestCategory.INTERACTION)
    fun displaysPersonPronouns() {
        val data = setupPeopleListData(studentCount = 1, teacherCount = 1)
        val teacher = data.teachers[0]
        val teacherWithPronouns = teacher.copy(pronouns = "She/Her")
        data.users[teacher.id] = teacherWithPronouns

        tokenLogin(data.domain, data.tokenFor(teacher)!!, teacher)
        dashboardPage.openCourse(data.courses.values.first())
        courseBrowserPage.openPeopleTab()

        peopleListPage.assertPersonPronouns(teacherWithPronouns.name, "(She/Her)")
    }

    private fun goToPeopleList(studentCount: Int = 1, teacherCount: Int = 1): MockCanvas {
        val data = setupPeopleListData(studentCount, teacherCount)
        val teacher = data.teachers[0]

        tokenLogin(data.domain, data.tokenFor(teacher)!!, teacher)
        dashboardPage.openCourse(data.courses.values.first())
        courseBrowserPage.openPeopleTab()
        return data
    }

    private fun setupPeopleListData(studentCount: Int, teacherCount: Int): MockCanvas {
        val data = MockCanvas.init(
            teacherCount = teacherCount,
            studentCount = studentCount,
            courseCount = 1,
            favoriteCourseCount = 1,
        )

        val course = data.courses.values.first()
        val peopleTab = Tab(position = 1, label = "People", visibility = "public", tabId = Tab.PEOPLE_ID)
        data.courseTabs += course.id to mutableListOf(peopleTab)

        data.addCoursePermissions(
            course.id,
            CanvasContextPermission()
        )

        // The People list search hits the recipients endpoint (separate from the enrollments-backed
        // full list), so seed the course recipients with the enrolled users by their full name.
        data.recipientGroups[course.id] = buildList {
            data.teachers.forEach { teacher ->
                add(
                    Recipient(
                        stringId = teacher.id.toString(),
                        name = teacher.name,
                        avatarURL = teacher.avatarUrl,
                        commonCourses = hashMapOf(course.id.toString() to arrayOf(EnrollmentType.TeacherEnrollment.rawValue))
                    )
                )
            }
            data.students.forEach { student ->
                add(
                    Recipient(
                        stringId = student.id.toString(),
                        name = student.name,
                        avatarURL = student.avatarUrl,
                        commonCourses = hashMapOf(course.id.toString() to arrayOf(EnrollmentType.StudentEnrollment.rawValue))
                    )
                )
            }
        }

        return data
    }
}