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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

object OfflineAnalyticsManager {
    private val SESSION_STORE_NAME = "session_store"
    private val SESSION_STARTED_KEY = longPreferencesKey("session_started")

    private val Context.dataStore by preferencesDataStore(
        name = SESSION_STORE_NAME
    )

    fun reportOfflineAutoSyncSwitchChanged(newState: Boolean) {
        val eventName = if (newState)
            AnalyticsEventConstants.OFFLINE_AUTO_SYNC_TURNED_ON
        else
            AnalyticsEventConstants.OFFLINE_AUTO_SYNC_TURNED_OFF

        Analytics.logEvent(eventName)
        PageViewUtils.saveSingleEvent(eventName, "${ApiPrefs.fullDomain}/${eventName}")
    }

    fun reportOfflineSyncStarted() {
        Analytics.logEvent(AnalyticsEventConstants.OFFLINE_SYNC_BUTTON_TAPPED)
        PageViewUtils.saveSingleEvent(AnalyticsEventConstants.OFFLINE_SYNC_BUTTON_TAPPED, "${ApiPrefs.fullDomain}/${AnalyticsEventConstants.OFFLINE_SYNC_BUTTON_TAPPED}")
    }

    fun reportCourseOpenedInOfflineMode() {
        Analytics.logEvent(AnalyticsEventConstants.OFFLINE_COURSE_OPENED)
        PageViewUtils.saveSingleEvent(AnalyticsEventConstants.OFFLINE_COURSE_OPENED, "${ApiPrefs.fullDomain}/${AnalyticsEventConstants.OFFLINE_COURSE_OPENED}")
    }

    suspend fun offlineModeStarted(context: Context) {
        context.dataStore.edit { preferences ->
            if (!preferences.contains(SESSION_STARTED_KEY)) {
                preferences[SESSION_STARTED_KEY] = System.currentTimeMillis()
            }
        }
    }

    suspend fun offlineModeEnded(context: Context) {
        val startTimeInMillis = context.dataStore.data.map { preferences ->
            preferences[SESSION_STARTED_KEY]
        }.firstOrNull()
        if (startTimeInMillis == null) return

        val endTimeInMillis = System.currentTimeMillis()
        val duration = ((endTimeInMillis - startTimeInMillis) / 1000) // Convert ms to s

        context.dataStore.edit { preferences ->
            preferences.remove(SESSION_STARTED_KEY)
        }

        val extrasBundle = Bundle().apply {
            putLong(AnalyticsParamConstants.DURATION, duration)
        }
        Analytics.logEvent(AnalyticsEventConstants.OFFLINE_COURSE_OPENED, extrasBundle)
        PageViewUtils.saveSingleEvent(AnalyticsEventConstants.OFFLINE_COURSE_OPENED, "${ApiPrefs.fullDomain}/${AnalyticsEventConstants.OFFLINE_COURSE_OPENED}?duration=$duration")
    }
}
