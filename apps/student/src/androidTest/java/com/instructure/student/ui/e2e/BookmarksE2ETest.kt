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
import com.instructure.canvas.espresso.refresh
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

private const val STEP_TAG = "BookmarksE2ETest #STEP# "
private const val PREPARATION_TAG = "BookmarksE2ETest #PREPARATION# "

@HiltAndroidTest
class BookmarksE2ETest : StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.BOOKMARKS, TestCategory.E2E)
    fun testBookmarksE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 2)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Preparing an assignment which will be saved as a bookmark.")
        val assignment = AssignmentsApi.createAssignment(
            AssignmentsApi.CreateAssignmentRequest(
            courseId = course.id,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
            gradingType = GradingType.POINTS,
            teacherToken = teacher.token,
            pointsPossible = 15.0,
            dueAt = 1.days.fromNow.iso8601
        ))

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId} , password: ${student.password}")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to assignments page and click on the prepared assignment.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAssignments()
        assignmentListPage.clickAssignment(assignment)

        val bookmarkName = "Assignment Details BM"
        Log.d(STEP_TAG,"Add a new bookmark with name: $bookmarkName")
        assignmentDetailsPage.addBookmark(bookmarkName)

        Log.d(STEP_TAG,"Navigate back to Bookmarks page and assert if the newly created bookmark has displayed.")
        Espresso.pressBack()
        Espresso.pressBack()
        Espresso.pressBack()
        dashboardPage.gotoBookmarks()
        bookmarkPage.assertBookmarkDisplayed(bookmarkName)

        Log.d(STEP_TAG,"Click on $bookmarkName bookmark and assert if it's navigating to the assignment details page.")
        bookmarkPage.clickBookmark(bookmarkName)
        assignmentDetailsPage.verifyAssignmentTitle(assignment.name)

        Log.d(STEP_TAG,"Navigate back to bookmark page.")
        Espresso.pressBack()
        dashboardPage.gotoBookmarks()
        bookmarkPage.assertBookmarkDisplayed(bookmarkName)

        val newName = "Assignment Details BM Modified"
        Log.d(STEP_TAG,"Change bookmark's name from $bookmarkName to $newName.")
        bookmarkPage.changeBookmarkName(bookmarkName, newName)

        Log.d(STEP_TAG,"Refresh bookmark page and assert if the bookmark's name has been changed.")
        refresh()
        bookmarkPage.assertBookmarkDisplayed(newName)

        Log.d(STEP_TAG,"Click on the previously renamed bookmark and assert if it's still navigating to the corresponding assignment's details page.")
        bookmarkPage.clickBookmark(newName)
        assignmentDetailsPage.verifyAssignmentTitle(assignment.name)

        Log.d(STEP_TAG,"Navigate back to the bookmark page.")
        Espresso.pressBack()
        dashboardPage.gotoBookmarks()

        Log.d(STEP_TAG, "Delete bookmark: $newName.")
        bookmarkPage.deleteBookmark(newName)

        Log.d(STEP_TAG,"Assert that empty view is displayed, so the bookmark has been deleted.")
        bookmarkPage.assertEmptyView()
    }

}