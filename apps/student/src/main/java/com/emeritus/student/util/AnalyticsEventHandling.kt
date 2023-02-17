package com.emeritus.student.util

interface AnalyticsEventHandling {
    fun trackButtonPressed(buttonName: String?, buttonValue: Long?)
    fun trackScreen(screenName: String?)
    fun trackEnrollment(enrollmentType: String?)
    fun trackDomain(domain: String?)
    fun trackEvent(category: String?, action: String?, label: String?, value: Long)
    fun trackUIEvent(action: String?, label: String?, value: Long)
    fun trackTiming(category: String?, name: String?, label: String?, duration: Long)
}
