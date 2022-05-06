package com.instructure.teacher.ui.e2e

import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.ui.utils.*
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class SyllabusE2ETest : TeacherTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() {
        //We dont want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SYLLABUS, TestCategory.E2E)
    fun testSyllabusE2E() {
        val data = seedData(teachers = 1, courses = 1, students = 1, favoriteCourses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openSyllabus()
        syllabusPage.assertEmptyView()

        val assignment = seedAssignments(
                courseId = course.id,
                dueAt = 1.days.fromNow.iso8601,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token,
                pointsPossible = 15.0,
                withDescription = true
        )

        val quiz = seedQuizzes(
                courseId = course.id,
                withDescription = true,
                published = true,
                teacherToken = teacher.token,
                dueAt = 1.days.fromNow.iso8601
        )

        //check for syllabus items to be present
        syllabusPage.refresh()
        syllabusPage.assertItemDisplayed(assignment[0].name)
        syllabusPage.assertItemDisplayed(quiz.quizList[0].title)

        //add a syllabus content and check tabs are visisble
        syllabusPage.refresh()
        syllabusPage.openEditSyllabus()
        var syllabusBody = "Syllabus Body"
        editSyllabusPage.editSyllabusBody(syllabusBody)
        editSyllabusPage.saveSyllabusEdit()
        syllabusPage.assertDisplaysSyllabus(syllabusBody = syllabusBody, shouldDisplayTabs = true)

        //check syllabus summary tab has the items
        syllabusPage.selectSummaryTab()
        syllabusPage.assertItemDisplayed(assignment[0].name)
        syllabusPage.assertItemDisplayed(quiz.quizList[0].title)

        //check syllabus summary can be turned off and syllabus body is edited
        syllabusPage.selectSyllabusTab()
        syllabusBody = "Edited Syllabus Body"
        syllabusPage.openEditSyllabus()
        editSyllabusPage.editSyllabusBody(syllabusBody)
        editSyllabusPage.editSyllabusToggleShowSummary()
        editSyllabusPage.saveSyllabusEdit()
        syllabusPage.assertDisplaysSyllabus(syllabusBody = syllabusBody, shouldDisplayTabs = false)
    }
}