/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.instructure.student.ui.e2e.offline

import android.util.Log
import com.google.android.material.checkbox.MaterialCheckBox
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.OfflineE2E
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.refresh
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
import com.instructure.student.ui.e2e.offline.utils.OfflineTestUtils
import com.instructure.student.ui.e2e.offline.utils.OfflineTestUtils.assertOfflineIndicator
import com.instructure.student.ui.e2e.offline.utils.OfflineTestUtils.waitForNetworkToGoOffline
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Test

@HiltAndroidTest
class OfflineModulesE2ETest : StudentTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @OfflineE2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.MODULES, TestCategory.E2E, SecondaryFeatureCategory.OFFLINE_MODE)
    fun testOfflineModulesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1, modules = 2)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val testModule1 = data.modulesList[0]
        val testModule2 = data.modulesList[1]

        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course.")
        val testAssignment1 = AssignmentsApi.createAssignment(course.id, teacher.token, withDescription = true, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(
            SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG, "Seeding another assignment for '${course.name}' course.")
        val testAssignment2 = AssignmentsApi.createAssignment(course.id, teacher.token, dueAt = 2.days.fromNow.iso8601, withDescription = true, gradingType = GradingType.POINTS, pointsPossible = 15.0, submissionTypes = listOf(
            SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG, "Create a PUBLISHED quiz for '${course.name}' course.")
        val testQuiz = QuizzesApi.createQuiz(course.id, teacher.token, dueAt = 3.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG, "Create a page for '${course.name}' course.")
        val testPage = PagesApi.createCoursePage(course.id, teacher.token)

        Log.d(PREPARATION_TAG, "Create a discussion topic for '${course.name}' course.")
        val testDiscussionTopic = DiscussionTopicsApi.createDiscussion(course.id, teacher.token)

        Log.d(PREPARATION_TAG, "Associate '${testAssignment1.name}' assignment with '${testModule1.name}' module.")
        ModulesApi.createModuleItem(course.id, teacher.token, testModule1.id, testAssignment1.name, ModuleItemTypes.ASSIGNMENT.stringVal, contentId = testAssignment1.id.toString())

        Log.d(PREPARATION_TAG, "Associate '${testQuiz.title}' quiz with '${testModule1.name}' module.")
        ModulesApi.createModuleItem(course.id, teacher.token, testModule1.id, testQuiz.title, ModuleItemTypes.QUIZ.stringVal, contentId = testQuiz.id.toString())

        Log.d(PREPARATION_TAG, "Associate '${testAssignment2.name}' assignment with '${testModule2.name}' module.")
        ModulesApi.createModuleItem(course.id, teacher.token, testModule2.id, testAssignment2.name, ModuleItemTypes.ASSIGNMENT.stringVal, contentId = testAssignment2.id.toString())

        Log.d(PREPARATION_TAG, "Associate '${testDiscussionTopic.title}' discussion topic with '${testModule2.name}' module.")
        ModulesApi.createModuleItem(course.id, teacher.token, testModule2.id, testDiscussionTopic.title, ModuleItemTypes.DISCUSSION.stringVal, contentId = testDiscussionTopic.id.toString())

        Log.d(PREPARATION_TAG, "Associate '${testQuiz.title}' page with '${testModule2.name}' module.")
        ModulesApi.createModuleItem(course.id, teacher.token, testModule2.id, testPage.title, ModuleItemTypes.PAGE.stringVal, pageUrl = testPage.url)

        Log.d(PREPARATION_TAG, "Publish '${testModule1.name}' module.")
        ModulesApi.updateModule(course.id, teacher.token, testModule1.id, published = true)

        Log.d(PREPARATION_TAG, "Publish '${testModule2.name}' module.")
        ModulesApi.updateModule(course.id, teacher.token, testModule2.id, published = true)

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the '${course.name}' course's 'Manage Offline Content' page via the more menu of the Dashboard Page.")
        dashboardPage.clickCourseOverflowMenu(course.name, "Manage Offline Content")

        Log.d(ASSERTION_TAG, "Assert that the '${course.name}' course's checkbox state is 'Unchecked'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course.name, MaterialCheckBox.STATE_UNCHECKED)

        Log.d(STEP_TAG, "Expand the course. Select the 'Modules' of '${course.name}' course for sync. Click on the 'Sync' button.")
        manageOfflineContentPage.expandCollapseItem(course.name)
        manageOfflineContentPage.changeItemSelectionState("Modules")
        manageOfflineContentPage.clickOnSyncButtonAndConfirm()

        Log.d(ASSERTION_TAG, "Assert that the offline sync icon only displayed on the synced course's course card.")
        dashboardPage.assertCourseOfflineSyncIconVisible(course.name)
        device.waitForIdle()

        Log.d(PREPARATION_TAG, "Turn off the Wi-Fi and Mobile Data on the device, so it will go offline.")
        turnOffConnectionViaADB()
        waitForNetworkToGoOffline(device)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Refresh the page.")
        dashboardPage.waitForRender()
        refresh()

        Log.d(STEP_TAG, "Select '${course.name}' course and click on 'Modules' tab to navigate to the Module List Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectModules()

        Log.d(ASSERTION_TAG, "Assert that the Offline Indicator (bottom banner) is displayed on the Module List Page.")
        assertOfflineIndicator()

        Log.d(ASSERTION_TAG, "Assert that '${testModule1.name}' module is displayed with the following items: '${testAssignment1.name}' assignment, '${testQuiz.title}' quiz.")
        modulesPage.assertModuleDisplayed(testModule1)
        modulesPage.assertModuleItemDisplayed(testModule1, testAssignment1.name)
        modulesPage.assertModuleItemDisplayed(testModule1, testQuiz.title)

        Log.d(ASSERTION_TAG, "Assert that '${testModule2.name}' module is displayed with the following items: '${testAssignment2.name}' assignment," +
                " '${testPage.title}' page, '${testDiscussionTopic.title}' discussion topic.")
        modulesPage.assertModuleDisplayed(testModule2)
        modulesPage.assertModuleItemDisplayed(testModule2, testAssignment2.name)
        modulesPage.assertModuleItemDisplayed(testModule2, testPage.title)
        modulesPage.assertModuleItemDisplayed(testModule2, testDiscussionTopic.title)

        Log.d(STEP_TAG, "Collapse the '${testModule2.name}' module.")
        modulesPage.clickOnModuleExpandCollapseIcon(testModule2.name)

        Log.d(ASSERTION_TAG, "Assert that there will be 4 countable items on the screen.")
        modulesPage.assertModulesAndItemsCount(4) // 2 modules titles and 2 module item in first module

        Log.d(STEP_TAG, "Expand the '${testModule2.name}' module.")
        modulesPage.clickOnModuleExpandCollapseIcon(testModule2.name)

        Log.d(ASSERTION_TAG, "Assert that there will be 7 countable items on the screen.")
        modulesPage.assertModulesAndItemsCount(7) // 2 modules titles, 2 module items in first module, 3 items in second module

        Log.d(ASSERTION_TAG, "Assert that '${testAssignment1.name}' module item is displayed and open it.")
        modulesPage.assertAndClickModuleItem(testModule1.name, testAssignment1.name, true)
        assignmentDetailsPage.assertDisplayToolbarTitle(testAssignment1.name)

        Log.d(ASSERTION_TAG, "Assert that the 'Not Available Offline' content is displayed.")
        assignmentDetailsPage.assertDetailsNotAvailableOffline()

        Log.d(ASSERTION_TAG, "Assert that the module name, '${testModule1.name}' is displayed at the bottom.")
        assignmentDetailsPage.moduleItemInteractions.assertModuleNameDisplayed(testModule1.name)

        Log.d(ASSERTION_TAG, "Assert that the previous arrow button is not displayed because the user is on the first module item's details page, but the next arrow button is displayed.")
        assignmentDetailsPage.moduleItemInteractions.assertPreviousArrowNotDisplayed()
        assignmentDetailsPage.moduleItemInteractions.assertNextArrowDisplayed()

        Log.d(STEP_TAG, "Click on the next arrow button.")
        assignmentDetailsPage.moduleItemInteractions.clickOnNextArrow()

        Log.d(ASSERTION_TAG, "Assert that the '${testQuiz.title}' quiz module item's 'Go To Quiz' page is displayed.")
        goToQuizPage.assertQuizTitle(testQuiz.title)

        Log.d(STEP_TAG, "Click on the 'Go To Quiz' button.")
        goToQuizPage.clickGoToQuizButton()

        Log.d(ASSERTION_TAG, "Assert that the 'No Internet Connection' dialog is popping-up.")
        OfflineTestUtils.assertNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Dismiss the 'No Internet Connection' dialog.")
        OfflineTestUtils.dismissNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Assert that the module name, '${testModule1.name}' is displayed at the bottom.")
        goToQuizPage.moduleItemInteractions.assertModuleNameDisplayed(testModule1.name)

        Log.d(ASSERTION_TAG, "Assert that both the previous and the next buttons are displayed (since we are not at the first or the last module item details page).")
        goToQuizPage.moduleItemInteractions.assertPreviousArrowDisplayed()
        goToQuizPage.moduleItemInteractions.assertNextArrowDisplayed()

        Log.d(STEP_TAG, "Click on the next arrow button.")
        goToQuizPage.moduleItemInteractions.clickOnNextArrow()

        Log.d(ASSERTION_TAG, "Assert that the Assignment Details Page is displayed with the corresponding assignment name: '${testAssignment2.name}'.")
        assignmentDetailsPage.assertDisplayToolbarTitle(testAssignment2.name)

        Log.d(ASSERTION_TAG, "Assert that the 'Not Available Offline' content is displayed.")
        assignmentDetailsPage.assertDetailsNotAvailableOffline()

        Log.d(ASSERTION_TAG, "Assert that the second module name, '${testModule2.name}' is displayed at the bottom since we can navigate even between modules with these arrows.")
        assignmentDetailsPage.moduleItemInteractions.assertModuleNameDisplayed(testModule2.name)

        Log.d(ASSERTION_TAG, "Assert that both the previous and the next buttons are displayed (since we are not at the first or the last module item details page, even if it's a first item of a module, but of the second module).")
        assignmentDetailsPage.moduleItemInteractions.assertPreviousArrowDisplayed()
        assignmentDetailsPage.moduleItemInteractions.assertNextArrowDisplayed()

        Log.d(STEP_TAG, "Click on the next arrow button.")
        assignmentDetailsPage.moduleItemInteractions.clickOnNextArrow()

        Log.d(ASSERTION_TAG, "Assert that the '${testDiscussionTopic.title}' discussion topic module item's details page is displayed.")
        discussionDetailsPage.assertModulesToolbarDiscussionTitle(testDiscussionTopic.title)

        Log.d(ASSERTION_TAG, "Assert that the 'Not Available Offline' content is displayed.")
        discussionDetailsPage.assertDetailsNotAvailableOffline()

        Log.d(ASSERTION_TAG, "Assert that the second module name, '${testModule2.name}' is displayed at the bottom.")
        discussionDetailsPage.moduleItemInteractions.assertModuleNameDisplayed(testModule2.name)

        Log.d(ASSERTION_TAG, "Assert that both the previous and the next buttons are displayed.")
        discussionDetailsPage.moduleItemInteractions.assertPreviousArrowDisplayed()
        discussionDetailsPage.moduleItemInteractions.assertNextArrowDisplayed()

        Log.d(STEP_TAG, "Click on the next arrow button.")
        discussionDetailsPage.moduleItemInteractions.clickOnNextArrow()

        Log.d(ASSERTION_TAG, "Assert that the '${testPage.url}' page module item's details page is displayed.")
        pageDetailsPage.webAssertPageUrl(testPage.url)

        Log.d(ASSERTION_TAG, "Assert that the second module name, '${testModule2.name}' is displayed at the bottom.")
        pageDetailsPage.moduleItemInteractions.assertModuleNameDisplayed(testModule2.name)

        Log.d(ASSERTION_TAG, "Assert that the previous arrow button is displayed but the next arrow button is not displayed because the user is on the last module item's details page.")
        pageDetailsPage.moduleItemInteractions.assertPreviousArrowDisplayed()
        pageDetailsPage.moduleItemInteractions.assertNextArrowNotDisplayed()
    }

    @After
    fun tearDown() {
        Log.d(PREPARATION_TAG, "Turn back on the Wi-Fi and Mobile Data on the device, so it will come back online.")
        turnOnConnectionViaADB()
    }

}