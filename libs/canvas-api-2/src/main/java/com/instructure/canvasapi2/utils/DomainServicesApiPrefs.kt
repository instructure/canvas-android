package com.instructure.canvasapi2.utils

private const val DOMAIN_SERVICES_PREFERENCE_FILE_NAME = "canvas-domain-services"

object DomainServicesApiPrefs: PrefManager(DOMAIN_SERVICES_PREFERENCE_FILE_NAME) {
    var pineToken: String by StringPref()
    var cedarToken: String by StringPref()
    var redwoodToken: String by StringPref()
}