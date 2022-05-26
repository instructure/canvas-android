package com.instructure.teacher.ui.e2e

import android.util.Log
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
        //We don't want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.MODULES, TestCategory.E2E)
    fun testModulesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for ${course.name} course.")
        val assignment = AssignmentsApi.createAssignment(AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                withDescription = true,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token,
                dueAt = 1.days.fromNow.iso8601
        ))

        Log.d(PREPARATION_TAG,"Seeding quiz for ${course.name} course.")
        val quiz = QuizzesApi.createQuiz(QuizzesApi.CreateQuizRequest(
                courseId = course.id,
                withDescription = true,
                dueAt = 3.days.fromNow.iso8601,
                token = teacher.token,
                published = true
        ))

        Log.d(PREPARATION_TAG,"Seeding a module for ${course.name} course. It starts as unpublished.")
        val module = ModulesApi.createModule(
                courseId = course.id,
                teacherToken = teacher.token,
                unlockAt = null)

        Log.d(PREPARATION_TAG,"Associate ${assignment.name} assignment (and the quiz within it) with module: ${module.id}.")
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

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId} , password: ${teacher.password}")
        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG,"Open ${course.name} course and navigate to 'Modules' Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openModulesTab()

        Log.d(STEP_TAG,"Assert that ${module.name} module is displayed and it is unpublished by default.")
        modulesPage.assertModuleIsPresent(module.name)
        modulesPage.assertModuleIsUnpublished()

        Log.d(PREPARATION_TAG,"Publish ${module.name} module via API.")
        ModulesApi.updateModule(
                courseId = course.id,
                id = module.id,
                published = true,
                teacherToken = teacher.token
        )

        Log.d(STEP_TAG,"Refresh the page. Assert that ${module.name} module is displayed and it is published.")
        modulesPage.refresh()
        modulesPage.assertModuleIsPresent(module.name)
        modulesPage.assertModuleIsPublished()

        Log.d(STEP_TAG,"Assert that ${assignment.name} assignment and ${quiz.title} quiz are present as module items.")
        modulesPage.assertModuleItemIsPresent(assignment.name)
        modulesPage.assertModuleItemIsPresent(quiz.title)
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.MODULES, TestCategory.E2E)
    fun testModulesEmptyE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId} , password: ${teacher.password}")
        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG,"Open ${course.name} course and navigate to 'Modules' Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openModulesTab()

        Log.d(STEP_TAG,"Assert that empty view is displayed because there is no Module within the course. Navigate back to Course Browser Page and assert it is displayed.")
        modulesPage.assertEmptyView()
        Espresso.pressBack()
        courseBrowserPage.assertCourseBrowserPageDisplayed()
    }
}