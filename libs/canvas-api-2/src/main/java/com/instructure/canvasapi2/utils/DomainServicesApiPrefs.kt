package com.instructure.canvasapi2.utils

abstract class DomainServicesApiPref(preferenceName: String): PrefManager(preferenceName) {
    abstract var token: String?
}

object JourneyApiPref: DomainServicesApiPref("journey_api_pref") {
    override var token: String? by NStringPref(null, "journey_token")
}