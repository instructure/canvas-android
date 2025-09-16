/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.ui.interaction

import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers
import org.junit.Test


@HiltAndroidTest
class ManageStudentsInteractionTest : ParentComposeTest() {

    @Test
    fun testStudentsDisplayed() {
        val data = initData()

        goToManageStudents(data)

        composeTestRule.waitForIdle()
        data.students.forEach {
            manageStudentsPage.assertStudentItemDisplayed(it.shortName!!)
        }
    }

    @Test
    fun testStudentTapped() {
        val data = initData()

        goToManageStudents(data)

        composeTestRule.waitForIdle()
        manageStudentsPage.clickStudent(data.students.first().shortName!!)
        composeTestRule.onNodeWithText("Alert Settings").assertIsDisplayed()
    }

    @Test
    fun testColorPickerDialog() {
        val data = initData()

        goToManageStudents(data)

        composeTestRule.waitForIdle()
        manageStudentsPage.tapStudentColor(data.students.first().shortName!!)
        manageStudentsPage.assertColorPickerDialogDisplayed()
    }

    @Test
    fun testAddStudentPairingCode() {
        val data = initData()

        goToManageStudents(data)

        composeTestRule.waitForIdle()

        manageStudentsPage.tapAddStudent()
        addStudentBottomPage.clickOnPairingCode()

        composeTestRule.onNodeWithTag("pairingCodeTextField").assertIsDisplayed()
    }

    @Test
    fun testAddStudentQrCode() {
        val data = initData()

        goToManageStudents(data)

        composeTestRule.waitForIdle()

        manageStudentsPage.tapAddStudent()
        addStudentBottomPage.clickOnQRCode()

        composeTestRule.onNodeWithText("Open Canvas app").assertIsDisplayed()
    }

    private fun initData(): MockCanvas {
        return MockCanvas.init(
            parentCount = 1,
            studentCount = 3,
            courseCount = 1
        )
    }

    private fun goToManageStudents(data: MockCanvas) {
        val parent = data.parents.first()
        val token = data.tokenFor(parent)!!
        tokenLogin(data.domain, token, parent)
        dashboardPage.openLeftSideMenu()
        leftSideNavigationDrawerPage.clickManageStudents()
    }

    override fun enableAndConfigureAccessibilityChecks() {
        extraAccessibilitySupressions = Matchers.allOf(
            AccessibilityCheckResultUtils.matchesCheck(
                SpeakableTextPresentCheck::class.java
            ),
            AccessibilityCheckResultUtils.matchesViews(
                ViewMatchers.withParent(
                    ViewMatchers.withClassName(
                        Matchers.equalTo(ComposeView::class.java.name)
                    )
                )
            )
        )

        super.enableAndConfigureAccessibilityChecks()
    }
}