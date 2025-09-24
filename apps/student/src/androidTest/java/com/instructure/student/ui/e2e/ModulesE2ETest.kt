/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.e2e

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.DiscussionTopicsApi
import com.instructure.dataseeding.api.ModulesApi
import com.instructure.dataseeding.api.PagesApi
import com.instructure.dataseeding.api.QuizzesApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.ModuleItemTypes
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class ModulesE2ETest: StudentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.MODULES, TestCategory.E2E)
    @Stub("There is a known issue with the API on beta, so this would always fail. Remove stubbing when LX-2147 is done.")
    fun testModulesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course.")
        val assignment1 = AssignmentsApi.createAssignment(course.id, teacher.token, withDescription = true, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG, "Seeding another assignment for '${course.name}' course.")
        val assignment2 = AssignmentsApi.createAssignment(course.id, teacher.token, dueAt = 2.days.fromNow.iso8601, withDescription = true, gradingType = GradingType.POINTS, pointsPossible = 15.0, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG, "Create a PUBLISHED quiz for '${course.name}' course.")
        val quiz1 = QuizzesApi.createQuiz(course.id, teacher.token, dueAt = 3.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG, "Create a page for '${course.name}' course.")
        val page1 = PagesApi.createCoursePage(course.id, teacher.token)

        Log.d(PREPARATION_TAG, "Create a discussion topic for '${course.name}' course.")
        val discussionTopic1 = DiscussionTopicsApi.createDiscussion(course.id, teacher.token)

        //Modules start out as unpublished.
        Log.d(PREPARATION_TAG, "Create a module for '${course.name}' course.")
        val module1 = ModulesApi.createModule(course.id, teacher.token)

        Log.d(PREPARATION_TAG, "Create another module for '${course.name}' course.")
        val module2 = ModulesApi.createModule(course.id, teacher.token)

        Log.d(PREPARATION_TAG, "Associate '${assignment1.name}' assignment with '${module1.name}' module.")
        ModulesApi.createModuleItem(course.id, teacher.token, module1.id, assignment1.name, ModuleItemTypes.ASSIGNMENT.stringVal, contentId = assignment1.id.toString())

        Log.d(PREPARATION_TAG, "Associate '${quiz1.title}' quiz with '${module1.name}' module.")
        ModulesApi.createModuleItem(course.id, teacher.token, module1.id, quiz1.title, ModuleItemTypes.QUIZ.stringVal, contentId = quiz1.id.toString())

        Log.d(PREPARATION_TAG, "Associate '${assignment2.name}' assignment with '${module2.name}' module.")
        ModulesApi.createModuleItem(course.id, teacher.token, module2.id, assignment2.name, ModuleItemTypes.ASSIGNMENT.stringVal, contentId = assignment2.id.toString())

        Log.d(PREPARATION_TAG, "Associate '${discussionTopic1.title}' discussion topic with '${module2.name}' module.")
        ModulesApi.createModuleItem(course.id, teacher.token, module2.id, discussionTopic1.title, ModuleItemTypes.DISCUSSION.stringVal, contentId = discussionTopic1.id.toString())

        Log.d(PREPARATION_TAG, "Associate '${quiz1.title}' page with '${module2.name}' module.")
        ModulesApi.createModuleItem(course.id, teacher.token, module2.id, page1.title, ModuleItemTypes.PAGE.stringVal, pageUrl = page1.url)

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert that '${course.name}' course is displayed.")
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG, "Select '${course.name}' course.")
        dashboardPage.selectCourse(course)

        Log.d(ASSERTION_TAG, "Assert that there are no modules displayed yet because there are not published. Assert that the 'Modules' Tab is not displayed as well.")
        courseBrowserPage.assertInitialBrowserTitle(course)
        courseBrowserPage.assertTabNotDisplayed("Modules")

        Log.d(STEP_TAG, "Click on 'Home' label.")
        courseBrowserPage.selectHome()

        Log.d(ASSERTION_TAG, "Assert that the empty view is displayed on the Modules Page.")
        modulesPage.assertEmptyView()

        Log.d(STEP_TAG, "Navigate back to Course Browser Page.")
        Espresso.pressBack()

        Log.d(PREPARATION_TAG, "Publish '${module1.name}' module.")
        ModulesApi.updateModule(course.id, teacher.token, module1.id, published = true)

        Log.d(PREPARATION_TAG, "Publish '${module2.name}' module.")
        ModulesApi.updateModule(course.id, teacher.token, module2.id, published = true)

        Log.d(ASSERTION_TAG, "Refresh the page. Assert that the 'Modules' Tab is displayed.")
        courseBrowserPage.refresh()
        courseBrowserPage.assertTabDisplayed("Modules")

        Log.d(STEP_TAG, "Navigate to Modules Page.")
        courseBrowserPage.selectModules()

        Log.d(ASSERTION_TAG, "Assert that '${module1.name}' module is displayed with the following items: '${assignment1.name}' assignment, '${quiz1.title}' quiz.")
        modulesPage.assertModuleDisplayed(module1)
        modulesPage.assertModuleItemDisplayed(module1, assignment1.name)
        modulesPage.assertModuleItemDisplayed(module1, quiz1.title)

        Log.d(ASSERTION_TAG, "Assert that '${module2.name}' module is displayed with the following items: '${assignment2.name}' assignment," +
                " '${page1.title}' page, '${discussionTopic1.title}' discussion topic.")
        modulesPage.assertModuleDisplayed(module2)
        modulesPage.assertModuleItemDisplayed(module2, assignment2.name)
        modulesPage.assertModuleItemDisplayed(module2, page1.title)
        modulesPage.assertModuleItemDisplayed(module2, discussionTopic1.title)

        Log.d(STEP_TAG, "Collapse the '${module2.name}' module.")
        modulesPage.clickOnModuleExpandCollapseIcon(module2.name)

        Log.d(ASSERTION_TAG, "Assert that there will be 4 countable items on the screen.")
        modulesPage.assertModulesAndItemsCount(4) // 2 modules titles and 2 module item in first module

        Log.d(STEP_TAG, "Expand the '${module2.name}' module.")
        modulesPage.clickOnModuleExpandCollapseIcon(module2.name)

        Log.d(ASSERTION_TAG, "Assert that there will be 7 countable items on the screen.")
        modulesPage.assertModulesAndItemsCount(7) // 2 modules titles, 2 module items in first module, 3 items in second module

        Log.d(ASSERTION_TAG, "Assert that '${assignment1.name}' module item is displayed and open it." +
                "Assert that the Assignment Details page is displayed with the corresponding assignment name: '${assignment1.name}'.")
        modulesPage.assertAndClickModuleItem(module1.name, assignment1.name, true)
        assignmentDetailsPage.assertPageObjects()
        assignmentDetailsPage.assertAssignmentTitle(assignment1.name)

        Log.d(ASSERTION_TAG, "Assert that the module name, '${module1.name}' is displayed at the bottom.")
        assignmentDetailsPage.moduleItemInteractions.assertModuleNameDisplayed(module1.name)

        Log.d(ASSERTION_TAG, "Assert that the previous arrow button is not displayed because the user is on the first module item's details page, but the next arrow button is displayed.")
        assignmentDetailsPage.moduleItemInteractions.assertPreviousArrowNotDisplayed()
        assignmentDetailsPage.moduleItemInteractions.assertNextArrowDisplayed()

        Log.d(STEP_TAG, "Click on the next arrow button.")
        assignmentDetailsPage.moduleItemInteractions.clickOnNextArrow()

        Log.d(ASSERTION_TAG, "Assert that the '${quiz1.title}' quiz module item's 'Go To Quiz' page is displayed.")
        goToQuizPage.assertQuizTitle(quiz1.title)

        Log.d(ASSERTION_TAG, "Assert that the module name, '${module1.name}' is displayed at the bottom.")
        goToQuizPage.moduleItemInteractions.assertModuleNameDisplayed(module1.name)

        Log.d(ASSERTION_TAG, "Assert that both the previous and the next buttons are displayed (since we are not at the first or the last module item details page).")
        goToQuizPage.moduleItemInteractions.assertPreviousArrowDisplayed()
        goToQuizPage.moduleItemInteractions.assertNextArrowDisplayed()

        Log.d(STEP_TAG, "Click on the next arrow button.")
        goToQuizPage.moduleItemInteractions.clickOnNextArrow()

        Log.d(ASSERTION_TAG, "Assert that the Assignment Details Page is displayed with the corresponding assignment name: '${assignment2.name}'.")
        assignmentDetailsPage.assertPageObjects()
        assignmentDetailsPage.assertAssignmentTitle(assignment2.name)

        Log.d(ASSERTION_TAG, "Assert that the second module name, '${module2.name}' is displayed at the bottom since we can navigate even between modules with these arrows.")
        assignmentDetailsPage.moduleItemInteractions.assertModuleNameDisplayed(module2.name)

        Log.d(ASSERTION_TAG, "Assert that both the previous and the next buttons are displayed (since we are not at the first or the last module item details page, even if it's a first item of a module, but of the second module).")
        assignmentDetailsPage.moduleItemInteractions.assertPreviousArrowDisplayed()
        assignmentDetailsPage.moduleItemInteractions.assertNextArrowDisplayed()

        Log.d(STEP_TAG, "Click on the next arrow button.")
        assignmentDetailsPage.moduleItemInteractions.clickOnNextArrow()

        Log.d(ASSERTION_TAG, "Assert that the '${discussionTopic1.title}' discussion topic module item's details page is displayed.")
        discussionDetailsPage.assertModulesToolbarDiscussionTitle(discussionTopic1.title)

        Log.d(ASSERTION_TAG, "Assert that the second module name, '${module2.name}' is displayed at the bottom.")
        discussionDetailsPage.moduleItemInteractions.assertModuleNameDisplayed(module2.name)

        Log.d(ASSERTION_TAG, "Assert that both the previous and the next buttons are displayed.")
        discussionDetailsPage.moduleItemInteractions.assertPreviousArrowDisplayed()
        discussionDetailsPage.moduleItemInteractions.assertNextArrowDisplayed()

        Log.d(STEP_TAG, "Click on the next arrow button.")
        discussionDetailsPage.moduleItemInteractions.clickOnNextArrow()

        Log.d(ASSERTION_TAG, "Assert that the '${page1.url}' page module item's details page is displayed.")
        pageDetailsPage.webAssertPageUrl(page1.url)

        Log.d(ASSERTION_TAG, "Assert that the second module name, '${module2.name}' is displayed at the bottom.")
        pageDetailsPage.moduleItemInteractions.assertModuleNameDisplayed(module2.name)

        Log.d(ASSERTION_TAG, "Assert that the previous arrow button is displayed but the next arrow button is not displayed because the user is on the last module item's details page.")
        pageDetailsPage.moduleItemInteractions.assertPreviousArrowDisplayed()
        pageDetailsPage.moduleItemInteractions.assertNextArrowNotDisplayed()
    }

}