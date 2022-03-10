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
package com.instructure.student.ui.e2e.k5

import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.refresh
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toDate
import com.instructure.dataseeding.api.AssignmentsApi
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

    override fun enableAndConfigureAccessibilityChecks() {
        //We dont want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.E2E)
    fun importantDatesE2ETest() {

        // Seed data for K5 sub-account
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

        val testAssignment1 = AssignmentsApi.createAssignment(
            AssignmentsApi.CreateAssignmentRequest(
                courseId = elementaryCourse1.id,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                gradingType = GradingType.POINTS,
                teacherToken = teacher.token,
                pointsPossible = 100.0,
                dueAt = 3.days.fromNow.iso8601,
                importantDate = true
            )
        )

        val testAssignment2 = AssignmentsApi.createAssignment(
            AssignmentsApi.CreateAssignmentRequest(
                courseId = elementaryCourse2.id,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                gradingType = GradingType.POINTS,
                teacherToken = teacher.token,
                pointsPossible = 100.0,
                dueAt = 4.days.fromNow.iso8601,
                importantDate = true
            )
        )

        val testAssignment3 = AssignmentsApi.createAssignment(
            AssignmentsApi.CreateAssignmentRequest(
                courseId = elementaryCourse3.id,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                gradingType = GradingType.POINTS,
                teacherToken = teacher.token,
                pointsPossible = 100.0,
                dueAt = 4.days.fromNow.iso8601,
                importantDate = true
            )
        )

        val testNotImportantAssignment = AssignmentsApi.createAssignment(
            AssignmentsApi.CreateAssignmentRequest(
                courseId = elementaryCourse4.id,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                gradingType = GradingType.POINTS,
                teacherToken = teacher.token,
                pointsPossible = 100.0,
                dueAt = 4.days.fromNow.iso8601,
                importantDate = false
            )
        )

        // Sign in with elementary (K5) student
        tokenLoginElementary(student)
        elementaryDashboardPage.waitForRender()
        elementaryDashboardPage.selectTab(ElementaryDashboardPage.ElementaryTabType.IMPORTANT_DATES)
        importantDatesPage.assertPageObjects()

        //Assert that the important date assignment are displayed and the 'not' important is not displayed.
        importantDatesPage.assertItemDisplayed(testAssignment1.name)
        importantDatesPage.assertItemDisplayed(testAssignment2.name)
        importantDatesPage.assertItemDisplayed(testAssignment3.name)
        importantDatesPage.assertItemNotDisplayed(testNotImportantAssignment.name)

        //Assert that the count of the items and the day strings are correct on the Important Dates page.
        importantDatesPage.assertRecyclerViewItemCount(5) // We count both day texts and calendar events here, since both types are part of the recyclerView.
        importantDatesPage.assertDayTextIsDisplayed(generateDayString(testAssignment1.dueAt.toDate()))
        importantDatesPage.assertDayTextIsDisplayed(generateDayString(testAssignment2.dueAt.toDate()))

        //Opening a calendar assignment event.
        importantDatesPage.clickImportantDatesItem(testAssignment1.name)
        assignmentDetailsPage.verifyAssignmentTitle(testAssignment1.name)

        //Navigate back to Important Dates page.
        Espresso.pressBack()
        importantDatesPage.assertPageObjects()

        //Refresh the Important Dates page and see that the corresponding items and lables are still displayed after the refresh.
        importantDatesPage.pullToRefresh()

        importantDatesPage.assertItemDisplayed(testAssignment1.name)
        importantDatesPage.assertItemDisplayed(testAssignment2.name)
        importantDatesPage.assertItemDisplayed(testAssignment3.name)
        importantDatesPage.assertItemNotDisplayed(testNotImportantAssignment.name)

        //Assert that the count of the items and the day strings are correct on the Important Dates page after the refresh.
        importantDatesPage.assertRecyclerViewItemCount(5) // We count both day texts and calendar events here, since both types are part of the recyclerView.
        importantDatesPage.assertDayTextIsDisplayed(generateDayString(testAssignment1.dueAt.toDate()))
        importantDatesPage.assertDayTextIsDisplayed(generateDayString(testAssignment2.dueAt.toDate()))

    }

    private fun generateDayString(date: Date?): String {
        return SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(date)
    }
}

