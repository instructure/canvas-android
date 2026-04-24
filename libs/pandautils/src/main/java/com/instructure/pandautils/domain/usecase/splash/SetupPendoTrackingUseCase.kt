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
package com.instructure.pandautils.domain.usecase.splash

import com.instructure.canvasapi2.managers.FeaturesManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.data.repository.features.FeaturesRepository
import com.instructure.pandautils.data.repository.user.UserRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.SHA256
import com.instructure.pandautils.utils.orDefault
import sdk.pendo.io.Pendo
import javax.inject.Inject

class SetupPendoTrackingUseCase @Inject constructor(
    private val featuresRepository: FeaturesRepository,
    private val userRepository: UserRepository,
    private val apiPrefs: ApiPrefs,
    private val featureFlagProvider: FeatureFlagProvider
) : BaseUseCase<Unit, Unit>() {

    override suspend fun execute(params: Unit) {
        val sendUsageMetrics = featuresRepository.getEnvironmentFeatureFlags(forceRefresh = true)
            .dataOrNull
            ?.get(FeaturesManager.SEND_USAGE_METRICS)
            .orDefault()

        if (sendUsageMetrics) {
            val userWithIds = userRepository.getSelfWithUuid(forceRefresh = true).dataOrNull
            val visitorData = mapOf("locale" to apiPrefs.effectiveLocale)
            val accountData = mapOf("surveyOptOut" to featureFlagProvider.checkAccountSurveyNotificationsFlag())
            Pendo.startSession(
                userWithIds?.uuid?.SHA256().orEmpty(),
                userWithIds?.accountUuid.orEmpty(),
                visitorData,
                accountData
            )
        } else {
            Pendo.endSession()
        }
    }
}