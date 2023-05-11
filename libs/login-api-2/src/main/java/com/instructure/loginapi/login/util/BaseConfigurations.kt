package com.instructure.loginapi.login.util

object BaseConfigurations {

    var protocol: String? = null
        private set
    var domain: String? = null
        private set
    var baseUrl: String? = null
        private set
    var clientId: String? = null
        private set
    var clientSecret: String? = null
        private set

    fun updateValues(
        protocol: String?,
        domain: String?,
        baseUrl: String?,
        clientId: String?,
        clientSecret: String?
    ) {
        this.protocol = protocol ?: this.protocol
        this.domain = domain ?: this.domain
        this.baseUrl = baseUrl ?: this.baseUrl
        this.clientId = clientId ?: this.clientId
        this.clientSecret = clientSecret ?: this.clientSecret
    }

}