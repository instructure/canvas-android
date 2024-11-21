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
package com.instructure.student.activity

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.instructure.pandautils.base.BaseCanvasActivity
import com.instructure.pandautils.binding.viewBinding
import com.instructure.student.R
import com.instructure.student.databinding.ActivityWidgetSetupBinding
import com.instructure.student.util.StudentPrefs
import com.instructure.student.widget.WidgetUpdater.updateWidgets

class WidgetSetupActivity : BaseCanvasActivity() {

    private val binding by viewBinding(ActivityWidgetSetupBinding::inflate)

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Sets the result canceled so if the user decides not to setup the widget it does not get added
        setResult(Activity.RESULT_CANCELED)

        setContentView(binding.root)

        binding.cardDark.setOnClickListener(cardClickListener)
        binding.cardLight.setOnClickListener(cardClickListener)
        binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
            StudentPrefs.putBoolean(WIDGET_DETAILS_PREFIX + appWidgetId, isChecked)
        }

        intent.extras?.let { extras ->
            // Get the widget id we are adding/updating
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        // If our appWidgetId is bad or does not exist we exit the activity
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) finish()
    }

    private val cardClickListener = View.OnClickListener { v ->
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        when (v.id) {
            R.id.cardDark -> {
                resultValue.putExtra(WIDGET_BACKGROUND_COLOR_KEY, WIDGET_BACKGROUND_COLOR_DARK)
                StudentPrefs.putString(WIDGET_BACKGROUND_PREFIX + appWidgetId, WIDGET_BACKGROUND_COLOR_DARK)
            }
            R.id.cardLight -> {
                resultValue.putExtra(WIDGET_BACKGROUND_COLOR_KEY, WIDGET_BACKGROUND_COLOR_LIGHT)
                StudentPrefs.putString(WIDGET_BACKGROUND_PREFIX + appWidgetId, WIDGET_BACKGROUND_COLOR_LIGHT)
            }
        }
        setResult(Activity.RESULT_OK, resultValue)
        finish()
        updateWidgets()
    }

    companion object {
        const val WIDGET_BACKGROUND_COLOR_KEY = "widgetBackgroundColorKey"
        const val WIDGET_BACKGROUND_COLOR_LIGHT = "widgetBackgroundColorLight"
        const val WIDGET_BACKGROUND_COLOR_DARK = "widgetBackgroundColorDark"
        const val WIDGET_BACKGROUND_PREFIX = "widgetBackground__"
        const val WIDGET_DETAILS_PREFIX = "widgetDetails__"
    }
}
