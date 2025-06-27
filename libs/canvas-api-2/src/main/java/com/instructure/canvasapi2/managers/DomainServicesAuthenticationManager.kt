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
package com.instructure.canvasapi2.managers

import com.auth0.jwt.JWT
import com.instructure.canvasapi2.apis.DomainServicesAuthenticationAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.DomainService
import com.instructure.canvasapi2.utils.CedarApiPref
import com.instructure.canvasapi2.utils.DomainServicesApiPref
import com.instructure.canvasapi2.utils.PineApiPref
import com.instructure.canvasapi2.utils.RedwoodApiPref
import java.util.Date
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

abstract class DomainServicesAuthenticationManager(
    private val domainServicesAuthenticationAPI: DomainServicesAuthenticationAPI,
    private val domainServicesApiPref: DomainServicesApiPref,
    private val domainService: DomainService
) {
    suspend fun getAuthenticationToken(forceRefresh: Boolean = false): String {
        val cachedToken = domainServicesApiPref.token?.ifEmpty { null }
        return if (forceRefresh || cachedToken == null || isTokenExpired(cachedToken)) {
            val newToken = requestAuthenticationToken(domainService)
            domainServicesApiPref.token = newToken

            newToken

        } else {
            cachedToken
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private suspend fun requestAuthenticationToken(domainService: DomainService): String {
        val audience = domainService.audience.replace("https://", "")
        val workflow = domainService.workflows
        val params = RestParams()
        val newToken = domainServicesAuthenticationAPI
            .getDomainServiceAuthentication(audience, workflow, params)
            .map { it.token }
            .dataOrNull
            .orEmpty()

        return String(Base64.decode(newToken))
    }

    private fun isTokenExpired(token: String?): Boolean {
        if (token.isNullOrEmpty()) return true
        if (JWT.decode(token).expiresAt <= Date()) return true

        return false
    }
}

class PineAuthenticationManager @Inject constructor(
    domainServicesAuthenticationAPI: DomainServicesAuthenticationAPI,
    pineApiPref: PineApiPref
) : DomainServicesAuthenticationManager(
    domainServicesAuthenticationAPI,
    pineApiPref,
    DomainService.PINE
)

class CedarAuthenticationManager @Inject constructor(
    domainServicesAuthenticationAPI: DomainServicesAuthenticationAPI,
    cedarApiPref: CedarApiPref
) : DomainServicesAuthenticationManager(
    domainServicesAuthenticationAPI,
    cedarApiPref,
    DomainService.CEDAR
)

class RedwoodAuthenticationManager @Inject constructor(
    domainServicesAuthenticationAPI: DomainServicesAuthenticationAPI,
    redwoodApiPref: RedwoodApiPref
) : DomainServicesAuthenticationManager(
    domainServicesAuthenticationAPI,
    redwoodApiPref,
    DomainService.REDWOOD
)