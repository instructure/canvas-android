/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
package com.emeritus.student.util

import android.app.Activity

object Analytics {
    fun trackBookmarkCreated(context: Activity?) {
        context ?: return
        (context.application as? AnalyticsEventHandling)?.trackUIEvent("Bookmarker Created", "Bookmarker", 512512L)
    }

    fun trackButtonPressed(context: Activity?, buttonName: String?, buttonValue: Long?) {
        if (context == null || buttonName == null) return
        (context.application as? AnalyticsEventHandling)?.trackButtonPressed(buttonName, buttonValue)
    }

    private fun trackAppFlow(context: Activity?, pageName: String?) {
        if (context == null || pageName == null) return
        (context.application as? AnalyticsEventHandling)?.trackScreen(pageName)
    }

    fun trackWidgetFlow(context: Activity?, widgetName: String?) {
        trackAppFlow(context, widgetName)
    }

    fun trackAppFlow(currentActivity: Activity) {
        trackAppFlow(currentActivity, currentActivity.javaClass.simpleName)
    }

    fun trackAppFlow(context: Activity?, cls: Class<*>?) {
        if (cls == null || context == null) return
        trackAppFlow(context, cls.simpleName)
    }

    fun trackBookmarkSelected(context: Activity?, className: String?) {
        if (context == null || className == null) return
        (context.application as? AnalyticsEventHandling)?.trackUIEvent("Bookmarker selected", className, 0)
    }

    fun trackUnsupportedFeature(context: Activity?, featureName: String?) {
        if (context == null || featureName == null) return
        (context.application as? AnalyticsEventHandling)?.trackUIEvent("Unsupported feature link selected", featureName, 0)
    }
}
