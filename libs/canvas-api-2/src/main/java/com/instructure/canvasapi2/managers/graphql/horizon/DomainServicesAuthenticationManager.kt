package com.instructure.canvasapi2.managers.graphql.horizon

import com.auth0.jwt.JWT
import com.instructure.canvasapi2.apis.DomainServicesAuthenticationAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.DomainService
import com.instructure.canvasapi2.models.DomainServicesWorkflow
import com.instructure.canvasapi2.utils.DomainServicesApiPref
import com.instructure.canvasapi2.utils.JourneyApiPref
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
        val workflows = domainService.workflows
        val params = RestParams()
        val newToken = domainServicesAuthenticationAPI
            .getDomainServiceAuthentication(null, false,
                DomainServicesWorkflow(workflows), params)
            .map { it.token }
            .dataOrNull
            .orEmpty()

        return String(Base64.Default.decode(newToken))
    }

    private fun isTokenExpired(token: String?): Boolean {
        if (token.isNullOrEmpty()) return true
        if (JWT.decode(token).expiresAt <= Date()) return true

        return false
    }
}

class RedwoodAuthenticationManager @Inject constructor(
    domainServicesAuthenticationAPI: DomainServicesAuthenticationAPI,
    redwoodApiPref: RedwoodApiPref
) : DomainServicesAuthenticationManager(
    domainServicesAuthenticationAPI,
    redwoodApiPref,
    DomainService.REDWOOD
)

class JourneyAuthenticationManager @Inject constructor(
    domainServicesAuthenticationAPI: DomainServicesAuthenticationAPI,
    journeyApiPref: JourneyApiPref
) : DomainServicesAuthenticationManager(
    domainServicesAuthenticationAPI,
    journeyApiPref,
    DomainService.JOURNEY
)