package com.instructure.teacher.ui.e2e

import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.*
import com.instructure.dataseeding.model.ModuleItemTypes
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class ModulesE2ETest : TeacherTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() {
        //We dont want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.MODULES, TestCategory.E2E)
    fun testModulesE2E() {
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        // Create some assignments, quizzes, pages, etc...
        val assignment = AssignmentsApi.createAssignment(AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                withDescription = true,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token,
                dueAt = 1.days.fromNow.iso8601
        ))

        val quiz = QuizzesApi.createQuiz(QuizzesApi.CreateQuizRequest(
                courseId = course.id,
                withDescription = true,
                dueAt = 3.days.fromNow.iso8601,
                token = teacher.token,
                published = true
        ))

        // Create a module.  It starts out as unpublished.
        val module = ModulesApi.createModule(
                courseId = course.id,
                teacherToken = teacher.token,
                unlockAt = null)

        // Associate items with module
        ModulesApi.createModuleItem(
                courseId = course.id,
                moduleId = module.id,
                teacherToken = teacher.token,
                title = assignment.name,
                type = ModuleItemTypes.ASSIGNMENT.stringVal,
                contentId = assignment.id.toString()
        )

        ModulesApi.createModuleItem(
                courseId = course.id,
                moduleId = module.id,
                teacherToken = teacher.token,
                title = quiz.title,
                type = ModuleItemTypes.QUIZ.stringVal,
                contentId = quiz.id.toString()
        )

        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.assertDisplaysCourse(course)
        dashboardPage.openCourse(course.name)

        courseBrowserPage.openModulesTab()
        modulesPage.assertModuleIsPresent(module.name)
        modulesPage.assertModuleIsUnpublished()

        //Publish module
        ModulesApi.updateModule(
                courseId = course.id,
                id = module.id,
                published = true,
                teacherToken = teacher.token
        )

        modulesPage.refresh()
        modulesPage.assertModuleIsPresent(module.name)
        modulesPage.assertModuleIsPublished()
        modulesPage.assertModuleItemIsPresent(assignment.name)
        modulesPage.assertModuleItemIsPresent(quiz.title)
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.MODULES, TestCategory.E2E)
    fun testModulesEmptyE2E() {
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.assertDisplaysCourse(course)
        dashboardPage.openCourse(course.name)

        courseBrowserPage.openModulesTab()
        modulesPage.assertEmptyView()
        Espresso.pressBack()
        courseBrowserPage.assertCourseBrowserPageDisplayed()
    }
}