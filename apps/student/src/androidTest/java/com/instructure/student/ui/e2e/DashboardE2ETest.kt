/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.e2e

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.dataseeding.api.ConversationsApi
import com.instructure.dataseeding.api.GroupsApi
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class DashboardE2ETest : StudentTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.DASHBOARD, TestCategory.E2E)
    fun testDashboardE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 2)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course1 = data.coursesList[0]
        val course2 = data.coursesList[1]

        Log.d(PREPARATION_TAG, "Seed an Inbox conversation via API.")
        ConversationsApi.createConversation(teacher.token, listOf(student.id.toString()))

        Log.d(PREPARATION_TAG, "Seed some group info.")
        val groupCategory = GroupsApi.createCourseGroupCategory(data.coursesList[0].id, teacher.token)
        val groupCategory2 = GroupsApi.createCourseGroupCategory(data.coursesList[0].id, teacher.token)
        val group = GroupsApi.createGroup(groupCategory.id, teacher.token)
        val group2 = GroupsApi.createGroup(groupCategory2.id, teacher.token)

        Log.d(PREPARATION_TAG, "Create group membership for '${student.name}' student.")
        GroupsApi.createGroupMembership(group.id, student.id, teacher.token)
        GroupsApi.createGroupMembership(group2.id, student.id, teacher.token)

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        for(course in data.coursesList) {
            Log.d(ASSERTION_TAG, "Assert that '${course.name}' course is displayed.")
            dashboardPage.assertDisplaysCourse(course)
        }

        Log.d(ASSERTION_TAG, "Assert that '${group.name}' groups is displayed.")
        dashboardPage.assertDisplaysGroup(group, data.coursesList[0])

        Log.d(ASSERTION_TAG, "Assert that there is an unread e-mail so we have the number '1' on the Inbox bottom-menu icon as a badge.")
        dashboardPage.assertUnreadEmails(1)

        Log.d(STEP_TAG, "Switch to List View.")
        dashboardPage.switchCourseView()

        for(course in data.coursesList) {
            Log.d(ASSERTION_TAG, "Assert that '${course.name}' course is displayed.")
            dashboardPage.assertDisplaysCourse(course)
        }

        Log.d(STEP_TAG, "Switch to back to Grid View.")
        dashboardPage.switchCourseView()

        for(course in data.coursesList) {
            Log.d(ASSERTION_TAG, "Assert that '${course.name}' course is displayed.")
            dashboardPage.assertDisplaysCourse(course)
        }

        Log.d(ASSERTION_TAG, "Assert that both of the groups '${group.name}' and '${group2.name}' are displayed because they are independent from the courses.")
        dashboardPage.assertDisplaysGroup(group, course1)
        dashboardPage.assertDisplaysGroup(group2, course1)

        Log.d(STEP_TAG, "Click on 'All Courses' button.")
        dashboardPage.openAllCoursesPage()

        Log.d(ASSERTION_TAG, "Assert that the All Courses Page is loaded.")
        allCoursesPage.assertPageObjects()

        Log.d(STEP_TAG, "Favorite '${course1.name}' course and navigate back to Dashboard Page.")
        allCoursesPage.favoriteCourse(course1.name)
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that only the favoured course, '${course1.name}' is displayed." +
                "Assert that the other course, '${course2.name}' is not displayed since it's not favoured.")
        dashboardPage.assertDisplaysCourse(course1)
        dashboardPage.assertCourseNotDisplayed(course2)

        Log.d(ASSERTION_TAG, "Assert that both of the groups '${group.name}' and '${group2.name}' are displayed because they are independent from the courses.")
        dashboardPage.assertDisplaysGroup(group, course1)
        dashboardPage.assertDisplaysGroup(group2, course1)

        Log.d(STEP_TAG, "Opens ${course1.name} course.")
        dashboardPage.selectCourse(course1)

        Log.d(ASSERTION_TAG, "Assert if Course Browser Page has been opened.")
        courseBrowserPage.assertPageObjects()

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is displayed correctly.")
        dashboardPage.assertPageObjects()

        Log.d(STEP_TAG, "Click on 'All Courses' button.")
        dashboardPage.openAllCoursesPage()

        Log.d(ASSERTION_TAG,"Assert that the All Courses Page is displayed correctly.")
        allCoursesPage.assertPageObjects()

        Log.d(ASSERTION_TAG, "Assert that the mass select button's text is 'Unselect All', since one of the courses is selected.")
        allCoursesPage.assertCourseMassSelectButtonIsDisplayed(true)

        Log.d(STEP_TAG, "Toggle off favourite star icon of '${course1.name}' course.")
        allCoursesPage.unfavoriteCourse(course1.name)

        Log.d(ASSERTION_TAG, "Assert that the 'mass' select button's label is 'Select All'.")
        allCoursesPage.assertCourseMassSelectButtonIsDisplayed(false)

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that both of the courses, '${course1.name}' and '${course2.name}'.")
        dashboardPage.assertDisplaysCourse(course1)
        dashboardPage.assertDisplaysCourse(course2)

        Log.d(STEP_TAG, "Click on 'Edit nickname' menu of '${course1.name}' course.")
        dashboardPage.clickCourseOverflowMenu(course1.name, "Edit nickname")

        val newNickname = "New course nickname"
        Log.d(STEP_TAG, "Change '${course1.name}' course's nickname to: '$newNickname'.")
        dashboardPage.changeCourseNickname(newNickname)

        Log.d(ASSERTION_TAG, "Wait for Dashboard Page to be reloaded and assert that the course's name has been changed to '$newNickname'.")
        dashboardPage.assertPageObjects()
        dashboardPage.assertDisplaysCourse(newNickname)

        Log.d(ASSERTION_TAG, "Assert that '${group.name}' groups is displayed and the '$newNickname' is displayed as the corresponding course name of the group.")
        dashboardPage.assertDisplaysGroup(group, newNickname)

        Log.d(STEP_TAG, "Click on 'Edit nickname' menu of '$newNickname' course.")
        dashboardPage.clickCourseOverflowMenu(newNickname, "Edit nickname")

        Log.d(STEP_TAG, "Make the course nickname empty and click on 'Ok' on the dialog.")
        dashboardPage.changeCourseNickname(EMPTY_STRING)

        Log.d(ASSERTION_TAG, "Wait for Dashboard Page to be reloaded. Assert that if there is no nickname for a course, the course's full name, '${course1.name}' will be displayed.")
        dashboardPage.assertPageObjects()
        dashboardPage.assertDisplaysCourse(course1.name)

        Log.d(ASSERTION_TAG, "Assert that '${group.name}' groups is displayed and the '${data.coursesList[0]}' is displayed as the corresponding course name of the group.")
        dashboardPage.assertDisplaysGroup(group, data.coursesList[0])

        Log.d(ASSERTION_TAG, "Assert that the grades does not displayed on both of the courses' cards by default.")
        dashboardPage.assertCourseGradeNotDisplayed(course1.name, "N/A", false)
        dashboardPage.assertCourseGradeNotDisplayed(course2.name, "N/A", false)

        Log.d(STEP_TAG, "Toggle ON 'Show Grades' and navigate back to Dashboard Page.")
        leftSideNavigationDrawerPage.setShowGrades(true)

        Log.d(ASSERTION_TAG, "Assert that the grades are displayed on both of the courses' cards.")
        dashboardPage.assertCourseGrade(course1.name, "N/A")
        dashboardPage.assertCourseGrade(course2.name, "N/A")

        Log.d(STEP_TAG, "Toggle OFF 'Show Grades' and navigate back to Dashboard Page.")
        leftSideNavigationDrawerPage.setShowGrades(false)

        Log.d(ASSERTION_TAG, "Assert that the grades does not displayed on both of the courses' cards by default.")
        dashboardPage.assertCourseGradeNotDisplayed(course1.name, "N/A")
        dashboardPage.assertCourseGradeNotDisplayed(course2.name, "N/A")

        Log.d(STEP_TAG, "Click on 'All Courses' button.")
        dashboardPage.openAllCoursesPage()

        Log.d(ASSERTION_TAG, "Assert that the All Courses Page is displayed correctly.")
        allCoursesPage.assertPageObjects()

        Log.d(STEP_TAG, "Swiping up on the All Courses page.")
        allCoursesPage.swipeUp()

        Log.d(ASSERTION_TAG, "Assert that the group 'mass' select button's label is 'Select All'.")
        allCoursesPage.assertGroupMassSelectButtonIsDisplayed(false)

        Log.d(STEP_TAG, "Favorite '${group.name}' course and navigate back to Dashboard Page.")
        allCoursesPage.favoriteGroup(group.name)
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that only the favoured group, '${group.name}' is displayed." +
                "Assert that the other group, '${group2.name}' is not displayed since it's not favoured.")
        dashboardPage.assertDisplaysGroup(group, course1)
        dashboardPage.assertGroupNotDisplayed(group2)

        Log.d(STEP_TAG, "Click on 'All Courses' button.")
        dashboardPage.openAllCoursesPage()

        Log.d(ASSERTION_TAG, "Assert that the All Courses Page is displayed correctly.")
        allCoursesPage.assertPageObjects()
        Thread.sleep(2000) //It can be flaky without this 2 seconds

        Log.d(STEP_TAG, "Swiping up on the All Courses page.")
        allCoursesPage.swipeUp()

        Log.d(ASSERTION_TAG, "Assert that the group 'mass' select button's label is 'Unselect All'.")
        allCoursesPage.assertGroupMassSelectButtonIsDisplayed(true)

        Log.d(STEP_TAG, "Toggle off favourite star icon of '${group.name}' group.")
        allCoursesPage.unfavoriteGroup(group.name)

        Log.d(ASSERTION_TAG, "Assert that the 'mass' select button's label is 'Select All'.")
        allCoursesPage.assertGroupMassSelectButtonIsDisplayed(false)

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Assert that both of the groups, '${group.name}' and '${group2.name}' are displayed" +
                "since if there is no group selected on the All Courses page, we are showing all of them (this is the same logics as with the courses).")
        dashboardPage.assertDisplaysGroup(group, course1)
        dashboardPage.assertDisplaysGroup(group2, course1)
    }

}