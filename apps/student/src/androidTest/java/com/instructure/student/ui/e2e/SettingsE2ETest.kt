/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.e2e

import android.content.Intent
import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.checkToastText
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import com.instructure.dataseeding.api.ConversationsApi
import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.EnrollmentsApi
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import com.instructure.espresso.ViewUtils
import com.instructure.pandautils.utils.AppTheme
import com.instructure.student.BuildConfig
import com.instructure.student.R
import com.instructure.student.ui.utils.IntentActionMatcher
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Assert
import org.junit.Test

@HiltAndroidTest
class SettingsE2ETest : StudentComposeTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @After
    fun tearDown() {
        Intents.release()
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testProfileSettingsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to User Settings Page.")
        leftSideNavigationDrawerPage.clickSettingsMenu()
        settingsPage.assertPageObjects()

        Log.d(STEP_TAG, "Open Profile Settings Page.")
        settingsPage.clickOnSettingsItem("Profile Settings")
        profileSettingsPage.assertPageObjects()

        val newUserName = "John Doe"
        Log.d(STEP_TAG, "Edit username to: '$newUserName'. Click on 'Save' button.")
        profileSettingsPage.changeUserNameTo(newUserName)

        Log.d(STEP_TAG, "Navigate back to Dashboard Page. Assert that the username has been changed to '$newUserName'.")
        ViewUtils.pressBackButton(2)
        leftSideNavigationDrawerPage.assertUserLoggedIn(newUserName)

        val originalSavedPandaAvatarCount = getSavedPandaAvatarCount()

        Log.d(STEP_TAG, "Navigate to Settings Page again and open Panda Avatar Creator.")
        leftSideNavigationDrawerPage.clickSettingsMenu()
        settingsPage.assertPageObjects()
        settingsPage.clickOnSettingsItem("Profile Settings")
        profileSettingsPage.assertPageObjects()
        profileSettingsPage.launchPandaAvatarCreator()

        Log.d(STEP_TAG, "Set panda avatar head.")
        pandaAvatarPage.selectChangeHead()
        pandaAvatarPage.choosePart(R.string.content_description_panda_head_4)
        pandaAvatarPage.clickBackButton()

        Log.d(STEP_TAG, "Set panda avatar body.")
        pandaAvatarPage.selectChangeBody()
        pandaAvatarPage.choosePart(R.string.content_description_panda_body_4)
        pandaAvatarPage.clickBackButton()

        Log.d(STEP_TAG, "Set panda avatar legs.")
        pandaAvatarPage.selectChangeLegs()
        pandaAvatarPage.choosePart(R.string.content_description_panda_feet_5)
        pandaAvatarPage.clickBackButton()

        Log.d(STEP_TAG, "Click on 'Save as avatar' button.")
        pandaAvatarPage.save()

        val newSavedPandaAvatarCount = getSavedPandaAvatarCount()
        Log.d(STEP_TAG, "Assert that saved panda avatar count has increased by one. Old value: $originalSavedPandaAvatarCount, new value: $newSavedPandaAvatarCount.")
        Assert.assertTrue(newSavedPandaAvatarCount == originalSavedPandaAvatarCount + 1)
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testDarkModeE2E() {
        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val course = data.coursesList[0]

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to User Settings Page.")
        leftSideNavigationDrawerPage.clickSettingsMenu()
        settingsPage.assertPageObjects()


        Log.d(STEP_TAG,"Select Dark App Theme and assert that the App Theme Title and Status has the proper text color (which is used in Dark mode).")
        settingsPage.selectAppTheme(AppTheme.DARK)

        Log.d(STEP_TAG,"Navigate back to Dashboard. Assert that the 'Courses' label has the proper text color (which is used in Dark mode).")
        Espresso.pressBack()
        dashboardPage.assertCourseLabelTextColor("#FFFFFFFF")

        Log.d(STEP_TAG,"Select ${course.name} course and assert on the Course Browser Page that the tabs has the proper text color (which is used in Dark mode).")
        dashboardPage.selectCourse(course)
        courseBrowserPage.assertTabLabelTextColor("Discussions","#FFFFFFFF")
        courseBrowserPage.assertTabLabelTextColor("Grades","#FFFFFFFF")

        Log.d(STEP_TAG,"Navigate to Settings Page and open App Theme Settings again.")
        Espresso.pressBack()
        leftSideNavigationDrawerPage.clickSettingsMenu()

        Log.d(STEP_TAG,"Select Light App Theme and assert that the App Theme Title and Status has the proper text color (which is used in Light mode).")
        settingsPage.selectAppTheme(AppTheme.LIGHT)

        Log.d(STEP_TAG,"Navigate back to Dashboard. Assert that the 'Courses' label has the proper text color (which is used in Light mode).")
        Espresso.pressBack()
        dashboardPage.assertCourseLabelTextColor("#FF273540")
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testLegalPageE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to Settings Page on the left-side menu.")
        leftSideNavigationDrawerPage.clickSettingsMenu()
        settingsPage.assertPageObjects()

        Log.d(STEP_TAG, "Click on 'Legal' link to open Legal Page. Assert that Legal Page has opened.")
        settingsPage.clickOnSettingsItem("Legal")
        legalPage.assertPageObjects()
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testAboutE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to Settings Page on the left-side menu.")
        leftSideNavigationDrawerPage.clickSettingsMenu()
        settingsPage.assertPageObjects()

        Log.d(STEP_TAG, "Click on 'About' link to open About Page. Assert that About Page has opened.")
        settingsPage.clickOnSettingsItem("About")
        aboutPage.assertPageObjects()

        Log.d(STEP_TAG,"Check that domain is equal to: ${student.domain} (student's domain).")
        aboutPage.domainIs(student.domain)

        Log.d(STEP_TAG,"Check that Login ID is equal to: ${student.loginId} (student's Login ID).")
        aboutPage.loginIdIs(student.loginId)

        Log.d(STEP_TAG,"Check that e-mail is equal to: ${student.loginId} (student's Login ID).")
        aboutPage.emailIs(student.loginId)

        Log.d(STEP_TAG,"Assert that the Instructure company logo has been displayed on the About page.")
        aboutPage.assertInstructureLogoDisplayed()
    }

    //The remote config settings page only available on DEBUG/DEV builds. So this test is testing a non user facing feature.
    @E2E
    @Test
    @TestMetaData(Priority.NICE_TO_HAVE, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testRemoteConfigSettingsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to Settings Page on the left-side menu.")
        leftSideNavigationDrawerPage.clickSettingsMenu()

        Log.d(PREPARATION_TAG,"Store the initial values on Remote Config Settings Page.")
        val initialValues = mutableMapOf<String, String?>()
        RemoteConfigParam.values().forEach {param -> initialValues.put(param.rc_name, RemoteConfigUtils.getString(param))}

        Log.d(STEP_TAG, "Navigate to Remote Config Settings Page.")
        settingsPage.clickOnSettingsItem("Remote Config Params")

        RemoteConfigParam.values().forEach { param ->

            Log.d(STEP_TAG, "Edit ${param.name} parameter.")

            Log.d(STEP_TAG, "Bring up the soft keyboard.")
            remoteConfigSettingsPage.clickRemoteConfigParamValue(param)

            Log.d(STEP_TAG, "Dismiss the soft keyboard.")
            Espresso.closeSoftKeyboard() //we need to do this to make this test work. TODO: investigate

            Log.d(STEP_TAG, "Clear remote config parameter valu: ${param.name}.")
            remoteConfigSettingsPage.clearRemoteConfigParamValueFocus(param) //we need to clear it because otherwise it would be flaky.
        }

        Log.d(STEP_TAG, "Navigate back to Settings Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Navigate to Remote Config Settings Page.")
        settingsPage.clickOnSettingsItem("Remote Config Params")

        Log.d(STEP_TAG, "Assert that all fields have maintained their initial value.")
        RemoteConfigParam.values().forEach { param ->
            remoteConfigSettingsPage.verifyRemoteConfigParamValue(param, initialValues.get(param.rc_name)!!)
        }
    }

    @E2E
    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testSubscribeToCalendar() {

        Log.d(PREPARATION_TAG, "Initialize Intents.")
        Intents.init()

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to User Settings Page.")
        leftSideNavigationDrawerPage.clickSettingsMenu()

        Log.d(STEP_TAG, "Click on 'Subscribe to Calendar'.")
        settingsPage.clickOnSettingsItem("Subscribe to Calendar Feed")

        Log.d(STEP_TAG, "Click on the 'SUBSCRIBE' button of the pop-up dialog.")
        settingsPage.clickOnSubscribeButton()

        Log.d(STEP_TAG, "Assert that the proper intents has launched, so the NavigationActivity has been launched with an Intent from SettingsActivity.")
        val calendarDataMatcherString = "https://calendar.google.com/calendar/r?cid=webcal://"
        val intentActionMatcher = IntentActionMatcher(Intent.ACTION_VIEW, calendarDataMatcherString)
        intended(intentActionMatcher)
    }

    @E2E
    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testPronounsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(courses = 1, teachers = 1)
        val course = data.coursesList[0]
        val teacher = data.teachersList[0]

        Log.d(STEP_TAG, "Click 'Find My School' button.")
        loginLandingPage.clickFindMySchoolButton()

        Log.d(STEP_TAG,"Enter domain: 'mobileqa.beta.instructure.com'.")
        loginFindSchoolPage.enterDomain("mobileqa.beta.instructure.com")

        Log.d(PREPARATION_TAG,"Enroll '${BuildConfig.PRONOUN_STUDENT_TEST_USER}' student to '${course.name}' course.")
        val pronounStudentId: Long = 12594913
        EnrollmentsApi.enrollUserAsStudent(course.id, pronounStudentId)

        Log.d(STEP_TAG,"Click on 'Next' button on the Toolbar.")
        loginFindSchoolPage.clickToolbarNextMenuItem()

        Log.d(STEP_TAG, "Log in with the dedicated student for testing the pronouns.")
        loginSignInPage.loginAs(BuildConfig.PRONOUN_STUDENT_TEST_USER, BuildConfig.PRONOUN_STUDENT_TEST_PASSWORD)
        dashboardPage.waitForRender()

        Log.d(PREPARATION_TAG,"Seed an email from the '${teacher.name}' teacher to Pronoun Student.")
        ConversationsApi.createConversation(teacher.token, listOf(pronounStudentId.toString()))[0]

        Log.d(STEP_TAG, "Open the Left Side Menu.")
        dashboardPage.openLeftSideMenu()

        val testPronoun = "(She/Her)"
        Log.d(STEP_TAG, "Assert that the corresponding user info, so does the 'Pronoun Student (She/Her)' as username and 'pronounstudent@gmail.com' as user email are displayed.")
        leftSideNavigationDrawerPage.assertUserInfo("Pronoun Student $testPronoun", "pronounstudent@gmail.com")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Select '${course.name}' course and open 'People' tab.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectPeople()
        peopleListPage.assertPageObjects()

        Log.d(STEP_TAG, "Assert that the '$testPronoun' pronouns are displayed next to the 'Pronoun Student' user's name.")
        peopleListPage.assertPersonPronouns("Pronoun Student", testPronoun)

        Log.d(STEP_TAG, "Click on the 'Pronoun Student' user.")
        peopleListPage.selectPerson("Pronoun Student $testPronoun")

        Log.d(STEP_TAG, "Assert that the Person Context Page also displays the '$testPronoun' pronouns besides all the corresponding information about the user.")
        personDetailsPage.assertIsPerson("Pronoun Student $testPronoun")

        CoursesApi.concludeCourse(course.id) // Need to conclude the course because otherwise there would be too much course with time on the dedicated user's dashboard.

        Log.d(STEP_TAG, "Navigate back to Dashboard.")
        ViewUtils.pressBackButton(3)

        Log.d(STEP_TAG, "Open the Left Side Menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Click on 'Change User' menu and assert on the Login Landing Page that the '$testPronoun' pronouns are displayed besides the 'Pronoun Student' user's name.")
        leftSideNavigationDrawerPage.clickChangeUserMenu()
        loginLandingPage.assertPreviousLoginUserDisplayed("Pronoun Student $testPronoun")
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SETTINGS, TestCategory.E2E, SecondaryFeatureCategory.SETTINGS_EMAIL_NOTIFICATIONS)
    fun testEmailNotificationsUIE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to Settings Page on the Left Side menu.")
        leftSideNavigationDrawerPage.clickSettingsMenu()

        Log.d(STEP_TAG, "Open Email Notifications Page.")
        settingsPage.clickOnSettingsItem("Email Notifications")

        Log.d(ASSERTION_TAG, "Assert that the toolbar title is 'Email Notifications' on the Email Notifications Page.")
        emailNotificationsPage.assertToolbarTitle()

        Log.d(ASSERTION_TAG, "Assert that all the 'Course Activities' email notifications are displayed.")
        emailNotificationsPage.assertCourseActivitiesEmailNotificationsDisplayed()

        Log.d(ASSERTION_TAG, "Assert that all the 'Discussions' email notifications are displayed.")
        emailNotificationsPage.assertDiscussionsEmailNotificationsDisplayed()

        Log.d(ASSERTION_TAG, "Assert that all the 'Conversations' email notifications are displayed.")
        emailNotificationsPage.assertConversationsEmailNotificationsDisplayed()

        Log.d(ASSERTION_TAG, "Assert that all the 'Scheduling' email notifications are displayed.")
        emailNotificationsPage.assertSchedulingEmailNotificationsDisplayed()

        Log.d(ASSERTION_TAG, "Assert that all the 'Groups' email notifications are displayed.")
        emailNotificationsPage.assertGroupsEmailNotificationsDisplayed()

        Log.d(ASSERTION_TAG, "Assert that all the 'Alerts' email notifications are displayed.")
        emailNotificationsPage.assertAlertsEmailNotificationsDisplayed()

        Log.d(ASSERTION_TAG, "Assert that all the 'Conferences' email notifications are displayed.")
        emailNotificationsPage.assertConferencesEmailNotificationsDisplayed()

        Log.d(ASSERTION_TAG, "Assert that the 'Appointment Availability' email notification's frequency is 'Immediately' yet.")
        emailNotificationsPage.assertNotificationFrequency("Appointment Availability", "Immediately")

        Log.d(STEP_TAG, "Click on the 'Appointment Availability' and select the 'Weekly' frequency.")
        emailNotificationsPage.clickOnNotification("Appointment Availability")
        emailNotificationsPage.selectFrequency("Weekly")

        Log.d(ASSERTION_TAG, "Assert that the 'Appointment Availability' email notification's frequency is 'Weekly' yet.")
        emailNotificationsPage.assertNotificationFrequency("Appointment Availability", "Weekly")
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.E2E, SecondaryFeatureCategory.INBOX_SIGNATURE)
    fun testInboxSignatureE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, courses = 1)
        val student = data.studentsList[0]

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Navigate to Settings Page on the left-side menu.")
        leftSideNavigationDrawerPage.clickSettingsMenu()

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

        Log.d(STEP_TAG,"Open Inbox Page.")
        dashboardPage.clickInboxTab()

        Log.d(STEP_TAG,"Click on 'New Message' button.")
        inboxPage.pressNewMessageButton()

        Log.d(ASSERTION_TAG, "Assert that the previously set inbox signature text is displayed by default when the user opens the Compose New Message Page.")
        inboxComposePage.assertBodyText("\n\n---\nPresident of AC Milan\nVice President of Ferencvaros")
    }

    @E2E
    @Test
    @TestMetaData(Priority.BUG_CASE, FeatureCategory.INBOX, TestCategory.E2E, SecondaryFeatureCategory.INBOX_SIGNATURE)
    fun testInboxSignaturesWithDifferentUsersE2E() {

        //Bug Ticket: MBL-18840
        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, courses = 1)
        val student = data.studentsList[0]
        val student2 = data.studentsList[1]

        Log.d(STEP_TAG, "Click 'Find My School' button.")
        loginLandingPage.clickFindMySchoolButton()

        Log.d(STEP_TAG, "Enter domain: '${CanvasNetworkAdapter.canvasDomain}'")
        loginFindSchoolPage.enterDomain(CanvasNetworkAdapter.canvasDomain)

        Log.d(STEP_TAG, "Click on 'Next' button on the toolbar.")
        loginFindSchoolPage.clickToolbarNextMenuItem()

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        loginSignInPage.loginAs(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Navigate to Settings Page on the Left Side Navigation Drawer menu.")
        leftSideNavigationDrawerPage.clickSettingsMenu()

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

        Log.d(STEP_TAG, "Open Inbox Page.")
        dashboardPage.clickInboxTab()

        Log.d(STEP_TAG,"Click on 'New Message' button.")
        inboxPage.pressNewMessageButton()

        Log.d(ASSERTION_TAG, "Assert that the previously set inbox signature text, '$firstSignatureText' is displayed by default when the user opens the Compose New Message Page.")
        inboxComposePage.assertBodyText("\n\n---\nPresident of AC Milan\nVice President of Ferencvaros")

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on 'Change User' button on the Left Side Navigation Drawer menu.")
        leftSideNavigationDrawerPage.clickChangeUserMenu()

        Log.d(STEP_TAG, "Click on the 'Find another school' button.")
        loginLandingPage.clickFindAnotherSchoolButton()

        Log.d(STEP_TAG, "Enter domain: '${CanvasNetworkAdapter.canvasDomain}'")
        loginFindSchoolPage.enterDomain(CanvasNetworkAdapter.canvasDomain)

        Log.d(STEP_TAG, "Click on 'Next' button on the toolbar.")
        loginFindSchoolPage.clickToolbarNextMenuItem()

        Log.d(STEP_TAG, "Login with the other user: '${student2.name}', login id: '${student2.loginId}'.")
        loginSignInPage.loginAs(student2)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open Inbox Page.")
        dashboardPage.clickInboxTab()

        Log.d(STEP_TAG, "Click on 'New Message' button.")
        inboxPage.pressNewMessageButton()

        Log.d(ASSERTION_TAG, "Assert that the previously set inbox signature text is NOT displayed since it was set for another user.")
        inboxComposePage.assertBodyText("")

        Log.d(STEP_TAG, "Navigate back to the Dashboard Page.")
        ViewUtils.pressBackButton(2)

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Navigate to Settings Page on the Left Side Navigation Drawer menu.")
        leftSideNavigationDrawerPage.clickSettingsMenu()

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

        Log.d(STEP_TAG, "Open Inbox Page.")
        dashboardPage.clickInboxTab()

        Log.d(STEP_TAG, "Click on 'New Message' button.")
        inboxPage.pressNewMessageButton()

        Log.d(ASSERTION_TAG, "Assert that the previously set inbox signature, '$secondSignatureText' text is displayed by default when the user opens the Compose New Message Page.")
        inboxComposePage.assertBodyText("\n\n---\nLoyal member of Instructure")

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        ViewUtils.pressBackButton(2)

        Log.d(STEP_TAG, "Click on 'Change User' button on the Left Side Navigation Drawer menu.")
        leftSideNavigationDrawerPage.clickChangeUserMenu()

        Log.d(STEP_TAG, "Login with user : '${student.name}', login id: '${student.loginId}' with 'one-click' login by selecting it from the 'Previous Logins' section.")
        loginLandingPage.loginWithPreviousUser(student)

        Log.d(STEP_TAG, "Open Inbox Page.")
        dashboardPage.clickInboxTab()

        Log.d(STEP_TAG, "Click on 'New Message' button.")
        inboxPage.pressNewMessageButton()

        Log.d(ASSERTION_TAG, "Assert that the previously set inbox signature text is displayed by default since we logged back with '${student.name}' student.")
        inboxComposePage.assertBodyText("\n\n---\nPresident of AC Milan\nVice President of Ferencvaros")
    }
}