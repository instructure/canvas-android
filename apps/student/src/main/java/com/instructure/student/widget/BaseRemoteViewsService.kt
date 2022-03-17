/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.student.widget

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import android.widget.RemoteViewsService
import com.instructure.student.R
import com.instructure.student.activity.WidgetSetupActivity
import com.instructure.student.util.StudentPrefs

abstract class BaseRemoteViewsService : RemoteViewsService() {

    companion object {

        fun getWidgetTextColor(widgetId: Int, context: Context): Int {
            val widgetBackgroundPref = getWidgetBackgroundPref(widgetId)
            return if (widgetBackgroundPref.equals(WidgetSetupActivity.WIDGET_BACKGROUND_COLOR_LIGHT, ignoreCase = true))
                ContextCompat.getColor(context, R.color.canvasTextDark) else ContextCompat.getColor(context, R.color.white)
        }

        fun getWidgetBackgroundResourceId(widgetId: Int): Int {
            val widgetBackgroundPref = getWidgetBackgroundPref(widgetId)
            return if (widgetBackgroundPref.equals(WidgetSetupActivity.WIDGET_BACKGROUND_COLOR_LIGHT, ignoreCase = true)) R.drawable.widget_light_bg else R.drawable.widget_dark_bg
        }

        fun shouldHideDetails(appWidgetId: Int): Boolean {
            return StudentPrefs.getBoolean(WidgetSetupActivity.WIDGET_DETAILS_PREFIX + appWidgetId)
        }

        //Data passed via the intent will get reused resulting in all widgets of this type having the same text color
        //unless the data is passed as part of a filter. When a filter is applied the intent does not get reused so data can be passed, like the widget id.
        // http://stackoverflow.com/questions/11350287/ongetviewfactory-only-called-once-for-multiple-widgets
        fun getAppWidgetId(intent: Intent): Int {
            return Integer.valueOf(intent.data!!.schemeSpecificPart)
        }

        private fun getWidgetBackgroundPref(widgetId: Int): String {
            return StudentPrefs.getString(WidgetSetupActivity.WIDGET_BACKGROUND_PREFIX + widgetId) ?: WidgetSetupActivity.WIDGET_BACKGROUND_COLOR_LIGHT
        }
    }
}
