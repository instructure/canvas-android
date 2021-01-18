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
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addSubmissionForAssignment
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertGone
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.tokenLogin
import org.junit.Test

class SpeedGraderGradePageTest : TeacherTest() {

    @Test
    override fun displaysPageObjects() {
        goToSpeedGraderGradePage()
        speedGraderGradePage.assertPageObjects()
    }

    @Test
    fun displaysGradeDialog() {
        goToSpeedGraderGradePage()
        speedGraderGradePage.openGradeDialog()
        speedGraderGradePage.assertGradeDialog()
        speedGraderGradePage.excuseStudentCheckbox.assertGone()
    }

    @Test
    fun displaysNewGrade() {
        if(isLowResDevice()) {
            // We don't want to run accessibility tests on this device, because it's impossible to
            // make all touch targets in the openGradeDialog 48dp high
            Log.v("SkippedTest", "SpeedGraderGradePageTest.displaysNewGrade skipped due to low resolution")
            return
        }
        goToSpeedGraderGradePage()
        speedGraderGradePage.openGradeDialog()
        val grade = "19"
        speedGraderGradePage.enterNewGrade(grade)
        speedGraderGradePage.assertHasGrade(grade)
    }

    @Test
    fun hidesRubricWhenMissing() {
        goToSpeedGraderGradePage()
        speedGraderGradePage.assertRubricHidden()
    }

    private fun goToSpeedGraderGradePage() {
        val data = MockCanvas.init(teacherCount = 1, courseCount = 1, favoriteCourseCount = 1, studentCount = 1)
        val teacher = data.teachers[0]
        val student = data.students[0]
        val course = data.courses.values.first()

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        val assignment = data.addAssignment(
                courseId = course.id,
                submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
                pointsPossible = 20,
                gradingType = "points"
        )

        val submission = data.addSubmissionForAssignment(
                assignmentId = assignment.id,
                userId = student.id,
                type = "online_text_entry"
        )

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)
        coursesListPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.openSubmissionsPage()
        assignmentSubmissionListPage.clickSubmission(student)
        speedGraderPage.selectGradesTab()
    }
}
