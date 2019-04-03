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
package com.instructure.pandautils.utils

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
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.isValid
import java.util.*

object LocaleUtils {

    @JvmStatic
    fun getSupportedLanguageTags() : Array<String> {
        return emptyArray() // Populated by LocaleTransformer at build time
    }

    @JvmStatic
    @Suppress("DEPRECATION")
    fun wrapContext(base: Context): Context {
        val localeString = ApiPrefs.effectiveLocale.replace("-x-", "-inst")
        val locale = if (localeString == ApiPrefs.DEVICE_LOCALE) {
            ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]
        } else {
            Locale.Builder().setLanguageTag(localeString).build()
        }
        Locale.setDefault(locale)
        val config = Configuration()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.locales = LocaleList(locale)
        } else {
            config.locale = locale
        }
        ContextKeeper.updateLocale(config)
        return base.createConfigurationContext(config)
    }

    fun restartApp(context: Context, startingClass: Class<*>) {
        // Restart the App to apply language after a short delay to guarantee shared prefs are saved
        Handler().postDelayed({
            val intent = Intent(context, startingClass)
            intent.putExtra(Const.LANGUAGES_PENDING_INTENT_KEY, Const.LANGUAGES_PENDING_INTENT_ID)
            val mPendingIntent = PendingIntent.getActivity(context, Const.LANGUAGES_PENDING_INTENT_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT)
            val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
            System.exit(0)
        }, 500)
    }

}

val Locale.cleanDisplayName: String
    get() {
        val displayLanguage = getDisplayLanguage(this).capitalize()
        val displayTags = listOf<String>(
            getDisplayScript(this),
            getDisplayCountry(this),
            getDisplayVariant(this).substringAfter("INST")
        ).filter { it.isValid() }
        return if (displayTags.isNotEmpty()) "$displayLanguage (${displayTags.joinToString()})" else displayLanguage
    }
