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
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import org.junit.Test

class SettingsE2ETest : StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // Basically just verifies that the proper items show up in the settings page,
    // legal page and help page.  As these are all somewhat dependent on API calls,
    // they seemed like legitimate targets for an E2E test.
    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.E2E)
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

        settingsPage.launchHelpPage()

        // May be brittle.  See comments in HelpPage.kt.
        helpPage.assertPageObjects()
    }
}