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
 */
package com.instructure.pandautils.features.cookieconsent

import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ConsentPrefs
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.SHA256
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import sdk.pendo.io.Pendo

@OptIn(DelicateCoroutinesApi::class)
abstract class AnalyticsConsentHandler(
    private val userApi: UserAPI.UsersInterface,
    private val featureFlagProvider: FeatureFlagProvider,
    private val consentPrefs: ConsentPrefs,
    private val apiPrefs: ApiPrefs
) {

    fun onConsentGranted() {
        GlobalScope.launch { startPendoSession() }
    }

    fun onConsentRevoked() {
        Pendo.endSession()
    }

    protected open suspend fun beforeStartPendoSession() = Unit

    private suspend fun startPendoSession() {
        if (consentPrefs.currentUserConsent != true) return

        val user = userApi.getSelfWithUUID(RestParams(isForceReadFromNetwork = true)).dataOrNull
        val visitorData = mapOf("locale" to apiPrefs.effectiveLocale)
        val accountData = mapOf("surveyOptOut" to featureFlagProvider.checkAccountSurveyNotificationsFlag())
        beforeStartPendoSession()
        Pendo.startSession(
            user?.uuid?.SHA256().orEmpty(),
            user?.accountUuid.orEmpty(),
            visitorData,
            accountData
        )
    }
}
