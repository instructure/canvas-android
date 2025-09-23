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
 *
 */
package com.instructure.student.ui.e2e.k5

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvasapi2.utils.toDate
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.student.ui.pages.ElementaryDashboardPage
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.seedDataForK5
import com.instructure.student.ui.utils.tokenLoginElementary
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltAndroidTest
class ImportantDatesE2ETest : StudentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.E2E, SecondaryFeatureCategory.IMPORTANT_DATES)
    fun importantDatesE2ETest() {

        Log.d(PREPARATION_TAG, "Seeding data for K5 sub-account.")
        val data = seedDataForK5(
            teachers = 1,
            students = 1,
            courses = 4,
            homeroomCourses = 4
        )

        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val elementaryCourse1 = data.coursesList[0]
        val elementaryCourse2 = data.coursesList[1]
        val elementaryCourse3 = data.coursesList[2]
        val elementaryCourse4 = data.coursesList[3]

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' IMPORTANT assignment for '${elementaryCourse1.name}' course.")
        val testAssignment1 = AssignmentsApi.createAssignment(elementaryCourse1.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 100.0, dueAt = 3.days.fromNow.iso8601, importantDate = true)

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' IMPORTANT assignment for '${elementaryCourse2.name}' course.")
        val testAssignment2 = AssignmentsApi.createAssignment(elementaryCourse2.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 100.0, dueAt = 4.days.fromNow.iso8601, importantDate = true)

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' IMPORTANT assignment for '${elementaryCourse3.name}' course.")
        val testAssignment3 = AssignmentsApi.createAssignment(elementaryCourse3.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 100.0, dueAt = 4.days.fromNow.iso8601, importantDate = true)

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' NOT IMPORTANT assignment for '${elementaryCourse4.name}' course.")
        val testNotImportantAssignment = AssignmentsApi.createAssignment(elementaryCourse4.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 100.0, dueAt = 4.days.fromNow.iso8601, importantDate = false)

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLoginElementary(student)
        elementaryDashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to K5 Important Dates Page.")
        elementaryDashboardPage.selectTab(ElementaryDashboardPage.ElementaryTabType.IMPORTANT_DATES)

        Log.d(ASSERTION_TAG, "Assert that the K5 Important Dates Page is displayed correctly.")
        importantDatesPage.assertPageObjects()

        Log.d(ASSERTION_TAG, "Assert that the important date assignments are displayed and the 'not' important one, '${testNotImportantAssignment.name}' is not displayed.")
        importantDatesPage.assertItemDisplayed(testAssignment1.name)
        importantDatesPage.assertItemDisplayed(testAssignment2.name)
        importantDatesPage.assertItemDisplayed(testAssignment3.name)
        importantDatesPage.assertItemNotDisplayed(testNotImportantAssignment.name)

        Log.d(ASSERTION_TAG, "Assert that the count of the items (3) and the day strings are correct on the Important Dates page.")
        importantDatesPage.assertRecyclerViewItemCount(3)
        importantDatesPage.assertDayTextIsDisplayed(generateDayString(testAssignment1.dueAt.toDate()))
        importantDatesPage.assertDayTextIsDisplayed(generateDayString(testAssignment2.dueAt.toDate()))

        Log.d(STEP_TAG, "Opening '${testAssignment1.name}' important date assignment's event.")
        importantDatesPage.clickImportantDatesItem(testAssignment1.name)

        Log.d(ASSERTION_TAG, "Assert that the Assignment Details Page is displayed.")
        assignmentDetailsPage.assertAssignmentTitle(testAssignment1.name)

        Log.d(STEP_TAG, "Navigate back to Important Dates page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the Important Dates Page is displayed correctly.")
        importantDatesPage.assertPageObjects()

        Log.d(STEP_TAG, "Refresh the Important Dates page.")
        importantDatesPage.pullToRefresh()

        Log.d(ASSERTION_TAG, "Assert that the corresponding items (all the assignments, except '${testNotImportantAssignment.name}' named assignment) and their labels are still displayed after the refresh.")
        importantDatesPage.assertItemDisplayed(testAssignment1.name)
        importantDatesPage.assertItemDisplayed(testAssignment2.name)
        importantDatesPage.assertItemDisplayed(testAssignment3.name)
        importantDatesPage.assertItemNotDisplayed(testNotImportantAssignment.name)

        Log.d(ASSERTION_TAG, "Assert that the count of the items (3) and the day strings are correct on the Important Dates page after the refresh.")
        importantDatesPage.assertRecyclerViewItemCount(3)
        importantDatesPage.assertDayTextIsDisplayed(generateDayString(testAssignment1.dueAt.toDate()))
        importantDatesPage.assertDayTextIsDisplayed(generateDayString(testAssignment2.dueAt.toDate()))
    }

    private fun generateDayString(date: Date?): String {
        return SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(date)
    }
}

