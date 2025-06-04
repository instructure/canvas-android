/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.parentapp.ui.e2e

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.checkToastText
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.ViewUtils
import com.instructure.pandautils.utils.AppTheme
import com.instructure.parentapp.R
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.seedData
import com.instructure.parentapp.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class SettingsE2ETest : ParentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testDarkModeE2E() {
        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, parents = 1, courses = 1)
        val parent = data.parentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course.")
        val testAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(
            SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Navigate to User Settings Page.")
        leftSideNavigationDrawerPage.clickSettings()

        Log.d(STEP_TAG, "Select Dark App Theme and assert that the App Theme Title and Status has the proper text color (which is used in Dark mode).")
        settingsPage.selectAppTheme(AppTheme.DARK)

        Log.d(STEP_TAG, "Navigate back to Dashboard.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the course label has the proper text color (which is used in Dark mode).")
        coursesPage.assertCourseLabelTextColor(course, 0xFFFFFFFF)

        Log.d(STEP_TAG, "Select '${course.name}' course.")
        coursesPage.clickCourseItem(course.name)

        Log.d(ASSERTION_TAG, "Assert on the Course Browser Page that the assignment label has the proper text color (which is used in Dark mode).")
        courseDetailsPage.assertAssignmentLabelTextColor(testAssignment.name,0xFFFFFFFF)

        Log.d(STEP_TAG, "Navigate back and open the Left Side Navigation Drawer menu.")
        Espresso.pressBack()
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Navigate to Settings Page and open App Theme Settings again.")
        leftSideNavigationDrawerPage.clickSettings()

        Log.d(STEP_TAG, "Select Light App Theme and assert that the App Theme Title and Status has the proper text color (which is used in Light mode).")
        settingsPage.selectAppTheme(AppTheme.LIGHT)

        Log.d(STEP_TAG, "Navigate back to Dashboard.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the course label has the proper text color (which is used in Light mode).")
        coursesPage.assertCourseLabelTextColor(course, 0xFF273540)

        Log.d(STEP_TAG, "Select '${course.name}' course.")
        coursesPage.clickCourseItem(course.name)

        Log.d(ASSERTION_TAG, "Assert on the Course Browser Page that the assignment label has the proper text color (which is used in Dark mode).")
        courseDetailsPage.assertAssignmentLabelTextColor(testAssignment.name,0xFF273540)
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testLegalPageE2E() {
        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, parents = 1, courses = 1)
        val parent = data.parentsList[0]

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Navigate to User Settings Page.")
        leftSideNavigationDrawerPage.clickSettings()

        Log.d(STEP_TAG, "Open Legal Page.")
        settingsPage.clickOnSettingsItem("Legal")

        Log.d(ASSERTION_TAG, "Assert that all the corresponding buttons are displayed.")
        legalPage.assertPageObjects()
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testAboutE2E() {
        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, parents = 1, courses = 1)
        val parent = data.parentsList[0]

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Navigate to User Settings Page.")
        leftSideNavigationDrawerPage.clickSettings()

        Log.d(STEP_TAG, "Click on 'About' link to open About Page. Assert that About Page has opened.")
        settingsPage.clickOnSettingsItem("About")
        aboutPage.assertPageObjects()

        Log.d(STEP_TAG, "Check that domain is equal to: '${parent.domain}' (parent's domain).")
        aboutPage.domainIs(parent.domain)

        Log.d(STEP_TAG, "Check that Login ID is equal to: '${parent.loginId}' (parent's Login ID).")
        aboutPage.loginIdIs(parent.loginId)

        Log.d(STEP_TAG, "Check that e-mail is equal to: '${parent.loginId}' (parent's Login ID).")
        aboutPage.emailIs(parent.loginId)

        Log.d(ASSERTION_TAG, "Assert that the Instructure company logo has been displayed on the About page.")
        aboutPage.assertInstructureLogoDisplayed()
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.E2E, SecondaryFeatureCategory.INBOX_SIGNATURE)
    fun testInboxSignatureE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, courses = 1, parents = 1)
        val course = data.coursesList[0]
        val parent = data.parentsList[0]
        val student = data.studentsList[0]

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Navigate to User Settings Page.")
        leftSideNavigationDrawerPage.clickSettings()

        Log.d(ASSERTION_TAG, "Assert that by default the Inbox Signature is 'Not Set'.")
        settingsPage.assertSettingsItemDisplayed("Inbox Signature", "Not Set")

        Log.d(STEP_TAG, "Click on the 'Inbox Signature' settings.")
        settingsPage.clickOnSettingsItem("Inbox Signature")

        Log.d(ASSERTION_TAG, "Assert that by default the 'Inbox Signature' toggle is turned off.")
        inboxSignatureSettingsPage.assertSignatureEnabledState(false)

        val signatureText = "President of AC Milan\nVice President of Ferencvaros"
        Log.d(STEP_TAG, "Turn on the 'Inbox Signature' and set the inbox signature text to: '$signatureText'. Save the changes.")
        inboxSignatureSettingsPage.toggleSignatureEnabledState()
        inboxSignatureSettingsPage.changeSignatureText(signatureText)
        inboxSignatureSettingsPage.saveChanges()

        Log.d(ASSERTION_TAG, "Assert that the 'Inbox settings saved!' toast message is displayed.")
        checkToastText(R.string.inboxSignatureSettingsUpdated, activityRule.activity)

        Log.d(STEP_TAG, "Refresh the Settings page.")
        settingsPage.refresh()

        Log.d(ASSERTION_TAG, "Assert that the Inbox Signature became 'Enabled'.")
        settingsPage.assertSettingsItemDisplayed("Inbox Signature", "Enabled")

        Log.d(STEP_TAG, "Click on the 'Inbox Signature' settings.")
        settingsPage.clickOnSettingsItem("Inbox Signature")

        Log.d(ASSERTION_TAG, "Assert that the previously changed inbox signature text has been really set to: '$signatureText' and the toggle has turned off.")
        inboxSignatureSettingsPage.assertSignatureText(signatureText)
        inboxSignatureSettingsPage.assertSignatureEnabledState(true)

        Log.d(STEP_TAG, "Navigate back to the Dashboard.")
        ViewUtils.pressBackButton(2)

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Open 'Inbox' menu.")
        leftSideNavigationDrawerPage.clickInbox()

        Log.d(STEP_TAG, "Click on 'New Message' button.")
        inboxPage.pressNewMessageButton()
        inboxCoursePickerPage.selectCourseWithUser(course.name, student.shortName)

        Log.d(ASSERTION_TAG, "Assert that the previously set inbox signature text is displayed by default when the user opens the Compose New Message Page.")
        inboxComposeMessagePage.assertBodyText("\n\n---\nPresident of AC Milan\nVice President of Ferencvaros")
    }

    @E2E
    @Test
    @TestMetaData(Priority.BUG_CASE, FeatureCategory.INBOX, TestCategory.E2E, SecondaryFeatureCategory.INBOX_SIGNATURE)
    fun testInboxSignaturesWithDifferentUsersE2E() {

        //Bug Ticket: MBL-18840
        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, courses = 1, parents = 2)
        val course = data.coursesList[0]
        val parent = data.parentsList[0]
        val parent2 = data.parentsList[1]
        val student = data.studentsList[0]

        Log.d(STEP_TAG, "Click 'Find My School' button.")
        loginLandingPage.clickFindMySchoolButton()

        Log.d(STEP_TAG, "Enter domain: '${CanvasNetworkAdapter.canvasDomain}'")
        loginFindSchoolPage.enterDomain(CanvasNetworkAdapter.canvasDomain)

        Log.d(STEP_TAG, "Click on 'Next' button on the toolbar.")
        loginFindSchoolPage.clickToolbarNextMenuItem()

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        loginSignInPage.loginAs(parent)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Navigate to Settings Page on the Left Side Navigation Drawer menu.")
        leftSideNavigationDrawerPage.clickSettings()

        Log.d(ASSERTION_TAG, "Assert that by default the Inbox Signature is 'Not Set'.")
        settingsPage.assertSettingsItemDisplayed("Inbox Signature", "Not Set")

        Log.d(STEP_TAG, "Click on the 'Inbox Signature' settings.")
        settingsPage.clickOnSettingsItem("Inbox Signature")

        Log.d(ASSERTION_TAG, "Assert that by default the 'Inbox Signature' toggle is turned off.")
        inboxSignatureSettingsPage.assertSignatureEnabledState(false)

        val firstSignatureText = "President of AC Milan\nVice President of Ferencvaros"
        Log.d(STEP_TAG, "Turn on the 'Inbox Signature' and set the inbox signature text to: '$firstSignatureText'. Save the changes.")
        inboxSignatureSettingsPage.toggleSignatureEnabledState()
        inboxSignatureSettingsPage.changeSignatureText(firstSignatureText)
        inboxSignatureSettingsPage.saveChanges()

        Log.d(ASSERTION_TAG, "Assert that the 'Inbox settings saved!' toast message is displayed.")
        checkToastText(R.string.inboxSignatureSettingsUpdated, activityRule.activity)

        Log.d(STEP_TAG, "Refresh the Settings page.")
        settingsPage.refresh()

        Log.d(ASSERTION_TAG, "Assert that the Inbox Signature became 'Enabled'.")
        settingsPage.assertSettingsItemDisplayed("Inbox Signature", "Enabled")

        Log.d(STEP_TAG, "Navigate back to the Dashboard.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Open 'Inbox' menu.")
        leftSideNavigationDrawerPage.clickInbox()

        Log.d(STEP_TAG, "Click on 'New Message' button.")
        inboxPage.pressNewMessageButton()
        inboxCoursePickerPage.selectCourseWithUser(course.name, student.shortName)

        Log.d(ASSERTION_TAG, "Assert that the previously set inbox signature text, '$firstSignatureText' is displayed by default when the user opens the Compose New Message Page.")
        inboxComposeMessagePage.assertBodyText("\n\n---\nPresident of AC Milan\nVice President of Ferencvaros")

        Log.d(STEP_TAG, "Click on the 'Close' (X) button on the Compose New Message Page.")
        inboxComposeMessagePage.clickOnCloseButton()

        Log.d(STEP_TAG, "Navigate back to the Dashboard Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Click on 'Change User' button on the Left Side Navigation Drawer menu.")
        leftSideNavigationDrawerPage.clickChangeUser()

        Log.d(STEP_TAG, "Click on the 'Find another school' button.")
        loginLandingPage.clickFindAnotherSchoolButton()

        Log.d(STEP_TAG, "Enter domain: '${CanvasNetworkAdapter.canvasDomain}'")
        loginFindSchoolPage.enterDomain(CanvasNetworkAdapter.canvasDomain)

        Log.d(STEP_TAG, "Click on 'Next' button on the toolbar.")
        loginFindSchoolPage.clickToolbarNextMenuItem()

        Log.d(STEP_TAG, "Login with the other user: '${parent2.name}', login id: '${parent2.loginId}'.")
        loginSignInPage.loginAs(parent2)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Open 'Inbox' menu.")
        leftSideNavigationDrawerPage.clickInbox()

        Log.d(STEP_TAG, "Click on 'New Message' button.")
        inboxPage.pressNewMessageButton()
        inboxCoursePickerPage.selectCourseWithUser(course.name, student.shortName)

        Log.d(ASSERTION_TAG, "Assert that the previously set inbox signature text is NOT displayed since it was set for another user.")
        inboxComposeMessagePage.assertBodyText("")

        Log.d(STEP_TAG, "Click on the 'Close' (X) button on the Compose New Message Page.")
        inboxComposeMessagePage.clickOnCloseButton()

        Log.d(STEP_TAG, "Navigate back to the Dashboard Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Navigate to Settings Page on the Left Side Navigation Drawer menu.")
        leftSideNavigationDrawerPage.clickSettings()

        Log.d(ASSERTION_TAG, "Assert that by default the Inbox Signature is 'Not Set'.")
        settingsPage.assertSettingsItemDisplayed("Inbox Signature", "Not Set")

        Log.d(STEP_TAG, "Click on the 'Inbox Signature' settings.")
        settingsPage.clickOnSettingsItem("Inbox Signature")

        Log.d(ASSERTION_TAG, "Assert that by default the 'Inbox Signature' toggle is turned off.")
        inboxSignatureSettingsPage.assertSignatureEnabledState(false)

        val secondSignatureText = "Loyal member of Instructure"

        Log.d(STEP_TAG, "Turn on the 'Inbox Signature' and set the inbox signature text to: '$secondSignatureText'. Save the changes.")
        inboxSignatureSettingsPage.toggleSignatureEnabledState()
        inboxSignatureSettingsPage.changeSignatureText(secondSignatureText)
        inboxSignatureSettingsPage.saveChanges()

        Log.d(STEP_TAG, "Refresh the Settings page.")
        settingsPage.refresh()

        Log.d(ASSERTION_TAG, "Assert that the Inbox Signature became 'Enabled'.")
        settingsPage.assertSettingsItemDisplayed("Inbox Signature", "Enabled")

        Log.d(STEP_TAG, "Navigate back to the Dashboard.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Open 'Inbox' menu.")
        leftSideNavigationDrawerPage.clickInbox()

        Log.d(STEP_TAG,"Click on 'New Message' button.")
        inboxPage.pressNewMessageButton()
        inboxCoursePickerPage.selectCourseWithUser(course.name, student.shortName)

        Log.d(ASSERTION_TAG, "Assert that the previously set inbox signature, '$secondSignatureText' text is displayed by default when the user opens the Compose New Message Page.")
        inboxComposeMessagePage.assertBodyText("\n\n---\nLoyal member of Instructure")

        Log.d(STEP_TAG, "Click on the 'Close' (X) button on the Compose New Message Page.")
        inboxComposeMessagePage.clickOnCloseButton()

        Log.d(STEP_TAG, "Navigate back to the Dashboard Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Click on 'Change User' button on the Left Side Navigation Drawer menu.")
        leftSideNavigationDrawerPage.clickChangeUser()

        Log.d(STEP_TAG, "Login with user : '${parent.name}', login id: '${parent.loginId}' with 'one-click' login by selecting it from the 'Previous Logins' section.")
        loginLandingPage.loginWithPreviousUser(parent)

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Open 'Inbox' menu.")
        leftSideNavigationDrawerPage.clickInbox()

        Log.d(STEP_TAG, "Click on 'New Message' button.")
        inboxPage.pressNewMessageButton()
        inboxCoursePickerPage.selectCourseWithUser(course.name, student.shortName)

        Log.d(ASSERTION_TAG, "Assert that the previously set inbox signature text is displayed by default since we logged back with '${parent.name}' teacher.")
        inboxComposeMessagePage.assertBodyText("\n\n---\nPresident of AC Milan\nVice President of Ferencvaros")
    }
}