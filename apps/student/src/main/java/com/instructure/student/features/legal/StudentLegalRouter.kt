/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.student.features.legal

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.instructure.pandautils.features.legal.LegalRouter
import com.instructure.student.R
import com.instructure.student.activity.InternalWebViewActivity

class StudentLegalRouter(private val context: Context) : LegalRouter {
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