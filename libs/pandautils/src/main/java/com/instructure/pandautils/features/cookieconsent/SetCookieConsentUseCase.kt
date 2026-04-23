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

import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ConsentPrefs
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

class SetCookieConsentUseCase @Inject constructor(
    private val apiPrefs: ApiPrefs,
    private val consentPrefs: ConsentPrefs
) : BaseUseCase<SetCookieConsentUseCase.Params, Unit>() {

    data class Params(val consent: Boolean)

    override suspend fun execute(params: Params) {
        val userId = apiPrefs.user?.id ?: return
        consentPrefs.setConsent(userId, apiPrefs.domain, params.consent)
    }
}
