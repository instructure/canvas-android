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
package com.instructure.pandautils.analytics

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.date.DateTimeProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class OfflineAnalyticsManagerTest {
    private lateinit var context: Context
    private lateinit var analytics: Analytics
    private lateinit var pageViewUtils: com.instructure.pandautils.analytics.pageview.PageViewUtils
    private lateinit var apiPrefs: ApiPrefs
    private lateinit var dateTimeProvider: DateTimeProvider
    private lateinit var featureFlagProvider: FeatureFlagProvider
    private lateinit var offlineAnalyticsManager: OfflineAnalyticsManager
    private lateinit var dataStore: DataStore<Preferences>

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        analytics = mockk(relaxed = true)
        pageViewUtils = mockk(relaxed = true)
        apiPrefs = mockk(relaxed = true)
        featureFlagProvider = mockk(relaxed = true)
        dataStore = mockk(relaxed = true)
        dateTimeProvider = mockk(relaxed = true)

        offlineAnalyticsManager = OfflineAnalyticsManager(context, analytics, pageViewUtils, apiPrefs, dateTimeProvider, featureFlagProvider, dataStore)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `reportOfflineAutoSyncSwitchChanged should log on event`() = runTest {
        every { apiPrefs.fullDomain } returns "https://example.com"
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        every { pageViewUtils.saveSingleEvent(any(), any()) } returns mockk()
        offlineAnalyticsManager.reportOfflineAutoSyncSwitchChanged(true)

        coVerify {
            analytics.logEvent("offline_auto_sync_turned_on")
            pageViewUtils.saveSingleEvent("offline_auto_sync_turned_on", "https://example.com/offline_auto_sync_turned_on")
        }
    }

    @Test
    fun `reportOfflineAutoSyncSwitchChanged should log off event`() = runTest {
        every { apiPrefs.fullDomain } returns "https://example.com"
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        every { pageViewUtils.saveSingleEvent(any(), any()) } returns mockk()
        offlineAnalyticsManager.reportOfflineAutoSyncSwitchChanged(false)

        coVerify {
            analytics.logEvent("offline_auto_sync_turned_off")
            pageViewUtils.saveSingleEvent("offline_auto_sync_turned_off", "https://example.com/offline_auto_sync_turned_off")
        }
    }

    @Test
    fun `reportOfflineSyncStarted should log event`() = runTest {
        every { apiPrefs.fullDomain } returns "https://example.com"
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        every { pageViewUtils.saveSingleEvent(any(), any()) } returns mockk()
        offlineAnalyticsManager.reportOfflineSyncStarted()

        coVerify {
            analytics.logEvent("offline_sync_button_tapped")
            pageViewUtils.saveSingleEvent("offline_sync_button_tapped", "https://example.com/offline_sync_button_tapped")
        }
    }

    @Test
    fun `reportCourseOpenedInOfflineMode should log event if feature flag is enabled`() = runTest {
        every { apiPrefs.fullDomain } returns "https://example.com"
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        every { pageViewUtils.saveSingleEvent(any(), any()) } returns mockk()
        offlineAnalyticsManager.reportCourseOpenedInOfflineMode()

        coVerify {
            analytics.logEvent("offline_course_opened_offline_enabled")
            pageViewUtils.saveSingleEvent("offline_course_opened_offline_enabled", "https://example.com/offline_course_opened_offline_enabled")
        }
    }

    @Test
    fun `reportCourseOpenedInOfflineMode should log event if feature flag is disabled`() = runTest {
        every { apiPrefs.fullDomain } returns "https://example.com"
        coEvery { featureFlagProvider.offlineEnabled() } returns false
        every { pageViewUtils.saveSingleEvent(any(), any()) } returns mockk()
        offlineAnalyticsManager.reportCourseOpenedInOfflineMode()

        coVerify {
            analytics.logEvent("offline_course_opened_offline_not_enabled")
            pageViewUtils.saveSingleEvent("offline_course_opened_offline_not_enabled", "https://example.com/offline_course_opened_offline_not_enabled")
        }
    }

    @Test
    fun `offlineModeEnded should clear datastore and calculates duration if feature flag is enabled`() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        every { apiPrefs.fullDomain } returns "https://example.com"

        every { dateTimeProvider.getCalendar().timeInMillis } returns 2000L
        every { pageViewUtils.saveSingleEvent(any(), any()) } returns mockk()

        val expectedDuration = 1000L

        val key = longPreferencesKey("session_started")
        val preferences: MutablePreferences = mockk(relaxed = true)
        every { dataStore.data } returns MutableStateFlow(preferences)
        every { preferences[key] } returns 1000L

        offlineAnalyticsManager.offlineModeEnded()

        verify {
            analytics.logEvent("offline_duration_offline_enabled", any())
            pageViewUtils.saveSingleEvent("offline_duration_offline_enabled", "https://example.com/offline_duration_offline_enabled?duration=$expectedDuration")
        }
    }

    @Test
    fun `offlineModeEnded should clear datastore and calculates duration if feature flag is disabled`() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns false
        every { apiPrefs.fullDomain } returns "https://example.com"
        every { pageViewUtils.saveSingleEvent(any(), any()) } returns mockk()

        every { dateTimeProvider.getCalendar().timeInMillis } returns 2000L

        val expectedDuration = 1000L

        val key = longPreferencesKey("session_started")
        val preferences: MutablePreferences = mockk(relaxed = true)
        every { dataStore.data } returns MutableStateFlow(preferences)
        every { preferences[key] } returns 1000L

        offlineAnalyticsManager.offlineModeEnded()

        verify {
            analytics.logEvent("offline_duration_offline_not_enabled", any())
            pageViewUtils.saveSingleEvent("offline_duration_offline_not_enabled", "https://example.com/offline_duration_offline_not_enabled?duration=$expectedDuration")
        }
    }
}