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

import android.util.Log
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

    override fun enableAndConfigureAccessibilityChecks() {
        //We don't want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testSettingsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId} , password: ${student.password}")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Navigate to Settings Page on the left-side menu.")
        dashboardPage.launchSettingsPage()
        settingsPage.assertPageObjects()

        Log.d(STEP_TAG,"Click on 'Legal' link to open Legal Page. Assert that Legal Page has opened.")
        settingsPage.launchLegalPage()
        legalPage.assertPageObjects()
    }

    // The remote config settings page only available on debug builds.
    @E2E
    @Test
    @TestMetaData(Priority.NICE_TO_HAVE, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testRemoteConfigSettingsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId} , password: ${student.password}")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Navigate to Settings Page on the left-side menu.")
        dashboardPage.launchSettingsPage()

        Log.d(PREPARATION_TAG,"Store the initial values on Remote Config Settings Page.")
        val initialValues = mutableMapOf<String, String?>()
        RemoteConfigParam.values().forEach {param -> initialValues.put(param.rc_name, RemoteConfigUtils.getString(param))}

        Log.d(STEP_TAG,"Navigate to Remote Config Settings Page.")
        settingsPage.launchRemoteConfigParams()

        RemoteConfigParam.values().forEach { param ->

            Log.d(STEP_TAG,"Edit ${param.name} parameter.")

            Log.d(STEP_TAG,"Bring up the soft keyboard.")
            remoteConfigSettingsPage.clickRemoteConfigParamValue(param)

            Log.d(STEP_TAG,"Dismiss the soft keyboard.")
            Espresso.closeSoftKeyboard() //we need to do this to make this test work. TODO: investigate

            Log.d(STEP_TAG,"Clear remote config parameter valu: ${param.name}.")
            remoteConfigSettingsPage.clearRemoteConfigParamValueFocus(param) //we need to clear it because otherwise it would be flaky.
        }

        Log.d(STEP_TAG,"Navigate back to Settings Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG,"Navigate to Remote Config Settings Page.")
        settingsPage.launchRemoteConfigParams()

        Log.d(STEP_TAG,"Assert that all fields have maintained their initial value.")
        RemoteConfigParam.values().forEach { param ->
            remoteConfigSettingsPage.verifyRemoteConfigParamValue(param, initialValues.get(param.rc_name)!!)
        }

    }
}