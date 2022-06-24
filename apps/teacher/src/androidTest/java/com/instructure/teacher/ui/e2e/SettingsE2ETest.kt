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
import com.instructure.teacher.R
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.instructure.canvas.espresso.E2E
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import com.instructure.espresso.ViewUtils
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.lang.Thread.sleep

@HiltAndroidTest
class SettingsE2ETest : TeacherTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() {
        //We don't want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testProfileSettingsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId} , password: ${teacher.password}")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Navigate to User Settings Page.")
        dashboardPage.openUserSettingsPage()
        settingsPage.assertPageObjects()

        Log.d(STEP_TAG,"Open Profile Settings Page.")
        settingsPage.openProfileSettingsPage()
        profileSettingsPage.assertPageObjects()

        Log.d(STEP_TAG,"Click on Edit Pencil Icon on the toolbar.")
        profileSettingsPage.clickEditPencilIcon()

        val newUserName = "John Doe"
        Log.d(STEP_TAG,"Edit username to: $newUserName. Click on 'Save' button.")
        editProfileSettingsPage.editUserName(newUserName)
        editProfileSettingsPage.clickOnSave()

        Log.d(STEP_TAG,"Assert that the username has been changed to $newUserName on the Profile Settings Page.")
        try {
          Log.d(STEP_TAG,"Check if the user has landed on Settings Page. If yes, navigate back to Profile Settings Page.")
          //Sometimes in Bitrise it's working different than locally, because in Bitrise sometimes the user has been navigated to Settings Page after saving a new name,
          settingsPage.assertPageObjects()
          settingsPage.openProfileSettingsPage()
        } catch(e: NoMatchingViewException) {
          Log.d(STEP_TAG,"Did not throw the user back to the Settings Page, so the scenario can be continued.")
      }
        profileSettingsPage.assertPageObjects()
        profileSettingsPage.assertUserNameIs(newUserName)

        Log.d(STEP_TAG,"Click on Edit Pencil Icon on the toolbar.")
        profileSettingsPage.clickEditPencilIcon()

        Log.d(STEP_TAG,"Edit username to 'Unsaved userName' but DO NOT CLICK ON SAVE. Navigate back to Profile Settings Page without saving.")
        editProfileSettingsPage.editUserName("Unsaved userName")
        ViewUtils.pressBackButton(2)
        sleep(3000) //Give some time to "realize" we are on Profile Settings Page.
        profileSettingsPage.assertPageObjects()

        Log.d(STEP_TAG,"Assert that the username value remained $newUserName.")
        profileSettingsPage.assertUserNameIs(newUserName)

    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testLegalPageE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId} , password: ${teacher.password}")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Navigate to User Settings Page.")
        dashboardPage.openUserSettingsPage()
        settingsPage.assertPageObjects()

        Log.d(STEP_TAG,"Open Legal Page and assert that all the corresponding buttons are displayed.")
        settingsPage.openLegalPage()
        legalPage.assertPageObjects()
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testRateAppDialogE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId} , password: ${teacher.password}")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Navigate to User Settings Page.")
        dashboardPage.openUserSettingsPage()
        settingsPage.assertPageObjects()

        Log.d(STEP_TAG,"Open Legal Page and assert that all the corresponding buttons are displayed.")
        settingsPage.openRateAppDialog()

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

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId} , password: ${teacher.password}")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Navigate to User Settings Page.")
        dashboardPage.openUserSettingsPage()

        Log.d(PREPARATION_TAG,"Capture the initial remote config values.")
        val initialValues = mutableMapOf<String, String?>()
        RemoteConfigParam.values().forEach {param -> initialValues.put(param.rc_name, RemoteConfigUtils.getString(param))}

        Log.d(STEP_TAG,"Navigate to Remote Config Params Page.")
        settingsPage.openRemoteConfigParamsPage()

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
        settingsPage.assertPageObjects()

        Log.d(STEP_TAG,"Navigate to Remote Config Params page again.")
        settingsPage.openRemoteConfigParamsPage()

        Log.d(STEP_TAG,"Assert that all fields have maintained their initial value.")
        RemoteConfigParam.values().forEach { param ->
            remoteConfigSettingsPage.verifyRemoteConfigParamValue(param, initialValues.get(param.rc_name)!!)
        }
    }
}