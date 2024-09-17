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
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import com.instructure.dataseeding.api.ConversationsApi
import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.EnrollmentsApi
import com.instructure.espresso.ViewUtils
import com.instructure.student.BuildConfig
import com.instructure.student.R
import com.instructure.student.ui.utils.IntentActionMatcher
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert
import org.junit.Test

@HiltAndroidTest
class SettingsE2ETest : StudentTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testProfileSettingsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to User Settings Page.")
        leftSideNavigationDrawerPage.clickSettingsMenu()
        settingsPage.assertPageObjects()

        Log.d(STEP_TAG, "Open Profile Settings Page.")
        settingsPage.openProfileSettings()
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
        settingsPage.openProfileSettings()
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

        Log.d(STEP_TAG,"Navigate to Settings Page and open App Theme Settings.")
        settingsPage.openAppThemeSettings()

        Log.d(STEP_TAG,"Select Dark App Theme and assert that the App Theme Title and Status has the proper text color (which is used in Dark mode).")
        settingsPage.selectAppTheme("Dark")
        settingsPage.assertAppThemeTitleTextColor("#FFFFFFFF") //Currently, this color is used in the Dark mode for the AppTheme Title text.
        settingsPage.assertAppThemeStatusTextColor("#FF919CA8") //Currently, this color is used in the Dark mode for the AppTheme Status text.

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
        settingsPage.openAppThemeSettings()

        Log.d(STEP_TAG,"Select Light App Theme and assert that the App Theme Title and Status has the proper text color (which is used in Light mode).")
        settingsPage.selectAppTheme("Light")
        settingsPage.assertAppThemeTitleTextColor("#FF273540") //Currently, this color is used in the Light mode for the AppTheme Title texts.
        settingsPage.assertAppThemeStatusTextColor("#FF6A7883") //Currently, this color is used in the Light mode for the AppTheme Status text.

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
        settingsPage.openLegalPage()
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
        settingsPage.openAboutPage()
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
        settingsPage.openRemoteConfigParams()

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
        settingsPage.openRemoteConfigParams()

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
        settingsPage.assertPageObjects()

        Log.d(STEP_TAG, "Click on 'Subscribe to Calendar'.")
        settingsPage.openSubscribeToCalendar()

        Log.d(STEP_TAG, "Click on the 'SUBSCRIBE' button of the pop-up dialog.")
        settingsPage.clickOnSubscribe()

        Log.d(STEP_TAG, "Assert that the proper intents has launched, so the NavigationActivity has been launched with an Intent from SettingsActivity.")
        val calendarDataMatcherString = "https://calendar.google.com/calendar/r?cid=webcal://"
        val intentActionMatcher = IntentActionMatcher(Intent.ACTION_VIEW, calendarDataMatcherString)
        intended(intentActionMatcher)

        Log.d(PREPARATION_TAG, "Release Intents.")
        Intents.release()
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.E2E)
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
}