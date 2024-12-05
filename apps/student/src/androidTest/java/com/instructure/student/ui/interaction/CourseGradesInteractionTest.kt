/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.ui.interaction

import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addSubmissionForAssignment
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.Grades
import com.instructure.canvasapi2.models.Tab
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class CourseGradesInteractionTest : StudentTest() {

    override fun displaysPageObjects() = Unit

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.GRADES, TestCategory.INTERACTION)
    fun testTotalGradeIsDisplayedWithGradeAndScoreWhenNotRestricted() {
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1)
        setUpCustomGrade("A", 100.0, data, false)
        goToGrades(data)
        courseGradesPage.assertTotalGrade(ViewMatchers.withText("100% A"))
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.GRADES, TestCategory.INTERACTION)
    fun testTotalGradeIsDisplayedWithOnlyScoreWhenNotRestrictedAndThereIsNoGrade() {
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1)
        setUpCustomGrade(score = 100.0, data = data, restrictQuantitativeData = false)
        goToGrades(data)
        courseGradesPage.assertTotalGrade(ViewMatchers.withText("100%"))
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.GRADES, TestCategory.INTERACTION)
    fun testGradeIsDisplayedWithOnlyGradeWhenQuantitativeDataIsRestricted() {
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1)
        setUpCustomGrade("A", 100.0, data, true)
        goToGrades(data)
        courseGradesPage.assertTotalGrade(ViewMatchers.withText("A"))
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.GRADES, TestCategory.INTERACTION)
    fun testConvertedGradeIsDisplayedWithOnlyScoreWhenRestrictedAndThereIsNoGrade() {
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1)
        setUpCustomGrade(score = 100.0, data = data, restrictQuantitativeData = true)
        goToGrades(data)
        courseGradesPage.assertTotalGrade(ViewMatchers.withText("A"))
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.GRADES, TestCategory.INTERACTION)
    fun testLetterGradeAssignmentWithoutQuantitativeRestriction() {
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1)
        setUpCustomGrade(score = 100.0, data = data, restrictQuantitativeData = false)
        val assignment = addAssignment(data, Assignment.GradingType.LETTER_GRADE, "B", 90.0, 100)

        goToGrades(data)

        courseGradesPage.assertAssignmentDisplayed(assignment.name!!, "90/100 (B)")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.GRADES, TestCategory.INTERACTION)
    fun testGpaScaleAssignmentWithoutQuantitativeRestriction() {
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1)
        setUpCustomGrade(score = 100.0, data = data, restrictQuantitativeData = false)
        val assignment = addAssignment(data, Assignment.GradingType.GPA_SCALE, "3.7", 90.0, 100)

        goToGrades(data)

        courseGradesPage.assertAssignmentDisplayed(assignment.name!!, "90/100 (3.7)")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.GRADES, TestCategory.INTERACTION)
    fun testPointsAssignmentWithoutQuantitativeRestriction() {
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1)
        setUpCustomGrade(score = 100.0, data = data, restrictQuantitativeData = false)
        val assignment = addAssignment(data, Assignment.GradingType.POINTS, "90", 90.0, 100)

        goToGrades(data)

        courseGradesPage.assertAssignmentDisplayed(assignment.name!!, "90/100")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.GRADES, TestCategory.INTERACTION)
    fun testPointsAssignmentExcusedWithoutQuantitativeRestriction() {
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1)
        setUpCustomGrade(score = 100.0, data = data, restrictQuantitativeData = false)
        val assignment = addAssignment(data, Assignment.GradingType.POINTS, null, 90.0, 100, excused = true)

        goToGrades(data)

        courseGradesPage.assertAssignmentDisplayed(assignment.name!!, "EX/100")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.GRADES, TestCategory.INTERACTION)
    fun testPercentageAssignmentWithoutQuantitativeRestriction() {
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1)
        setUpCustomGrade(score = 100.0, data = data, restrictQuantitativeData = false)
        val assignment = addAssignment(data, Assignment.GradingType.PERCENT, "90%", 90.0, 100)

        goToGrades(data)

        courseGradesPage.assertAssignmentDisplayed(assignment.name!!, "90%")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.GRADES, TestCategory.INTERACTION)
    fun testPassFailAssignmentWithoutQuantitativeRestriction() {
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1)
        setUpCustomGrade(score = 100.0, data = data, restrictQuantitativeData = false)
        val assignment = addAssignment(data, Assignment.GradingType.PASS_FAIL, "complete", 0.0, 0)

        goToGrades(data)

        courseGradesPage.assertAssignmentDisplayed(assignment.name!!, "Complete")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.GRADES, TestCategory.INTERACTION)
    fun testLetterGradeAssignmentWithQuantitativeRestriction() {
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1)
        setUpCustomGrade(score = 100.0, data = data, restrictQuantitativeData = true)
        val assignment = addAssignment(data, Assignment.GradingType.LETTER_GRADE, "B", 90.0, 100)

        goToGrades(data)

        courseGradesPage.assertAssignmentDisplayed(assignment.name!!, "B")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.GRADES, TestCategory.INTERACTION)
    fun testGpaScaleAssignmentWithQuantitativeRestriction() {
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1)
        setUpCustomGrade(score = 100.0, data = data, restrictQuantitativeData = true)
        val assignment = addAssignment(data, Assignment.GradingType.GPA_SCALE, "3.7", 90.0, 100)

        goToGrades(data)

        courseGradesPage.assertAssignmentDisplayed(assignment.name!!, "3.7")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.GRADES, TestCategory.INTERACTION)
    fun testPointsAssignmentWithQuantitativeRestriction() {
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1)
        setUpCustomGrade(score = 100.0, data = data, restrictQuantitativeData = true)
        val assignment = addAssignment(data, Assignment.GradingType.POINTS, "90", 90.0, 100)

        goToGrades(data)

        courseGradesPage.assertAssignmentDisplayed(assignment.name!!, "A")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.GRADES, TestCategory.INTERACTION)
    fun testPointsAssignmentExcusedWithQuantitativeRestriction() {
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1)
        setUpCustomGrade(score = 100.0, data = data, restrictQuantitativeData = true)
        val assignment = addAssignment(data, Assignment.GradingType.POINTS, null, 90.0, 100, excused = true)

        goToGrades(data)

        courseGradesPage.assertAssignmentDisplayed(assignment.name!!, "Excused")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.GRADES, TestCategory.INTERACTION)
    fun testPercentageAssignmentWithQuantitativeRestriction() {
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1)
        setUpCustomGrade(score = 100.0, data = data, restrictQuantitativeData = true)
        val assignment = addAssignment(data, Assignment.GradingType.PERCENT, "80%", 80.0, 100)

        goToGrades(data)

        courseGradesPage.assertAssignmentDisplayed(assignment.name!!, "B")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.GRADES, TestCategory.INTERACTION)
    fun testPassFailAssignmentWithQuantitativeRestriction() {
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1)
        setUpCustomGrade(score = 100.0, data = data, restrictQuantitativeData = true)
        val assignment = addAssignment(data, Assignment.GradingType.PASS_FAIL, "complete", 0.0, 0)

        goToGrades(data)

        courseGradesPage.assertAssignmentDisplayed(assignment.name!!, "Complete")
    }

    private fun setUpData(
        courseCount: Int = 1,
        invitedCourseCount: Int = 0,
        pastCourseCount: Int = 0,
        favoriteCourseCount: Int = 0,
        announcementCount: Int = 0
    ): MockCanvas {
        val data = MockCanvas.init(
            studentCount = 1,
            courseCount = courseCount,
            invitedCourseCount = invitedCourseCount,
            pastCourseCount = pastCourseCount,
            favoriteCourseCount = favoriteCourseCount,
            accountNotificationCount = announcementCount)

        val course = data.courses.values.first()

        val gradesTab = Tab(position = 2, label = "Grades", visibility = "public", tabId = Tab.GRADES_ID)
        data.courseTabs[course.id]!! += gradesTab

        return data
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

    private fun goToGrades(data: MockCanvas) {
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        val course = data.courses.values.first()
        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectGrades()
    }

    private fun setUpCustomGrade(grade: String? = null, score: Double? = null, data: MockCanvas, restrictQuantitativeData: Boolean) {
        val student = data.students[0]
        val course = data.courses.values.first()

        val enrollment = course.enrollments!!.first { it.userId == student.id }
            .copy(
                grades = Grades(currentGrade = grade, currentScore = score),
                computedCurrentGrade = grade,
                computedCurrentScore = score
            )

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
                enrollments = mutableListOf(enrollment),
                gradingSchemeRaw = gradingScheme)
        data.courses[course.id] = newCourse
    }
}