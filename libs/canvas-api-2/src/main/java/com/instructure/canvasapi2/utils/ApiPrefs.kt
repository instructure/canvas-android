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
 */
package com.instructure.canvasapi2.utils

import android.content.res.Resources
import android.webkit.URLUtil
import androidx.core.os.ConfigurationCompat
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.pageview.PandataInfo
import java.io.File

/** Preference file name **/
const val PREFERENCE_FILE_NAME = "canvas-kit-sp"

/**
 * Canvas API preferences containing data required for most core networking such as
 * the school domain, protocol, auth token, and user object.
 *
 * All public properties of ApiPrefs should be cached in memory (after any initial loading), so it is
 * safe to access these properties in hot code paths like View.OnDraw() and RecyclerView binders.
 */
@Suppress("unused", "UNUSED_PARAMETER")
object ApiPrefs : PrefManager(PREFERENCE_FILE_NAME) {

    const val DEVICE_LOCALE: String = "root"

    const val ACCOUNT_LOCALE: String = "account"

    @Deprecated("Deprecated in favor of accessToken")
    var token by StringPref()

    var accessToken by StringPref()

    var refreshToken by StringPref()

    fun getValidToken(): String = if (accessToken.isNotEmpty()) accessToken else token

    var protocol by StringPref("https", "api_protocol")

    var userAgent by StringPref("", "user_agent")

    var clientId by StringPref("", "client_id")

    var clientSecret by StringPref("", "client_secret")

    var canvasRegion by NStringPref(null, "canvas_region")


    var perPageCount = 100

    var theme: CanvasTheme? by GsonPref(CanvasTheme::class.java, null)

    /* Non-masquerading Prefs */
    internal var originalDomain by StringPref("", "domain")
    private var originalUser: User? by GsonPref(User::class.java, null, "user", false)

    var selectedLocale by StringPref(ACCOUNT_LOCALE)

    val effectiveLocale: String
        get() {
            return selectedLocale.takeUnless { it == ACCOUNT_LOCALE }
                    ?: user?.effective_locale
                    ?: user?.locale
                    ?: ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]?.language ?: ""
        }

    /* Masquerading Prefs */
    var canBecomeUser by NBooleanPref()
    var isMasquerading by BooleanPref()
    var isStudentView by BooleanPref()
    var isMasqueradingFromQRCode by BooleanPref()
    var masqueradeId by LongPref(-1L)
    internal var masqueradeDomain by StringPref()
    internal var masqueradeUser: User? by GsonPref(User::class.java, null, "masq-user", false)

    // Used to determine if a student can generate a pairing code, saved during splash
    var canGeneratePairingCode by NBooleanPref()

    var domain: String
        get() = if (isMasquerading || isStudentView) masqueradeDomain else originalDomain
        set(newDomain) {
            val strippedDomain = newDomain.replaceFirst(Regex("https?://"), "").removeSuffix("/")
            if (isMasquerading || isStudentView) masqueradeDomain = strippedDomain else originalDomain = strippedDomain
        }

    var overrideDomains: MutableMap<Long, String?> = mutableMapOf()

    var shardIds: MutableMap<Long, String?> = mutableMapOf()

    val fullDomain: String
        get() = if (isMasquerading || isStudentView)  {
            when {
                masqueradeDomain.isBlank() || protocol.isBlank() -> ""
                URLUtil.isHttpUrl(masqueradeDomain) || URLUtil.isHttpsUrl(masqueradeDomain) -> masqueradeDomain
                else -> "$protocol://$masqueradeDomain"
            }
        } else {
            when {
                domain.isBlank() || protocol.isBlank() -> ""
                URLUtil.isHttpUrl(domain) || URLUtil.isHttpsUrl(domain) -> domain
                else -> "$protocol://$domain"
            }
        }

    var user: User?
        get() = if (isMasquerading || isStudentView) masqueradeUser else originalUser
        set(newUser) {
            if (isMasquerading || isStudentView) masqueradeUser = newUser else originalUser = newUser
        }

    /* Notorious Prefs */
    var notoriousDomain by StringPref()

    var notoriousToken by StringPref()

    val fullNotoriousDomain: String
        get() = when {
            notoriousDomain.isBlank() || protocol.isBlank() -> ""
                URLUtil.isHttpUrl(notoriousDomain) || URLUtil.isHttpsUrl(notoriousDomain) -> domain
                else -> "$protocol://$notoriousDomain"
            }

    var pandataInfo by GsonPref(PandataInfo::class.java)

    var airwolfDomain by StringPref("", "airwolf_domain")

    // Flag that comes from the API whether the user is an elementary user
    var canvasForElementary by BooleanPref()

    // Switch that lets the user manually override and turn off the elementary view in the settings
    var elementaryDashboardEnabledOverride by BooleanPref(true)

    var checkTokenAfterOfflineLogin by BooleanPref()

    val showElementaryView
        get() = canvasForElementary && elementaryDashboardEnabledOverride

    var canvasCareerView by NBooleanPref()

    var canSwitchToCanvasCareer by BooleanPref(defaultValue = false)

    var webViewAuthenticationTimestamp by LongPref(0)

    /**
     * clearAllData is required for logout.
     * Clears all data including credentials and cache.
     * @return true if caches files were deleted
     */
    fun clearAllData(): Boolean {
        // Clear preferences
        clearPrefs()

        pandataInfo = null

        // Clear http cache
        RestBuilder.clearCacheDirectory()

        // Clear file cache
        val cacheDir = File(ContextKeeper.appContext.filesDir, FileUtils.FILE_DIRECTORY)
        return FileUtils.deleteAllFilesInDirectory(cacheDir)
    }
}
