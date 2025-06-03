/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.student.widget

import android.content.Context
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.FeaturesManager
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.PendoInitCallbackHandler
import com.instructure.canvasapi2.utils.PendoInitListener
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.SHA256
import com.instructure.student.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import sdk.pendo.io.Pendo
import javax.inject.Inject


class WidgetLogger @Inject constructor(
    private val userApi: UserAPI.UsersInterface,
    private val featureFlagProvider: FeatureFlagProvider,
    private val analytics: Analytics
): PendoInitListener {

    private val coroutineScope = MainScope()
    private var loggingJob: Job? = null

    fun logEvent(event: String, context: Context) {
        loggingJob = coroutineScope.launch(Dispatchers.IO) {
            if (!Analytics.isSessionActive()) {
                PendoInitCallbackHandler.addEvent(event)
                val featureFlagsResult =
                    FeaturesManager.getEnvironmentFeatureFlagsAsync(true).await().dataOrNull
                val sendUsageMetrics =
                    featureFlagsResult?.get(FeaturesManager.SEND_USAGE_METRICS) ?: false
                if (sendUsageMetrics) {
                    PendoInitCallbackHandler.addListener(this@WidgetLogger)
                    setupPendo(context)
                }
            } else {
                analytics.logEvent(event)
            }
        }
    }

    private suspend fun setupPendo(context: Context) {
        val options = Pendo.PendoOptions.Builder().setJetpackComposeBeta(true).build()
        Pendo.setup(context, BuildConfig.PENDO_TOKEN, options, PendoInitCallbackHandler)
        val visitorData = mapOf("locale" to ApiPrefs.effectiveLocale)
        val accountData =
            mapOf("surveyOptOut" to featureFlagProvider.checkAccountSurveyNotificationsFlag())
        val user = userApi.getSelfWithUUID(RestParams(isForceReadFromNetwork = true)).dataOrNull
        Pendo.startSession(
            user?.uuid?.SHA256().orEmpty(),
            user?.accountUuid.orEmpty(),
            visitorData,
            accountData
        )
    }

    fun cancelLogging() {
        loggingJob?.cancel()
        PendoInitCallbackHandler.removeListener(this)
    }

    override fun onInitComplete() {
        PendoInitCallbackHandler.removeListener(this)
        Pendo.endSession()
    }

    override fun onInitFailed() {
        PendoInitCallbackHandler.removeListener(this)
        Pendo.endSession()
    }
}
