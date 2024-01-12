/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.GradingPeriodsApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.espresso.page.getStringFromResource
import com.instructure.student.R
import com.instructure.student.ui.pages.ElementaryDashboardPage
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedDataForK5
import com.instructure.student.ui.utils.tokenLoginElementary
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.util.*

@HiltAndroidTest
class GradesElementaryE2ETest : StudentTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.K5_DASHBOARD, TestCategory.E2E)
    fun gradesE2ETest() {

        Log.d(PREPARATION_TAG,"Seeding data for K5 sub-account.")
        val data = seedDataForK5(
            teachers = 1,
            students = 1,
            courses = 4,
            homeroomCourses = 1,
            announcements = 3,
            gradingPeriods = true
        )

        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val nonHomeroomCourses = data.coursesList.filter { !it.homeroomCourse }
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val testGradingPeriodListApiModel = GradingPeriodsApi.getGradingPeriodsOfCourse(nonHomeroomCourses[0].id)

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for ${nonHomeroomCourses[1].name} course.")
        val testAssignment = createAssignment(nonHomeroomCourses[1].id, teacher, calendar, GradingType.PERCENT, 100.0)

        Log.d(PREPARATION_TAG,"Seeding another 'Text Entry' assignment for ${nonHomeroomCourses[0].name} course.")
        val testAssignment2 = createAssignment(nonHomeroomCourses[0].id,  teacher, calendar, GradingType.LETTER_GRADE, 100.0)

        Log.d(PREPARATION_TAG,"Grade the previously seeded submission for ${nonHomeroomCourses[1].name} assignment.")
        gradeSubmission(teacher,nonHomeroomCourses[1].id, student, testAssignment.id, "9")

        Log.d(PREPARATION_TAG,"Grade the previously seeded submission for ${nonHomeroomCourses[0].name} assignment.")
        gradeSubmission(teacher, nonHomeroomCourses[0].id, student, testAssignment2.id, "A-")

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLoginElementary(student)
        elementaryDashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to K5 Grades Page and assert it is loaded.")
        elementaryDashboardPage.selectTab(ElementaryDashboardPage.ElementaryTabType.GRADES)
        gradesPage.assertPageObjects()
        Thread.sleep(3000)

        Log.d(STEP_TAG, "Assert that the corresponding course grades are displayed.")
        gradesPage.assertCourseShownWithGrades(nonHomeroomCourses[0].name, "93%")
        gradesPage.assertCourseShownWithGrades(nonHomeroomCourses[1].name, "9%")
        gradesPage.assertCourseShownWithGrades(nonHomeroomCourses[2].name, "Not Graded")

        Log.d(PREPARATION_TAG,"Grade the previously seeded submission for ${testAssignment2.name} assignment.")
        gradeSubmission(teacher,nonHomeroomCourses[0].id, student, testAssignment2.id, "C-")

        Thread.sleep(5000) //This time is needed here to let the SubMissionApi does it's job.

        Log.d(STEP_TAG, "Refresh Grades Elementary Page. Assert that the previously graded, ${testAssignment2.name} assignment's grade has been changed, but only that one.")
        gradesPage.refresh()
        Thread.sleep(5000) //We need to wait here because sometimes if we refresh the page fastly, the old grade will be seen.
        gradesPage.assertCourseShownWithGrades(nonHomeroomCourses[0].name, "73%")
        gradesPage.assertCourseShownWithGrades(nonHomeroomCourses[1].name, "9%")

        Log.d(STEP_TAG, "Change 'Current Grading Period' to '${testGradingPeriodListApiModel.gradingPeriods[0].title}'.")
        gradesPage.assertSelectedGradingPeriod(gradesPage.getStringFromResource(R.string.currentGradingPeriod))
        gradesPage.scrollToItem(R.id.gradingPeriodSelector, gradesPage.getStringFromResource(R.string.currentGradingPeriod))
        gradesPage.clickGradingPeriodSelector()
        gradesPage.selectGradingPeriod(testGradingPeriodListApiModel.gradingPeriods[0].title)

        Log.d(STEP_TAG, "Checking if a course's grades page is displayed after clicking on a course row on elementary grades page. Assert that we have left the grades elementary page. We are asserting this because in beta environment, subject page's not always available for k5 user.")
        gradesPage.clickGradeRow(nonHomeroomCourses[0].name)
        gradesPage.assertCourseNotDisplayed(nonHomeroomCourses[0].name)

        Log.d(STEP_TAG, "Navigate back to Grades Elementary Page and assert it's displayed.")
        Espresso.pressBack()
        gradesPage.assertPageObjects()

    }

    private fun createAssignment(
        courseId: Long,
        teacher: CanvasUserApiModel,
        calendar: Calendar,
        gradingType: GradingType,
        pointsPossible: Double
    ): AssignmentApiModel {
        return AssignmentsApi.createAssignment(
            AssignmentsApi.CreateAssignmentRequest(
                courseId = courseId,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                gradingType = gradingType,
                teacherToken = teacher.token,
                pointsPossible = pointsPossible,
                dueAt = calendar.time.toApiString()
            )
        )
    }

    private fun gradeSubmission(
        teacher: CanvasUserApiModel,
        courseId: Long,
        student: CanvasUserApiModel,
        assignmentId: Long,
        postedGrade: String
    ) {
        SubmissionsApi.gradeSubmission(
            teacherToken = teacher.token,
            courseId = courseId,
            assignmentId = assignmentId,
            studentId = student.id,
            postedGrade = postedGrade,
            excused = false
        )
    }
}

