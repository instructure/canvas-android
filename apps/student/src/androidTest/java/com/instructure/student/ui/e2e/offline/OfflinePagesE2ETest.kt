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
import androidx.test.espresso.Espresso
import androidx.test.espresso.web.webdriver.Locator
import com.google.android.material.checkbox.MaterialCheckBox
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.OfflineE2E
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.dataseeding.util.ApiManager
import com.instructure.student.ui.e2e.offline.utils.OfflineTestUtils
import com.instructure.student.ui.e2e.offline.utils.OfflineTestUtils.assertOfflineIndicator
import com.instructure.student.ui.e2e.offline.utils.OfflineTestUtils.waitForNetworkToGoOffline
import com.instructure.student.ui.pages.WebViewTextCheck
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Test

@HiltAndroidTest
class OfflinePagesE2ETest : StudentTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @OfflineE2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.PAGES, TestCategory.E2E, SecondaryFeatureCategory.OFFLINE_MODE)
    fun testOfflinePagesE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seed an UNPUBLISHED page for '${course.name}' course.")
        val pageUnpublished = ApiManager.createPage(course, teacher, published = false, frontPage = false)

        Log.d(PREPARATION_TAG,"Seed a PUBLISHED page for '${course.name}' course.")
        val pagePublished = ApiManager.createPage(course, teacher, published = true, frontPage = false, editingRoles = "teachers,students", body = "<h1 id=\"header1\">Regular Page Text</h1>")

        Log.d(PREPARATION_TAG,"Seed a PUBLISHED, but NOT editable page for '${course.name}' course.")
        val pageNotEditable = ApiManager.createPage(course, teacher, published = true, frontPage = false, body = "<h1 id=\"header1\">Regular Page Text</h1>")

        Log.d(PREPARATION_TAG,"Seed a PUBLISHED, FRONT page for '${course.name}' course.")
        val pagePublishedFront = ApiManager.createPage(course, teacher, published = true, frontPage = true, editingRoles = "public", body = "<h1 id=\"header1\">Front Page Text</h1>")

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the '${course.name}' course's 'Manage Offline Content' page via the more menu of the Dashboard Page.")
        dashboardPage.clickCourseOverflowMenu(course.name, "Manage Offline Content")

        Log.d(STEP_TAG, "Assert that the '${course.name}' course's checkbox state is 'Unchecked'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course.name, MaterialCheckBox.STATE_UNCHECKED)

        Log.d(STEP_TAG, "Expand the course. Select the 'Pages' of '${course.name}' course for sync. Click on the 'Sync' button.")
        manageOfflineContentPage.expandCollapseItem(course.name)
        manageOfflineContentPage.changeItemSelectionState("Pages")
        manageOfflineContentPage.clickOnSyncButtonAndConfirm()

        Log.d(STEP_TAG, "Wait for the 'Download Started' and 'Syncing Offline Content' dashboard notifications to be displayed, and then to disappear.")
        dashboardPage.waitForOfflineSyncDashboardNotifications()

        Log.d(PREPARATION_TAG, "Turn off the Wi-Fi and Mobile Data on the device, so it will go offline.")
        turnOffConnectionViaADB()
        waitForNetworkToGoOffline(device)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Refresh the page.")
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Select '${course.name}' course and click on 'Pages' tab to navigate to the Page List Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectPages()

        Log.d(STEP_TAG, "Assert that the Offline Indicator (bottom banner) is displayed on the Page List Page.")
        assertOfflineIndicator()

        Log.d(STEP_TAG,"Assert that '${pagePublishedFront.title}' published front page is displayed.")
        pageListPage.assertFrontPageDisplayed(pagePublishedFront)

        Log.d(STEP_TAG,"Assert that '${pagePublished.title}' published page is displayed.")
        pageListPage.assertRegularPageDisplayed(pagePublished)

        Log.d(STEP_TAG,"Assert that '${pageUnpublished.title}' unpublished page is NOT displayed.")
        pageListPage.assertPageNotDisplayed(pageUnpublished)

        Log.d(STEP_TAG, "Click on 'Search' (magnifying glass) icon and type '${pagePublishedFront.title}', the page's name to the search input field.")
        pageListPage.searchable.clickOnSearchButton()
        pageListPage.searchable.typeToSearchBar(pagePublishedFront.title)

        Log.d(STEP_TAG,"Assert that '${pagePublished.title}' published page is NOT displayed and there is only one page (the front page) is displayed.")
        pageListPage.assertPageNotDisplayed(pagePublished)
        pageListPage.assertPageListItemCount(1)

        Log.d(STEP_TAG, "Click on clear search icon (X).")
        pageListPage.searchable.clickOnClearSearchButton()

        Log.d(STEP_TAG,"Assert that '${pagePublishedFront.title}' published front page is displayed.")
        pageListPage.assertFrontPageDisplayed(pagePublishedFront)

        Log.d(STEP_TAG,"Assert that '${pagePublished.title}' published page is displayed.")
        pageListPage.assertRegularPageDisplayed(pagePublished)

        Log.d(STEP_TAG,"Assert that '${pageUnpublished.title}' unpublished page is NOT displayed.")
        pageListPage.assertPageNotDisplayed(pageUnpublished)

        Log.d(STEP_TAG,"Open '${pagePublishedFront.title}' page. Assert that it is really a front (published) page via web view assertions.")
        pageListPage.selectFrontPage(pagePublishedFront)
        canvasWebViewPage.runTextChecks(WebViewTextCheck(Locator.ID, "header1", "Front Page Text"))

        Log.d(STEP_TAG,"Navigate back to Pages page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Select '${pageNotEditable.title}' page. Assert that it is not editable as a student, then navigate back to Page List page.")
        pageListPage.selectRegularPage(pageNotEditable)
        canvasWebViewPage.assertDoesNotEditable()
        Espresso.pressBack()

        Log.d(STEP_TAG,"Open '${pagePublished.title}' page. Assert that it is really a regular published page via web view assertions.")
        pageListPage.selectRegularPage(pagePublished)
        canvasWebViewPage.runTextChecks(WebViewTextCheck(Locator.ID, "header1", "Regular Page Text"))

        Log.d(STEP_TAG, "Click on the 'Pencil' icon. Assert that the 'No Internet Connection' dialog has displayed. Dismiss the dialog by accepting it.")
        canvasWebViewPage.clickEditPencilIcon()
        OfflineTestUtils.assertNoInternetConnectionDialog()
        OfflineTestUtils.dismissNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Navigate back to Page List page. Select '${pagePublishedFront.title}' front page.")
        Espresso.pressBack()
        pageListPage.selectFrontPage(pagePublishedFront)

        Log.d(STEP_TAG, "Click on the 'Pencil' icon. Assert that the 'No Internet Connection' dialog has displayed. Dismiss the dialog by accepting it.")
        canvasWebViewPage.clickEditPencilIcon()
        OfflineTestUtils.assertNoInternetConnectionDialog()
        OfflineTestUtils.dismissNoInternetConnectionDialog()
    }

    @After
    fun tearDown() {
        Log.d(PREPARATION_TAG, "Turn back on the Wi-Fi and Mobile Data on the device, so it will come back online.")
        turnOnConnectionViaADB()
    }

}