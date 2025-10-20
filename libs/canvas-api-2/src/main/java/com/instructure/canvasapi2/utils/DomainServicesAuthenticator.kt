/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.canvasapi2.utils

import com.instructure.canvasapi2.managers.graphql.horizon.CedarAuthenticationManager
import com.instructure.canvasapi2.managers.graphql.horizon.DomainServicesAuthenticationManager
import com.instructure.canvasapi2.managers.graphql.horizon.JourneyAuthenticationManager
import com.instructure.canvasapi2.managers.graphql.horizon.PineAuthenticationManager
import com.instructure.canvasapi2.managers.graphql.horizon.RedwoodAuthenticationManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

abstract class DomainServicesAuthenticator(
    private val authenticationManager: DomainServicesAuthenticationManager
): Authenticator {
    private val retryHeader = "mobile_refresh"
    private val authHeader = "Authorization"

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header(retryHeader) != null) {
            return null
        }

        val token = runBlocking { authenticationManager.getAuthenticationToken(true) }

        return response.request.newBuilder()
            .header(authHeader, "Bearer $token")
            .header(retryHeader, retryHeader) // Mark retry to prevent infinite recursion
            .build()
    }
}

class PineAuthenticator @Inject constructor(
    pineAuthenticationManager: PineAuthenticationManager
) : DomainServicesAuthenticator(pineAuthenticationManager)

class CedarAuthenticator @Inject constructor(
    cedarAuthenticationManager: CedarAuthenticationManager
) : DomainServicesAuthenticator(cedarAuthenticationManager)

class RedwoodAuthenticator @Inject constructor(
    redwoodAuthenticationManager: RedwoodAuthenticationManager
) : DomainServicesAuthenticator(redwoodAuthenticationManager)

class JourneyAuthenticator @Inject constructor(
    journeyAuthenticationManager: JourneyAuthenticationManager
) : DomainServicesAuthenticator(journeyAuthenticationManager)