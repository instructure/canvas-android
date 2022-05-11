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
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.GradingPeriodsApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.espresso.page.getStringFromResource
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
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
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enableAndConfigureAccessibilityChecks() {
        //We dont want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.K5_DASHBOARD, TestCategory.E2E)
    fun gradesE2ETest() {

        // Seed data for K5 sub-account
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

        val testAssignment = AssignmentsApi.createAssignment(
             AssignmentsApi.CreateAssignmentRequest(
                 courseId = nonHomeroomCourses[1].id,
                 submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                 gradingType = GradingType.PERCENT,
                 teacherToken = teacher.token,
                 pointsPossible = 100.0,
                 dueAt = calendar.time.toApiString()
             )
         )

         val testAssignment2 = AssignmentsApi.createAssignment(
            AssignmentsApi.CreateAssignmentRequest(
                courseId = nonHomeroomCourses[0].id,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                gradingType = GradingType.LETTER_GRADE,
                teacherToken = teacher.token,
                pointsPossible = 100.0,
                dueAt = calendar.time.toApiString()
            )
        )

        SubmissionsApi.gradeSubmission(
            teacherToken = teacher.token,
            courseId = nonHomeroomCourses[1].id,
            assignmentId = testAssignment.id,
            studentId = student.id,
            postedGrade="9",
            excused = false)

        SubmissionsApi.gradeSubmission(
            teacherToken = teacher.token,
            courseId = nonHomeroomCourses[0].id,
            assignmentId = testAssignment2.id,
            studentId = student.id,
            postedGrade="A-",
            excused = false)

        tokenLoginElementary(student)
        elementaryDashboardPage.waitForRender()
        elementaryDashboardPage.selectTab(ElementaryDashboardPage.ElementaryTabType.GRADES)
        gradesPage.assertPageObjects()
        Thread.sleep(3000)
        gradesPage.assertCourseShownWithGrades(nonHomeroomCourses[0].name, "93%")
        gradesPage.assertCourseShownWithGrades(nonHomeroomCourses[1].name, "9%")
        gradesPage.assertCourseShownWithGrades(nonHomeroomCourses[2].name, "Not Graded")

        SubmissionsApi.gradeSubmission(
            teacherToken = teacher.token,
            courseId = nonHomeroomCourses[0].id,
            assignmentId = testAssignment2.id,
            studentId = student.id,
            postedGrade="C-",
            excused = false)

        Thread.sleep(5000) //This time is needed here to let the SubMissionApi does it's job.
        gradesPage.refresh()
        Thread.sleep(5000) //We need to wait here because sometimes if we refresh the page fastly, the old grade will be seen.
        gradesPage.assertCourseShownWithGrades(nonHomeroomCourses[0].name, "73%")
        gradesPage.assertCourseShownWithGrades(nonHomeroomCourses[1].name, "9%")

        //Changing grade period.
        gradesPage.assertSelectedGradingPeriod(gradesPage.getStringFromResource(R.string.currentGradingPeriod))
        gradesPage.scrollToItem(R.id.gradingPeriodSelector, gradesPage.getStringFromResource(R.string.currentGradingPeriod))
        gradesPage.clickGradingPeriodSelector()
        gradesPage.selectGradingPeriod(testGradingPeriodListApiModel.gradingPeriods[0].title)

        //Checking if a course's grades page is displayed after clicking on a course row on elementary grades page. Assert that we have left the grades elementary page. We are asserting this because in beta environment, subject page's not always available for k5 user.
        gradesPage.clickGradeRow(nonHomeroomCourses[0].name)
        gradesPage.assertCourseNotDisplayed(nonHomeroomCourses[0].name)

        Espresso.pressBack()
        gradesPage.assertPageObjects()

    }
}

