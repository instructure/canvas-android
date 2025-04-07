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
package com.instructure.teacher.features.legal

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.instructure.pandautils.features.legal.LegalRouter
import com.instructure.teacher.R
import com.instructure.teacher.activities.InternalWebViewActivity

class TeacherLegalRouter(private val context: Context) : LegalRouter {

    override fun routeToTermsOfService(html: String) {
        val intent = InternalWebViewActivity.createIntent(
            context, "https://www.canvaslms.com/policies/terms-of-use", html, context.getString(
                R.string.termsOfUse
            ), false
        )
        context.startActivity(intent)
    }

    override fun routeToPrivacyPolicy() {
        val intent = InternalWebViewActivity.createIntent(
            context,
            "https://www.instructure.com/policies/product-privacy-policy",
            context.getString(R.string.privacyPolicy),
            false
        )
        context.startActivity(intent)
    }

    override fun routeToOpenSource() {
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/instructure/canvas-android"))
        context.startActivity(intent)
    }
}