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
package com.instructure.teacher.ui.e2e

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoMatchingViewException
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import com.instructure.dataseeding.api.ConversationsApi
import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.EnrollmentsApi
import com.instructure.espresso.ViewUtils
import com.instructure.pandautils.utils.AppTheme
import com.instructure.teacher.BuildConfig
import com.instructure.teacher.ui.pages.PersonContextPage
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.openLeftSideMenu
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class SettingsE2ETest : TeacherComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testPronounsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(courses = 1, students = 1)
        val course = data.coursesList[0]
        val student = data.studentsList[0]

        Log.d(STEP_TAG, "Click 'Find My School' button.")
        loginLandingPage.clickFindMySchoolButton()

        Log.d(STEP_TAG,"Enter domain: 'mobileqa.beta.instructure.com'.")
        loginFindSchoolPage.enterDomain("mobileqa.beta.instructure.com")

        Log.d(PREPARATION_TAG,"Enroll '${BuildConfig.PRONOUN_TEACHER_TEST_USER}' teacher to '${course.name}' course.")
        val pronounTeacherId: Long = 12594806
        EnrollmentsApi.enrollUserAsTeacher(course.id, pronounTeacherId)

        Log.d(STEP_TAG,"Click on 'Next' button on the Toolbar.")
        loginFindSchoolPage.clickToolbarNextMenuItem()

        Log.d(STEP_TAG, "Log in with the dedicated teacher for testing the pronouns.")
        loginSignInPage.loginAs(BuildConfig.PRONOUN_TEACHER_TEST_USER, BuildConfig.PRONOUN_TEACHER_TEST_PASSWORD)
        dashboardPage.waitForRender()

        Log.d(PREPARATION_TAG,"Seed an email from the '${student.name}' student to Pronoun Teacher.")
        ConversationsApi.createConversation(student.token, listOf(pronounTeacherId.toString()))[0]

        Log.d(STEP_TAG, "Open the Left Side Menu.")
        dashboardPage.openLeftSideMenu()

        val testPronoun = "(He/Him)"
        Log.d(STEP_TAG, "Assert that the corresponding user info, so does the 'Pronoun Teacher (He/Him)' as username and 'pronounteacher@gmail.com' as user email are displayed.")
        leftSideNavigationDrawerPage.assertUserInfo("Pronoun Teacher $testPronoun", "pronounteacher@gmail.com")

        Log.d(STEP_TAG, "Navigate to User Settings Page.")
        leftSideNavigationDrawerPage.clickSettingsMenu()

        Log.d(STEP_TAG, "Open Profile Settings Page.")
        settingsPage.clickOnSettingsItem("Profile Settings")
        profileSettingsPage.assertPageObjects()

        Log.d(STEP_TAG, "Assert that the '$testPronoun' pronouns are displayed on the Profile Settings Page.")
        profileSettingsPage.assertPronouns(testPronoun)

        Log.d(STEP_TAG, "Navigate back to the Dashboard Page.")
        ViewUtils.pressBackButton(2)

        Log.d(STEP_TAG, "Select '${course.name}' course and open 'People' tab.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.openPeopleTab()
        peopleListPage.assertPageObjects()

        Log.d(STEP_TAG, "Assert that the '$testPronoun' pronouns are displayed next to the 'Pronoun Teacher' user's name.")
        peopleListPage.assertPersonPronouns("Pronoun Teacher", testPronoun)

        Log.d(STEP_TAG, "Click on the 'Pronoun Teacher' user.")
        peopleListPage.clickPerson("Pronoun Teacher $testPronoun")

        Log.d(STEP_TAG, "Assert that the Person Context Page also displays the '$testPronoun' pronouns besides all the corresponding information about the user.")
        personContextPage.assertDisplaysCourseInfo(course)
        personContextPage.assertSectionNameView(PersonContextPage.UserRole.TEACHER)
        personContextPage.assertPersonPronouns(testPronoun)

        CoursesApi.concludeCourse(course.id) // Need to conclude the course because otherwise there would be too much course with time on the dedicated user's dashboard.

        Log.d(STEP_TAG, "Navigate back to Dashboard.")
        ViewUtils.pressBackButton(3)

        Log.d(STEP_TAG, "Open the Left Side Menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Click on 'Change User' menu and assert on the Login Landing Page that the '$testPronoun' pronouns are displayed besides the 'Pronoun Teacher' user's name.")
        leftSideNavigationDrawerPage.clickChangeUserMenu()
        loginLandingPage.assertPreviousLoginUserDisplayed("Pronoun Teacher $testPronoun")
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testProfileSettingsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to User Settings Page.")
        leftSideNavigationDrawerPage.clickSettingsMenu()

        Log.d(STEP_TAG, "Open Profile Settings Page.")
        settingsPage.clickOnSettingsItem("Profile Settings")
        profileSettingsPage.assertPageObjects()

        Log.d(STEP_TAG, "Click on Edit Pencil Icon on the toolbar.")
        profileSettingsPage.clickEditPencilIcon()

        val newUserName = "John Doe"
        Log.d(STEP_TAG, "Edit username to: '$newUserName'. Click on 'Save' button.")
        editProfileSettingsPage.editUserName(newUserName)
        editProfileSettingsPage.clickOnSave()

        Log.d(STEP_TAG, "Assert that the username has been changed to '$newUserName' on the Profile Settings Page.")
        try {
            Log.d(STEP_TAG, "Check if the user has landed on Settings Page. If yes, navigate back to Profile Settings Page.")
            //Sometimes in Bitrise it's working different than locally, because in Bitrise sometimes the user has been navigated to Settings Page after saving a new name,
            settingsPage.clickOnSettingsItem("Profile Settings")
        } catch (e: IllegalStateException) {
            Log.d(STEP_TAG, "Did not throw the user back to the Settings Page, so the scenario can be continued.")
        }

        Log.d(STEP_TAG, "Assert that the Profile Settings Page is displayed and the username is '$newUserName'.")
        profileSettingsPage.assertPageObjects()
        profileSettingsPage.assertUserNameIs(newUserName)

        Log.d(STEP_TAG, "Click on Edit Pencil Icon on the toolbar.")
        profileSettingsPage.clickEditPencilIcon()

        Log.d(STEP_TAG, "Edit username to 'Unsaved userName' but DO NOT CLICK ON SAVE.")
        editProfileSettingsPage.editUserName("Unsaved userName")

        //this is a workaround for that sometimes on FTL we need to click twice on the back button to navigate back to the Profile Settings page.
        //Probably because of sometimes the soft keyboard does not show up.
        try {
            Log.d(STEP_TAG, "Press back button (without saving). The goal is to navigate back to the Profile Settings Page.")
            Espresso.pressBack()

            Log.d(STEP_TAG, "Assert that the username value remained '$newUserName'.")
            profileSettingsPage.assertUserNameIs(newUserName)
        } catch (e: NoMatchingViewException) {
            Log.d(STEP_TAG, "Press back button (without saving). The goal is to navigate back to the Profile Settings Page.")
            Espresso.pressBack()

            Log.d(STEP_TAG, "Assert that the username value remained '$newUserName'.")
            profileSettingsPage.assertUserNameIs(newUserName)
        }
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testDarkModeE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to User Settings Page.")
        leftSideNavigationDrawerPage.clickSettingsMenu()

        Log.d(STEP_TAG,"Select Dark App Theme and assert that the App Theme Title and Status has the proper text color (which is used in Dark mode).")
        settingsPage.selectAppTheme(AppTheme.DARK)
        //settingsPage.assertAppThemeTitleTextColor("#FFFFFFFF") //Currently, this color is used in the Dark mode for the AppTheme Title text.
        //settingsPage.assertAppThemeStatusTextColor("#FF919CA8") //Currently, this color is used in the Dark mode for the AppTheme Status text.

        Log.d(STEP_TAG,"Navigate back to Dashboard. Assert that the 'Courses' label has the proper text color (which is used in Dark mode).")
        Espresso.pressBack()
        dashboardPage.assertCourseLabelTextColor("#FFFFFFFF")

        Log.d(STEP_TAG,"Select '${course.name}' course and assert on the Course Browser Page that the tabs has the proper text color (which is used in Dark mode).")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.assertTabLabelTextColor("Announcements","#FFFFFFFF")
        courseBrowserPage.assertTabLabelTextColor("Assignments","#FFFFFFFF")

        Log.d(STEP_TAG,"Navigate to Settings Page and open App Theme Settings again.")
        Espresso.pressBack()
        leftSideNavigationDrawerPage.clickSettingsMenu()

        Log.d(STEP_TAG,"Select Light App Theme and assert that the App Theme Title and Status has the proper text color (which is used in Light mode).")
        settingsPage.selectAppTheme(AppTheme.LIGHT)
        //settingsPage.assertAppThemeTitleTextColor("#FF273540") //Currently, this color is used in the Light mode for the AppTheme Title texts.
        //settingsPage.assertAppThemeStatusTextColor("#FF6A7883") //Currently, this color is used in the Light mode for the AppTheme Status text.

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
        val teacher = data.teachersList[0]

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Navigate to User Settings Page.")
        leftSideNavigationDrawerPage.clickSettingsMenu()

        Log.d(STEP_TAG,"Open Legal Page and assert that all the corresponding buttons are displayed.")
        settingsPage.clickOnSettingsItem("Legal")
        legalPage.assertPageObjects()
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testAboutE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to Settings Page on the left-side menu.")
        leftSideNavigationDrawerPage.clickSettingsMenu()

        Log.d(STEP_TAG, "Click on 'About' link to open About Page. Assert that About Page has opened.")
        settingsPage.clickOnSettingsItem("About")
        aboutPage.assertPageObjects()

        Log.d(STEP_TAG,"Check that domain is equal to: '${teacher.domain}' (teacher's domain).")
        aboutPage.domainIs(teacher.domain)

        Log.d(STEP_TAG,"Check that Login ID is equal to: '${teacher.loginId}' (teacher's Login ID).")
        aboutPage.loginIdIs(teacher.loginId)

        Log.d(STEP_TAG,"Check that e-mail is equal to: '${teacher.loginId}' (teacher's Login ID).")
        aboutPage.emailIs(teacher.loginId)

        Log.d(STEP_TAG,"Assert that the Instructure company logo has been displayed on the About page.")
        aboutPage.assertInstructureLogoDisplayed()
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testRateAppDialogE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Navigate to User Settings Page.")
        leftSideNavigationDrawerPage.clickSettingsMenu()

        Log.d(STEP_TAG,"Open Legal Page and assert that all the corresponding buttons are displayed.")
        settingsPage.clickOnSettingsItem("Rate on the Play Store")

        Log.d(STEP_TAG,"Assert that the five starts are displayed.")
        settingsPage.assertFiveStarRatingDisplayed()
    }

    //The remote config settings page only available on debug builds.
    @E2E
    @Test
    @TestMetaData(Priority.NICE_TO_HAVE, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testRemoteConfigSettingsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Navigate to User Settings Page.")
        leftSideNavigationDrawerPage.clickSettingsMenu()

        Log.d(PREPARATION_TAG,"Capture the initial remote config values.")
        val initialValues = mutableMapOf<String, String?>()
        RemoteConfigParam.values().forEach { param -> initialValues[param.rc_name] = RemoteConfigUtils.getString(param) }

        Log.d(STEP_TAG,"Navigate to Remote Config Params Page.")
        settingsPage.clickOnSettingsItem("Remote Config Params")

        Log.d(STEP_TAG,"Click on each EditText, which brings up the soft keyboard, then dismiss it.")
        RemoteConfigParam.values().forEach { param ->

            Log.d(STEP_TAG,"Bring up the soft keyboard and dismiss it.")
            remoteConfigSettingsPage.clickRemoteConfigParamValue(param)
            Espresso.closeSoftKeyboard()

            Log.d(STEP_TAG,"Clear focus from EditText.")
            remoteConfigSettingsPage.clearRemoteConfigParamValueFocus(param)
        }

        Log.d(STEP_TAG,"Navigate back to Settings Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG,"Navigate to Remote Config Params page again.")
        settingsPage.clickOnSettingsItem("Remote Config Params")

        Log.d(STEP_TAG,"Assert that all fields have maintained their initial value.")
        RemoteConfigParam.values().forEach { param ->
            remoteConfigSettingsPage.verifyRemoteConfigParamValue(param, initialValues.get(param.rc_name)!!)
        }
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
}