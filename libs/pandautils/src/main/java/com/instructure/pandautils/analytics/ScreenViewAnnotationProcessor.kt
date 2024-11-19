package com.instructure.pandautils.analytics

import com.instructure.canvasapi2.utils.Analytics
import com.instructure.pandautils.base.AppConfigProvider

object ScreenViewAnnotationProcessor {

    fun processScreenView(javaClass: Class<*>) {
        javaClass.getAnnotation(ScreenView::class.java)?.let { annotation ->
            val fullEvent = "${AppConfigProvider.appConfig?.appName}_screen_view_${annotation.screenName}"
            Analytics.logEvent(fullEvent)
        }
    }
}