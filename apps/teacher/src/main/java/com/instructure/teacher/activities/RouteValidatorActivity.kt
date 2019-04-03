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
package com.instructure.teacher.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.FragmentActivity
import android.view.Window
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.utils.Const
import com.instructure.teacher.R
import com.instructure.teacher.fragments.FileListFragment
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.services.FileDownloadService

class RouteValidatorActivity : FragmentActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_validator)

        val data: Uri? = intent.data
        val url: String? = data?.toString()

        if (data == null || url.isNullOrBlank()) {
            finish()
            return
        }

        val host = data.host.orEmpty() // "mobiledev.instructure.com"

        val isSignedIn = ApiPrefs.token.isNotEmpty()
        val domain = ApiPrefs.domain

        if (!isSignedIn) {
            val intent = if (host.isNotBlank()) {
                SignInActivity.createIntent(this, AccountDomain(host))
            } else {
                LoginActivity.createIntent(this)
            }
            startActivity(intent)
            finish()
        } else if (host !in domain) {
            val intent = LoginActivity.createLaunchApplicationMainActivityIntent(this, null)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        } else {
            // Allow the UI to show.
            Handler().postDelayed({
                // If it's a file link we need to start a service so that our app can download it before we show it
                val route = RouteMatcher.getInternalRoute(url ?: "", domain)
                // If we've already downloaded the file we just want to route to it
                val fileDownloaded = intent.extras?.getBoolean(Const.FILE_DOWNLOADED, false) ?: false
                if (!fileDownloaded && (route?.routeContext == RouteContext.FILE || route?.primaryClass == FileListFragment::class.java && route.queryParamsHash.containsKey(RouterParams.PREVIEW))) {
                    val intent = Intent(this@RouteValidatorActivity, FileDownloadService::class.java)
                    val bundle = Bundle()
                    bundle.putParcelable(Route.ROUTE, route)
                    bundle.putString(Const.URL, url)
                    intent.putExtras(bundle)
                    this@RouteValidatorActivity.startService(intent)
                }
                RouteMatcher.routeUrl(this@RouteValidatorActivity, url ?: "", domain)

                finish()
            }, 1000)
        }
    }

    companion object {

        fun createIntent(context: Context, uri: Uri): Intent {
            val intent = Intent(context, RouteValidatorActivity::class.java)
            intent.data = uri
            return intent
        }
    }
}
