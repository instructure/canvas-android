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
import com.instructure.canvas.espresso.mockCanvas.*
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.canvasapi2.models.RubricCriterionRating
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
    fun correctViewsForPointGradedWithoutRubric() {
        goToSpeedGraderGradePage("points")
        speedGraderGradePage.assertSliderVisible()
        speedGraderGradePage.assertRubricHidden()
    }

    @Test
    fun correctViewsForPercentageGradedWithoutRubric() {
        goToSpeedGraderGradePage("percent")
        speedGraderGradePage.assertSliderVisible()
        speedGraderGradePage.assertRubricHidden()
    }

    @Test
    fun correctViewsForPointGradedWithRubric() {
        goToSpeedGraderGradePage("points", true)
        speedGraderGradePage.assertSliderHidden()
        speedGraderGradePage.assertRubricVisible()
    }

    @Test
    fun correctViewsForPercentageGradedWithRubric() {
        goToSpeedGraderGradePage("percent", true)
        speedGraderGradePage.assertSliderHidden()
        speedGraderGradePage.assertRubricVisible()
    }

    @Test
    fun sliderGoneForPassFailAssignment() {
        goToSpeedGraderGradePage("pass_fail")
        speedGraderGradePage.assertSliderHidden()
    }

    @Test
    fun sliderGoneForLetterGradeAssignment() {
        goToSpeedGraderGradePage("letter_grade")
        speedGraderGradePage.assertSliderHidden()
    }

    @Test
    fun sliderGoneForGpaScaleAssignment() {
        goToSpeedGraderGradePage("gpa_scale")
        speedGraderGradePage.assertSliderHidden()
    }

    @Test
    fun displaysGradeDialog() {
        goToSpeedGraderGradePage()
        speedGraderGradePage.openGradeDialog()
        speedGraderGradePage.assertGradeDialog()
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

    private fun goToSpeedGraderGradePage(gradingType: String = "points", hasRubric: Boolean = false) {
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
                gradingType = gradingType
        )

        if (hasRubric) {
            val rubricCriterion = RubricCriterion(
                    id = data.newItemId().toString(),
                    description = "Description of criterion",
                    longDescription = "0, 3, 7 or 10 points",
                    points = 10.0,
                    ratings = mutableListOf(
                            RubricCriterionRating(id="1",points=0.0,description="No Marks", longDescription = "Really?"),
                            RubricCriterionRating(id="2",points=3.0,description="Meh", longDescription = "You're better than this!"),
                            RubricCriterionRating(id="3",points=7.0,description="Passable", longDescription = "Getting there!"),
                            RubricCriterionRating(id="4",points=10.0,description="Full Marks", longDescription = "Way to go!")
                    )
            )
            data.addRubricToAssignment(assignment.id, listOf(rubricCriterion))
        }

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
