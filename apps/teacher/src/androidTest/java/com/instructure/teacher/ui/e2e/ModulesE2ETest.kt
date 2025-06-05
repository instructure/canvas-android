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
import com.instructure.dataseeding.api.FileFolderApi
import com.instructure.dataseeding.api.ModulesApi
import com.instructure.dataseeding.api.PagesApi
import com.instructure.dataseeding.api.QuizzesApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.FileUploadType
import com.instructure.dataseeding.model.ModuleItemTypes
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.getCustomDateCalendar
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.openOverflowMenu
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import com.instructure.teacher.ui.utils.uploadTextFile
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class ModulesE2ETest : TeacherComposeTest() {

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

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert that '${course.name}' course is displayed on the Dashboard.")
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG, "Open '${course.name}' course and navigate to Modules Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openModulesTab()

        Log.d(ASSERTION_TAG, "Assert that empty view is displayed because there is no Module within the course.")
        moduleListPage.assertEmptyView()

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for '${course.name}' course.")
        val assignment = AssignmentsApi.createAssignment(course.id, teacher.token, withDescription = true, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY), dueAt = 1.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG, "Seeding quiz for '${course.name}' course.")
        val quiz = QuizzesApi.createQuiz(course.id, teacher.token, withDescription = true, dueAt = 3.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG, "Create an unpublished page for course: '${course.name}'.")
        val testPage = PagesApi.createCoursePage(course.id, teacher.token, published = false, body = "<h1 id=\"header1\">Test Page Text</h1>")

        Log.d(PREPARATION_TAG, "Create a discussion topic for '${course.name}' course.")
        val discussionTopic = DiscussionTopicsApi.createDiscussion(courseId = course.id, token = teacher.token)

        Log.d(PREPARATION_TAG, "Seeding a module for '${course.name}' course. It starts as unpublished.")
        val module = ModulesApi.createModule(course.id, teacher.token)

        Log.d(PREPARATION_TAG, "Associate '${assignment.name}' assignment with module: '${module.id}'.")
        ModulesApi.createModuleItem(course.id, teacher.token, module.id, moduleItemTitle = assignment.name, moduleItemType = ModuleItemTypes.ASSIGNMENT.stringVal, contentId = assignment.id.toString())

        Log.d(PREPARATION_TAG, "Associate '${quiz.title}' quiz with module: '${module.id}'.")
        ModulesApi.createModuleItem(course.id, teacher.token, module.id, moduleItemTitle = quiz.title, moduleItemType = ModuleItemTypes.QUIZ.stringVal, contentId = quiz.id.toString())

        Log.d(PREPARATION_TAG, "Associate '${testPage.title}' page with module: '${module.id}'.")
        ModulesApi.createModuleItem(course.id, teacher.token, module.id, moduleItemTitle = testPage.title, moduleItemType = ModuleItemTypes.PAGE.stringVal, contentId = null, pageUrl = testPage.url)

        Log.d(PREPARATION_TAG, "Associate '${discussionTopic.title}' discussion with module: '${module.id}'.")
        ModulesApi.createModuleItem(course.id, teacher.token, module.id, moduleItemTitle = discussionTopic.title, moduleItemType = ModuleItemTypes.DISCUSSION.stringVal, contentId = discussionTopic.id.toString())

        Log.d(ASSERTION_TAG, "Refresh the page. Assert that '${module.name}' module is displayed and it is unpublished by default.")
        moduleListPage.refresh()
        moduleListPage.assertModuleIsDisplayed(module.name)
        moduleListPage.assertModuleNotPublished(module.name)

        Log.d(ASSERTION_TAG, "Assert that '${testPage.title}' page is present as a module item, but it's not published.")
        moduleListPage.assertModuleItemIsDisplayed(testPage.title)
        moduleListPage.assertModuleItemNotPublished(testPage.title)

        Log.d(PREPARATION_TAG, "Publish '${module.name}' module via API.")
        ModulesApi.updateModule(courseId = course.id, moduleId = module.id, published = true, teacherToken = teacher.token)

        Log.d(ASSERTION_TAG, "Refresh the page. Assert that '${module.name}' module is displayed and it is published.")
        moduleListPage.refresh()
        moduleListPage.assertModuleIsDisplayed(module.name)
        moduleListPage.assertModuleIsPublished()

        Log.d(ASSERTION_TAG, "Assert that '${assignment.name}' assignment and '${quiz.title}' quiz are present as module items, and they are published since their module is published.")
        moduleListPage.assertModuleItemIsDisplayed(assignment.name)
        moduleListPage.assertModuleItemIsPublished(assignment.name)
        moduleListPage.assertModuleItemIsDisplayed(quiz.title)
        moduleListPage.assertModuleItemIsPublished(quiz.title)

        Log.d(ASSERTION_TAG, "Assert that '${testPage.title}' page is present as a module item, but it's not published.")
        moduleListPage.assertModuleItemIsDisplayed(testPage.title)
        moduleListPage.assertModuleItemIsPublished(testPage.title)

        Log.d(STEP_TAG, "Collapse the '${module.name}'.")
        moduleListPage.clickOnCollapseExpandIcon()

        Log.d(ASSERTION_TAG, "Assert that the module items has not displayed.")
        moduleListPage.assertItemCountInModule(module.name, 0)

        Log.d(STEP_TAG, "Expand the '${module.name}'.")
        moduleListPage.clickOnCollapseExpandIcon()

        Log.d(ASSERTION_TAG, "Assert that the module items are displayed.")
        moduleListPage.assertItemCountInModule(module.name, 4)

        Log.d(STEP_TAG, "Open the '${assignment.name}' assignment module item.")
        moduleListPage.clickOnModuleItem(assignment.name)

        Log.d(ASSERTION_TAG, "Assert that the Assignment Details Page is displayed. Assert that the module name is displayed at the bottom.")
        assignmentDetailsPage.assertPageObjects()
        assignmentDetailsPage.assertAssignmentDetails(assignment)
        assignmentDetailsPage.moduleItemInteractions.assertModuleNameDisplayed(module.name)

        Log.d(ASSERTION_TAG, "Assert that the previous arrow button is not displayed because the user is on the first assignment's details page, but the next arrow button is displayed.")
        assignmentDetailsPage.moduleItemInteractions.assertPreviousArrowNotDisplayed()
        assignmentDetailsPage.moduleItemInteractions.assertNextArrowDisplayed()

        Log.d(STEP_TAG, "Click on the next arrow button.")
        assignmentDetailsPage.moduleItemInteractions.clickOnNextArrow()

        Log.d(ASSERTION_TAG, "Assert that the '${quiz.title}' quiz module item's details page is displayed. Assert that the module name is displayed at the bottom.")
        quizDetailsPage.assertQuizDetails(quiz)
        quizDetailsPage.moduleItemInteractions.assertModuleNameDisplayed(module.name)

        Log.d(ASSERTION_TAG, "Assert that both the previous and next arrow buttons are displayed.")
        quizDetailsPage.moduleItemInteractions.assertPreviousArrowDisplayed()
        quizDetailsPage.moduleItemInteractions.assertNextArrowDisplayed()

        Log.d(STEP_TAG, "Click on the next arrow button.")
        quizDetailsPage.moduleItemInteractions.clickOnNextArrow()

        Log.d(ASSERTION_TAG, "Assert that the '${testPage.title}' page module item's details page is displayed. Assert that the module name is displayed at the bottom.")
        editPageDetailsPage.assertPageDetails(testPage)
        editPageDetailsPage.moduleItemInteractions.assertModuleNameDisplayed(module.name)

        Log.d(ASSERTION_TAG, "Assert that both the previous and next arrow buttons are displayed.")
        editPageDetailsPage.moduleItemInteractions.assertPreviousArrowDisplayed()
        editPageDetailsPage.moduleItemInteractions.assertNextArrowDisplayed()

        Log.d(STEP_TAG, "Click on the next arrow button.")
        editPageDetailsPage.moduleItemInteractions.clickOnNextArrow()

        Log.d(ASSERTION_TAG, "Assert that the '${discussionTopic.title}' discussion module item's details (web view) page is displayed. Assert that the module name is displayed at the bottom.")
        discussionDetailsPage.assertToolbarDiscussionTitle(discussionTopic.title)
        discussionDetailsPage.moduleItemInteractions.assertModuleNameDisplayed(module.name)

        Log.d(ASSERTION_TAG, "Assert that the next arrow button is not displayed because the user is on the last assignment's details page, but the previous arrow button is displayed.")
        discussionDetailsPage.moduleItemInteractions.assertPreviousArrowDisplayed()
        discussionDetailsPage.moduleItemInteractions.assertNextArrowNotDisplayed()

        Log.d(STEP_TAG, "Click on the previous arrow button.")
        quizDetailsPage.moduleItemInteractions.clickOnPreviousArrow()

        Log.d(ASSERTION_TAG, "Assert that the '${testPage.title}' page module item's details page is displayed. Assert that the module name is displayed at the bottom.")
        editPageDetailsPage.assertPageDetails(testPage)
        editPageDetailsPage.moduleItemInteractions.assertModuleNameDisplayed(module.name)

        Log.d(STEP_TAG, "Navigate back to Module List Page.")
        Espresso.pressBack()

        Log.d(PREPARATION_TAG, "Unpublish '${module.name}' module via API.")
        ModulesApi.updateModule(courseId = course.id, moduleId = module.id, published = false, teacherToken = teacher.token)

        Log.d(STEP_TAG, "Refresh the Module List Page.")
        moduleListPage.refresh()

        Log.d(STEP_TAG, "Assert that '${assignment.name}' assignment and '${quiz.title}' quiz and '${testPage.title}' page are present as module items, and they are NOT published since their module is unpublished.")
        moduleListPage.assertModuleItemIsDisplayed(assignment.name)
        moduleListPage.assertModuleItemNotPublished(assignment.name)
        moduleListPage.assertModuleItemIsDisplayed(quiz.title)
        moduleListPage.assertModuleItemNotPublished(quiz.title)
        moduleListPage.assertModuleItemIsDisplayed(testPage.title)
        moduleListPage.assertModuleItemNotPublished(testPage.title)

        Log.d(STEP_TAG, "Open the '${assignment.name}' assignment module item.")
        moduleListPage.clickOnModuleItem(assignment.name)

        Log.d(ASSERTION_TAG, "Assert that the published status of the '${assignment.name}' assignment became 'Unpublished' on the Assignment Details Page.")
        assignmentDetailsPage.assertPublishedStatus(false)

        Log.d(STEP_TAG, "Open Edit Page of '${assignment.name}' assignment and publish it. Save the change.")
        assignmentDetailsPage.openEditPage()
        editAssignmentDetailsPage.clickPublishSwitch()
        editAssignmentDetailsPage.saveAssignment()

        Log.d(ASSERTION_TAG, "Assert that the published status of the '${assignment.name}' assignment became 'Published' on the Assignment Details Page (as well).")
        assignmentDetailsPage.assertPublishedStatus(true)

        Log.d(STEP_TAG, "Navigate back to Module List Page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the '${assignment.name}' assignment module item's status became 'Published'.")
        moduleListPage.assertModuleItemIsPublished(assignment.name)
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.MODULES, TestCategory.E2E)
    fun testBulkUpdateModulesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val student = data.studentsList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for '${course.name}' course.")
        val assignment = AssignmentsApi.createAssignment(course.id, teacher.token, withDescription = true, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY), dueAt = 1.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG, "Submit '${assignment.name}' assignment for '${student.name}' student.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, assignment.id, submissionSeedsList = listOf(SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(PREPARATION_TAG, "Seeding another 'Text Entry' assignment for '${course.name}' course.")
        val assignment2 = AssignmentsApi.createAssignment(course.id, teacher.token, withDescription = true, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY), dueAt = 1.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG, "Seeding quiz for '${course.name}' course.")
        val quiz =  QuizzesApi.createQuiz(course.id, teacher.token, withDescription = true, dueAt = 3.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG, "Create an unpublished page for course: '${course.name}'.")
        val testPage = PagesApi.createCoursePage(course.id, teacher.token, published = false, body = "<h1 id=\"header1\">Test Page Text</h1>")

        Log.d(PREPARATION_TAG, "Create another unpublished page for course: '${course.name}'.")
        val testPage2 = PagesApi.createCoursePage(course.id, teacher.token, published = true, frontPage = false, body = "<h1 id=\"header1\">This is another test page</h1>")

        Log.d(PREPARATION_TAG, "Create a discussion topic for '${course.name}' course.")
        val discussionTopic = DiscussionTopicsApi.createDiscussion(courseId = course.id, token = teacher.token)

        Log.d(PREPARATION_TAG, "Seeding a module for '${course.name}' course. It starts as unpublished.")
        val module = ModulesApi.createModule(course.id, teacher.token)

        Log.d(PREPARATION_TAG, "Seeding another module for '${course.name}' course. It starts as unpublished.")
        val module2 = ModulesApi.createModule(course.id, teacher.token)

        Log.d(PREPARATION_TAG, "Associate '${assignment.name}' assignment with module: '${module.id}'.")
        ModulesApi.createModuleItem(course.id, teacher.token, module.id, moduleItemTitle = assignment.name, moduleItemType = ModuleItemTypes.ASSIGNMENT.stringVal, contentId = assignment.id.toString())

        Log.d(PREPARATION_TAG, "Associate '${quiz.title}' quiz with module: '${module.id}'.")
        ModulesApi.createModuleItem(course.id, teacher.token, module.id, moduleItemTitle = quiz.title, moduleItemType = ModuleItemTypes.QUIZ.stringVal, contentId = quiz.id.toString())

        Log.d(PREPARATION_TAG, "Associate '${testPage.title}' page with module: '${module.id}'.")
        ModulesApi.createModuleItem(course.id, teacher.token, module.id, moduleItemTitle = testPage.title, moduleItemType = ModuleItemTypes.PAGE.stringVal, contentId = null, pageUrl = testPage.url)

        Log.d(PREPARATION_TAG, "Associate '${discussionTopic.title}' discussion with module: '${module.id}'.")
        ModulesApi.createModuleItem(course.id, teacher.token, module.id, moduleItemTitle = discussionTopic.title, moduleItemType = ModuleItemTypes.DISCUSSION.stringVal, contentId = discussionTopic.id.toString())

        Log.d(PREPARATION_TAG, "Associate '${assignment2.name}' assignment with module: '${module2.id}'.")
        ModulesApi.createModuleItem(course.id, teacher.token, module2.id , assignment2.name, ModuleItemTypes.ASSIGNMENT.stringVal, assignment2.id.toString())

        Log.d(PREPARATION_TAG, "Associate '${testPage2.title}' page with module: '${module2.id}'.")
        ModulesApi.createModuleItem(course.id, teacher.token, module2.id, testPage2.title, ModuleItemTypes.PAGE.stringVal, null, pageUrl = testPage2.url)

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'. Assert that '${course.name}' course is displayed on the Dashboard.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open '${course.name}' course and navigate to Modules Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openModulesTab()

        Log.d(STEP_TAG, "Assert that '${module.name}' and '${module2.name}' modules are displayed and they are unpublished by default. " +
                "Assert that the '${testPage.title}' page module item is not published and the other module items are published in '${module.name}' module.")
        moduleListPage.assertModuleIsDisplayed(module.name)
        moduleListPage.assertModuleNotPublished(module.name)
        moduleListPage.assertModuleIsDisplayed(module2.name)
        moduleListPage.assertModuleNotPublished(module2.name)
        moduleListPage.assertModuleItemIsPublished(assignment.name)
        moduleListPage.assertModuleStatusIconAlpha(assignment.name, 0.5f)
        moduleListPage.assertModuleItemIsPublished(quiz.title)
        moduleListPage.assertModuleItemIsPublished(discussionTopic.title)
        moduleListPage.assertModuleItemNotPublished(testPage.title)

        //Upper layer - All Modules and Items
        Log.d(STEP_TAG, "Open Module List Page overflow menu.")
        openOverflowMenu()

        Log.d(ASSERTION_TAG, "Assert that the corresponding menu items are displayed.")
        moduleListPage.assertToolbarMenuItems()

        Log.d(STEP_TAG, "Click on 'Publish all Modules and Items' and confirm it via the publish dialog.")
        moduleListPage.clickOnText(R.string.publishAllModulesAndItems)
        moduleListPage.clickOnText(R.string.publishDialogPositiveButton)

        Log.d(ASSERTION_TAG, "Assert that the 'All Modules and Items' is displayed as title and the corresponding note also displayed on the Progress Page.")
        progressPage.assertProgressPageTitle(R.string.allModulesAndItems)
        progressPage.assertProgressPageNote(R.string.moduleBulkUpdateNote)

        Log.d(STEP_TAG, "Click on 'Done' on the Progress Page once it finished.")
        progressPage.clickDone()

        Log.d(ASSERTION_TAG, "Assert that the proper snack bar text is displayed and the '${module.name}' module and all of it's items became published.")
        moduleListPage.assertSnackbarText(R.string.allModulesAndAllItemsPublished)
        moduleListPage.assertModuleIsPublished(module.name)
        moduleListPage.assertModuleItemIsPublished(assignment.name)
        moduleListPage.assertModuleItemIsPublished(quiz.title)
        moduleListPage.assertModuleItemIsPublished(testPage.title)
        moduleListPage.assertModuleItemIsPublished(discussionTopic.title)

        Log.d(ASSERTION_TAG, "Assert that '${module2.name}' module and all of it's items became published.")
        moduleListPage.assertModuleIsPublished(module2.name)
        moduleListPage.assertModuleItemIsPublished(assignment2.name)
        moduleListPage.assertModuleItemIsPublished(testPage2.title)

        Log.d(STEP_TAG, "Open Module List Page overflow menu")
        openOverflowMenu()

        Log.d(STEP_TAG, "Click on 'Unpublish all Modules and Items' and confirm it via the unpublish dialog.")
        moduleListPage.clickOnText(R.string.unpublishAllModulesAndItems)
        moduleListPage.clickOnText(R.string.unpublishDialogPositiveButton)

        Log.d(ASSERTION_TAG, "Assert that the 'All Modules and Items' is displayed as title on the Progress page.")
        progressPage.assertProgressPageTitle(R.string.allModulesAndItems)

        Log.d(STEP_TAG, "Click on 'Done' on the Progress Page once it finished.")
        progressPage.clickDone()

        Log.d(ASSERTION_TAG, "Assert that the proper snack bar text is displayed and the '${module.name}' module and all of it's items (except '${assignment.name}' assignment)  became unpublished.")
        moduleListPage.assertSnackbarText(R.string.allModulesAndAllItemsUnpublished)
        moduleListPage.assertModuleNotPublished(module.name)
        moduleListPage.assertModuleItemNotPublished(quiz.title)
        moduleListPage.assertModuleItemNotPublished(testPage.title)
        moduleListPage.assertModuleItemNotPublished(discussionTopic.title)

        Log.d(ASSERTION_TAG, "Assert that the '${assignment.name}' assignment remained published because it's unpublishable since it has a submission already.")
        moduleListPage.assertModuleItemIsPublished(assignment.name)

        Log.d(ASSERTION_TAG, "Assert that '${module2.name}' module and all of it's items became unpublished.")
        moduleListPage.assertModuleNotPublished(module2.name)
        moduleListPage.assertModuleItemNotPublished(assignment2.name)
        moduleListPage.assertModuleItemNotPublished(testPage2.title)

        Log.d(STEP_TAG, "Open Module List Page overflow menu")
        openOverflowMenu()

        Log.d(STEP_TAG, "Click on 'Publish Modules only' and confirm it via the publish dialog.")
        moduleListPage.clickOnText(R.string.publishModulesOnly)
        moduleListPage.clickOnText(R.string.publishDialogPositiveButton)

        Log.d(ASSERTION_TAG, "Assert that the 'All Modules' title is displayed on the Progress page.")
        progressPage.assertProgressPageTitle(R.string.allModules)

        Log.d(STEP_TAG, "Click on 'Done' on the Progress Page once it finished.")
        progressPage.clickDone()

        Log.d(ASSERTION_TAG, "Assert that the proper snack bar text is displayed and only the '${module.name}' module became published, but it's items remaining unpublished.")
        moduleListPage.assertSnackbarText(R.string.onlyModulesPublished)
        moduleListPage.assertModuleIsPublished(module.name)
        moduleListPage.assertModuleItemIsPublished(assignment.name)
        moduleListPage.assertModuleItemNotPublished(quiz.title)
        moduleListPage.assertModuleItemNotPublished(testPage.title)
        moduleListPage.assertModuleItemNotPublished(discussionTopic.title)

        Log.d(ASSERTION_TAG, "Assert that '${module2.name}' module became published but all of it's items are remaining unpublished.")
        moduleListPage.assertModuleIsPublished(module2.name)
        moduleListPage.assertModuleItemNotPublished(assignment2.name)
        moduleListPage.assertModuleItemNotPublished(testPage2.title)

        //Middle layer - One Module and Items

        Log.d(STEP_TAG, "Click on '${module.name}' module overflow.")
        moduleListPage.clickItemOverflow(module.name)

        Log.d(ASSERTION_TAG, "Assert that the corresponding menu items are displayed.")
        moduleListPage.assertModuleMenuItems()

        Log.d(STEP_TAG, "Click on 'Publish Module and all Items' and confirm it via the publish dialog.")
        moduleListPage.clickOnText(R.string.publishModuleAndItems)
        moduleListPage.clickOnText(R.string.publishDialogPositiveButton)

        Log.d(ASSERTION_TAG, "Assert that the 'Selected Modules and Items' is displayed as title on the Progress page.")
        progressPage.assertProgressPageTitle(R.string.selectedModulesAndItems)

        Log.d(STEP_TAG, "Click on 'Done' on the Progress Page once it finished.")
        progressPage.clickDone()

        Log.d(ASSERTION_TAG, "Assert that the proper snack bar text is displayed and the '${module.name}' module and all of it's items became published.")
        moduleListPage.assertSnackbarText(R.string.moduleAndAllItemsPublished)
        moduleListPage.assertModuleIsPublished(module.name)
        moduleListPage.assertModuleItemIsPublished(assignment.name)
        moduleListPage.assertModuleItemIsPublished(quiz.title)
        moduleListPage.assertModuleItemIsPublished(testPage.title)
        moduleListPage.assertModuleItemIsPublished(discussionTopic.title)

        Log.d(STEP_TAG, "Click on '${module.name}' module overflow.")
        moduleListPage.clickItemOverflow(module.name)

        Log.d(STEP_TAG, "Click on 'Unpublish Module and all Items' and confirm it via the unpublish dialog.")
        moduleListPage.clickOnText(R.string.unpublishModuleAndItems)
        moduleListPage.clickOnText(R.string.unpublishDialogPositiveButton)

        Log.d(ASSERTION_TAG, "Assert that the 'Selected Modules and Items' is displayed as title on the Progress page.")
        progressPage.assertProgressPageTitle(R.string.selectedModulesAndItems)

        Log.d(STEP_TAG, "Click on 'Done' on the Progress Page once it finished.")
        progressPage.clickDone()

        Log.d(ASSERTION_TAG, "Assert that the proper snack bar text is displayed and the '${module.name}' module and all of it's items (except '${assignment.name}' assignment) became unpublished.")
        moduleListPage.assertSnackbarText(R.string.moduleAndAllItemsUnpublished)
        moduleListPage.assertModuleNotPublished(module.name)
        moduleListPage.assertModuleItemNotPublished(quiz.title)
        moduleListPage.assertModuleItemNotPublished(testPage.title)
        moduleListPage.assertModuleItemNotPublished(discussionTopic.title)

        Log.d(ASSERTION_TAG, "Assert that the '${assignment.name}' assignment remained published because it's unpublishable since it has a submission already.")
        moduleListPage.assertModuleItemIsPublished(assignment.name)

        Log.d(STEP_TAG, "Click on '${module.name}' module overflow.")
        moduleListPage.clickItemOverflow(module.name)

        Log.d(STEP_TAG, "Click on 'Publish Module only' and confirm it via the publish dialog.")
        moduleListPage.clickOnText(R.string.publishModuleOnly)
        moduleListPage.clickOnText(R.string.publishDialogPositiveButton)
        device.waitForWindowUpdate(null, 3000)
        device.waitForIdle()

        Log.d(ASSERTION_TAG, "Assert that only the '${module.name}' module became published, but it's items (except '${assignment.name}' assignment) remaining unpublished.")
        moduleListPage.assertModuleIsPublished(module.name)
        moduleListPage.assertModuleItemIsPublished(assignment.name)
        moduleListPage.assertModuleItemNotPublished(quiz.title)
        moduleListPage.assertModuleItemNotPublished(testPage.title)
        moduleListPage.assertModuleItemNotPublished(discussionTopic.title)

        //Bottom layer - One module item

        Log.d(STEP_TAG, "Click on '${quiz.title}' quiz's overflow menu and publish it. Confirm the publish via the publish dialog.")
        moduleListPage.clickItemOverflow(quiz.title)
        moduleListPage.clickOnText(R.string.publishModuleItemAction)
        moduleListPage.clickOnText(R.string.publishDialogPositiveButton)

        Log.d(ASSERTION_TAG, "Assert that the 'Item published' snack bar has displayed and the '${quiz.title}' quiz became published.")
        moduleListPage.assertSnackbarText(R.string.moduleItemPublished)
        moduleListPage.assertModuleItemIsPublished(quiz.title)

        Log.d(STEP_TAG, "Click on '${testPage.title}' page's overflow menu and publish it. Confirm the publish via the publish dialog.")
        moduleListPage.clickItemOverflow(testPage.title)
        moduleListPage.clickOnText(R.string.publishModuleItemAction)
        moduleListPage.clickOnText(R.string.publishDialogPositiveButton)

        Log.d(ASSERTION_TAG, "Assert that the 'Item published' snack bar has displayed and the '${testPage.title}' page module item became published.")
        moduleListPage.assertSnackbarText(R.string.moduleItemPublished)
        moduleListPage.assertModuleItemIsPublished(assignment.name)

        Log.d(STEP_TAG, "Click on '${discussionTopic.title}' discussion topic's overflow menu and publish it. Confirm the publish via the publish dialog.")
        moduleListPage.clickItemOverflow(discussionTopic.title)
        moduleListPage.clickOnText(R.string.publishModuleItemAction)
        moduleListPage.clickOnText(R.string.publishDialogPositiveButton)

        Log.d(ASSERTION_TAG, "Assert that the 'Item published' snack bar has displayed and the '${discussionTopic.title}' discussion topic became published.")
        moduleListPage.assertSnackbarText(R.string.moduleItemPublished)
        moduleListPage.assertModuleItemIsPublished(discussionTopic.title)

        Log.d(STEP_TAG, "Try to click on '${assignment.name}' assignment's overflow menu (in order to unpublish it).")
        moduleListPage.clickItemOverflow(assignment.name)

        Log.d(ASSERTION_TAG, "Assert that a snack bar with a proper text will be displayed that it cannot be unpublished since it has student submissions.")
        moduleListPage.assertSnackbarContainsText(assignment.name)
        moduleListPage.assertModuleItemIsPublished(assignment.name)

        Log.d(STEP_TAG, "Click on '${quiz.title}' quiz's overflow menu and unpublish it. Confirm the unpublish via the unpublish dialog.")
        moduleListPage.clickItemOverflow(quiz.title)
        moduleListPage.clickOnText(R.string.unpublishModuleItemAction)
        moduleListPage.clickOnText(R.string.unpublishDialogPositiveButton)

        Log.d(ASSERTION_TAG, "Assert that the 'Item unpublished' snack bar has displayed and the '${quiz.title}' quiz became unpublished.")
        moduleListPage.assertSnackbarText(R.string.moduleItemUnpublished)
        moduleListPage.assertModuleItemNotPublished(quiz.title)

        Log.d(STEP_TAG, "Click on '${testPage.title}' page overflow menu and unpublish it. Confirm the unpublish via the unpublish dialog.")
        moduleListPage.clickItemOverflow(testPage.title)
        moduleListPage.clickOnText(R.string.unpublishModuleItemAction)
        moduleListPage.clickOnText(R.string.unpublishDialogPositiveButton)

        Log.d(ASSERTION_TAG, "Assert that the 'Item unpublished' snack bar has displayed and the '${testPage.title}' page module item became unpublished.")
        moduleListPage.assertSnackbarText(R.string.moduleItemUnpublished)
        moduleListPage.assertModuleItemNotPublished(testPage.title)

        Log.d(STEP_TAG, "Click on '${discussionTopic.title}' discussion topic's overflow menu and unpublish it. Confirm the unpublish via the unpublish dialog.")
        moduleListPage.clickItemOverflow(discussionTopic.title)
        moduleListPage.clickOnText(R.string.unpublishModuleItemAction)
        moduleListPage.clickOnText(R.string.unpublishDialogPositiveButton)

        Log.d(ASSERTION_TAG, "Assert that the 'Item unpublished' snack bar has displayed and the '${discussionTopic.title}' discussion topic became unpublished.")
        moduleListPage.assertSnackbarText(R.string.moduleItemUnpublished)
        moduleListPage.assertModuleItemNotPublished(discussionTopic.title)
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.MODULES, TestCategory.E2E)
    fun testFileModuleItemUpdateE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seeding a module for '${course.name}' course. It starts as unpublished.")
        val module = ModulesApi.createModule(course.id, teacher.token)

        Log.d(PREPARATION_TAG, "Get the root folder of the course.")
        val courseRootFolder = FileFolderApi.getCourseRootFolder(course.id, teacher.token)

        Log.d(PREPARATION_TAG, "Create a (text) file within the root folder (so the 'Files' tab file list) of the '${course.name}' course.")
        val testTextFile = uploadTextFile(courseRootFolder.id, token = teacher.token, fileUploadType = FileUploadType.COURSE_FILE)

        Log.d(PREPARATION_TAG, "Create another (text) file within the root folder (so the 'Files' tab file list) of the '${course.name}' course.")
        val testTextFile2 = uploadTextFile(courseRootFolder.id, token = teacher.token, fileUploadType = FileUploadType.COURSE_FILE)

        Log.d(PREPARATION_TAG, "Associate '${testTextFile.fileName}' (course) file with module: '${module.id}'.")
        ModulesApi.createModuleItem(
            course.id,
            teacher.token,
            module.id,
            moduleItemTitle = testTextFile.fileName,
            moduleItemType = ModuleItemTypes.FILE.stringVal,
            contentId = testTextFile.id.toString()
        )

        Log.d(PREPARATION_TAG, "Associate '${testTextFile2.fileName}' (course) file with module: '${module.id}'.")
        ModulesApi.createModuleItem(
            course.id,
            teacher.token,
            module.id,
            moduleItemTitle = testTextFile2.fileName,
            moduleItemType = ModuleItemTypes.FILE.stringVal,
            contentId = testTextFile2.id.toString()
        )

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'. Assert that '${course.name}' course is displayed on the Dashboard.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open '${course.name}' course and navigate to Modules Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openModulesTab()

        Log.d(ASSERTION_TAG, "Assert that both, the '${testTextFile.fileName}' and '${testTextFile2.fileName}' files are published.")
        moduleListPage.assertModuleItemIsPublished(testTextFile.fileName)
        moduleListPage.assertModuleItemIsPublished(testTextFile2.fileName)

        Log.d(STEP_TAG, "Click on the 'more menu' of the '${testTextFile.fileName}' file.")
        moduleListPage.clickItemOverflow(testTextFile.fileName)

        Log.d(ASSERTION_TAG, "Assert that by default, on the Update File Permissions Page the 'Published' radio button is checked within the 'Availability' section.")
        updateFilePermissionsPage.assertFilePublished()

        Log.d(ASSERTION_TAG, "Assert that the 'Visibility' section radio buttons are enabled and the 'Inherit from course' radio button is checked by default within the 'Visibility' section.")
        updateFilePermissionsPage.assertVisibilityEnabled()
        updateFilePermissionsPage.assertFileVisibilityInherit()

        Log.d(STEP_TAG, "Click on the 'Unpublish' radio button.")
        updateFilePermissionsPage.clickUnpublishRadioButton()

        Log.d(ASSERTION_TAG, "Assert that the 'Visibility' section radio buttons became disabled.")
        updateFilePermissionsPage.assertVisibilityDisabled()

        Log.d(STEP_TAG, "Click on the 'Update' button.")
        updateFilePermissionsPage.clickUpdateButton()

        Log.d(ASSERTION_TAG, "Assert on the Module List Page that the '${testTextFile.fileName}' file became unpublished.")
        moduleListPage.assertModuleItemNotPublished(testTextFile.fileName)

        Log.d(STEP_TAG, "Click on the 'more menu' of the '${testTextFile.fileName}' file.")
        moduleListPage.clickItemOverflow(testTextFile.fileName)

        Log.d(ASSERTION_TAG, "Assert that the 'Unpublished' radio button is checked because of the previous modifications.")
        updateFilePermissionsPage.assertFileUnpublished()

        Log.d(STEP_TAG, "Click on the 'Only available with link' (aka. 'Hide') radio button and click on the 'Update' button to save the changes.")
        updateFilePermissionsPage.clickHideRadioButton()
        updateFilePermissionsPage.clickUpdateButton()

        Log.d(ASSERTION_TAG, "Assert that the '${testTextFile.fileName}' file module item became hidden.")
        moduleListPage.assertModuleItemHidden(testTextFile.fileName)

        Log.d(STEP_TAG, "Click on the 'more menu' of the '${testTextFile.fileName}' file.")
        moduleListPage.clickItemOverflow(testTextFile.fileName)

        Log.d(ASSERTION_TAG, "Assert that the 'Hidden' radio button is checked because of the previous modifications.")
        updateFilePermissionsPage.assertFileHidden()

        Log.d(STEP_TAG, "Click on the 'Schedule availability' (aka. 'Scheduled') radio button without setting any 'From' and 'Until' dates and click on the 'Update' button to save the changes.")
        updateFilePermissionsPage.clickScheduleRadioButton()
        updateFilePermissionsPage.clickUpdateButton()

        Log.d(ASSERTION_TAG, "Assert that the '${testTextFile.fileName}' file is published since that is the expected behaviour if we does not select any dates for schedule.")
        moduleListPage.assertModuleItemIsPublished(testTextFile.fileName)

        Log.d(STEP_TAG, "Click on the 'more menu' of the '${testTextFile.fileName}' file.")
        moduleListPage.clickItemOverflow(testTextFile.fileName)

        Log.d(ASSERTION_TAG, "Assert that by default, on the Update File Permissions Page the 'Published' radio button is checked within the 'Availability' section.")
        updateFilePermissionsPage.assertFilePublished()

        Log.d(STEP_TAG, "Click on the 'Schedule availability' (aka. 'Scheduled') radio button, set some dates and click on the 'Update' button to save the changes.")
        updateFilePermissionsPage.clickScheduleRadioButton()

        Log.d(PREPARATION_TAG, "Create a calendar with 3 days ago and another one with 3 days later.")
        val unlockDateCalendar = getCustomDateCalendar(-3)
        val lockDateCalendar = getCustomDateCalendar(3)

        Log.d(STEP_TAG, "Set the 'From' and 'Until' dates (and times) as well. These are coming from the calendars which has been previously created.")
        updateFilePermissionsPage.setFromDateTime(unlockDateCalendar)
        updateFilePermissionsPage.setUntilDateTime(lockDateCalendar)

        Log.d(STEP_TAG, "Click on the 'Update' button to save the changes.")
        updateFilePermissionsPage.clickUpdateButton()

        Log.d(ASSERTION_TAG, "Assert that the '${testTextFile.fileName}' file module item is scheduled.")
        moduleListPage.assertModuleItemScheduled(testTextFile.fileName)
    }

}