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
import com.instructure.canvas.espresso.refresh
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.ViewUtils
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
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
        ViewUtils.pressBackButton(3)
        leftSideNavigationDrawerPage.clickBookmarksMenu()

        Log.d(ASSERTION_TAG, "Assert if the newly created bookmark has displayed.")
        bookmarkPage.assertBookmarkDisplayed(bookmarkName)

        Log.d(STEP_TAG, "Click on '$bookmarkName' bookmark.")
        bookmarkPage.clickBookmark(bookmarkName)

        Log.d(ASSERTION_TAG, "Assert if it's navigating to the assignment details page.")
        assignmentDetailsPage.assertAssignmentTitle(assignment.name)

        Log.d(STEP_TAG, "Navigate back to bookmark page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on the bookmark page's overflow menu.")
        leftSideNavigationDrawerPage.clickBookmarksMenu()

        Log.d(ASSERTION_TAG, "Assert if the bookmark is displayed.")
        bookmarkPage.assertBookmarkDisplayed(bookmarkName)

        val newName = "Assignment Details BM Modified"
        Log.d(STEP_TAG, "Change bookmark's name from '$bookmarkName' to '$newName'.")
        bookmarkPage.changeBookmarkName(bookmarkName, newName)

        Log.d(ASSERTION_TAG, "Refresh bookmark page and assert if the bookmark's name has been changed.")
        refresh()
        bookmarkPage.assertBookmarkDisplayed(newName)

        Log.d(STEP_TAG, "Click on the previously renamed bookmark.")
        bookmarkPage.clickBookmark(newName)

        Log.d(ASSERTION_TAG, "Assert if it's still navigating to the corresponding assignment's details page.")
        assignmentDetailsPage.assertAssignmentTitle(assignment.name)

        Log.d(STEP_TAG, "Navigate back to the bookmark page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on the bookmark page's overflow menu.")
        leftSideNavigationDrawerPage.clickBookmarksMenu()

        Log.d(STEP_TAG, "Delete bookmark: '$newName'.")
        bookmarkPage.deleteBookmark(newName)

        Log.d(ASSERTION_TAG, "Assert that empty view is displayed, so the bookmark has been deleted.")
        bookmarkPage.assertEmptyView()
    }

}