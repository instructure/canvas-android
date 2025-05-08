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
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.DomainService
import com.instructure.canvasapi2.utils.DomainServicesApiPrefs
import java.util.Date
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class DomainServicesAuthenticationManager @Inject constructor(
    private val domainServicesAuthenticationAPI: DomainServicesAuthenticationAPI,
    private val domainApiPrefs: DomainServicesApiPrefs
) {
    @OptIn(ExperimentalEncodingApi::class)
    suspend fun getDomainServicesAuthenticationToken(
        domainService: DomainService
    ): String {
        val adapter = RestBuilder()
        val params = RestParams()
        return when(domainService) {
            DomainService.PINE -> {
                val cachedToken = domainApiPrefs.pineToken.ifEmpty { null }
                if (cachedToken == null || isTokenExpired(cachedToken)) {
                    val newToken = domainServicesAuthenticationAPI
                        .getDomainServiceAuthentication(domainService, adapter, params)
                        .map { it.token }
                        .dataOrNull
                        .orEmpty()

                    val decoded = String(Base64.decode(newToken))
                    domainApiPrefs.pineToken = decoded

                    decoded

                } else {
                    cachedToken
                }
            }
            DomainService.CEDAR -> {
                val cachedToken = domainApiPrefs.cedarToken.ifEmpty { null }
                if (cachedToken == null || isTokenExpired(cachedToken)) {
                    val newToken = domainServicesAuthenticationAPI
                        .getDomainServiceAuthentication(domainService, adapter, params)
                        .map { it.token }
                        .dataOrNull
                        .orEmpty()

                    val decoded = String(Base64.decode(newToken))
                    domainApiPrefs.cedarToken = decoded

                    decoded
                } else {
                    cachedToken
                }
            }
            DomainService.REDWOOD -> {
                val cachedToken = domainApiPrefs.redwoodToken.ifEmpty { null }
                if (cachedToken == null || isTokenExpired(cachedToken)) {
                    val newToken = domainServicesAuthenticationAPI
                        .getDomainServiceAuthentication(domainService, adapter, params)
                        .map { it.token }
                        .dataOrNull
                        .orEmpty()

                    val decoded = String(Base64.decode(newToken))
                    domainApiPrefs.redwoodToken = decoded

                    decoded
                } else {
                    cachedToken
                }
            }
        }
    }

    private fun isTokenExpired(token: String?): Boolean {
        if (token.isNullOrEmpty()) return true
        if (JWT.decode(token).expiresAt <= Date()) return true

        return false
    }
}