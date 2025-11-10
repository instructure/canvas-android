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
 */
package com.instructure.parentapp.ui.interaction

import androidx.compose.ui.platform.ComposeView
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addObserverAlertThreshold
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.models.AlertType
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers
import org.junit.Test
import kotlin.random.Random

@HiltAndroidTest
class AlertSettingsInteractionTest : ParentComposeTest() {

    @Test
    fun deleteSwitchThreshold() {
        val data = initData()
        data.addObserverAlertThreshold(
            Random.nextLong(),
            AlertType.ASSIGNMENT_MISSING,
            data.currentUser!!,
            data.students[0],
            null
        )
        goToAlertSettings(data)
        studentAlertSettingsPage.assertSwitchThreshold(AlertType.ASSIGNMENT_MISSING, true)
        studentAlertSettingsPage.clickThreshold(AlertType.ASSIGNMENT_MISSING)
        studentAlertSettingsPage.assertSwitchThreshold(AlertType.ASSIGNMENT_MISSING, false)
    }

    @Test
    fun deletePercentageThreshold() {
        val data = initData()
        data.addObserverAlertThreshold(
            Random.nextLong(),
            AlertType.COURSE_GRADE_LOW,
            data.currentUser!!,
            data.students[0],
            "50"
        )
        goToAlertSettings(data)
        studentAlertSettingsPage.assertPercentageThreshold(AlertType.COURSE_GRADE_LOW, "50%")
        studentAlertSettingsPage.clickThreshold(AlertType.COURSE_GRADE_LOW)
        studentAlertSettingsPage.tapThresholdNeverButton()
        studentAlertSettingsPage.assertPercentageThreshold(AlertType.COURSE_GRADE_LOW, "Never")
    }

    @Test
    fun createSwitchThreshold() {
        val data = initData()
        goToAlertSettings(data)
        studentAlertSettingsPage.assertSwitchThreshold(AlertType.COURSE_ANNOUNCEMENT, false)
        studentAlertSettingsPage.clickThreshold(AlertType.COURSE_ANNOUNCEMENT)
        studentAlertSettingsPage.assertSwitchThreshold(AlertType.COURSE_ANNOUNCEMENT, true)
    }

    @Test
    fun createPercentageThreshold() {
        val data = initData()
        goToAlertSettings(data)
        studentAlertSettingsPage.assertPercentageThreshold(AlertType.COURSE_GRADE_HIGH, "Never")
        studentAlertSettingsPage.clickThreshold(AlertType.COURSE_GRADE_HIGH)
        studentAlertSettingsPage.enterThreshold("101")
        studentAlertSettingsPage.assertThresholdDialogError()
        studentAlertSettingsPage.enterThreshold("50")
        studentAlertSettingsPage.assertThresholdDialogNotError()
        studentAlertSettingsPage.tapThresholdSaveButton()
        studentAlertSettingsPage.assertPercentageThreshold(AlertType.COURSE_GRADE_HIGH, "50%")
    }

    @Test
    fun minThreshold() {
        val data = initData()
        goToAlertSettings(data)
        studentAlertSettingsPage.clickThreshold(AlertType.ASSIGNMENT_GRADE_LOW)
        studentAlertSettingsPage.enterThreshold("50")
        studentAlertSettingsPage.tapThresholdSaveButton()
        studentAlertSettingsPage.clickThreshold(AlertType.ASSIGNMENT_GRADE_HIGH)
        studentAlertSettingsPage.enterThreshold("49")
        studentAlertSettingsPage.assertThresholdDialogError()
        studentAlertSettingsPage.enterThreshold("51")
        studentAlertSettingsPage.assertThresholdDialogNotError()
        studentAlertSettingsPage.tapThresholdSaveButton()
        studentAlertSettingsPage.assertPercentageThreshold(AlertType.ASSIGNMENT_GRADE_HIGH, "51%")
    }

    @Test
    fun maxThreshold() {
        val data = initData()
        goToAlertSettings(data)
        studentAlertSettingsPage.clickThreshold(AlertType.ASSIGNMENT_GRADE_HIGH)
        studentAlertSettingsPage.enterThreshold("50")
        studentAlertSettingsPage.tapThresholdSaveButton()
        studentAlertSettingsPage.clickThreshold(AlertType.ASSIGNMENT_GRADE_LOW)
        studentAlertSettingsPage.enterThreshold("51")
        studentAlertSettingsPage.assertThresholdDialogError()
        studentAlertSettingsPage.enterThreshold("49")
        studentAlertSettingsPage.assertThresholdDialogNotError()
        studentAlertSettingsPage.tapThresholdSaveButton()
        studentAlertSettingsPage.assertPercentageThreshold(AlertType.ASSIGNMENT_GRADE_LOW, "49%")
    }

    @Test
    fun deleteStudent() {
        val data = initData()
        goToAlertSettings(data)
        composeTestRule.waitForIdle()
        studentAlertSettingsPage.clickOverflowMenu()
        studentAlertSettingsPage.clickDeleteStudent()
        studentAlertSettingsPage.clickDeleteStudentButton()
        manageStudentsPage.assertStudentItemNotDisplayed(data.students.first().shortName!!)
    }

    private fun initData(): MockCanvas {
        val data = MockCanvas.init(
            courseCount = 1,
            studentCount = 2,
            parentCount = 1
        )

        return data
    }

    private fun goToAlertSettings(data: MockCanvas) {
        val parent = data.parents[0]
        val token = data.tokenFor(parent)!!
        tokenLogin(data.domain, token, parent)
        dashboardPage.openLeftSideMenu()
        leftSideNavigationDrawerPage.clickManageStudents()
        manageStudentsPage.clickStudent(data.students.first().shortName!!)
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