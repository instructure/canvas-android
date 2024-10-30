package com.instructure.pandautils.analytics

import android.content.Context
import android.os.Bundle
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.AnalyticsParamConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageViewUtils
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.date.DateTimeProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.sessionDataStore by preferencesDataStore(name = OfflineAnalyticsManager.SESSION_STORE_NAME)

class OfflineAnalyticsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analytics: Analytics,
    private val pageViewUtils: PageViewUtils,
    private val apiPrefs: ApiPrefs,
    private val dateTimeProvider: DateTimeProvider,
    private val featureFlagProvider: FeatureFlagProvider,
    private val sessionDataStore: DataStore<Preferences> = context.sessionDataStore
) {
    companion object {
        const val SESSION_STORE_NAME = "session_store"
        private val SESSION_STARTED_KEY = longPreferencesKey("session_started")
    }

    fun reportOfflineAutoSyncSwitchChanged(newState: Boolean) {
        val eventName = if (newState)
            AnalyticsEventConstants.OFFLINE_AUTO_SYNC_TURNED_ON
        else
            AnalyticsEventConstants.OFFLINE_AUTO_SYNC_TURNED_OFF

        analytics.logEvent(eventName)
        pageViewUtils.saveSingleEvent(eventName, "${apiPrefs.fullDomain}/${eventName}")
    }

    fun reportOfflineSyncStarted() {
        analytics.logEvent(AnalyticsEventConstants.OFFLINE_SYNC_BUTTON_TAPPED)
        pageViewUtils.saveSingleEvent(AnalyticsEventConstants.OFFLINE_SYNC_BUTTON_TAPPED, "${apiPrefs.fullDomain}/${AnalyticsEventConstants.OFFLINE_SYNC_BUTTON_TAPPED}")
    }

    suspend fun reportCourseOpenedInOfflineMode() {
        val eventName = if (featureFlagProvider.offlineEnabled())
            AnalyticsEventConstants.OFFLINE_COURSE_OPENED_OFFLINE_ENABLED
        else
            AnalyticsEventConstants.OFFLINE_COURSE_OPENED_OFFLINE_NOT_ENABLED

        analytics.logEvent(eventName)
        pageViewUtils.saveSingleEvent(eventName, "${apiPrefs.fullDomain}/$eventName")
    }

    suspend fun offlineModeStarted() {
        sessionDataStore.edit { preferences ->
            if (!preferences.contains(SESSION_STARTED_KEY)) {
                preferences[SESSION_STARTED_KEY] = dateTimeProvider.getCalendar().timeInMillis
            }
        }
    }

    suspend fun offlineModeEnded() {
        val eventName = if (featureFlagProvider.offlineEnabled())
            AnalyticsEventConstants.OFFLINE_DURATION_OFFLINE_ENABLED
        else
            AnalyticsEventConstants.OFFLINE_DURATION_OFFLINE_NOT_ENABLED

        val startTimeInMillis = sessionDataStore.data.map { preferences ->
            preferences[SESSION_STARTED_KEY]
        }.firstOrNull()
        if (startTimeInMillis == null) return

        val endTimeInMillis = dateTimeProvider.getCalendar().timeInMillis
        val duration = endTimeInMillis - startTimeInMillis

        sessionDataStore.edit { preferences ->
            preferences.remove(SESSION_STARTED_KEY)
        }

        val extrasBundle = Bundle().apply {
            putLong(AnalyticsParamConstants.DURATION, duration)
        }
        analytics.logEvent(eventName, extrasBundle)
        pageViewUtils.saveSingleEvent(eventName, "${apiPrefs.fullDomain}/$eventName?${AnalyticsParamConstants.DURATION}=$duration")
    }
}
