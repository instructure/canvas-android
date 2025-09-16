/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addSubmissionForAssignment
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeCustomGradeStatusesManager
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.di.graphql.CustomGradeStatusModule
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Test

@HiltAndroidTest
@UninstallModules(CustomGradeStatusModule::class)
class AssignmentListInteractionTest : StudentComposeTest() {

    @BindValue
    @JvmField
    val customGradeStatusesManager: CustomGradeStatusesManager = FakeCustomGradeStatusesManager()

    override fun displaysPageObjects() = Unit

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun displaysNoAssignmentsView() {
        setUpData(0)
        goToAssignmentsPage()
        assignmentListPage.assertDisplaysNoAssignmentsView()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun displaysAssignment() {
        val assignment = setUpData()[0]
        goToAssignmentsPage()
        assignmentListPage.assertHasAssignment(assignment)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testLetterGradeAssignmentWithoutQuantitativeRestriction() {
        setUpData()
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.LETTER_GRADE, "B", 90.0, 100)
        goToAssignmentsPage()

        assignmentListPage.assertHasAssignment(assignment, "90/100 (B)")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testGpaScaleAssignmentWithoutQuantitativeRestriction() {
        setUpData()
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.GPA_SCALE, "3.7", 90.0, 100)
        goToAssignmentsPage()

        assignmentListPage.assertHasAssignment(assignment, "90/100 (3.7)")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testPointsAssignmentWithoutQuantitativeRestriction() {
        setUpData()
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.POINTS, "90", 90.0, 100)
        goToAssignmentsPage()

        assignmentListPage.assertHasAssignment(assignment, "90/100")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testPointsAssignmentExcusedWithoutQuantitativeRestriction() {
        setUpData()
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.POINTS, null, 90.0, 100, excused = true)
        goToAssignmentsPage()

        assignmentListPage.assertHasAssignment(assignment, "EX/100")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testPercentageAssignmentWithoutQuantitativeRestriction() {
        setUpData()
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.PERCENT, "90%", 90.0, 100)
        goToAssignmentsPage()

        assignmentListPage.assertHasAssignment(assignment, "90%")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testPassFailAssignmentWithoutQuantitativeRestriction() {
        setUpData()
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.PASS_FAIL, "complete", 0.0, 0)
        goToAssignmentsPage()

        assignmentListPage.assertHasAssignment(assignment, "Complete")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testLetterGradeAssignmentWithQuantitativeRestriction() {
        setUpData(restrictQuantitativeData = true)
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.LETTER_GRADE, "B", 90.0, 100)
        goToAssignmentsPage()

        assignmentListPage.assertHasAssignment(assignment, "B")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testGpaScaleAssignmentWithQuantitativeRestriction() {
        setUpData(restrictQuantitativeData = true)
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.GPA_SCALE, "3.7", 90.0, 100)
        goToAssignmentsPage()

        assignmentListPage.assertHasAssignment(assignment, "3.7")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testPointsAssignmentWithQuantitativeRestriction() {
        setUpData(restrictQuantitativeData = true)
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.POINTS, "90", 90.0, 100)
        goToAssignmentsPage()

        assignmentListPage.assertHasAssignment(assignment, "A")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testPointsAssignmentExcusedWithQuantitativeRestriction() {
        setUpData(restrictQuantitativeData = true)
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.POINTS, null, 90.0, 100, excused = true)
        goToAssignmentsPage()

        assignmentListPage.assertHasAssignment(assignment, "Excused")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testPercentageAssignmentWithQuantitativeRestriction() {
        setUpData(restrictQuantitativeData = true)
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.PERCENT, "80%", 80.0, 100)
        goToAssignmentsPage()

        assignmentListPage.assertHasAssignment(assignment, "B")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testPassFailAssignmentWithQuantitativeRestriction() {
        setUpData(restrictQuantitativeData = true)
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.PASS_FAIL, "complete", 0.0, 0)
        goToAssignmentsPage()

        assignmentListPage.assertHasAssignment(assignment, "Complete")
    }

    private fun setUpData(assignmentCount: Int = 1, restrictQuantitativeData: Boolean = false): List<Assignment> {
        val data = MockCanvas.init(
            courseCount = 1,
            favoriteCourseCount = 1,
            studentCount = 1,
            teacherCount = 1
        )

        val course = data.courses.values.first()

        val gradingScheme = listOf(
            listOf("A", 0.9),
            listOf("B", 0.8),
            listOf("C", 0.7),
            listOf("D", 0.6),
            listOf("F", 0.0)
        )

        data.courseSettings[course.id] = CourseSettings(restrictQuantitativeData = restrictQuantitativeData)

        val newCourse = course
            .copy(settings = CourseSettings(restrictQuantitativeData = restrictQuantitativeData),
                gradingSchemeRaw = gradingScheme)
        data.courses[course.id] = newCourse

        val assignmentList = mutableListOf<Assignment>()
        repeat(assignmentCount) {
            val assignment = data.addAssignment(
                courseId = course.id,
                submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
            )
            assignmentList.add(assignment)
        }

        return assignmentList
    }

    private fun goToAssignmentsPage() {
        val data = MockCanvas.data

        val course = data.courses.values.first()
        val student = data.students[0]

        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()

        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAssignments()
    }

    private fun addAssignment(data: MockCanvas, gradingType: Assignment.GradingType, grade: String?, score: Double?, maxScore: Int, excused: Boolean = false): Assignment {
        val course = data.courses.values.first()
        val student = data.students.first()

        val assignment = data.addAssignment(
            courseId = course.id,
            submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY),
            gradingType = Assignment.gradingTypeToAPIString(gradingType) ?: "",
            pointsPossible = maxScore,
        )

        data.addSubmissionForAssignment(assignment.id, student.id, Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString, grade = grade, score = score, excused = excused)

        return assignment
    }
}

