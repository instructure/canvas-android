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
package com.instructure.canvasapi2.utils

object ConsentPrefs : PrefManager("consent-prefs") {

    private var consentMap: HashMap<String, Boolean> by BooleanMapPref()

    fun getConsent(userId: Long, domain: String): Boolean? = consentMap["$domain:$userId"]

    fun setConsent(userId: Long, domain: String, consent: Boolean) {
        consentMap = HashMap(consentMap).apply { put("$domain:$userId", consent) }
    }

    fun removeConsent(userId: Long, domain: String) {
        consentMap = HashMap(consentMap).apply { remove("$domain:$userId") }
    }

    val currentUserConsent: Boolean?
        get() {
            val userId = ApiPrefs.user?.id ?: return null
            val domain = ApiPrefs.domain.takeIf { it.isNotBlank() } ?: return null
            return getConsent(userId, domain)
        }
}
