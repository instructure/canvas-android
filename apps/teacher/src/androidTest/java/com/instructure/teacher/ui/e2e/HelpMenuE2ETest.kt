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
package com.instructure.teacher.ui.e2e

import android.util.Log
import androidx.test.espresso.intent.Intents
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.checkToastText
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test


@HiltAndroidTest
class HelpMenuE2ETest: TeacherTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.NICE_TO_HAVE, FeatureCategory.DASHBOARD, TestCategory.E2E)
    @Stub
    fun testHelpMenuE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(teachers = 1, courses = 1)
        val teacher = data.teachersList[0]

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open Help Menu.")
        leftSideNavigationDrawerPage.clickHelpMenu()

        Log.d(ASSERTION_TAG, "Assert Help Menu Dialog is displayed.")
        helpPage.assertHelpMenuDisplayed()

        Log.d(ASSERTION_TAG, "Assert that all the corresponding Help menu content are displayed.")
        helpPage.assertHelpMenuContent()

        Log.d(STEP_TAG, "Click on 'Report a problem' menu.")
        helpPage.verifyReportAProblem("Test Subject", "Test Description")

        Log.d(ASSERTION_TAG, "Assert that it is possible to write into the input fields and the corresponding buttons are displayed as well.")
        helpPage.assertReportProblemDialogDisplayed()

        Log.d(STEP_TAG, "Click on 'Cancel' button on the 'Report a problem' dialog.")
        helpPage.clickCancelReportProblem()

        Log.d(ASSERTION_TAG, "Assert that when clicking on the different help menu items then the corresponding intents will be fired and has the proper URLs.")
        Intents.init()

        try {
            helpPage.assertHelpMenuURL("Search the Canvas Guides", "https://community.canvaslms.com/t5/Canvas/ct-p/canvas")
            helpPage.assertHelpMenuURL("Submit a Feature Idea", "https://community.canvaslms.com/t5/Idea-Conversations/idb-p/ideas")
            helpPage.assertHelpMenuURL("Ask the Community", "https://community.canvaslms.com/community/answers")
            helpPage.assertHelpMenuURL("Conference Guides for Remote Classrooms", "https://community.canvaslms.com/docs/DOC-18572-conferences-resources")
            helpPage.assertHelpMenuURL("Share Your Love for the App", "https://community.canvaslms.com/t5/Canvas/ct-p/canvas")
        }
        finally {
            Intents.release()
        }
    }

    @E2E
    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.DASHBOARD, TestCategory.E2E)
    @Stub
    fun testHelpMenuReportProblemE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(teachers = 1, courses = 1)
        val teacher = data.teachersList[0]

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open Help Menu.")
        leftSideNavigationDrawerPage.clickHelpMenu()

        Log.d(ASSERTION_TAG, "Assert Help Menu Dialog is displayed.")
        helpPage.assertHelpMenuDisplayed()

        Log.d(ASSERTION_TAG, "Assert that all the corresponding Help menu content are displayed.")
        helpPage.assertHelpMenuContent()

        Log.d(STEP_TAG, "Click on 'Report a problem' menu.")
        helpPage.verifyReportAProblem("Test Subject", "Test Description")

        Log.d(ASSERTION_TAG, "Assert that it is possible to write into the input fields and the corresponding buttons are displayed as well.")
        helpPage.assertReportProblemDialogDisplayed()

        Log.d(STEP_TAG, "Click on the 'Send' button on the 'Report a problem' dialog.")
        helpPage.clickSendReportProblem()

        Log.d(ASSERTION_TAG, "Assert that the corresponding toast message is displayed.")
        checkToastText(R.string.errorReportThankyou, activityRule.activity)
    }

}