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

import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class SettingsE2ETest : StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // Basically just verifies that the proper items show up in the settings page,
    // legal page and help page.  As these are all somewhat dependent on API calls,
    // they seemed like legitimate targets for an E2E test.
    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testSettingsE2E() {
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        tokenLogin(student)

        dashboardPage.waitForRender()
        dashboardPage.launchSettingsPage()

        settingsPage.assertPageObjects()
        settingsPage.launchLegalPage()

        legalPage.assertPageObjects()
        Espresso.pressBack() // Exit legal page
    }

    // The remote config settings page (only available on debug builds) used to do some
    // really bizarre things when reacting to the soft keyboard appearing and disappearing.
    // This test verifies that no remote config values change in response to the
    // soft keyboard appearing.
    //
    // Marked as P2 because this is not testing user-facing functionality.
    @E2E
    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testRemoteConfigSettingsE2E() {
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        tokenLogin(student)

        dashboardPage.waitForRender()
        dashboardPage.launchSettingsPage()

        // Capture the initial remote config values
        val initialValues = mutableMapOf<String, String?>()
        RemoteConfigParam.values().forEach {param -> initialValues.put(param.rc_name, RemoteConfigUtils.getString(param))}

        // Launch the remote config page
        settingsPage.launchRemoteConfigParams()

        // Click on each EditText, which brings up the soft keyboard, then dismiss it.
        RemoteConfigParam.values().forEach { param ->

            // Bring up the soft keyboard
            remoteConfigSettingsPage.clickRemoteConfigParamValue(param)

            // and dismiss it
            Espresso.closeSoftKeyboard()

            // If we don't clear the focus on the EditText, it can cause
            // funky behavior when we click on the next EditText (like the
            // "paste | select all" menu popping up).
            remoteConfigSettingsPage.clearRemoteConfigParamValueFocus(param)
        }

        // Exit the remote config page
        Espresso.pressBack()

        // Go back in again
        settingsPage.launchRemoteConfigParams()

        // Verify that all fields have maintained their initial value
        RemoteConfigParam.values().forEach { param ->
            remoteConfigSettingsPage.verifyRemoteConfigParamValue(param, initialValues.get(param.rc_name)!!)
        }

        // Exit the remote config page
        Espresso.pressBack()
    }
}