package com.instructure.teacher.ui.e2e

import android.util.Log
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedAssignments
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.seedQuizzes
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class SyllabusE2ETest : TeacherTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SYLLABUS, TestCategory.E2E)
    fun testSyllabusE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(teachers = 1, courses = 1, students = 1, favoriteCourses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open '${course.name}' course and navigate to Syllabus Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openSyllabus()

        Log.d(ASSERTION_TAG, "Assert that empty view is displayed.")
        syllabusPage.assertEmptyView()

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for '${course.name}' course.")
        val assignment = seedAssignments(courseId = course.id, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY), teacherToken = teacher.token, pointsPossible = 15.0, withDescription = true)

        Log.d(PREPARATION_TAG, "Seed a quiz for the '${course.name}' course.")
        val quiz = seedQuizzes(courseId = course.id, withDescription = true, published = true, teacherToken = teacher.token, dueAt = 1.days.fromNow.iso8601)

        Log.d(ASSERTION_TAG, "Refresh the Syllabus page and assert that the '${assignment[0].name}' assignment and '${quiz.quizList[0].title}' quiz are displayed as syllabus items.")
        syllabusPage.refresh()
        syllabusPage.assertItemDisplayed(assignment[0].name)
        syllabusPage.assertItemDisplayed(quiz.quizList[0].title)

        Log.d(STEP_TAG, "Refresh the Syllabus page. Click on 'Pencil' (aka. 'Edit') icon.")
        syllabusPage.refresh()
        syllabusPage.openEditSyllabus()

        var syllabusBody = "Syllabus Body"
        Log.d(STEP_TAG, "Edit syllabus description (aka. 'Syllabus Body') by adding new value to it: '$syllabusBody'. Click on 'Save'.")
        editSyllabusPage.editSyllabusBody(syllabusBody)
        editSyllabusPage.saveSyllabusEdit()

        Log.d(ASSERTION_TAG, "Assert that the previously made modifications has been applied on the syllabus.")
        syllabusPage.assertDisplaysSyllabus(syllabusBody = syllabusBody, shouldDisplayTabs = true)

        Log.d(STEP_TAG, "Select 'Summary' Tab.")
        syllabusPage.selectSummaryTab()

        Log.d(ASSERTION_TAG, "Assert that the '${assignment[0].name}' assignment and '${quiz.quizList[0].title}' quiz are displayed.")
        syllabusPage.assertItemDisplayed(assignment[0].name)
        syllabusPage.assertItemDisplayed(quiz.quizList[0].title)

        Log.d(STEP_TAG, "Select 'Syllabus' Tab and click on 'Pencil' (aka. 'Edit') icon.")
        syllabusPage.selectSyllabusTab()
        syllabusBody = "Edited Syllabus Body"
        syllabusPage.openEditSyllabus()

        Log.d(STEP_TAG, "Edit syllabus description (aka. 'Syllabus Body') by adding new value to it: '$syllabusBody'. Toggle 'Show course summary'. Click on 'Save'.")
        editSyllabusPage.editSyllabusBody(syllabusBody)
        editSyllabusPage.editSyllabusToggleShowSummary()
        editSyllabusPage.saveSyllabusEdit()

        Log.d(ASSERTION_TAG, "Assert that the previously made modifications has been applied on the syllabus.")
        syllabusPage.assertDisplaysSyllabus(syllabusBody = syllabusBody, shouldDisplayTabs = false)
    }
}