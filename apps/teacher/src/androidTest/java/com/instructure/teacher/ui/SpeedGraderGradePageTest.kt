/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.ui

import android.util.Log
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.SubmissionType.ONLINE_TEXT_ENTRY
import com.instructure.teacher.ui.utils.*
import com.instructure.espresso.ditto.Ditto
import org.junit.Test

class SpeedGraderGradePageTest : TeacherTest() {

    @Test
    @Ditto
    override fun displaysPageObjects() {
        goToSpeedGraderGradePage()
        speedGraderGradePage.assertPageObjects()
    }

    @Test
    @Ditto
    fun displaysGradeDialog() {
        goToSpeedGraderGradePage()
        speedGraderGradePage.openGradeDialog()
        speedGraderGradePage.assertGradeDialog()
    }

    @Test
    @Ditto
    fun displaysNewGrade() {
        if(isLowResDevice()) {
            // We don't want to run accessibility tests on this device, because it's impossible to
            // make all touch targets in the openGradeDialog 48dp high
            Log.v("SkippedTest", "SpeedGraderGradePageTest.displaysNewGrade skipped due to low resolution")
            return
        }
        goToSpeedGraderGradePage()
        speedGraderGradePage.openGradeDialog()
        val grade = "20"
        speedGraderGradePage.enterNewGrade(grade)
        speedGraderGradePage.assertHasGrade(grade)
    }

    @Test
    @Ditto
    fun hidesRubricWhenMissing() {
        goToSpeedGraderGradePage()
        speedGraderGradePage.assertRubricHidden()
    }

    private fun goToSpeedGraderGradePage() {
        val data = seedData(teachers = 1, courses = 1, students = 1, favoriteCourses = 1)
        val teacher = data.teachersList[0]
        val student = data.studentsList[0]
        val course = data.coursesList[0]
        val assignment = seedAssignments(
                course.id,
                submissionTypes = listOf(ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token).assignmentList[0]

        seedAssignmentSubmission(
                listOf(
                        SubmissionsApi.SubmissionSeedInfo(submissionType = ONLINE_TEXT_ENTRY, amount = 1)
                ), assignment.id, course.id, student.token)

        tokenLogin(teacher)
        coursesListPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.openSubmissionsPage()
        assignmentSubmissionListPage.clickSubmission(student)
        speedGraderPage.selectGradesTab()
    }
}
