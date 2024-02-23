package com.instructure.teacher.ui.e2e

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.DiscussionTopicsApi
import com.instructure.dataseeding.api.ModulesApi
import com.instructure.dataseeding.api.PagesApi
import com.instructure.dataseeding.api.QuizzesApi
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.dataseeding.model.ModuleApiModel
import com.instructure.dataseeding.model.ModuleItemTypes
import com.instructure.dataseeding.model.PageApiModel
import com.instructure.dataseeding.model.QuizApiModel
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.teacher.ui.pages.WebViewTextCheck
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

        Log.d(PREPARATION_TAG,"Create an unpublished page for course: ${course.name}.")
        val testPage = createCoursePage(course, teacher, published = false, frontPage = false, body = "<h1 id=\"header1\">Test Page Text</h1>")

        Log.d(PREPARATION_TAG,"Create a discussion topic for ${course.name} course.")
        val discussionTopic = createDiscussion(course, teacher)

        Log.d(PREPARATION_TAG,"Seeding a module for ${course.name} course. It starts as unpublished.")
        val module = createModule(course, teacher)

        Log.d(PREPARATION_TAG,"Associate ${assignment.name} assignment (and the quiz within it) with module: ${module.id}.")
        createModuleItem(course, module, teacher, assignment.name, ModuleItemTypes.ASSIGNMENT.stringVal, assignment.id.toString())
        createModuleItem(course, module, teacher, quiz.title, ModuleItemTypes.QUIZ.stringVal, quiz.id.toString())

        Log.d(PREPARATION_TAG,"Associate ${testPage.title} page with module: ${module.id}.")
        createModuleItem(course, module, teacher, testPage.title, ModuleItemTypes.PAGE.stringVal, null, pageUrl = testPage.url)

        Log.d(PREPARATION_TAG,"Associate ${discussionTopic.title} discussion with module: ${module.id}.")
        createModuleItem(course, module, teacher, discussionTopic.title, ModuleItemTypes.DISCUSSION.stringVal, discussionTopic.id.toString())

        Log.d(STEP_TAG,"Refresh the page. Assert that ${module.name} module is displayed and it is unpublished by default.")
        modulesPage.refresh()
        modulesPage.assertModuleIsDisplayed(module.name)
        modulesPage.assertModuleNotPublished()

        Log.d(STEP_TAG,"Assert that ${testPage.title} page is present as a module item, but it's not published.")
        modulesPage.assertModuleItemIsDisplayed(testPage.title)
        modulesPage.assertModuleItemNotPublished(testPage.title)

        Log.d(PREPARATION_TAG,"Publish ${module.name} module via API.")
        ModulesApi.updateModule(
                courseId = course.id,
                moduleId = module.id,
                published = true,
                teacherToken = teacher.token
        )

        Log.d(STEP_TAG,"Refresh the page. Assert that ${module.name} module is displayed and it is published.")
        modulesPage.refresh()
        modulesPage.assertModuleIsDisplayed(module.name)
        modulesPage.assertModuleIsPublished()

        Log.d(STEP_TAG,"Assert that ${assignment.name} assignment and ${quiz.title} quiz are present as module items, and they are published since their module is published.")
        modulesPage.assertModuleItemIsDisplayed(assignment.name)
        modulesPage.assertModuleItemIsPublished(assignment.name)
        modulesPage.assertModuleItemIsDisplayed(quiz.title)
        modulesPage.assertModuleItemIsPublished(quiz.title)

        Log.d(STEP_TAG,"Assert that ${testPage.title} page is present as a module item, but it's not published.")
        modulesPage.assertModuleItemIsDisplayed(testPage.title)
        modulesPage.assertModuleItemIsPublished(testPage.title)

        Log.d(STEP_TAG, "Collapse the ${module.name} and assert that the module items has not displayed.")
        modulesPage.clickOnCollapseExpandIcon()
        modulesPage.assertItemCountInModule(module.name, 0)

        Log.d(STEP_TAG, "Expand the ${module.name} and assert that the module items are displayed.")
        modulesPage.clickOnCollapseExpandIcon()
        modulesPage.assertItemCountInModule(module.name, 4)

        Log.d(PREPARATION_TAG,"Unpublish ${module.name} module via API.")
        ModulesApi.updateModule(
            courseId = course.id,
            moduleId = module.id,
            published = false,
            teacherToken = teacher.token
        )

        Log.d(STEP_TAG, "Refresh the Modules Page.")
        modulesPage.refresh()

        Log.d(STEP_TAG,"Assert that ${assignment.name} assignment and ${quiz.title} quiz and ${testPage.title} page are present as module items, and they are NOT published since their module is unpublished.")
        modulesPage.assertModuleItemIsDisplayed(assignment.name)
        modulesPage.assertModuleItemNotPublished(assignment.name)
        modulesPage.assertModuleItemIsDisplayed(quiz.title)
        modulesPage.assertModuleItemNotPublished(quiz.title)
        modulesPage.assertModuleItemIsDisplayed(testPage.title)
        modulesPage.assertModuleItemNotPublished(testPage.title)

        Log.d(STEP_TAG, "Open the ${assignment.name} assignment module item and assert that the Assignment Details Page is displayed. Navigate back to Modules Page.")
        modulesPage.clickOnModuleItem(assignment.name)
        assignmentDetailsPage.assertPageObjects()
        Espresso.pressBack()

        Log.d(STEP_TAG, "Open the ${quiz.title} quiz module item and assert that the Quiz Details Page is displayed. Navigate back to Modules Page.")
        modulesPage.clickOnModuleItem(quiz.title)
        quizDetailsPage.assertPageObjects()
        Espresso.pressBack()

        Log.d(STEP_TAG, "Open the ${testPage.title} page module item and assert that the Page Details Page is displayed. Navigate back to Modules Page.")
        modulesPage.clickOnModuleItem(testPage.title)
        editPageDetailsPage.runTextChecks(WebViewTextCheck(Locator.ID, "header1", "Test Page Text"))
        Espresso.pressBack()

        Log.d(STEP_TAG, "Open the ${discussionTopic.title} discussion module item and assert that the Discussion Details Page is displayed.")
        modulesPage.clickOnModuleItem(discussionTopic.title)
        discussionsDetailsPage.assertPageObjects()
    }

    private fun createModuleItem(
        course: CourseApiModel,
        module: ModuleApiModel,
        teacher: CanvasUserApiModel,
        title: String,
        moduleItemType: String,
        contentId: String?,
        pageUrl: String? = null
    ) {
        ModulesApi.createModuleItem(
            courseId = course.id,
            moduleId = module.id,
            teacherToken = teacher.token,
            moduleItemTitle = title,
            moduleItemType = moduleItemType,
            contentId = contentId,
            pageUrl = pageUrl
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

    private fun createCoursePage(
        course: CourseApiModel,
        teacher: CanvasUserApiModel,
        published: Boolean = true,
        frontPage: Boolean = false,
        body: String = EMPTY_STRING
    ): PageApiModel {
        return PagesApi.createCoursePage(
            courseId = course.id,
            published = published,
            frontPage = frontPage,
            token = teacher.token,
            body = body
        )
    }

    private fun createDiscussion(
        course: CourseApiModel,
        teacher: CanvasUserApiModel
    ) = DiscussionTopicsApi.createDiscussion(
        courseId = course.id,
        token = teacher.token
    )

}