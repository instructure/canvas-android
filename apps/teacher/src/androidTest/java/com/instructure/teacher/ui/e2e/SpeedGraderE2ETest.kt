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
 *
 */
package com.instructure.teacher.ui.e2e

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.refresh
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.*
import org.junit.Test

class SpeedGraderE2ETest: TeacherTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.GRADES, TestCategory.E2E)
    fun testSpeedGraderE2E() {
        val data = seedData(1, 1,1,1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student = data.studentsList[0]

        val assignment = seedAssignments(
                courseId = course.id,
                dueAt = 1.days.fromNow.iso8601,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token,
                pointsPossible = 15.0
        )
        val sub = seedAssignmentSubmission(
                submissionSeeds = listOf(SubmissionsApi.SubmissionSeedInfo(
                        amount = 1,
                        submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                )),
                assignmentId = assignment[0].id,
                courseId = course.id,
                studentToken = student.token
        )

        tokenLogin(teacher)
        /*
        coursesListPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        these can fail on landscape mode because cardView is not displayed for at least 90%
        */
        onView(withText(course.name)).perform(withCustomConstraints(click(), isDisplayingAtLeast(10)))
        onView(withText("Assignments")).perform(withCustomConstraints(click(), isDisplayingAtLeast(10)))

        assignmentListPage.clickAssignment(assignment[0])
        assignmentDetailsPage.openSubmissionsPage()
        assignmentSubmissionListPage.clickSubmission(student)
        speedGraderPage.assertDisplaysTextSubmissionView()
        speedGraderPage.selectGradesTab()
        speedGraderGradePage.openGradeDialog()
        val grade = "10"
        speedGraderGradePage.enterNewGrade(grade)
        speedGraderGradePage.assertHasGrade(grade)
        speedGraderGradePage.navigateBack()
        refresh()
        assignmentSubmissionListPage.assertStudentHasGrade(grade)
        assignmentSubmissionListPage.clickFilterButton()
        assignmentSubmissionListPage.clickFilterSubmissions()
        //assignmentSubmissionListPage.clickFilterUngraded()
        onView(withText(R.string.not_graded)).perform(withCustomConstraints(click(), isDisplayingAtLeast(10)))
        assignmentSubmissionListPage.clickFilterDialogOk()
        assignmentSubmissionListPage.assertHasNoSubmission()
    }
}