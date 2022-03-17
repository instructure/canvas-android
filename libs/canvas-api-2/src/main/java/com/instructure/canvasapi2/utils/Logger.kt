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

package com.instructure.canvasapi2.utils

import android.content.Context
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

object Logger {

    // Pact tests need to be able to disable Android logging
    var IS_LOGGING = true

    const val LOG_TAG = "canvasLog"

    /**
     * List of ISO 3166-1 alpha-2 codes of countries whose laws restrict us from logging user details
     */
    private val LOGGING_DISALLOWED_COUNTRY_CODES = arrayOf("CA")

    @JvmStatic fun d(s: String?) {
        if(IS_LOGGING) {
            Log.d(LOG_TAG, s ?: "Value was null.")
        }
    }
    @JvmStatic fun i(s: String?) { Log.i(LOG_TAG, s ?: "Value was null.") }
    @JvmStatic fun e(s: String?) { Log.e(LOG_TAG, s ?: "Value was null.") }
    @JvmStatic fun v(s: String?) { Log.v(LOG_TAG, s ?: "Value was null.") }
    @JvmStatic fun w(s: String?) { Log.w(LOG_TAG, s ?: "Value was null.") }

    fun date(s: String?, date: GregorianCalendar) {
        val sdf = SimpleDateFormat("dd MMM yyyy hh:mm:ss zzz", Locale.US)
        Log.d(LOG_TAG, s + ": " + sdf.format(Date(date.timeInMillis)))
    }

    fun logBundle(extras: Bundle?) {
        if (extras != null) {
            d("---====---LOGGING BUNDLE---====---")

            if (extras.size() == 0) {
                d("- Bundle was empty.")
            }

            for (key in extras.keySet()) {
                d("- Bundle: " + key)

                if ("bundledExtras" == key) {
                    val innerExtras = extras.getBundle("bundledExtras")
                    if (innerExtras != null) {
                        for (innerKey in innerExtras.keySet()) {
                            d("   -> Inner Bundle: " + innerKey)
                        }
                    }
                }
            }
        } else {
            d("Bundle was null.")
        }
    }

    fun <F : Fragment> getFragmentName(fragment: F?): String {
        return if (fragment != null) {
            fragment.javaClass.name
        } else "UNKNOWN"
    }

    /**
     * Whether user detail logging is allowed for the current country. This checks the network country,
     * the SIM country, and the current locale country. If *any* of these match a disallowed
     * country then this method will return false.
     * @return True if user detail logging is allowed, false otherwise.
     */
    fun canLogUserDetails(): Boolean {
        val telephonyManager = ContextKeeper.appContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val networkCountryCode = telephonyManager.networkCountryIso.uppercase(Locale.US)
        val simCountryCode = telephonyManager.simCountryIso.uppercase(Locale.US)
        val localeCountryCode = Locale.getDefault().country.uppercase(Locale.US)

        return LOGGING_DISALLOWED_COUNTRY_CODES.none {
            (it == networkCountryCode || it == simCountryCode || it == localeCountryCode)
        }
    }
}
