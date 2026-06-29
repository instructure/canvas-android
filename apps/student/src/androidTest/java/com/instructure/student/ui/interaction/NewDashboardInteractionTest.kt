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
package com.instructure.student.ui.interaction

import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.MutableLiveData
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.RemoteConfigOverrides
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.pandautils.di.NetworkStateProviderModule
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.espresso.fakes.FakeNetworkStateProvider
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.extensions.tokenLogin
import com.instructure.student.ui.utils.extensions.tokenLoginWithoutDashboardWait
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Test

/**
 * Verifies that the `DASHBOARD_REDESIGN` Firebase Remote Config flag controls which dashboard is
 * shown, demonstrating how features behind remote config flags can be tested (MBL-19979).
 *
 * Remote config values are seeded deterministically via [RemoteConfigOverrides] (TestAppManager
 * initializes remote config without a live Firebase fetch). The new dashboard additionally requires
 * the `widget_dashboard` Canvas environment flag, which is supplied through MockCanvas.
 */
@UninstallModules(NetworkStateProviderModule::class)
@HiltAndroidTest
class NewDashboardInteractionTest : StudentComposeTest() {

    override fun displaysPageObjects() = Unit // Not used for interaction tests

    private val isOnlineLiveData = MutableLiveData<Boolean>()

    @BindValue
    @JvmField
    val networkStateProvider: NetworkStateProvider = FakeNetworkStateProvider(isOnlineLiveData)

    @Before
    fun setUp() {
        isOnlineLiveData.postValue(true)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.DASHBOARD, TestCategory.INTERACTION)
    fun testNewDashboardDisplayedWhenFlagEnabled() {
        RemoteConfigOverrides.set(RemoteConfigParam.DASHBOARD_REDESIGN, true)

        val data = MockCanvas.init(studentCount = 1, courseCount = 1, favoriteCourseCount = 1)
        data.widgetDashboardEnabled = true
        val student = data.students[0]
        // The standard tokenLogin waits for the classic dashboard view, so skip that wait and let the
        // assertion below wait for the Compose dashboard instead.
        tokenLoginWithoutDashboardWait(data.domain, data.tokenFor(student)!!, student)

        // The redesigned dashboard is Compose-based; this tag only exists on the new dashboard.
        composeTestRule.waitUntil(timeoutMillis = 20000) {
            composeTestRule.onAllNodesWithTag("dashboardPullRefreshIndicator").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("dashboardPullRefreshIndicator").assertExists()
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.DASHBOARD, TestCategory.INTERACTION)
    fun testOldDashboardDisplayedWhenFlagDisabled() {
        // Leave DASHBOARD_REDESIGN at its default (false). Even with the Canvas widget_dashboard flag
        // on, the remote config kill switch alone should keep the classic dashboard in place.
        val data = MockCanvas.init(studentCount = 1, courseCount = 1, favoriteCourseCount = 1)
        data.widgetDashboardEnabled = true
        val student = data.students[0]
        // tokenLogin waits for the classic dashboard view and asserts its page objects.
        tokenLogin(data.domain, data.tokenFor(student)!!, student)

        dashboardPage.assertDisplaysCourse(data.courses.values.first())
    }
}
