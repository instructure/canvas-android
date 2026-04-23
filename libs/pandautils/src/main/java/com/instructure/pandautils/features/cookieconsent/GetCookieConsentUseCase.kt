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

import com.instructure.canvasapi2.utils.ConsentPrefs
import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.pandautils.utils.FeatureFlagProvider
import javax.inject.Inject

class GetCookieConsentUseCase @Inject constructor(
    private val featureFlagProvider: FeatureFlagProvider,
    private val consentPrefs: ConsentPrefs
) : BaseUseCase<Unit, GetCookieConsentUseCase.Result>() {

    data class Result(
        val flagEnabled: Boolean,
        val consent: Boolean?
    )

    override suspend fun execute(params: Unit): Result {
        featureFlagProvider.fetchEnvironmentFeatureFlags()
        val flagEnabled = featureFlagProvider.checkCookieConsentFlag()
        if (!flagEnabled) return Result(flagEnabled = false, consent = null)

        return Result(flagEnabled = true, consent = consentPrefs.currentUserConsent)
    }
}
