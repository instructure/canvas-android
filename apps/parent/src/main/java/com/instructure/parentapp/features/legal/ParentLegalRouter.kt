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
import com.instructure.pandautils.features.legal.LegalRouter
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.launchCustomTab

class ParentLegalRouter(private val context: Context) : LegalRouter {

    override fun routeToTermsOfService(html: String) {
        context.launchCustomTab("http://www.canvaslms.com/policies/terms-of-use", ThemePrefs.primaryColor)
    }

    override fun routeToPrivacyPolicy() {
        context.launchCustomTab("https://www.instructure.com/policies/product-privacy-policy", ThemePrefs.primaryColor)
    }

    override fun routeToOpenSource() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://github.com/instructure/canvas-android")
        )
        context.startActivity(intent)
    }
}
