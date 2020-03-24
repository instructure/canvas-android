/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.student.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.loginapi.login.util.QRLogin.performSSOLogin
import com.instructure.loginapi.login.util.QRLogin.verifySSOLoginUri
import com.instructure.pandautils.utils.Const
import com.instructure.student.R
import com.instructure.student.router.RouteMatcher
import com.instructure.student.tasks.StudentLogoutTask
import com.instructure.student.util.LoggingUtility
import kotlinx.android.synthetic.main.loading_canvas_view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class InterwebsToApplication : AppCompatActivity() {

    private var loadingJob: Job? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.interwebs_to_application)
        loadingRoute.visibility = View.VISIBLE

        val url = intent.dataString

        if (TextUtils.isEmpty(url)) {
            finish()
            return
        }

        val data = Uri.parse(url)

        if (data == null) {
            finish()
            return
        }

        loadRoute(data, url)
    }

    private fun loadRoute(data: Uri, url: String) {
        loadingJob = tryWeave {
            val host = data.host.orEmpty() // example: "mobiledev.instructure.com"

            // Do some logging
            LoggingUtility.Log(this@InterwebsToApplication, Log.WARN, data.toString())

            val token = ApiPrefs.getValidToken()
            val signedIn = token.isNotEmpty()
            val domain = ApiPrefs.domain

            val qrLoginEnabled = RemoteConfigUtils.getString(
                    RemoteConfigParam.QR_LOGIN_ENABLED)?.equals("true", ignoreCase = true)
                    ?: false
            if (verifySSOLoginUri(data) && qrLoginEnabled) {
                // This is an App Link from a QR code, let's try to login the user and launch navigationActivity
                try {
                    if(signedIn) { // If the user is already signed in, use the QR Switch
                        StudentLogoutTask(type = LogoutTask.Type.QR_CODE_SWITCH, uri = data).execute()
                        finish()
                        return@tryWeave
                    }

                    val tokenResponse = performSSOLogin(data, this@InterwebsToApplication)


                    // Add delay for animation and launch Navigation Activity
                    delay(700)

                    // If we have a real user, this is a QR code from a masquerading web user
                    val intent = if(tokenResponse.realUser != null && tokenResponse.user != null) {
                        // We need to set the masquerade request to the user (masqueradee), the real user it the admin user currently masquerading
                        ApiPrefs.isMasqueradingFromQRCode = true
                        NavigationActivity.createIntent(this@InterwebsToApplication, tokenResponse.user!!.id)
                    } else {
                        // Log the analytics - only for real logins, not masquerading
                        logQREvent(ApiPrefs.domain, true)
                        Intent(this@InterwebsToApplication, NavigationActivity.startActivityClass)
                    }

                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    return@tryWeave
                } catch (e: Throwable) {
                    // If the user wasn't already signed in, let's clear the prefs in case it was a partial success
                    if(!signedIn) {
                        ApiPrefs.clearAllData()
                    }

                    // Log the analytics
                    logQREvent(ApiPrefs.domain, false)

                    Toast.makeText(this@InterwebsToApplication, R.string.loginWithQRCodeError, Toast.LENGTH_LONG).show()
                    finish()
                    return@tryWeave
                }
            }


            if (!signedIn) {
                delay(700)
                val intent = if (host.isNotBlank()) {
                    SignInActivity.createIntent(this@InterwebsToApplication, AccountDomain(host))
                } else {
                    LoginActivity.createIntent(this@InterwebsToApplication)
                }
                startActivity(intent)
                finish()
                return@tryWeave
            }

            if (signedIn && !domain.contains(host)) {
                delay(700)
                val intent = Intent(this@InterwebsToApplication, NavigationActivity.startActivityClass)
                intent.putExtra(Const.MESSAGE, getString(R.string.differentDomainFromLink))
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
                return@tryWeave
            } else {
                // Allow the UI to show
                delay(700)
                RouteMatcher.routeUrl(this@InterwebsToApplication, url, domain)
            }

        } catch {
            finish()
        }
    }

    private fun logQREvent(domain: String, isSuccess: Boolean) {
        val bundle = Bundle().apply {
            putString(AnalyticsParamConstants.DOMAIN_PARAM, domain)
        }
        if(isSuccess) {
            Analytics.logEvent(AnalyticsEventConstants.QR_CODE_LOGIN_SUCCESS, bundle)
        } else {
            Analytics.logEvent(AnalyticsEventConstants.QR_CODE_LOGIN_FAILURE, bundle)
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
            val intent = Intent(context, InterwebsToApplication::class.java)
            intent.data = uri
            return intent
        }
    }
}
