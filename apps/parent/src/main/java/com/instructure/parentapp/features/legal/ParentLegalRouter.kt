/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.parentapp.features.legal

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.instructure.pandautils.features.legal.LegalRouter
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.asChooserExcludingInstructure

class ParentLegalRouter(private val context: Context) : LegalRouter {

    override fun routeToTermsOfService(html: String) {
        launchCustomTab("http://www.canvaslms.com/policies/terms-of-use")
    }

    override fun routeToPrivacyPolicy() {
        launchCustomTab("https://www.instructure.com/policies/product-privacy-policy")
    }

    override fun routeToOpenSource() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://github.com/instructure/canvas-android")
        )
        context.startActivity(intent)
    }

    private fun launchCustomTab(url: String) {
        val uri = Uri.parse(url)
            .buildUpon()
            .appendQueryParameter("display", "borderless")
            .appendQueryParameter("platform", "android")
            .build()

        val colorSchemeParams = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(ThemePrefs.primaryColor)
            .build()

        var intent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(colorSchemeParams)
            .setShowTitle(true)
            .build()
            .intent

        intent.data = uri

        // Exclude Instructure apps from chooser options
        intent = intent.asChooserExcludingInstructure()

        context.startActivity(intent)
    }
}