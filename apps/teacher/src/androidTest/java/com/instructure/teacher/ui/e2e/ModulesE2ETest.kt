package com.instructure.teacher.ui.e2e

import android.util.Log
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.ModulesApi
import com.instructure.dataseeding.api.QuizzesApi
import com.instructure.dataseeding.model.*
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
class   ModulesE2ETest : TeacherTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.MODULES, TestCategory.E2E)
    fun testModulesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId}. Assert that ${course.name} course is displayed on the Dashboard.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG,"Open ${course.name} course and navigate to Modules Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openModulesTab()

        Log.d(STEP_TAG,"Assert that empty view is displayed because there is no Module within the course.")
        modulesPage.assertEmptyView()

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for ${course.name} course.")
        val assignment = createAssignment(course, teacher)

        Log.d(PREPARATION_TAG,"Seeding quiz for ${course.name} course.")
        val quiz = createQuiz(course, teacher)

        Log.d(PREPARATION_TAG,"Seeding a module for ${course.name} course. It starts as unpublished.")
        val module = createModule(course, teacher)

        Log.d(PREPARATION_TAG,"Associate ${assignment.name} assignment (and the quiz within it) with module: ${module.id}.")
        createModuleAssignmentItem(course, module, teacher, assignment)

        createModuleQuizItem(course, module, teacher, quiz)

        Log.d(STEP_TAG,"Refresh the page. Assert that ${module.name} module is displayed and it is unpublished by default.")
        modulesPage.refresh()
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

    private fun createModuleQuizItem(
        course: CourseApiModel,
        module: ModuleApiModel,
        teacher: CanvasUserApiModel,
        quiz: QuizApiModel
    ) {
        ModulesApi.createModuleItem(
            courseId = course.id,
            moduleId = module.id,
            teacherToken = teacher.token,
            title = quiz.title,
            type = ModuleItemTypes.QUIZ.stringVal,
            contentId = quiz.id.toString()
        )
    }

    private fun createModuleAssignmentItem(
        course: CourseApiModel,
        module: ModuleApiModel,
        teacher: CanvasUserApiModel,
        assignment: AssignmentApiModel
    ) {
        ModulesApi.createModuleItem(
            courseId = course.id,
            moduleId = module.id,
            teacherToken = teacher.token,
            title = assignment.name,
            type = ModuleItemTypes.ASSIGNMENT.stringVal,
            contentId = assignment.id.toString()
        )
    }

    private fun createModule(
        course: CourseApiModel,
        teacher: CanvasUserApiModel
    ): ModuleApiModel {
        return ModulesApi.createModule(
            courseId = course.id,
            teacherToken = teacher.token,
            unlockAt = null
        )
    }

    private fun createQuiz(
        course: CourseApiModel,
        teacher: CanvasUserApiModel
    ): QuizApiModel {
        return QuizzesApi.createQuiz(
            QuizzesApi.CreateQuizRequest(
                courseId = course.id,
                withDescription = true,
                dueAt = 3.days.fromNow.iso8601,
                token = teacher.token,
                published = true
            )
        )
    }

    private fun createAssignment(
        course: CourseApiModel,
        teacher: CanvasUserApiModel
    ): AssignmentApiModel {
        return AssignmentsApi.createAssignment(
            AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                withDescription = true,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token,
                dueAt = 1.days.fromNow.iso8601
            )
        )
    }

}