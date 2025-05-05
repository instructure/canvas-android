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
import com.instructure.canvas.espresso.StubMultiAPILevel
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addRubricToAssignment
import com.instructure.canvas.espresso.mockCanvas.addSubmissionForAssignment
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.canvasapi2.models.RubricCriterionRating
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class SpeedGraderGradePageTest : TeacherComposeTest() {


    @Test
    override fun displaysPageObjects() {
        goToSpeedGraderGradePage()
        speedGraderGradePage.assertPageObjects()
    }

    @Test
    fun correctViewsForPointGradedWithoutRubric() {
        val possiblePoint = 20
        goToSpeedGraderGradePage(gradingType = "points", pointsPossible = possiblePoint)
        speedGraderGradePage.assertSliderVisible()
        speedGraderGradePage.assertSliderMinValue("0")
        speedGraderGradePage.assertSliderMaxValue(possiblePoint.toString())
        speedGraderGradePage.assertRubricHidden()
        speedGraderGradePage.openGradeDialog()
        speedGraderGradePage.assertGradeDialog()
        speedGraderGradePage.assertCheckboxHidden()
    }

    @Test
    fun correctViewsForPercentageGradedWithoutRubric() {
        goToSpeedGraderGradePage("percent")
        speedGraderGradePage.assertSliderVisible()
        speedGraderGradePage.assertSliderMinValue("0%")
        speedGraderGradePage.assertSliderMaxValue("100%")
        speedGraderGradePage.assertRubricHidden()
        speedGraderGradePage.openGradeDialog()
        speedGraderGradePage.assertGradeDialog()
        speedGraderGradePage.assertCheckboxHidden()
    }

    @Test
    fun correctViewsForPointGradedWithRubric() {
        goToSpeedGraderGradePage("points", true)
        speedGraderGradePage.assertSliderHidden()
        speedGraderGradePage.assertRubricVisible()
        speedGraderGradePage.openGradeDialog()
        speedGraderGradePage.assertGradeDialog()
        speedGraderGradePage.assertCheckboxVisible()
    }

    @Test
    fun correctViewsForPercentageGradedWithRubric() {
        goToSpeedGraderGradePage("percent", true)
        speedGraderGradePage.assertSliderHidden()
        speedGraderGradePage.assertRubricVisible()
        speedGraderGradePage.openGradeDialog()
        speedGraderGradePage.assertGradeDialog()
        speedGraderGradePage.assertCheckboxVisible()
    }

    @Test
    fun correctViewsForPassFailAssignment() {
        goToSpeedGraderGradePage("pass_fail")
        speedGraderGradePage.assertSliderHidden()
        speedGraderGradePage.openGradeDialog()
        speedGraderGradePage.assertGradeDialog()
        speedGraderGradePage.assertCheckboxVisible()
    }

    @Test
    fun correctViewsForLetterGradeAssignment() {
        goToSpeedGraderGradePage("letter_grade")
        speedGraderGradePage.assertSliderHidden()
        speedGraderGradePage.openGradeDialog()
        speedGraderGradePage.assertGradeDialog()
        speedGraderGradePage.assertCheckboxVisible()
    }

    @Test
    fun correctViewsForGpaScaleAssignment() {
        goToSpeedGraderGradePage("gpa_scale")
        speedGraderGradePage.assertSliderHidden()
        speedGraderGradePage.openGradeDialog()
        speedGraderGradePage.assertGradeDialog()
        speedGraderGradePage.assertCheckboxVisible()
    }

    @Test
    fun displaysGradeDialog() {
        goToSpeedGraderGradePage()
        speedGraderGradePage.openGradeDialog()
        speedGraderGradePage.assertGradeDialog()
    }

    @Test
    fun displaysNewGrade() {
        if (isLowResDevice()) {
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

    @Test
    @StubMultiAPILevel("Failed API levels = { 27, 28, 29 }")
    fun overgradePointAssignment() {
        val pointsPossible = 20
        goToSpeedGraderGradePage(pointsPossible = pointsPossible)
        speedGraderGradePage.openGradeDialog()
        val grade = 28.6
        speedGraderGradePage.enterNewGrade(grade.toString())
        speedGraderGradePage.assertHasGrade(grade.toString())
        speedGraderGradePage.assertHasOvergradeWarning(grade - pointsPossible)
        speedGraderGradePage.assertSliderMaxValue(grade.toInt().toString())
    }

    @Test
    fun excuseStudent() {
        goToSpeedGraderGradePage()
        speedGraderPage.swipeUpGradesTab()
        speedGraderGradePage.assertExcuseButtonEnabled()
        speedGraderGradePage.clickExcuseStudentButton()
        speedGraderGradePage.assertStudentExcused()
        speedGraderGradePage.assertExcuseButtonDisabled()
    }

    @Test
    @StubMultiAPILevel("Failed API levels = { 27, 28, 29 }")
    fun clearGrade() {
        goToSpeedGraderGradePage()
        speedGraderPage.swipeUpGradesTab()
        speedGraderGradePage.assertNoGradeButtonDisabled()
        speedGraderGradePage.openGradeDialog()
        speedGraderGradePage.enterNewGrade("15")
        speedGraderGradePage.assertNoGradeButtonEnabled()
        speedGraderGradePage.clickNoGradeButton()
        speedGraderGradePage.assertNoGradeButtonDisabled()
        speedGraderGradePage.assertHasNoGrade()
    }

    private fun goToSpeedGraderGradePage(gradingType: String = "points", hasRubric: Boolean = false, pointsPossible: Int = 20) {
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
                submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY),
                pointsPossible = pointsPossible,
                gradingType = gradingType
        )

        if (hasRubric) {
            val rubricCriterion = RubricCriterion(
                    id = data.newItemId().toString(),
                    description = "Description of criterion",
                    longDescription = "0, 3, 7 or 10 points",
                    points = 10.0,
                    ratings = mutableListOf(
                            RubricCriterionRating(id = "1", points = 0.0, description = "No Marks", longDescription = "Really?"),
                            RubricCriterionRating(id = "2", points = 3.0, description = "Meh", longDescription = "You're better than this!"),
                            RubricCriterionRating(id = "3", points = 7.0, description = "Passable", longDescription = "Getting there!"),
                            RubricCriterionRating(id = "4", points = 10.0, description = "Full Marks", longDescription = "Way to go!")
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
        dashboardPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.clickAllSubmissions()
        assignmentSubmissionListPage.clickSubmission(student)
        speedGraderPage.selectGradesTab()
    }
}
