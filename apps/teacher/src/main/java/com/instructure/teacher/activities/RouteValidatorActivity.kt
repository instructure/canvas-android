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
import android.view.Window
import android.widget.Toast
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.AnalyticsParamConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.apiAsync
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.interactions.router.RouterParams
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.loginapi.login.util.QRLogin
import com.instructure.loginapi.login.util.QRLogin.verifySSOLoginUri
import com.instructure.pandautils.base.BaseCanvasActivity
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.pandautils.utils.AppType
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.Utils
import com.instructure.teacher.R
import com.instructure.teacher.databinding.ActivityRouteValidatorBinding
import com.instructure.teacher.fragments.FileListFragment
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.services.FileDownloadService
import com.instructure.teacher.tasks.TeacherLogoutTask
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import javax.inject.Inject

@AndroidEntryPoint
class RouteValidatorActivity : BaseCanvasActivity() {

    private val binding by viewBinding(ActivityRouteValidatorBinding::inflate)

    private var routeValidatorJob: Job? = null

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    public override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val data: Uri? = intent.data
        val url: String? = data?.toString()

        if (data == null || url.isNullOrBlank()) {
            finish()
            return
        }

        routeValidatorJob = tryWeave {

            val host = data.host.orEmpty() // "mobiledev.instructure.com"

            val isSignedIn = ApiPrefs.getValidToken().isNotEmpty()
            val domain = ApiPrefs.domain

            if (verifySSOLoginUri(data, AppType.TEACHER)) {
                // This is an App Link from a QR code, let's try to login the user and launch navigationActivity
                try {
                    if (isSignedIn) { // If the user is already signed in, use the QR Switch
                        TeacherLogoutTask(
                            type = LogoutTask.Type.QR_CODE_SWITCH,
                            uri = data,
                            alarmScheduler = alarmScheduler
                        ).execute()
                        finish()
                        return@tryWeave
                    }

                    // Mobile verify requires a user agent be set, multiple attempts/failures can clear this out
                    if(ApiPrefs.userAgent == "") {
                        ApiPrefs.userAgent = Utils.generateUserAgent(this@RouteValidatorActivity, Const.TEACHER_USER_AGENT)
                    }

                    val tokenResponse = QRLogin.performSSOLogin(data, this@RouteValidatorActivity, AppType.TEACHER)

                    val authResult = apiAsync { OAuthManager.getAuthenticatedSession(ApiPrefs.fullDomain, it) }.await()
                    if (authResult.isSuccess) {
                        authResult.dataOrNull?.sessionUrl?.let {
                            binding.dummyWebView.loadUrl(it)
                        }
                    }

                    // If we have a real user, this is a QR code from a masquerading web user
                    val intent = if (tokenResponse.realUser != null && tokenResponse.user != null) {
                        // We need to set the masquerade request to the user (masqueradee), the real user it the admin user currently masquerading
                        ApiPrefs.isMasqueradingFromQRCode = true
                        val extras = Bundle()
                        extras.putLong(Const.QR_CODE_MASQUERADE_ID, tokenResponse.user!!.id)
                        LoginActivity.createLaunchApplicationMainActivityIntent(this@RouteValidatorActivity, extras)
                    } else {
                        logQREvent(ApiPrefs.domain, true)
                        LoginActivity.createLaunchApplicationMainActivityIntent(this@RouteValidatorActivity, null)
                    }

                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finishAffinity()
                    return@tryWeave
                } catch (e: Throwable) {
                    // If the user wasn't already signed in, let's clear the prefs in case it was a partial success
                    if (!isSignedIn) {
                        ApiPrefs.clearAllData()
                    }

                    // Log the analytics
                    logQREvent(ApiPrefs.domain, false)

                    Toast.makeText(this@RouteValidatorActivity, R.string.loginWithQRCodeError, Toast.LENGTH_LONG).show()
                    finish()
                    return@tryWeave
                }
            }

            if (!isSignedIn) {
                val intent = if (host.isNotBlank()) {
                    SignInActivity.createIntent(this@RouteValidatorActivity, AccountDomain(host))
                } else {
                    LoginActivity.createIntent(this@RouteValidatorActivity)
                }
                startActivity(intent)
                finish()
                return@tryWeave
            } else if (host !in domain) {
                val intent = LoginActivity.createLaunchApplicationMainActivityIntent(this@RouteValidatorActivity, null)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
                return@tryWeave
            } else {
                // Allow the UI to show.
                Handler().postDelayed({
                    // If it's a file link we need to start a service so that our app can download it before we show it
                    val route = RouteMatcher.getInternalRoute(url, domain)
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
                    RouteMatcher.routeUrl(this@RouteValidatorActivity, url, domain)

                    finish()
                }, 1000)
                return@tryWeave
            }
        } catch {
            finish()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        routeValidatorJob?.cancel()
    }

    private fun logQREvent(domain: String, isSuccess: Boolean) {
        val bundle = Bundle().apply {
            putString(AnalyticsParamConstants.DOMAIN_PARAM, domain)
        }
        if (isSuccess) {
            Analytics.logEvent(AnalyticsEventConstants.QR_CODE_LOGIN_SUCCESS, bundle)
        } else {
            Analytics.logEvent(AnalyticsEventConstants.QR_CODE_LOGIN_FAILURE, bundle)
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
