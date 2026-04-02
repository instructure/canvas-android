/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.student.ui.e2e.compose

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.canvasapi2.models.RubricCriterionRating
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.extensions.seedAssignmentSubmission
import com.instructure.student.ui.utils.extensions.seedData
import com.instructure.student.ui.utils.extensions.seedRubricWithAssignment
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class RubricE2ETest : StudentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.RUBRICS, TestCategory.E2E)
    fun testRubricDisplayedInSubmissionDetailsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(teachers = 1, courses = 1, students = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for '${course.name}' course with 10 max points.")
        val assignment = AssignmentsApi.createAssignment(
            courseId = course.id,
            teacherToken = teacher.token,
            gradingType = GradingType.POINTS,
            pointsPossible = 10.0,
            dueAt = 1.days.fromNow.iso8601,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        val writingQualityCriterion = RubricCriterion(
            description = "Writing Quality",
            longDescription = "Evaluates the overall quality of written expression and clarity.",
            points = 10.0,
            ratings = mutableListOf(
                RubricCriterionRating(description = "Excellent", longDescription = "Demonstrates outstanding writing skills with clear and compelling expression.", points = 10.0),
                RubricCriterionRating(description = "Satisfactory", longDescription = "Meets basic writing requirements with adequate clarity.", points = 5.0),
                RubricCriterionRating(description = "Poor", longDescription = "Does not meet writing standards; lacks clarity and structure.", points = 0.0)
            )
        )

        val researchDepthCriterion = RubricCriterion(
            description = "Research Depth",
            longDescription = null,
            points = 9.0,
            ratings = mutableListOf(
                RubricCriterionRating(description = "Exceptional", longDescription = "Thorough and comprehensive research with strong source diversity.", points = 9.0),
                RubricCriterionRating(description = "Proficient", longDescription = "Well-researched with only minor gaps in coverage.", points = 7.0),
                RubricCriterionRating(description = "Developing", longDescription = "Adequate research coverage but missing important perspectives.", points = 4.0),
                RubricCriterionRating(description = "Beginning", longDescription = "Limited research depth with significant gaps.", points = 2.0),
                RubricCriterionRating(description = "Not good", longDescription = "Insufficient research; sources are missing or unreliable.", points = 1.0)
            )
        )

        Log.d(PREPARATION_TAG, "Creating a rubric with 2 criteria and associating it with '${assignment.name}' assignment.")
        val rubric = seedRubricWithAssignment(
            courseId = course.id,
            assignmentId = assignment.id,
            teacherToken = teacher.token,
            title = "Test Rubric",
            criteria = listOf(writingQualityCriterion, researchDepthCriterion)
        )

        Log.d(PREPARATION_TAG, "Seeding a submission for '${assignment.name}' assignment with '${student.name}' student.")
        seedAssignmentSubmission(
            submissionSeeds = listOf(SubmissionsApi.SubmissionSeedInfo(
                amount = 1,
                submissionType = SubmissionType.ONLINE_TEXT_ENTRY
            )),
            assignmentId = assignment.id,
            courseId = course.id,
            studentToken = student.token
        )

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Select course: '${course.name}'.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG, "Navigate to course Assignments Page.")
        courseBrowserPage.selectAssignments()

        Log.d(STEP_TAG, "Click on assignment '${assignment.name}'.")
        assignmentListPage.clickAssignment(assignment)

        Log.d(ASSERTION_TAG, "Assert that 'Submission & Feedback' label is displayed on the Assignment Details Page.")
        assignmentDetailsPage.assertSubmissionAndFeedbackLabel()

        Log.d(STEP_TAG, "Navigate to Submission Details Page.")
        assignmentDetailsPage.goToSubmissionDetails()

        Log.d(STEP_TAG, "Open the 'Rubric' tab in Submission Details.")
        submissionDetailsPage.openRubric()

        Log.d(STEP_TAG, "Expand the sliding panel to see all of the Rubric criterion.")
        submissionDetailsPage.expandSlidingPanel()

        Log.d(ASSERTION_TAG, "Assert that the 'Writing Quality' criterion description button is displayed and opens the long description.")
        submissionDetailsPage.assertRubricDescriptionDisplays(writingQualityCriterion)

        Log.d(ASSERTION_TAG, "Assert that the 'Writing Quality' rubric criterion is displayed with all 3 ratings and their descriptions.")
        submissionDetailsPage.assertRubricCriterionDisplayed(writingQualityCriterion)

        Log.d(ASSERTION_TAG, "Assert that the 'Research Depth' rubric criterion is displayed with all 5 ratings and their descriptions.")
        submissionDetailsPage.assertRubricCriterionDisplayed(researchDepthCriterion)

        Log.d(STEP_TAG, "Grade the submission with rubric via API: selecting 'Poor' (defined rating, 0 pts) for 'Writing Quality' and a custom score of 3 pts for 'Research Depth'.")
        val writingQualityCriterionResponse = rubric.criteria.first { it.description == writingQualityCriterion.description }
        val poorRating = writingQualityCriterionResponse.ratings.first { it.description == "Poor" }
        val researchDepthCriterionResponse = rubric.criteria.first { it.description == researchDepthCriterion.description }
        SubmissionsApi.gradeSubmissionWithRubric(
            teacherToken = teacher.token,
            courseId = course.id,
            assignmentId = assignment.id,
            studentId = student.id,
            rubricAssessment = mapOf(
                writingQualityCriterionResponse.id to SubmissionsApi.RubricAssessmentEntry(points = 0.0, ratingId = poorRating.id),
                researchDepthCriterionResponse.id to SubmissionsApi.RubricAssessmentEntry(points = 3.0)
            )
        )

        Log.d(STEP_TAG, "Navigate back to Assignment Details Page and refresh to pick up the new grade.")
        Espresso.pressBack()
        assignmentDetailsPage.refresh()

        Log.d(STEP_TAG, "Navigate to Submission Details Page.")
        assignmentDetailsPage.goToSubmissionDetails()

        Log.d(STEP_TAG, "Open the 'Rubric' tab and expand the sliding panel.")
        submissionDetailsPage.openRubric()
        submissionDetailsPage.expandSlidingPanel()

        Log.d(ASSERTION_TAG, "Assert that the 'Writing Quality' 'Poor' rating is pre-selected as a defined rubric grade.")
        submissionDetailsPage.assertRubricRatingSelected(writingQualityCriterion, writingQualityCriterion.ratings.first { it.description == "Poor" })

        Log.d(ASSERTION_TAG, "Assert that 'Research Depth' shows a custom score as pre-selected.")
        submissionDetailsPage.assertRubricCustomScoreSelected(researchDepthCriterion)
    }

}