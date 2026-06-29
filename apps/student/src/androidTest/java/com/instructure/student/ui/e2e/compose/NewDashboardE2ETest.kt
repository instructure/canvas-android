/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.student.ui.e2e.compose

import android.util.Log
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.RemoteConfigOverrides
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.dataseeding.api.FeatureFlagsApi
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.pandautils.utils.FEATURE_FLAG_WIDGET_DASHBOARD
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.extensions.seedData
import com.instructure.student.ui.utils.extensions.tokenLoginWithoutDashboardWait
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * End-to-end demonstration that the redesigned dashboard (gated by the `DASHBOARD_REDESIGN` Firebase
 * Remote Config flag) can be exercised in E2E tests (MBL-19979).
 *
 * The new dashboard requires all of: the `DASHBOARD_REDESIGN` remote config flag, the server-side
 * `widget_dashboard` Canvas feature flag, and the (default-on) widget config. This test controls the
 * first via [RemoteConfigOverrides] and the second by enabling the account feature flag through the
 * data-seeding API, then logs in and asserts the Compose dashboard is shown.
 */
@HiltAndroidTest
class NewDashboardE2ETest : StudentComposeTest() {

    override fun displaysPageObjects() = Unit

    @Before
    fun setUp() {
        // Set the flags this test needs up front: the server-side widget_dashboard Canvas flag and
        // the DASHBOARD_REDESIGN remote config override.
        Log.d(PREPARATION_TAG, "Enable the '$FEATURE_FLAG_WIDGET_DASHBOARD' Canvas feature flag on the account.")
        FeatureFlagsApi.setAccountFeatureFlag(feature = FEATURE_FLAG_WIDGET_DASHBOARD, state = "on")

        Log.d(PREPARATION_TAG, "Override the DASHBOARD_REDESIGN remote config flag (default 'false') to 'true'.")
        RemoteConfigOverrides.set(RemoteConfigParam.DASHBOARD_REDESIGN, true)
    }

    @After
    fun tearDown() {
        // Reset the flags set in setUp so they don't leak into other tests/runs on the shared E2E
        // instance. (performReset also clears the remote config override, but reset it here too so
        // both flags are torn down in one place.)
        FeatureFlagsApi.setAccountFeatureFlag(feature = FEATURE_FLAG_WIDGET_DASHBOARD, state = "off")
        RemoteConfigOverrides.clear()
    }

    @E2E
    @Test
    @TestMetaData(Priority.NICE_TO_HAVE, FeatureCategory.DASHBOARD, TestCategory.E2E)
    fun testNewDashboardE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        login(student)

        Log.d(ASSERTION_TAG, "Assert that the redesigned (Compose) dashboard is displayed.")
        composeTestRule.waitUntil(timeoutMillis = 20000) {
            composeTestRule.onAllNodesWithTag("dashboardPullRefreshIndicator").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("dashboardPullRefreshIndicator").assertExists()
    }

    // The standard tokenLogin waits for the classic dashboard view, so skip that wait and let the
    // assertion wait for the Compose dashboard instead.
    private fun login(user: CanvasUserApiModel) {
        tokenLoginWithoutDashboardWait(
            user.domain,
            user.token,
            User(
                id = user.id,
                name = user.name,
                shortName = user.shortName,
                avatarUrl = user.avatarUrl,
                effective_locale = "en" // Avoid a locale-driven restart that kills the test process.
            )
        )
    }
}
