package com.instructure.pandautils.analytics

import android.content.Context
import android.os.Bundle
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.AnalyticsParamConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageViewUtils
import com.instructure.pandautils.utils.FeatureFlagProvider
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = OfflineAnalyticsManager.SESSION_STORE_NAME)

class OfflineAnalyticsManager @Inject constructor(
    private val analytics: Analytics,
    private val pageViewUtils: PageViewUtils,
    private val apiPrefs: ApiPrefs,
    private val featureFlagProvider: FeatureFlagProvider
) {
    companion object {
        const val SESSION_STORE_NAME = "session_store"
        private val SESSION_STARTED_KEY = longPreferencesKey("session_started")
    }



    suspend fun reportOfflineAutoSyncSwitchChanged(newState: Boolean) {
        if (!featureFlagProvider.offlineEnabled()) {
            return
        }

        val eventName = if (newState)
            AnalyticsEventConstants.OFFLINE_AUTO_SYNC_TURNED_ON
        else
            AnalyticsEventConstants.OFFLINE_AUTO_SYNC_TURNED_OFF

        analytics.logEvent(eventName)
        pageViewUtils.saveSingleEvent(eventName, "${apiPrefs.fullDomain}/${eventName}")
    }

    suspend fun reportOfflineSyncStarted() {
        if (!featureFlagProvider.offlineEnabled()) {
            return
        }

        analytics.logEvent(AnalyticsEventConstants.OFFLINE_SYNC_BUTTON_TAPPED)
        pageViewUtils.saveSingleEvent(AnalyticsEventConstants.OFFLINE_SYNC_BUTTON_TAPPED, "${apiPrefs.fullDomain}/${AnalyticsEventConstants.OFFLINE_SYNC_BUTTON_TAPPED}")
    }

    suspend fun reportCourseOpenedInOfflineMode() {
        if (!featureFlagProvider.offlineEnabled()) {
            return
        }

        analytics.logEvent(AnalyticsEventConstants.OFFLINE_COURSE_OPENED)
        pageViewUtils.saveSingleEvent(AnalyticsEventConstants.OFFLINE_COURSE_OPENED, "${apiPrefs.fullDomain}/${AnalyticsEventConstants.OFFLINE_COURSE_OPENED}")
    }

    suspend fun offlineModeStarted(context: Context) {
        if (!featureFlagProvider.offlineEnabled()) {
            return
        }

        context.dataStore.edit { preferences ->
            if (!preferences.contains(SESSION_STARTED_KEY)) {
                preferences[SESSION_STARTED_KEY] = System.currentTimeMillis()
            }
        }
    }

    suspend fun offlineModeEnded(context: Context) {
        if (!featureFlagProvider.offlineEnabled()) {
            return
        }

        val startTimeInMillis = context.dataStore.data.map { preferences ->
            preferences[SESSION_STARTED_KEY]
        }.firstOrNull()
        if (startTimeInMillis == null) return

        val endTimeInMillis = System.currentTimeMillis()
        val duration = endTimeInMillis - startTimeInMillis

        context.dataStore.edit { preferences ->
            preferences.remove(SESSION_STARTED_KEY)
        }

        val extrasBundle = Bundle().apply {
            putLong(AnalyticsParamConstants.DURATION, duration)
        }
        analytics.logEvent(AnalyticsEventConstants.OFFLINE_COURSE_OPENED, extrasBundle)
        pageViewUtils.saveSingleEvent(AnalyticsEventConstants.OFFLINE_COURSE_OPENED, "${apiPrefs.fullDomain}/${AnalyticsEventConstants.OFFLINE_COURSE_OPENED}?duration=$duration")
    }
}
