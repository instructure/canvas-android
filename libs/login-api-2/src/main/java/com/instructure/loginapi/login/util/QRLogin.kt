/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.loginapi.login.util

import android.content.Context
import android.net.Uri
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.OAuthTokenResponse
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.loginapi.login.api.MobileVerifyAPI
import com.instructure.loginapi.login.model.DomainVerificationResult
import com.instructure.loginapi.login.model.SignedInUser

object QRLogin {

    private const val QR_DOMAIN = "domain"
    private const val QR_AUTH_CODE_STUDENT = "code_android"
    private const val QR_AUTH_CODE_TEACHER = "code_android_teacher"
    private const val QR_HOST = "sso.canvaslms.com"
    private const val QR_HOST_BETA = "sso.beta.canvaslms.com"
    private const val QR_HOST_TEST = "sso.test.canvaslms.com"

    // Returns True if Masquerading, false otherwise
    suspend fun performSSOLogin(data: Uri, context: Context, isTeacher: Boolean = false): OAuthTokenResponse {
        val domain = data.getQueryParameter(QR_DOMAIN)
        val code = if(isTeacher) {
            data.getQueryParameter(QR_AUTH_CODE_TEACHER)
        } else {
            data.getQueryParameter(QR_AUTH_CODE_STUDENT)
        }

        val domainVerificationResult = awaitApi<DomainVerificationResult> {
            MobileVerifyAPI.mobileVerify(domain, it)
        }

        // Mobile verify can change the hostname we need to use
        var updatedDomain = domainVerificationResult.baseUrl.validOrNull() ?: domain

        if (updatedDomain?.endsWith("/") == true) {
            updatedDomain = updatedDomain.substring(0, updatedDomain.length - 1)
        }

        // Set the updated domain
        ApiPrefs.domain = updatedDomain!!

        // Set client id and secret
        ApiPrefs.clientId = domainVerificationResult.clientId
        ApiPrefs.clientSecret = domainVerificationResult.clientSecret

        // Set the protocol
        ApiPrefs.protocol = domainVerificationResult.protocol

        // Let's fetch the token!
        val tokenResponse = awaitApi<OAuthTokenResponse> {
            OAuthManager.getToken(ApiPrefs.clientId, ApiPrefs.clientSecret, code!!, it)
        }

        // Configure the token in prefs
        ApiPrefs.refreshToken = tokenResponse.refreshToken!!
        ApiPrefs.accessToken = tokenResponse.accessToken!!
        ApiPrefs.token = "" // TODO: Remove when we're 100% using refresh tokens

        if(tokenResponse.realUser == null) {
            // This is a real login, not masquerading. Go ahead and cache the user.
            val user = awaitApi<User> { UserManager.getSelf(it) }

            // Add the user to signed in and api prefs
            ApiPrefs.user = user
            PreviousUsersUtils.add(context, SignedInUser(
                user = user,
                domain = updatedDomain,
                protocol = ApiPrefs.protocol,
                token = "",  // TODO - delete once we move over 100% to refresh tokens
                accessToken = tokenResponse.accessToken!!,
                refreshToken = tokenResponse.refreshToken!!,
                clientId = domainVerificationResult.clientId,
                clientSecret = domainVerificationResult.clientSecret,
                calendarFilterPrefs = null,
                lastLogoutDate = null
            ))
        }

        return tokenResponse
    }

    fun verifySSOLoginUri(uri: Uri?, isTeacher: Boolean = false): Boolean {
        if (uri == null) return false
        val codeParam = if(isTeacher) QR_AUTH_CODE_TEACHER else QR_AUTH_CODE_STUDENT
        val hostList = listOf(QR_HOST, QR_HOST_BETA, QR_HOST_TEST)
        return hostList.contains(uri.host.orEmpty())
                && uri.queryParameterNames.contains(QR_DOMAIN)
                && uri.queryParameterNames.contains(codeParam)
    }
}
