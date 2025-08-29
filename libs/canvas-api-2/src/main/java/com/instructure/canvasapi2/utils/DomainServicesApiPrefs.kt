package com.instructure.canvasapi2.utils

abstract class DomainServicesApiPref(preferenceName: String): PrefManager(preferenceName) {
    abstract var token: String?
}

object PineApiPref: DomainServicesApiPref("pine_api_prefs") {
    override var token: String? by NStringPref(null, "pine_token")
}

object CedarApiPref: DomainServicesApiPref("cedar_api_prefs") {
    override var token: String? by NStringPref(null, "cedar_token")
}

object RedwoodApiPref: DomainServicesApiPref("redwood_api_prefs") {
    override var token: String? by NStringPref(null, "redwood_token")
}

object JourneyApiPref: DomainServicesApiPref("journey_api_prefs") {
    override var token: String? by NStringPref(null, "journey_token")
}