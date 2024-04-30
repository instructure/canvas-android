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

package com.instructure.parentapp.features.login.routevalidator

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.AnalyticsParamConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.loginapi.login.util.QRLogin
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.utils.AppType
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.Utils
import com.instructure.parentapp.R
import com.instructure.parentapp.util.ParentLogoutTask
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import javax.inject.Inject


@HiltViewModel
class RouteValidatorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiPrefs: ApiPrefs,
    private val oAuthApi: OAuthAPI.OAuthInterface
) : ViewModel() {

    val events: LiveData<Event<RouteValidatorAction>>
        get() = _events
    private val _events = MutableLiveData<Event<RouteValidatorAction>>()

    fun loadRoute(url: String?) {
        viewModelScope.tryLaunch {
            val data = Uri.parse(url.orEmpty())
            if (url.isNullOrEmpty() || data == null) {
                _events.value = Event(RouteValidatorAction.Finish)
                return@tryLaunch
            }

            val host = data.host.orEmpty() // example: "mobiledev.instructure.com"
            val token = apiPrefs.getValidToken()
            val signedIn = token.isNotEmpty()
            val domain = apiPrefs.domain

            if (QRLogin.verifySSOLoginUri(data)) {
                // This is an App Link from a QR code, let's try to login the user and launch MainActivity
                try {
                    if (signedIn) { // If the user is already signed in, use the QR Switch
                        ParentLogoutTask(type = LogoutTask.Type.QR_CODE_SWITCH, uri = data).execute()
                        _events.value = Event(RouteValidatorAction.Finish)
                        return@tryLaunch
                    }

                    if (apiPrefs.userAgent.isEmpty()) {
                        apiPrefs.userAgent = Utils.generateUserAgent(context, Const.PARENT_USER_AGENT)
                    }

                    val tokenResponse = QRLogin.performSSOLogin(data, context, AppType.PARENT)

                    val authResult = oAuthApi.getAuthenticatedSession(ApiPrefs.fullDomain, RestParams(isForceReadFromNetwork = true))
                    if (authResult.isSuccess) {
                        _events.value = Event(RouteValidatorAction.LoadWebViewUrl(authResult.dataOrNull?.sessionUrl.orEmpty()))
                    }

                    // If we have a real user, this is a QR code from a masquerading web user
                    if (tokenResponse.realUser != null && tokenResponse.user != null) {
                        // We need to set the masquerade request to the user (masqueradee), the real user it the admin user currently masquerading
                        val masqueradeId = tokenResponse.user!!.id
                        apiPrefs.isMasqueradingFromQRCode = true
                        apiPrefs.masqueradeId = masqueradeId
                        postActionWithDelay(RouteValidatorAction.StartMainActivity(masqueradeId))
                    } else {
                        // Log the analytics - only for real logins, not masquerading
                        logQREvent(apiPrefs.domain, true)
                        postActionWithDelay(RouteValidatorAction.StartMainActivity())
                    }
                    return@tryLaunch
                } catch (e: Throwable) {
                    // If the user wasn't already signed in, let's clear the prefs in case it was a partial success
                    if (!signedIn) {
                        apiPrefs.clearAllData()
                    }

                    // Log the analytics
                    logQREvent(apiPrefs.domain, false)

                    _events.value = Event(RouteValidatorAction.ShowToast(context.getString(R.string.loginWithQRCodeError)))
                    _events.value = Event(RouteValidatorAction.Finish)
                    return@tryLaunch
                }
            }

            if (!signedIn) {
                if (host.isNotBlank()) {
                    postActionWithDelay(RouteValidatorAction.StartSignInActivity(AccountDomain(host)))
                } else {
                    postActionWithDelay(RouteValidatorAction.StartLoginActivity)
                }
                return@tryLaunch
            }

            if (!domain.contains(host)) {
                // TODO: Handle different domain
                _events.value = Event(RouteValidatorAction.Finish)
            } else {
                postActionWithDelay(RouteValidatorAction.StartMainActivity(data = data))
            }
        } catch {
            _events.value = Event(RouteValidatorAction.Finish)
        }
    }

    private suspend fun postActionWithDelay(event: RouteValidatorAction, delay: Long = 700L) {
        delay(delay) // Allow the UI to show
        _events.value = Event(event)
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
}
