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

package com.instructure.parentapp.plugins

import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.json.JSONObject
import java.util.*

object OldAppMigrations {
    private const val CHANNEL = "com.instructure.parentapp/oldAppMigrations"
    private const val METHOD_GET_LOGINS = "getLogins"
    private const val METHOD_HAS_OLD_REMINDERS = "hasOldReminders"
    private const val PREF_NAME_CANVAS = "canvas-kit-sp"
    private const val PREF_NAME_LOGIN_LIST = "signedInUsersList"

    private lateinit var context: Context

    fun init(flutterEngine: FlutterEngine, context: Context) {
        this.context = context
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler(::handleCall)
    }

    private fun handleCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            METHOD_GET_LOGINS -> getLogins(result)
            METHOD_HAS_OLD_REMINDERS -> result.success(OldReminderUtil.hasPendingReminders(context))
            else -> result.notImplemented()
        }
    }

    private fun getLogins(result: MethodChannel.Result) {
        val prefs = context.getSharedPreferences(PREF_NAME_CANVAS, Context.MODE_PRIVATE)
        val refreshToken = prefs.getString("refreshToken", "INVALID")

        // Get list of login data json strings
        val logins = context.getSharedPreferences(PREF_NAME_LOGIN_LIST, Context.MODE_PRIVATE).all.values
            .map { JSONObject(it as String) }
            .onEach {
                // Convert user ID to a string
                it.optJSONObject("user")?.apply { put("id", optLong("id").toString()) }

                // We use full domain on the flutter side, so we'll combine it here
                it.put("domain", "${it.optString("protocol") ?: "https"}://${it.optString("domain")}")
                it.remove("protocol")

                // Add client id/secret if this is the current user
                if (refreshToken == it.optString("refreshToken")) {
                    it.put("clientId", prefs.getString("client_id", null))
                    it.put("clientSecret", prefs.getString("client_secret", null))
                }
            }.sortedBy {
                // Sort the current login to the first position
                if (it.optString("refreshToken") == refreshToken) -1 else 0
            }.map { it.toString() }

        result.success(logins.toList())
    }
}

object OldReminderUtil {
    private const val DATABASE_NAME = "alarmDatabase"
    private const val TABLE = "alarms"
    private const val KEY_YEAR = "_year"
    private const val KEY_MONTH = "_month"
    private const val KEY_DAY = "_day"

    private class DbHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
        override fun onCreate(db: SQLiteDatabase?) = Unit
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    }

    /** Returns true if the old database contains reminders with a future date */
    fun hasPendingReminders(context: Context): Boolean {
        val dbHelper = DbHelper(context)
        val db = dbHelper.writableDatabase

        val now = Calendar.getInstance()
        val today = 10_000 * now.get(Calendar.YEAR) + 100 * now.get(Calendar.MONTH) + now.get(Calendar.DAY_OF_MONTH)
        val query = "select count(*) from $TABLE where (10000 * $KEY_YEAR + 100 * $KEY_MONTH + $KEY_DAY) >= $today"

        return try {
            DatabaseUtils.longForQuery(db, query, null) > 0L
        } catch (e: SQLiteException) {
            // Exception expected if the table doesn't exist
            false
        }
    }
}
