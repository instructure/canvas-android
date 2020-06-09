/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
 */

package com.instructure.student.flutterChannels

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Window
import com.google.gson.Gson
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.student.events.StatusBarColorChangeEvent
import com.instructure.student.events.post
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.json.JSONObject
import java.util.*

object FlutterComm {
    private const val CHANNEL = "com.instructure.student/flutterComm"

    private const val METHOD_RESET = "reset"
    private const val METHOD_ROUTE_TO_CALENDAR = "routeToCalendar"
    private const val METHOD_SET_STATUS_BAR_COLOR = "setStatusBarColor"
    private const val METHOD_UPDATE_CALENDAR_DATES = "updateCalendarDates"
    private const val METHOD_UPDATE_LOGIN_DATA = "updateLoginData"
    private const val METHOD_UPDATE_SHOULD_POP = "updateShouldPop"
    private const val METHOD_UPDATE_THEME_DATA = "updateThemeData"

    private lateinit var context: Context
    private lateinit var channel: MethodChannel

    var shouldPop: Boolean = true
        private set

    var statusBarColor: Int = 0
        private set(value) {
            field = value
            // We can't directly update the status bar color here because need an Activity reference. Instead, we'll
            // post a bus event with the color information and subscribe to it in NavigationActivity.
            StatusBarColorChangeEvent(value).post();
        }

    fun init(flutterEngine: FlutterEngine, context: Context) {
        this.context = context
        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
        channel.setMethodCallHandler(::handleCall)
    }

    private fun handleCall(call: MethodCall, result: MethodChannel.Result) {
        when(call.method) {
            METHOD_UPDATE_SHOULD_POP -> {
                shouldPop = call.arguments as? Boolean ?: true
                result.success(null)
            }
            METHOD_SET_STATUS_BAR_COLOR -> {
                val colorString = '#' + (call.arguments as String)
                statusBarColor = Color.parseColor(colorString)
            }
        }
    }

    fun sendUpdatedLogin() {
        // Send null if not logged in
        if (!ApiPrefs.getValidToken().isValid()) {
            channel.invokeMethod(METHOD_UPDATE_LOGIN_DATA, null)
            return
        }

        val userJson = JSONObject(Gson().toJson(ApiPrefs.user)).apply {
            // Convert ID from Long to String
            put("id", getLong("id").toString())
        }
        val loginJson = JSONObject().apply {
            put("uuid", "")
            put("domain", ApiPrefs.fullDomain)
            put("accessToken", ApiPrefs.getValidToken())
            put("user", userJson)
            if (ApiPrefs.isMasquerading) put("masqueradeId", ApiPrefs.masqueradeId.toString())
        }
        channel.invokeMethod(METHOD_UPDATE_LOGIN_DATA, loginJson.toString())
    }

    fun sendUpdatedTheme() {
        val data = mutableMapOf<String, Any?>()
        data["primaryColor"] = Integer.toHexString(ThemePrefs.primaryColor)
        data["accentColor"] = Integer.toHexString(ThemePrefs.brandColor)
        data["buttonColor"] = Integer.toHexString(ThemePrefs.buttonColor)
        data["primaryTextColor"] = Integer.toHexString(ThemePrefs.primaryTextColor)
        data["contextColors"] = ColorKeeper.cachedColors.map {
            it.key.toLowerCase(Locale.US) to Integer.toHexString(it.value)
        }.toMap()
        channel.invokeMethod(METHOD_UPDATE_THEME_DATA, data)
    }

    fun routeToCalendar(channelId: String) = channel.invokeMethod(METHOD_ROUTE_TO_CALENDAR, channelId)

    fun reset() = channel.invokeMethod(METHOD_RESET, null)

    fun updateCalendarDates(dates: List<Date?>) {
        val affectedDates = dates.filterNotNull().distinct() // Sanitize
        val isoDates = affectedDates.map { it.toApiString() }
        channel.invokeMethod(METHOD_UPDATE_CALENDAR_DATES, isoDates)
    }
}
