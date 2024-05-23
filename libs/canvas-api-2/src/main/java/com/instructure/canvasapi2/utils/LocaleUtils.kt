/*
 * Copyright (C) 2018 - present Instructure, Inc.
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

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Handler
import android.os.LocaleList
import androidx.core.os.ConfigurationCompat
import java.util.*
import kotlin.system.exitProcess

object LocaleUtils {

    const val LANGUAGES_PENDING_INTENT_KEY = "languagesPendingIntentKey"
    const val LANGUAGES_PENDING_INTENT_ID = 654321

    fun getSupportedLanguageTags() : Array<String> {
        return emptyArray() // Populated by LocaleTransformer at build time
    }

    @Suppress("DEPRECATION", "UNUSED")
    fun wrapContext(base: Context): Context {
        var localeString = ApiPrefs.effectiveLocale

        // Replace the private use identifier "-x-" with the padded "-inst" if the variant is less than 5 letters
        val localeParts = localeString.split("-x-")
        if (localeParts.size > 1) {
            localeString = if (localeParts[1].length < 5) {
                "${localeParts[0]}-inst${localeParts[1]}" // da-x-k12 -> da-instk12
            } else {
                // Only take the first 8 characters, the maximum size of locale variants
                "${localeParts[0]}-${localeParts[1].take(8)}" // en-AU-x-unimelb -> en-AU-unimelb
            }
        }

        // This is a workaround, because the AAPT bundled in the latest AGP (7.0.0+) has a bug.
        // It seems like the resource merger cannot recognise resources using custom BCP-47 language subtags without also specifing a region tag.
        // We should monitor this in later releases and remove the workaround once it's fixed.
        // Related issue: https://issuetracker.google.com/issues/234820481
        if (localeString == "da-instk12") localeString = "da-DK-instk12"
        if (localeString == "nb-instk12") localeString = "nb-NO-instk12"
        if (localeString == "sv-instk12") localeString = "sv-SE-instk12"

        val locale = if (localeString == ApiPrefs.DEVICE_LOCALE) {
            ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]
        } else {
            Locale.Builder().setLanguageTag(localeString).build()
        }
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocales(LocaleList(locale))
        ContextKeeper.updateLocale(config)
        return base.createConfigurationContext(config)
    }

    fun restartApp(context: Context, startingClass: Class<*>) {
        // Restart the App to apply language after a short delay to guarantee shared prefs are saved
        Handler().postDelayed({
            val intent = Intent(context, startingClass)
            intent.putExtra(LANGUAGES_PENDING_INTENT_KEY, LANGUAGES_PENDING_INTENT_ID)
            val mPendingIntent = PendingIntent.getActivity(context, LANGUAGES_PENDING_INTENT_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
            exitProcess(0)
        }, 500)
    }

}

val Locale.cleanDisplayName: String
    get() {
        val displayLanguage = getDisplayLanguage(this).capitalized()
        val displayTags = listOf<String>(
            getDisplayScript(this),
            getDisplayCountry(this),
            getDisplayVariant(this).substringAfter("INST")
        ).filter { it.isValid() }
        return if (displayTags.isNotEmpty()) "$displayLanguage (${displayTags.joinToString()})" else displayLanguage
    }
