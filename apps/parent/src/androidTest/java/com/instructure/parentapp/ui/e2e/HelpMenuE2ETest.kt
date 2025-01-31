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
import androidx.test.espresso.intent.Intents
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.seedData
import com.instructure.parentapp.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class HelpMenuE2ETest : ParentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.NICE_TO_HAVE, FeatureCategory.DASHBOARD, TestCategory.E2E)
    fun testHelpMenuE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(parents = 1, students = 1, courses = 1)
        val parent = data.parentsList[0]

        Log.d(STEP_TAG,"Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openNavigationDrawer()

        Log.d(STEP_TAG, "Open 'Help' menu.")
        leftSideNavigationDrawerPage.clickHelpMenu()

        Log.d(STEP_TAG, "Assert Help Menu Dialog is displayed.")
        helpPage.assertHelpMenuDisplayed()

        Log.d(STEP_TAG, "Assert that all the corresponding Help menu content are displayed.")
        helpPage.assertHelpMenuContent()

        Log.d(STEP_TAG, "Click on the 'Report a Problem' help menu.")
        helpPage.clickReportProblemLabel()

        Log.d(ASSERTION_TAG, "Assert that the 'Report a Problem' dialog has displayed.")
        helpPage.assertReportProblemDialogDisplayed()

        Log.d(STEP_TAG, "Assert that when clicking on the different help menu items then the corresponding intents will be fired and has the proper URLs.")
        Intents.init()

        try {
            helpPage.assertHelpMenuURL("Search the Canvas Guides", "https://community.canvaslms.com/t5/Canvas/ct-p/canvas")
            helpPage.assertHelpMenuURL("Submit a Feature Idea", "https://community.canvaslms.com/t5/Idea-Conversations/idb-p/ideas")
            helpPage.assertHelpMenuURL("Share Your Love for the App", "https://community.canvaslms.com/t5/Canvas/ct-p/canvas")
        }
        finally {
            Intents.release()
        }
    }
}