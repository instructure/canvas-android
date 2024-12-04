/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.student.ui.interaction

import androidx.compose.ui.platform.ComposeView
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addBookmark
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvas.espresso.refresh
import com.instructure.canvasapi2.models.Assignment
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers
import org.junit.Test

@HiltAndroidTest
class BookmarkInteractionTest : StudentTest() {

    override fun displaysPageObjects() = Unit // Not used for interaction tests

    // Test that we can create a bookmark via the UI and see it in the bookmark list
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.BOOKMARKS, TestCategory.INTERACTION)
    fun testBookmark_create() {
        val data = init()
        val course = data.courses.values.first()
        val assignment = data.assignments.values.first()

        dashboardPage.waitForRender()
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAssignments()
        assignmentListPage.clickAssignment(assignment)
        val bookmarkName = "Bookmark name 1"
        assignmentDetailsPage.addBookmark(bookmarkName)
        Espresso.pressBack() // to assignment list
        Espresso.pressBack() // to course
        Espresso.pressBack() // to main dashboard

        leftSideNavigationDrawerPage.clickBookmarksMenu()
        bookmarkPage.assertBookmarkDisplayed(bookmarkName)
    }

    // Tests that we can click a bookmark and end up in the bookmarked location
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.BOOKMARKS, TestCategory.INTERACTION)
    fun testBookmark_click() {
        val data = init()
        val student = data.students.first()
        val assignment = data.assignments.values.first()
        val bookmarkName = "My Bookmark"

        val bookmark = data.addBookmark(
                user = student,
                assignment = assignment,
                name = bookmarkName
        )

        dashboardPage.waitForRender()
        leftSideNavigationDrawerPage.clickBookmarksMenu()
        bookmarkPage.assertBookmarkDisplayed(bookmarkName)
        bookmarkPage.clickBookmark(bookmarkName)

        assignmentDetailsPage.assertAssignmentDetails(assignment)
    }

    // Tests that we can change the name of a bookmark and still click through to the intended location
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.BOOKMARKS, TestCategory.INTERACTION)
    fun testBookmark_changeName() {
        val data = init()
        val student = data.students.first()
        val assignment = data.assignments.values.first()
        val bookmarkName = "My Bookmark"

        val bookmark = data.addBookmark(
                user = student,
                assignment = assignment,
                name = bookmarkName
        )

        dashboardPage.waitForRender()
        leftSideNavigationDrawerPage.clickBookmarksMenu()
        bookmarkPage.assertBookmarkDisplayed(bookmarkName)
        val newName = "New Bookmark Name"
        bookmarkPage.changeBookmarkName(bookmarkName, newName)
        refresh()
        bookmarkPage.assertBookmarkDisplayed(newName)
        bookmarkPage.clickBookmark(newName)

        assignmentDetailsPage.assertAssignmentDetails(assignment)
    }

    // Common initialization code.  Creates a student, course and assignment, and signs you in.
    private fun init() : MockCanvas {
        val data = MockCanvas.init(courseCount = 1, studentCount = 1, teacherCount = 1)
        val student = data.students.first()
        val course = data.courses.values.first()
        val assignment = data.addAssignment(
                courseId = course.id,
                submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        )

        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)

        return data
    }

    override fun enableAndConfigureAccessibilityChecks() {
        extraAccessibilitySupressions = Matchers.allOf(
            AccessibilityCheckResultUtils.matchesCheck(
                SpeakableTextPresentCheck::class.java
            ),
            AccessibilityCheckResultUtils.matchesViews(
                ViewMatchers.withParent(
                    ViewMatchers.withClassName(
                        Matchers.equalTo(ComposeView::class.java.name)
                    )
                )
            )
        )

        super.enableAndConfigureAccessibilityChecks()
    }

}