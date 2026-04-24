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
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.user.UserRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

class GetCookieConsentUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val consentPrefs: ConsentPrefs
) : BaseUseCase<Unit, GetCookieConsentUseCase.Result>() {

    data class Result(
        val usageMetrics: String?,
        val consent: Boolean?
    )

    override suspend fun execute(params: Unit): Result {
        val settings = userRepository.getMobileSettings(forceRefresh = true)
        if (settings !is DataResult.Success) return Result(usageMetrics = null, consent = null)

        val usageMetrics = settings.data.usageMetrics ?: return Result(usageMetrics = null, consent = null)

        return Result(usageMetrics = usageMetrics, consent = consentPrefs.currentUserConsent)
    }
}
