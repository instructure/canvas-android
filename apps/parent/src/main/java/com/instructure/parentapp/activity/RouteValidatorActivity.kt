/*
 * Copyright (C) 2019 - present  Instructure, Inc.
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
package com.instructure.parentapp.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.utils.Const
import com.instructure.parentapp.R
import com.instructure.parentapp.util.RouteMatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class RouteValidatorActivity : AppCompatActivity() {

    private var loadingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_validator)

        val data: Uri? = intent.data
        val url: String? = data?.toString()

        if (data == null || url.isNullOrBlank()) {
            finish()
            return
        }

        loadRoute(data, url)
    }

    private fun loadRoute(data: Uri, url: String) {
        loadingJob = tryWeave {
            val host = data.host.orEmpty() // example: "mobiledev.instructure.com"

            // Log data
            Logger.w(data.toString())

            val token = ApiPrefs.token
            val signedIn = token.isNotEmpty()
            val domain = ApiPrefs.domain

            if (!signedIn) {
                delay(700)
                val intent = if (host.isNotBlank()) {
                    SignInActivity.createIntent(this@RouteValidatorActivity, AccountDomain(host), false)
                } else {
                    LoginActivity.createIntent(this@RouteValidatorActivity)
                }
                startActivity(intent)
                finish()
                return@tryWeave
            }

            if (signedIn && !domain.contains(host)) {
                delay(700)
                val intent = SplashActivity.createIntent(this@RouteValidatorActivity)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

                // We're not doing anything with the message in the intent above so until we do, here's a Toast
                Toast.makeText(this@RouteValidatorActivity, getString(R.string.differentDomainFromLink), Toast.LENGTH_SHORT).show()

                startActivity(intent)
                finish()
                return@tryWeave
            } else {
                // Allow the UI to show
                delay(700)
                RouteMatcher.routeUrl(this@RouteValidatorActivity, url, ApiPrefs.user, domain)
            }
        } catch {
            finish()
        }
    }

    override fun finish() {
        overridePendingTransition(0, 0)
        super.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingJob?.cancel()
    }

    companion object {
        fun createIntent(context: Context, uri: Uri): Intent {
            val intent = Intent(context, RouteValidatorActivity::class.java)
            intent.data = uri
            return intent
        }
    }
}
