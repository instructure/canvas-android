package com.instructure.canvasapi2.utils

abstract class DomainServicesApiPref(preferenceName: String): PrefManager(preferenceName) {
    abstract var token: String?
}

class PineApiPref: DomainServicesApiPref("pine_api_prefs") {
    override var token: String? by NStringPref(null, "pine_token")
}

class CedarApiPref: DomainServicesApiPref("cedar_api_prefs") {
    override var token: String? by NStringPref(null, "cedar_token")
}

class RedwoodApiPref: DomainServicesApiPref("redwood_api_prefs") {
    override var token: String? by NStringPref(null, "redwood_token")
}