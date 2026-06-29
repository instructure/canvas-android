package com.instructure.canvasapi2.managers.graphql.horizon

import com.auth0.jwt.JWT
import com.instructure.canvasapi2.apis.DomainServicesAuthenticationAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.DomainService
import com.instructure.canvasapi2.models.DomainServicesWorkflow
import com.instructure.canvasapi2.utils.DomainServicesApiPref
import com.instructure.canvasapi2.utils.JourneyApiPref
import java.util.Date
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

abstract class DomainServicesAuthenticationManager(
    private val domainServicesAuthenticationAPI: DomainServicesAuthenticationAPI,
    private val domainServicesApiPref: DomainServicesApiPref,
    private val domainService: DomainService
) {
    suspend fun getAuthenticationToken(forceRefresh: Boolean = false): String? {
        val cachedToken = domainServicesApiPref.token?.ifEmpty { null }
        if (!forceRefresh && cachedToken != null && !isTokenExpired(cachedToken)) {
            return cachedToken
        }
        val newToken = requestAuthenticationToken(domainService) ?: return null
        domainServicesApiPref.token = newToken
        return newToken
    }

    @OptIn(ExperimentalEncodingApi::class)
    private suspend fun requestAuthenticationToken(domainService: DomainService): String? {
        val workflows = domainService.workflows
        val params = RestParams()
        val encoded = domainServicesAuthenticationAPI
            .getDomainServiceAuthentication(null, false,
                DomainServicesWorkflow(workflows), params)
            .map { it.token }
            .dataOrNull ?: return null

        return runCatching { String(Base64.Default.decode(encoded)) }.getOrNull()
    }

    private fun isTokenExpired(token: String?): Boolean {
        if (token.isNullOrEmpty()) return true
        return runCatching { JWT.decode(token).expiresAt <= Date() }.getOrDefault(true)
    }
}

class JourneyAuthenticationManager @Inject constructor(
    domainServicesAuthenticationAPI: DomainServicesAuthenticationAPI,
    journeyApiPref: JourneyApiPref
) : DomainServicesAuthenticationManager(
    domainServicesAuthenticationAPI,
    journeyApiPref,
    DomainService.JOURNEY
)