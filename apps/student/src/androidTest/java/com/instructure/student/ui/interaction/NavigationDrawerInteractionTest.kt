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
 */
package com.instructure.student.ui.interaction

import android.os.Build
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.loginapi.login.model.SignedInUser
import com.instructure.loginapi.login.util.PreviousUsersUtils
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import org.junit.Test

class NavigationDrawerInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    private lateinit var student1: User
    private lateinit var student2: User

    // Should be able to change the user from the navigation drawer
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.LOGIN, TestCategory.INTERACTION, false)
    fun testNavDrawer_changeUser() {

        // This test fails on API-28 in FTL due to a "TOO_MANY_REGISTRATIONS" issue on logout.
        // IMO, this is not something that we can fix.  So let's not run the test.
        if(Build.VERSION.SDK_INT == 28) {
            return
        }

        // Sign in student 1, then sign him out
        val data = signInStudent()

        // Need to remember student1 via PreviousUserUtils in order to be able to "change user"
        // back to student1.
        PreviousUsersUtils.add(ContextKeeper.appContext, SignedInUser(
                user = student1,
                domain = data.domain,
                protocol = ApiPrefs.protocol,
                token = data.tokenFor(student1)!!,
                accessToken = "",
                refreshToken = "",
                clientId = "",
                clientSecret = "",
                calendarFilterPrefs = null
        ))
        dashboardPage.pressChangeUser()

        // Sign in student 2
        val token = data.tokenFor(student2)!!
        tokenLogin(data.domain, token, student2)
        dashboardPage.waitForRender()

        // Change back to student 1
        dashboardPage.pressChangeUser()
        loginLandingPage.assertDisplaysPreviousLogins()
        loginLandingPage.loginWithPreviousUser(student1)

        // Make sure that student 1 is now logged in
        dashboardPage.waitForRender()
        dashboardPage.assertUserLoggedIn(student1)
    }

    // Should be able to log out from the navigation drawer
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.LOGIN, TestCategory.INTERACTION, false)
    fun testNavDrawer_logOut() {

        // This test fails on API-28 in FTL due to a "TOO_MANY_REGISTRATIONS" issue on logout.
        // IMO, this is not something that we can fix.  So let's not run the test.
        if(Build.VERSION.SDK_INT == 28) {
            return
        }

        signInStudent()

        dashboardPage.signOut()
        loginLandingPage.assertPageObjects()
    }

    /**
     * Create two mocked students, sign in the first one, end up on the dashboard page
     */
    private fun signInStudent() : MockCanvas {
        val data = MockCanvas.init(
                studentCount = 2,
                courseCount = 1,
                favoriteCourseCount = 1,
                teacherCount = 1
        )

        student1 = data.students.first()
        student2 = data.students.last()

        val token = data.tokenFor(student1)!!
        tokenLogin(data.domain, token, student1)
        dashboardPage.waitForRender()

        return data
    }
}
