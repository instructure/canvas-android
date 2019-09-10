/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.canvasapi2

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.CanvasAuthError
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.AnalyticsParamConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.jakewharton.threetenabp.AndroidThreeTen
import io.paperdb.Paper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import retrofit2.Call
import retrofit2.Response

abstract class AppManager : Application() {

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        Paper.init(this)
        // Permissions missing lint suppressed - we have the permissions in the app manifests
        Analytics.firebase = FirebaseAnalytics.getInstance(this)
        EventBus.getDefault().register(this)
        logTokenAnalytics()
    }

    override fun onTerminate() {
        EventBus.getDefault().unregister(this)
        super.onTerminate()
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe
    fun authErrorEvent(event: CanvasAuthError) {
        validateAuthentication()
    }

    private fun logTokenAnalytics() {
        val analyticsString = if (ApiPrefs.refreshToken.isNotEmpty()) {
            AnalyticsEventConstants.REFRESH_TOKEN
        } else if (ApiPrefs.token.isNotEmpty()) {
            AnalyticsEventConstants.FOREVER_TOKEN
        } else {
            // No token means new user, which means they'll also get a refresh token
            AnalyticsEventConstants.REFRESH_TOKEN
        }

        // Ideally, tokens will be paired with user ids to determine unique events
        val bundle = Bundle().apply {
            putString(AnalyticsParamConstants.USER_CONTEXT_ID, ApiPrefs.user?.contextId)
        }

        Analytics.logEvent(analyticsString, bundle)
    }

    open fun validateAuthentication() {
        // Don't use weave/awaitApi; override onResponse to avoid an infinite loop of CanvasAuthError events
        UserManager.getSelf(true, object : StatusCallback<User>() {
            override fun onResponse(data: Call<User>, response: Response<User>) {
                if (!response.isSuccessful && response.code() == 401) performLogoutOnAuthError()
            }
        })
    }

    abstract fun performLogoutOnAuthError()

}
