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

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.compose.ui.platform.ComposeView
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addPairingCode
import com.instructure.canvas.espresso.mockCanvas.addStudent
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers
import org.hamcrest.core.AllOf
import org.junit.Test

@HiltAndroidTest
class AddStudentInteractionTest : ParentComposeTest() {

    private lateinit var activityResult: Instrumentation.ActivityResult

    @Test
    fun testAddStudentWithCode() {
        val data = initData()
        val student = data.addStudent(data.courses.values.toList())
        val code = data.addPairingCode(student)

        goToAddStudent(data)
        addStudentPage.tapPairingCode()

        pairingCodePage.enterPairingCode(code)
        pairingCodePage.tapSubmit()

        composeTestRule.waitForIdle()
        manageStudentsPage.assertStudentItemDisplayed(data.students.first())
    }

    @Test
    fun testAddStudentCodeError() {
        val data = initData()
        goToAddStudent(data)
        addStudentPage.tapPairingCode()

        pairingCodePage.enterPairingCode("invalid")
        pairingCodePage.tapSubmit()
        pairingCodePage.assertErrorDisplayed()
    }

    @Test
    fun testAddStudentQrCode() {
        val data = initData()
        val student = data.addStudent(data.courses.values.toList())
        val code = data.addPairingCode(student)

        activityResult = Instrumentation.ActivityResult(Activity.RESULT_OK, Intent().apply {
            putExtra(
                com.google.zxing.client.android.Intents.Scan.RESULT,
                "canvas://pairing-code/?code=$code"
            )
        })

        goToAddStudent(data)
        addStudentPage.tapQrCode()
        Intents.init()
        try {
            intending(
                AllOf.allOf(
                    IntentMatchers.anyIntent()
                )
            ).respondWith(activityResult)
            qrPairingPage.tapNext()
        } finally {
            Intents.release()
        }

        composeTestRule.waitForIdle()
        manageStudentsPage.assertStudentItemDisplayed(data.students.first())
    }

    @Test
    fun testAddStudentQrCodeError() {
        val data = initData()
        goToAddStudent(data)
        addStudentPage.tapQrCode()

        activityResult = Instrumentation.ActivityResult(Activity.RESULT_OK, Intent().apply {
            putExtra(
                com.google.zxing.client.android.Intents.Scan.RESULT,
                "canvas://pairing-code/?code=invalid"
            )
        })

        Intents.init()
        try {
            intending(
                AllOf.allOf(
                    IntentMatchers.anyIntent()
                )
            ).respondWith(activityResult)
            qrPairingPage.tapNext()
        } finally {
            Intents.release()
        }

        qrPairingPage.assertErrorDisplayed()
    }

    @Test
    fun testAddStudentPairingCodeResetError() {
        val data = initData()
        goToAddStudent(data)
        val student = data.addStudent(data.courses.values.toList())
        val code = data.addPairingCode(student)

        addStudentPage.tapPairingCode()

        pairingCodePage.enterPairingCode("invalid")
        pairingCodePage.tapSubmit()
        pairingCodePage.assertErrorDisplayed()

        pairingCodePage.enterPairingCode(code)
        pairingCodePage.assertErrorNotDisplayed()
        pairingCodePage.tapSubmit()
        manageStudentsPage.assertStudentItemDisplayed(data.students.first())
    }

    private fun initData(): MockCanvas {
        val data = MockCanvas.init(
            courseCount = 1,
            studentCount = 1,
            parentCount = 1
        )

        return data
    }

    private fun goToAddStudent(data: MockCanvas) {
        val parent = data.parents.first()
        val token = data.tokenFor(parent)!!
        tokenLogin(data.domain, token, parent)
        dashboardPage.openNavigationDrawer()
        leftSideNavigationDrawerPage.clickManageStudents()
        manageStudentsPage.tapAddStudent()
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