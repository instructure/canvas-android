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
import com.instructure.canvasapi2.utils.toDate
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.pages.ElementaryDashboardPage
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedDataForK5
import com.instructure.student.ui.utils.tokenLoginElementary
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

@HiltAndroidTest
class ImportantDatesE2ETest : StudentTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.K5_DASHBOARD, TestCategory.E2E)
    fun importantDatesE2ETest() {

        Log.d(PREPARATION_TAG,"Seeding data for K5 sub-account.")
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

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' IMPORTANT assignment for ${elementaryCourse1.name} course.")
        val testAssignment1 = createAssignment(elementaryCourse1.id,teacher, GradingType.POINTS, 100.0, 3.days.fromNow.iso8601, importantDate = true)

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' IMPORTANT assignment for ${elementaryCourse2.name} course.")
        val testAssignment2 = createAssignment(elementaryCourse2.id,teacher, GradingType.POINTS, 100.0, 4.days.fromNow.iso8601, importantDate = true)

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' IMPORTANT assignment for ${elementaryCourse3.name} course.")
        val testAssignment3 = createAssignment(elementaryCourse3.id,teacher, GradingType.POINTS, 100.0, 4.days.fromNow.iso8601, importantDate = true)

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' NOT IMPORTANT assignment for ${elementaryCourse4.name} course.")
        val testNotImportantAssignment = createAssignment(elementaryCourse4.id,teacher, GradingType.POINTS, 100.0, 4.days.fromNow.iso8601, importantDate = false)

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLoginElementary(student)
        elementaryDashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to K5 Important Dates Page and assert it is loaded.")
        elementaryDashboardPage.selectTab(ElementaryDashboardPage.ElementaryTabType.IMPORTANT_DATES)
        importantDatesPage.assertPageObjects()

        Log.d(STEP_TAG, "Assert that the important date assignments are displayed and the 'not' important (${testNotImportantAssignment.name}) is not displayed.")
        importantDatesPage.assertItemDisplayed(testAssignment1.name)
        importantDatesPage.assertItemDisplayed(testAssignment2.name)
        importantDatesPage.assertItemDisplayed(testAssignment3.name)
        importantDatesPage.assertItemNotDisplayed(testNotImportantAssignment.name)

        Log.d(STEP_TAG, "Assert that the count of the items (5) and the day strings are correct on the Important Dates page.")
        importantDatesPage.assertRecyclerViewItemCount(5) // We count both day texts and calendar events here, since both types are part of the recyclerView.
        importantDatesPage.assertDayTextIsDisplayed(generateDayString(testAssignment1.dueAt.toDate()))
        importantDatesPage.assertDayTextIsDisplayed(generateDayString(testAssignment2.dueAt.toDate()))

        Log.d(STEP_TAG, "Opening ${testAssignment1.name} important date assignment's event. Assert that the Assignment Details Page is displayed.")
        importantDatesPage.clickImportantDatesItem(testAssignment1.name)
        assignmentDetailsPage.assertAssignmentTitle(testAssignment1.name)

        Log.d(STEP_TAG, "Navigate back to Important Dates page.")
        Espresso.pressBack()
        importantDatesPage.assertPageObjects()

        Log.d(STEP_TAG, "Refresh the Important Dates page. Assert that the corresponding items" +
                "(all the assignments, except ${testNotImportantAssignment.name} named assignment) and their labels are still displayed after the refresh.")
        importantDatesPage.pullToRefresh()
        importantDatesPage.assertItemDisplayed(testAssignment1.name)
        importantDatesPage.assertItemDisplayed(testAssignment2.name)
        importantDatesPage.assertItemDisplayed(testAssignment3.name)
        importantDatesPage.assertItemNotDisplayed(testNotImportantAssignment.name)

        Log.d(STEP_TAG, "Assert that the count of the items (5) and the day strings are correct on the Important Dates page after the refresh.")
        importantDatesPage.assertRecyclerViewItemCount(5) // We count both day texts and calendar events here, since both types are part of the recyclerView.
        importantDatesPage.assertDayTextIsDisplayed(generateDayString(testAssignment1.dueAt.toDate()))
        importantDatesPage.assertDayTextIsDisplayed(generateDayString(testAssignment2.dueAt.toDate()))

    }

    private fun generateDayString(date: Date?): String {
        return SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(date)
    }

    private fun createAssignment(
        courseId: Long,
        teacher: CanvasUserApiModel,
        gradingType: GradingType,
        pointsPossible: Double,
        dueAt: String,
        importantDate: Boolean
    ): AssignmentApiModel {
        return AssignmentsApi.createAssignment(
            AssignmentsApi.CreateAssignmentRequest(
                courseId = courseId,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                gradingType = gradingType,
                teacherToken = teacher.token,
                pointsPossible = pointsPossible,
                dueAt = dueAt,
                importantDate = importantDate
            )
        )
    }
}

