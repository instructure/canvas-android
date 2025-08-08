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
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.StubMultiAPILevel
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addRubricToAssignment
import com.instructure.canvas.espresso.mockCanvas.addSubmissionForAssignment
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeAssignmentDetailsManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeCommentLibraryManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeCustomGradeStatusesManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeInboxSettingsManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeStudentContextManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionCommentsManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionContentManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionDetailsManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionGradeManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionRubricManager
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.di.GraphQlApiModule
import com.instructure.canvasapi2.managers.CommentLibraryManager
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.managers.StudentContextManager
import com.instructure.canvasapi2.managers.SubmissionRubricManager
import com.instructure.canvasapi2.managers.graphql.AssignmentDetailsManager
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.managers.graphql.SubmissionCommentsManager
import com.instructure.canvasapi2.managers.graphql.SubmissionContentManager
import com.instructure.canvasapi2.managers.graphql.SubmissionDetailsManager
import com.instructure.canvasapi2.managers.graphql.SubmissionGradeManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.canvasapi2.models.RubricCriterionRating
import com.instructure.canvasapi2.models.Submission
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Test

@HiltAndroidTest
@UninstallModules(GraphQlApiModule::class)
class SpeedGraderGradePageTest : TeacherComposeTest() {

    override fun displaysPageObjects()  = Unit

    @BindValue
    @JvmField
    val commentLibraryManager: CommentLibraryManager = FakeCommentLibraryManager()

    @BindValue
    @JvmField
    val inboxSettingsManager: InboxSettingsManager = FakeInboxSettingsManager()

    @BindValue
    @JvmField
    val personContextManager: StudentContextManager = FakeStudentContextManager()

    @BindValue
    @JvmField
    val assignmentDetailsManager: AssignmentDetailsManager = FakeAssignmentDetailsManager()

    @BindValue
    @JvmField
    val submissionContentManager: SubmissionContentManager = FakeSubmissionContentManager()

    @BindValue
    @JvmField
    val submissionGradeManager: SubmissionGradeManager = FakeSubmissionGradeManager()

    @BindValue
    @JvmField
    val submissionDetailsManager: SubmissionDetailsManager = FakeSubmissionDetailsManager()

    @BindValue
    @JvmField
    val submissionRubricManager: SubmissionRubricManager = FakeSubmissionRubricManager()

    @BindValue
    @JvmField
    val submissionCommentsManager: SubmissionCommentsManager = FakeSubmissionCommentsManager()

    @BindValue
    @JvmField
    val customGradeStatusesManager: CustomGradeStatusesManager = FakeCustomGradeStatusesManager()

    @Test
    fun correctViewsForPointGradedWithoutRubric() {
        goToSpeedGraderGradePage(gradingType = "points")

        speedGraderGradePage.assertSpeedGraderLabelDisplayed()
        speedGraderGradePage.assertCurrentEnteredScore("10")
        speedGraderGradePage.assertPointsPossible("20")

        speedGraderGradePage.assertSliderVisible()

        speedGraderGradePage.assertNoGradeButtonEnabled()
        speedGraderGradePage.assertExcuseButtonEnabled()

        speedGraderGradePage.assertSelectedStatusText("submitted")

        speedGraderGradePage.assertDaysLate("1")

        speedGraderGradePage.assertFinalGradePointsValueDisplayed("10 / 20 pts")
        speedGraderGradePage.assertLatePenaltyValueDisplayed("0 pts")
        speedGraderGradePage.assertFinalGradeIsDisplayed("10 / 20 pts")

        speedGraderGradePage.assertNoRubricCriterionDisplayed()
    }

    @Test
    fun correctViewsForPercentageGradedWithoutRubric() {
        goToSpeedGraderGradePage(gradingType = "percent")

        speedGraderGradePage.assertSpeedGraderLabelDisplayed()
        speedGraderGradePage.assertPossiblePointsForPercentageGradingType("20")
        speedGraderGradePage.assertCurrentEnteredPercentage("60")

        speedGraderGradePage.assertSliderVisible()

        speedGraderGradePage.assertNoGradeButtonEnabled()
        speedGraderGradePage.assertExcuseButtonEnabled()

        speedGraderGradePage.assertSelectedStatusText("submitted")

        speedGraderGradePage.assertDaysLate("1")

        speedGraderGradePage.assertFinalGradePointsValueDisplayed("12 / 20 pts")
        speedGraderGradePage.assertLatePenaltyValueDisplayed("0 pts")
        speedGraderGradePage.assertFinalGradeIsDisplayed("12.0")

        speedGraderGradePage.assertNoRubricCriterionDisplayed()
    }

    @Test
    fun correctViewsForPassFailAssignment() {
        goToSpeedGraderGradePage(gradingType = "pass_fail")

        speedGraderGradePage.assertSpeedGraderLabelDisplayed()
        speedGraderGradePage.assertCurrentEnteredPassFailScore("10 / 20")

        speedGraderGradePage.assertSliderHidden()

        speedGraderGradePage.assertCompleteIncompleteButtonsDisplayed()
        speedGraderGradePage.assertCompleteButtonNotSelected()
        speedGraderGradePage.assertIncompleteButtonNotSelected()

        speedGraderGradePage.assertNoGradeButtonDoesNotExist()
        speedGraderGradePage.assertExcuseButtonEnabled()

        speedGraderGradePage.assertSelectedStatusText("submitted")

        speedGraderGradePage.assertDaysLate("1")

        speedGraderGradePage.assertFinalGradePointsValueDisplayed("10 / 20 pts")
        speedGraderGradePage.assertLatePenaltyValueDisplayed("0 pts")
        speedGraderGradePage.assertFinalGradeIsDisplayed("No Grade")

        speedGraderGradePage.assertNoRubricCriterionDisplayed()

        speedGraderGradePage.selectCompleteButton()
        speedGraderGradePage.assertCompleteButtonSelected()
        speedGraderGradePage.assertIncompleteButtonNotSelected()

        speedGraderGradePage.selectIncompleteButton()
        speedGraderGradePage.assertCompleteButtonNotSelected()
        speedGraderGradePage.assertIncompleteButtonSelected()
    }

    @Test //This is right now 'rollbacking' to points based grading as gpa_scale is not handled in the SpeedGraderGradingScreen
    fun correctViewsForGpaScaleAssignment() {
        goToSpeedGraderGradePage("gpa_scale")
        speedGraderGradePage.assertSpeedGraderLabelDisplayed()
        speedGraderGradePage.assertCurrentEnteredScore("10")
        speedGraderGradePage.assertPointsPossible("20")

        speedGraderGradePage.assertSliderVisible()

        speedGraderGradePage.assertNoGradeButtonEnabled()
        speedGraderGradePage.assertExcuseButtonEnabled()

        speedGraderGradePage.assertSelectedStatusText("submitted")

        speedGraderGradePage.assertDaysLate("1")

        speedGraderGradePage.assertFinalGradePointsValueDisplayed("10 / 20 pts")
        speedGraderGradePage.assertLatePenaltyValueDisplayed("0 pts")
        speedGraderGradePage.assertFinalGradeIsDisplayed("10.0") // It's a bit weird that why is this isn't the same as in the point based test

        speedGraderGradePage.assertNoRubricCriterionDisplayed()
    }

    @Stub
    @Test
    fun correctViewsForLetterGradeAssignment() {
        goToSpeedGraderGradePage(gradingType = "letter_grade")

        speedGraderGradePage.assertSpeedGraderLabelDisplayed()
        speedGraderGradePage.assertCurrentEnteredScore("10")
        speedGraderGradePage.assertPointsPossible("20")

        speedGraderGradePage.assertSliderVisible()

        speedGraderGradePage.assertNoGradeButtonEnabled()
        speedGraderGradePage.assertExcuseButtonEnabled()

        speedGraderGradePage.assertSelectedStatusText("submitted")

        speedGraderGradePage.assertDaysLate("1")

        speedGraderGradePage.assertFinalGradePointsValueDisplayed("10 / 20 pts")
        speedGraderGradePage.assertLatePenaltyValueDisplayed("0 pts")
        speedGraderGradePage.assertFinalGradeIsDisplayed("10 / 20 pts")

        speedGraderGradePage.assertRubricsLabelDisplayed()
        speedGraderGradePage.assertNoRubricCriterionDisplayed()
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
        val grade = "19"
        speedGraderGradePage.enterNewGrade(grade)
        speedGraderGradePage.assertSliderVisible()
        speedGraderGradePage.assertFinalGradePointsValueDisplayed("19 / 20 pts")
    }

    @Stub  //TODO: Known issue that first when entering new grade which is over 100, it "throws back" to the maximum points possible and shows the slider again.
    @Test
    fun sliderHideWhenEnteredGradeIsOverHundred() {
        goToSpeedGraderGradePage()
        val grade = "128.5"

        speedGraderGradePage.assertSliderVisible()
        speedGraderGradePage.enterNewGrade(grade)
        speedGraderGradePage.assertSliderHidden()
        // This should be fixed in the future to make this test work properly. (Because this behaviour in the test is the expected).
        speedGraderGradePage.assertFinalGradePointsValueDisplayed("128.5 / 20 pts")
        speedGraderGradePage.assertLatePenaltyValueDisplayed("0 pts")
        speedGraderGradePage.assertFinalGradeIsDisplayed("128.5 / 20 pts")
    }

    @Test
    fun excuseStudent() {
        goToSpeedGraderGradePage()

        speedGraderGradePage.assertNoGradeButtonEnabled()
        speedGraderGradePage.assertExcuseButtonEnabled()
        speedGraderGradePage.clickExcuseStudentButton()
        speedGraderGradePage.assertExcuseButtonDisabled()

        speedGraderGradePage.assertSliderVisible()
        speedGraderGradePage.assertSelectedStatusText("Excused")
        speedGraderGradePage.assertFinalGradePointsValueDisplayed("0 / 20 pts")
        speedGraderGradePage.assertFinalGradeIsDisplayed("0")
    }

    @Test
    @StubMultiAPILevel("Failed API levels = { 27, 28, 29 }")
    fun clearGrade() {
        goToSpeedGraderGradePage()
        speedGraderGradePage.assertNoGradeButtonEnabled()
        speedGraderGradePage.assertExcuseButtonEnabled()
        speedGraderGradePage.clickNoGradeButton()
        speedGraderGradePage.assertNoGradeButtonEnabled()

        speedGraderGradePage.assertSliderVisible()
        speedGraderGradePage.assertSelectedStatusText("None")
        speedGraderGradePage.assertFinalGradePointsValueDisplayed("0 / 20 pts")
        speedGraderGradePage.assertLatePenaltyValueDisplayed("0 pts")
        speedGraderGradePage.assertFinalGradeIsDisplayed("0 / 20 pts")
    }

    @Stub
    @Test
    fun correctViewsForPointGradedWithRubric() {
        goToSpeedGraderGradePage("points", true)
        speedGraderGradePage.assertSliderVisible()
        speedGraderGradePage.assertRubricsLabelDisplayed()
        //TODO
    }

    @Stub
    @Test
    fun correctViewsForPercentageGradedWithRubric() {
        goToSpeedGraderGradePage("percent", true)
        speedGraderGradePage.assertSliderVisible()
        speedGraderGradePage.assertRubricsLabelDisplayed()
        //TODO
    }

    private fun goToSpeedGraderGradePage(gradingType: String = "points", hasRubric: Boolean = false, pointsPossible: Int = 20, submission: Submission? = null) {
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
                type = "online_text_entry",
                body = "This is a test submission",
                score = 10.0,
                grade = "60",
        )

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)
        dashboardPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.clickAllSubmissions()
        assignmentSubmissionListPage.clickSubmission(student)
        composeTestRule.waitForIdle()
        if(!isLandscapeDevice()) speedGraderPage.clickExpandPanelButton()
        speedGraderPage.selectTab("Grade & Rubric")
    }
}
