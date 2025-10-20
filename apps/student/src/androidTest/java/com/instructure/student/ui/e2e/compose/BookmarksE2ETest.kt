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
package com.instructure.student.ui.e2e.compose

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.uiautomator.UiSelector
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.canvas.espresso.pressBackButton
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.PagesApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.model.UpdateCourse
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.student.ui.pages.classic.WebViewTextCheck
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.extensions.seedData
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class BookmarksE2ETest : StudentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.BOOKMARKS, TestCategory.E2E)
    fun testBookmarksE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 2)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Preparing an assignment which will be saved as a bookmark.")
        val assignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to assignments page and click on the prepared assignment.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAssignments()
        assignmentListPage.clickAssignment(assignment)

        val bookmarkName = "Assignment Details BM"
        Log.d(STEP_TAG, "Add a new bookmark with name: '$bookmarkName'")
        assignmentDetailsPage.addBookmark(bookmarkName)

        Log.d(STEP_TAG, "Navigate back to Bookmarks page.")
        pressBackButton(3)

        Log.d(STEP_TAG, "Click on the 'Bookmarks' menu within the left side menu to open the Bookmarks page.")
        leftSideNavigationDrawerPage.clickBookmarksMenu()

        Log.d(ASSERTION_TAG, "Assert if the newly created bookmark has displayed.")
        bookmarkPage.assertBookmarkDisplayed(bookmarkName)

        Log.d(STEP_TAG, "Click on '$bookmarkName' bookmark.")
        bookmarkPage.clickBookmark(bookmarkName)

        Log.d(ASSERTION_TAG, "Assert if the '$bookmarkName' bookmark is navigating to the Assignment Details page.")
        assignmentDetailsPage.assertAssignmentTitle(assignment.name)

        Log.d(STEP_TAG, "Navigate back to the Bookmark List page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert if the '${bookmarkName}' bookmark is displayed.")
        bookmarkPage.assertBookmarkDisplayed(bookmarkName)

        val newName = "Assignment Details BM Modified"
        Log.d(STEP_TAG, "Change bookmark's name from '$bookmarkName' to '$newName'.")
        bookmarkPage.changeBookmarkName(bookmarkName, newName)

        Log.d(ASSERTION_TAG, "Assert if the '${bookmarkName}' bookmark's name has been changed to '$newName'.")
        bookmarkPage.assertBookmarkDisplayed(newName)

        Log.d(STEP_TAG, "Click on the previously renamed bookmark.")
        bookmarkPage.clickBookmark(newName)

        Log.d(ASSERTION_TAG, "Assert if it's still navigating to the corresponding assignment's details page.")
        assignmentDetailsPage.assertAssignmentTitle(assignment.name)

        Log.d(STEP_TAG, "Navigate back to the Bookmark List page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Delete bookmark: '$newName'.")
        bookmarkPage.deleteBookmark(newName)

        Log.d(ASSERTION_TAG, "Assert that empty view is displayed, so the bookmark has been deleted.")
        bookmarkPage.assertEmptyView()
    }

    @E2E
    @Test
    @TestMetaData(Priority.BUG_CASE, FeatureCategory.BOOKMARKS, TestCategory.E2E)
    fun testBookmarkCourseHomePageE2E() {

        //Bug Ticket: MBL-18910
        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seed a PUBLISHED, FRONT page for '${course.name}' course.")
        val pagePublishedFront = PagesApi.createCoursePage(course.id, teacher.token, frontPage = true, editingRoles = "public", body = "<h1 id=\"header1\">Front Page Text</h1>")

        Log.d(PREPARATION_TAG, "Update '${course.name}' course to set previously seeded '$pagePublishedFront' Front Page as (Course) Home Page.")
        CoursesApi.updateCourse(course.id, UpdateCourse(homePage = "wiki"))

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Select course: '${course.name}'.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG, "Click on 'Home' label to select the course home page, which is currently the '$pagePublishedFront' Front Page.")
        courseBrowserPage.selectHome()

        val bookmarkName = "Home Front Page Bookmark"
        Log.d(STEP_TAG, "Add a new bookmark with name: '$bookmarkName'")
        assignmentDetailsPage.addBookmark(bookmarkName)

        Log.d(STEP_TAG, "Navigate back to the Dashboard page.")
        pressBackButton(2)

        Log.d(STEP_TAG, "Click on the 'Bookmarks' menu within the left side menu to open the Bookmarks page.")
        leftSideNavigationDrawerPage.clickBookmarksMenu()

        Log.d(ASSERTION_TAG, "Assert if the newly created bookmark has displayed.")
        bookmarkPage.assertBookmarkDisplayed(bookmarkName)

        Log.d(STEP_TAG, "Click on the previously created '$bookmarkName' bookmark.")
        bookmarkPage.clickBookmark(bookmarkName)

        Log.d(ASSERTION_TAG, "Assert if it's navigating to the '$pagePublishedFront' Front Page.")
        canvasWebViewPage.runTextChecks(WebViewTextCheck(Locator.ID, "header1", "Front Page Text"))
    }

    @E2E
    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.BOOKMARKS, TestCategory.E2E)
    fun testBookmarkAddToHomeScreenE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Preparing an assignment which will be saved as a bookmark.")
        val assignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to assignments page and click on the prepared assignment: '${assignment.name}'.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAssignments()
        assignmentListPage.clickAssignment(assignment)

        val bookmarkName = "HomeScreen Bookmark"
        Log.d(STEP_TAG, "Add a new bookmark with name: '$bookmarkName'")
        assignmentDetailsPage.addBookmark(bookmarkName)

        Log.d(STEP_TAG, "Navigate back to Dashboard page.")
        pressBackButton(3)

        Log.d(STEP_TAG, "Click on the 'Bookmarks' menu within the left side menu to open the Bookmarks page.")
        leftSideNavigationDrawerPage.clickBookmarksMenu()

        Log.d(ASSERTION_TAG, "Assert if the newly created bookmark: '$bookmarkName' has displayed.")
        bookmarkPage.assertBookmarkDisplayed(bookmarkName)

        Log.d(STEP_TAG, "Click on 'Add to Home Screen' option for the bookmark: '$bookmarkName'.")
        bookmarkPage.addBookmarkToHomeScreen(bookmarkName, device)

        Log.d(ASSERTION_TAG, "Assert that we're still on the bookmarks page after adding the bookmark: '$bookmarkName' to home screen.")
        bookmarkPage.assertBookmarkDisplayed(bookmarkName)

        Log.d(STEP_TAG, "Press 'Home' button to go to home screen.")
        device.pressHome()

        Log.d(STEP_TAG, "Find and click the '$bookmarkName' shortcut on the home screen.")
        device.findObject(UiSelector().textContains(bookmarkName)).click()

        Log.d(ASSERTION_TAG, "Assert that the bookmark shortcut opened the correct assignment: '${assignment.name}' with all details.")
        assignmentDetailsPage.assertAssignmentTitle(assignment.name)
        assignmentDetailsPage.assertAssignmentDetails(assignment)

        Log.d(STEP_TAG, "Navigate back to the system home screen.")
        device.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the app returned to the system home screen by verifying the bookmark shortcut is visible.")
        bookmarkPage.assertBookmarkShortcutVisibleOnHomeScreen(bookmarkName, device)
    }

}