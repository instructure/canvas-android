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

import android.app.Activity
import com.instructure.pandautils.features.legal.LegalRouter
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.launchCustomTab
import com.instructure.parentapp.R
import com.instructure.parentapp.features.webview.HtmlContentActivity

class ParentLegalRouter(private val activity: Activity) : LegalRouter {

    override fun routeToTermsOfService(html: String) {
        val intent = HtmlContentActivity.createIntent(
            activity,
            activity.getString(R.string.termsOfUse),
            html,
            true
        )
        activity.startActivity(intent)
    }

    override fun routeToPrivacyPolicy() {
        activity.launchCustomTab(Const.PRIVACY_POLICY_URL, ThemePrefs.primaryColor)
    }
}
