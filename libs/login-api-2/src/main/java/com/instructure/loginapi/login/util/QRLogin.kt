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
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.loginapi.login.api.MobileVerifyAPI
import com.instructure.loginapi.login.model.DomainVerificationResult
import com.instructure.loginapi.login.model.SignedInUser

object QRLogin {

    suspend fun performSSOLogin(data: Uri, isSwitchingUsers: Boolean, context: Context) {
        val domain = data.getQueryParameter("domain")
        val code = data.getQueryParameter("android_code")

        val domainVerificationResult = awaitApi<DomainVerificationResult?> {
            MobileVerifyAPI.mobileVerify(domain, it)
        }

        //mobile verify can change the hostname we need to use
        var updatedDomain = if (domainVerificationResult!!.base_url != null && domainVerificationResult.base_url != "") {
            domainVerificationResult.base_url
        } else {
            domain
        }

        if (updatedDomain.endsWith("/")) {
            updatedDomain = updatedDomain.substring(0, updatedDomain.length - 1)
        }


        // Set the updated domain
        ApiPrefs.domain = updatedDomain

        // Set client id and secret
        ApiPrefs.clientId = domainVerificationResult.client_id
        ApiPrefs.clientSecret = domainVerificationResult.client_secret

        // Set the protocol
        ApiPrefs.protocol = domainVerificationResult.protocol

        // Let's fetch the token!
        val tokenResponse = awaitApi<OAuthTokenResponse> {
            OAuthManager.getToken(ApiPrefs.clientId, ApiPrefs.clientSecret, code, it)
        }

        // Configure the token in prefs
        ApiPrefs.refreshToken = tokenResponse.refreshToken!!
        ApiPrefs.accessToken = tokenResponse.accessToken!!
        ApiPrefs.token = "" // TODO: Remove when we're 100% using refresh tokens

        val user = awaitApi<User> { UserManager.getSelf(it) }

        // Add the user to signed in and api prefs, check if they are already signed in and switching users instead
        if(isSwitchingUsers) {
            // Update SignedInUser to preserve changes to name, locale, etc
            val currentUser = ApiPrefs.user
            val signedInUser = PreviousUsersUtils.getSignedInUser(
                    ContextKeeper.appContext,
                    ApiPrefs.domain,
                    currentUser?.id ?: 0
            )
            if (currentUser != null && signedInUser != null) {
                signedInUser.user = currentUser
                PreviousUsersUtils.add(ContextKeeper.appContext, signedInUser)
            }
        } else {
            ApiPrefs.user = user
            PreviousUsersUtils.add(context, SignedInUser(
                    user,
                    updatedDomain,
                    ApiPrefs.protocol,
                    "",  // TODO - delete once we move over 100% to refresh tokens
                    tokenResponse.accessToken!!,
                    tokenResponse.refreshToken!!,
                    null,
                    null
            ))
        }

    }

    @JvmStatic
    fun verifySSOLoginUri(uri: Uri?): Boolean {
        if (uri == null) return false
        val hostList = listOf("sso.canvaslms.com", "sso.beta.canvaslms.com", "sso.test.canvaslms.com")
        return hostList.contains(uri.host.orEmpty())
                && uri.queryParameterNames.contains("android_code")
                && uri.queryParameterNames.contains("domain")
    }

}