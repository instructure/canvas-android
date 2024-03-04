package com.instructure.teacher.ui.e2e

import android.util.Log
import androidx.test.espresso.Espresso
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

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'. Assert that '${course.name}' course is displayed on the Dashboard.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG,"Open '${course.name}' course and navigate to Modules Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openModulesTab()

        Log.d(STEP_TAG,"Assert that empty view is displayed because there is no Module within the course.")
        moduleListPage.assertEmptyView()

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for '${course.name}' course.")
        val assignment = createAssignment(course, teacher)

        Log.d(PREPARATION_TAG,"Seeding quiz for '${course.name}' course.")
        val quiz = createQuiz(course, teacher)

        Log.d(PREPARATION_TAG,"Create an unpublished page for course: '${course.name}'.")
        val testPage = createCoursePage(course, teacher, published = false, frontPage = false, body = "<h1 id=\"header1\">Test Page Text</h1>")

        Log.d(PREPARATION_TAG,"Create a discussion topic for '${course.name}' course.")
        val discussionTopic = createDiscussion(course, teacher)

        Log.d(PREPARATION_TAG,"Seeding a module for '${course.name}' course. It starts as unpublished.")
        val module = createModule(course, teacher)

        Log.d(PREPARATION_TAG,"Associate '${assignment.name}' assignment (and the quiz within it) with module: '${module.id}'.")
        createModuleItem(course, module, teacher, assignment.name, ModuleItemTypes.ASSIGNMENT.stringVal, assignment.id.toString())
        createModuleItem(course, module, teacher, quiz.title, ModuleItemTypes.QUIZ.stringVal, quiz.id.toString())

        Log.d(PREPARATION_TAG,"Associate '${testPage.title}' page with module: '${module.id}'.")
        createModuleItem(course, module, teacher, testPage.title, ModuleItemTypes.PAGE.stringVal, null, pageUrl = testPage.url)

        Log.d(PREPARATION_TAG,"Associate '${discussionTopic.title}' discussion with module: '${module.id}'.")
        createModuleItem(course, module, teacher, discussionTopic.title, ModuleItemTypes.DISCUSSION.stringVal, discussionTopic.id.toString())

        Log.d(STEP_TAG,"Refresh the page. Assert that '${module.name}' module is displayed and it is unpublished by default.")
        moduleListPage.refresh()
        moduleListPage.assertModuleIsDisplayed(module.name)
        moduleListPage.assertModuleNotPublished()

        Log.d(STEP_TAG,"Assert that '${testPage.title}' page is present as a module item, but it's not published.")
        moduleListPage.assertModuleItemIsDisplayed(testPage.title)
        moduleListPage.assertModuleItemNotPublished(testPage.title)

        Log.d(PREPARATION_TAG,"Publish '${module.name}' module via API.")
        ModulesApi.updateModule(
                courseId = course.id,
                moduleId = module.id,
                published = true,
                teacherToken = teacher.token
        )

        Log.d(STEP_TAG,"Refresh the page. Assert that '${module.name}' module is displayed and it is published.")
        moduleListPage.refresh()
        moduleListPage.assertModuleIsDisplayed(module.name)
        moduleListPage.assertModuleIsPublished()

        Log.d(STEP_TAG,"Assert that '${assignment.name}' assignment and '${quiz.title}' quiz are present as module items, and they are published since their module is published.")
        moduleListPage.assertModuleItemIsDisplayed(assignment.name)
        moduleListPage.assertModuleItemIsPublished(assignment.name)
        moduleListPage.assertModuleItemIsDisplayed(quiz.title)
        moduleListPage.assertModuleItemIsPublished(quiz.title)

        Log.d(STEP_TAG,"Assert that '${testPage.title}' page is present as a module item, but it's not published.")
        moduleListPage.assertModuleItemIsDisplayed(testPage.title)
        moduleListPage.assertModuleItemIsPublished(testPage.title)

        Log.d(STEP_TAG, "Collapse the '${module.name}' and assert that the module items has not displayed.")
        moduleListPage.clickOnCollapseExpandIcon()
        moduleListPage.assertItemCountInModule(module.name, 0)

        Log.d(STEP_TAG, "Expand the '${module.name}' and assert that the module items are displayed.")
        moduleListPage.clickOnCollapseExpandIcon()
        moduleListPage.assertItemCountInModule(module.name, 4)

        Log.d(STEP_TAG, "Open the '${assignment.name}' assignment module item and assert that the Assignment Details Page is displayed. Assert that the module name is displayed at the bottom.")
        moduleListPage.clickOnModuleItem(assignment.name)
        assignmentDetailsPage.assertPageObjects()
        assignmentDetailsPage.assertAssignmentDetails(assignment)
        assignmentDetailsPage.moduleItemInteractions.assertModuleNameDisplayed(module.name)

        Log.d(STEP_TAG, "Assert that the previous arrow button is not displayed because the user is on the first assignment's details page, but the next arrow button is displayed.")
        assignmentDetailsPage.moduleItemInteractions.assertPreviousArrowNotDisplayed()
        assignmentDetailsPage.moduleItemInteractions.assertNextArrowDisplayed()

        Log.d(STEP_TAG, "Click on the next arrow button and assert that the '${quiz.title}' quiz module item's details page is displayed. Assert that the module name is displayed at the bottom.")
        assignmentDetailsPage.moduleItemInteractions.clickOnNextArrow()
        quizDetailsPage.assertQuizDetails(quiz)
        quizDetailsPage.moduleItemInteractions.assertModuleNameDisplayed(module.name)

        Log.d(STEP_TAG, "Assert that both the previous and next arrow buttons are displayed.")
        quizDetailsPage.moduleItemInteractions.assertPreviousArrowDisplayed()
        quizDetailsPage.moduleItemInteractions.assertNextArrowDisplayed()

        Log.d(STEP_TAG, "Click on the next arrow button and assert that the '${testPage.title}' page module item's details page is displayed. Assert that the module name is displayed at the bottom.")
        quizDetailsPage.moduleItemInteractions.clickOnNextArrow()
        editPageDetailsPage.assertPageDetails(testPage)
        editPageDetailsPage.moduleItemInteractions.assertModuleNameDisplayed(module.name)

        Log.d(STEP_TAG, "Assert that both the previous and next arrow buttons are displayed.")
        editPageDetailsPage.moduleItemInteractions.assertPreviousArrowDisplayed()
        editPageDetailsPage.moduleItemInteractions.assertNextArrowDisplayed()

        Log.d(STEP_TAG, "Click on the next arrow button and assert that the '${discussionTopic.title}' discussion module item's details page is displayed. Assert that the module name is displayed at the bottom.")
        editPageDetailsPage.moduleItemInteractions.clickOnNextArrow()
        discussionsDetailsPage.assertDiscussionTitle(discussionTopic.title)
        discussionsDetailsPage.assertDiscussionPublished()
        discussionsDetailsPage.moduleItemInteractions.assertModuleNameDisplayed(module.name)

        Log.d(STEP_TAG, "Assert that the next arrow button is not displayed because the user is on the last assignment's details page, but the previous arrow button is displayed.")
        discussionsDetailsPage.moduleItemInteractions.assertPreviousArrowDisplayed()
        discussionsDetailsPage.moduleItemInteractions.assertNextArrowNotDisplayed()

        Log.d(STEP_TAG, "Click on the previous arrow button and assert that the '${testPage.title}' page module item's details page is displayed. Assert that the module name is displayed at the bottom.")
        quizDetailsPage.moduleItemInteractions.clickOnPreviousArrow()
        editPageDetailsPage.assertPageDetails(testPage)
        editPageDetailsPage.moduleItemInteractions.assertModuleNameDisplayed(module.name)

        Log.d(STEP_TAG, "Navigate back to Module List Page.")
        Espresso.pressBack()

        Log.d(PREPARATION_TAG,"Unpublish ${module.name} module via API.")
        ModulesApi.updateModule(
            courseId = course.id,
            moduleId = module.id,
            published = false,
            teacherToken = teacher.token
        )

        Log.d(STEP_TAG, "Refresh the Modules Page.")
        moduleListPage.refresh()

        Log.d(STEP_TAG,"Assert that ${assignment.name} assignment and ${quiz.title} quiz and ${testPage.title} page are present as module items, and they are NOT published since their module is unpublished.")
        moduleListPage.assertModuleItemIsDisplayed(assignment.name)
        moduleListPage.assertModuleItemNotPublished(assignment.name)
        moduleListPage.assertModuleItemIsDisplayed(quiz.title)
        moduleListPage.assertModuleItemNotPublished(quiz.title)
        moduleListPage.assertModuleItemIsDisplayed(testPage.title)
        moduleListPage.assertModuleItemNotPublished(testPage.title)

        Log.d(STEP_TAG, "Open the '${assignment.name}' assignment module item and assert that the Assignment Details Page is displayed")
        moduleListPage.clickOnModuleItem(assignment.name)

        Log.d(STEP_TAG, "Assert that the published status of the '${assignment.name}' assignment became 'Unpublished' on the Assignment Details Page.")
        assignmentDetailsPage.assertPublishedStatus(false)

        Log.d(STEP_TAG, "Open Edit Page of '${assignment.name}' assignment and publish it. Save the change.")
        assignmentDetailsPage.openEditPage()
        editAssignmentDetailsPage.clickPublishSwitch()
        editAssignmentDetailsPage.saveAssignment()

        Log.d(STEP_TAG, "Assert that the published status of the '${assignment.name}' assignment became 'Published' on the Assignment Details Page (as well).")
        assignmentDetailsPage.assertPublishedStatus(true)

        Log.d(STEP_TAG, "Navigate back to Module List Page and assert that the '${assignment.name}' assignment module item's status became 'Published'.")
        Espresso.pressBack()
        moduleListPage.assertModuleItemIsPublished(assignment.name)
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