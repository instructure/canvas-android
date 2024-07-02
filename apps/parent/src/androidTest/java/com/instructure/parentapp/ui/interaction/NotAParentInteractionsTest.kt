/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.parentapp.ui.interaction

import android.content.Intent
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addCourse
import com.instructure.canvas.espresso.mockCanvas.addEnrollment
import com.instructure.canvas.espresso.mockCanvas.addTerm
import com.instructure.canvas.espresso.mockCanvas.addUser
import com.instructure.canvas.espresso.waitForMatcherWithSleeps
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.loginapi.login.R
import com.instructure.parentapp.ui.pages.NotAParentPage
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers
import org.junit.Test


@HiltAndroidTest
class NotAParentInteractionsTest : ParentComposeTest() {

    private val notAParentPage = NotAParentPage(composeTestRule)

    @Test
    fun testLogout() {
        val data = initData()
        goToNotAParentScreen(data)

        notAParentPage.tapReturnToLogin()
        waitForMatcherWithSleeps(ViewMatchers.withId(R.id.canvasLogo), 20000).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )
    }

    @Test
    fun testTapStudent() {
        val data = initData()
        goToNotAParentScreen(data)

        notAParentPage.expandAppOptions()
        Intents.init()
        try {
            val expectedIntent = CoreMatchers.allOf(
                IntentMatchers.hasAction(Intent.ACTION_VIEW),
                CoreMatchers.anyOf(
                    // Could be either of these, depending on whether the play store app is installed
                    IntentMatchers.hasData("market://details?id=com.instructure.candroid"),
                    IntentMatchers.hasData("https://play.google.com/store/apps/details?id=com.instructure.candroid")
                )
            )
            notAParentPage.tapApp("STUDENT")
            Intents.intended(expectedIntent)
        } finally {
            Intents.release()
        }
    }

    @Test
    fun testTapTeacher() {
        val data = initData()
        goToNotAParentScreen(data)

        notAParentPage.expandAppOptions()
        Intents.init()
        try {
            val expectedIntent = CoreMatchers.allOf(
                IntentMatchers.hasAction(Intent.ACTION_VIEW),
                CoreMatchers.anyOf(
                    // Could be either of these, depending on whether the play store app is installed
                    IntentMatchers.hasData("market://details?id=com.instructure.teacher"),
                    IntentMatchers.hasData("https://play.google.com/store/apps/details?id=com.instructure.teacher")
                )
            )
            notAParentPage.tapApp("TEACHER")
            Intents.intended(expectedIntent)
        } finally {
            Intents.release()
        }
    }

    private fun initData(): MockCanvas {
        return MockCanvas().apply {
            val parent = addUser()
            addTerm()
            val course = addCourse()
            addEnrollment(parent, course, Enrollment.EnrollmentType.Observer)
        }
    }

    private fun goToNotAParentScreen(data: MockCanvas) {
        val parent = data.parents.first()
        val token = data.tokenFor(parent)!!
        tokenLogin(data.domain, token, parent, false)
    }
}
