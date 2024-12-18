/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.util

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.loginapi.login.model.SignedInUser
import com.instructure.loginapi.login.util.LoginPrefs
import com.instructure.loginapi.login.util.PreviousUsersUtils
import com.instructure.loginapi.login.util.SavedLoginInfo
import com.instructure.pandautils.dialogs.RatingDialog
import com.instructure.pandautils.features.reminder.ReminderRepository
import com.instructure.pandautils.room.calendar.daos.CalendarFilterDao
import com.instructure.pandautils.utils.fromJson
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.math.BigInteger
import java.time.Instant

private const val LIST_IDENTIFIER = "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBhIGxpc3Qu"
private const val BIG_INTEGER_PREFIX = "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBCaWdJbnRlZ2Vy"
private const val DOUBLE_PREFIX = "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBEb3VibGUu"

private const val KEY_LOGINS = "flutter.logins"
private const val KEY_CURRENT_LOGIN_UUID = "flutter.current_login_uuid"
private const val KEY_CURRENT_STUDENT = "flutter.current_student"
private const val KEY_LAST_ACCOUNT = "flutter.last_account"
private const val KEY_LAST_ACCOUNT_LOGIN_FLOW = "flutter.last_account_login_flow"
private const val KEY_RATING_DONT_SHOW_AGAIN = "flutter.dont_show_again"

class FlutterAppMigration(
    @ApplicationContext private val context: Context,
    private val parentPrefs: ParentPrefs,
    private val loginPrefs: LoginPrefs,
    private val previousUsersUtils: PreviousUsersUtils,
    private val apiPrefs: ApiPrefs,
    private val ratingDialogPrefs: RatingDialog.Prefs,
    private val reminderRepository: ReminderRepository,
    private val calendarFilterDao: CalendarFilterDao
) {
    fun migratePreferencesIfNecessary() {
        try {
            //    if (!parentPrefs.hasMigratedPrefs) {
            parentPrefs.hasMigratedPrefs = true
            migratePrefs()
            migrateReminders()
            migrateCalendarFilters()
            //      }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Suppress("DEPRECATION")
    private fun migratePrefs() {
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val encryptedPrefs = EncryptedSharedPreferences.create(
            "FlutterEncryptedSharedPreferences",
            masterKey,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val prefs = getAllPrefs(encryptedPrefs.all)

        prefs[KEY_LAST_ACCOUNT]?.let {
            val accountDomain: AccountDomain = it.toString().fromJson()
            val loginFlow = (prefs[KEY_LAST_ACCOUNT_LOGIN_FLOW] as? Long).orDefault().toInt()
            loginPrefs.lastSavedLogin = SavedLoginInfo(accountDomain, loginFlow)
        }

        prefs[KEY_LOGINS]?.let { loginListJsonString ->
            val logins = parseSignedInUsersWithUuids(loginListJsonString.toString())
            logins.map { it.second }.forEach {
                previousUsersUtils.add(context, it, it.domain, it.user)
            }

            prefs[KEY_CURRENT_LOGIN_UUID]?.let { currentLoginUuid ->
                logins.find { it.first == currentLoginUuid }?.let {
                    setCurrentLoginInfo(it.second)
                }
            }
        }

        (prefs[KEY_RATING_DONT_SHOW_AGAIN] as? Boolean)?.let {
            ratingDialogPrefs.dontShowAgain = it
        }

        prefs[KEY_CURRENT_STUDENT]?.let {
            val currentStudent: User = it.toString().fromJson()
            parentPrefs.currentStudent = currentStudent
        }
    }

    private fun getAllPrefs(allPrefs: Map<String, *>): Map<String, Any?> {
        val decodedPrefs = mutableMapOf<String, Any?>()

        allPrefs.keys
            .filter { it.startsWith("flutter.") }
            .forEach { key ->
                var value = allPrefs[key]
                if (value is String) {
                    value = decodeValue(value)
                }
                decodedPrefs[key] = value
            }

        return decodedPrefs
    }

    private fun decodeValue(value: String): Any {
        return when {
            value.startsWith(LIST_IDENTIFIER) -> decodeList(value.substring(LIST_IDENTIFIER.length))
            value.startsWith(BIG_INTEGER_PREFIX) -> BigInteger(value.substring(BIG_INTEGER_PREFIX.length), Character.MAX_RADIX)
            value.startsWith(DOUBLE_PREFIX) -> value.substring(DOUBLE_PREFIX.length).toDouble()
            else -> value
        }
    }

    private fun decodeList(encodedList: String): List<String> {
        ObjectInputStream(ByteArrayInputStream(Base64.decode(encodedList, Base64.DEFAULT))).use { stream ->
            return (stream.readObject() as List<*>).filterIsInstance<String>()
        }
    }

    private fun parseSignedInUsersWithUuids(jsonString: String): List<Pair<String, SignedInUser>> {
        val parsedList: List<Map<String, Any>> = jsonString.fromJson()

        return parsedList.map { map ->
            val uuid = map["uuid"] as String
            val userJson = Gson().toJson(map.filterKeys { it != "uuid" })
            val signedInUser: SignedInUser = userJson.fromJson()
            val domainUri = Uri.parse(signedInUser.domain)
            signedInUser.domain = domainUri.host.orEmpty()
            signedInUser.protocol = domainUri.scheme.orEmpty()
            uuid to signedInUser
        }
    }

    private fun setCurrentLoginInfo(signedInUser: SignedInUser) = with(apiPrefs) {
        accessToken = signedInUser.accessToken.orEmpty()
        clientId = signedInUser.clientId.orEmpty()
        clientSecret = signedInUser.clientSecret.orEmpty()
        domain = signedInUser.domain
        protocol = signedInUser.protocol
        refreshToken = signedInUser.refreshToken
        user = signedInUser.user
    }

    private fun migrateReminders() {
        val database = SQLiteDatabase.openDatabase(
            context.getDatabasePath("canvas_parent.db").path,
            null,
            SQLiteDatabase.OPEN_READONLY
        )

        val cursor = database.rawQuery("SELECT * FROM reminders", null)

        while (cursor.moveToNext()) {
            val userDomain = cursor.getString(cursor.getColumnIndexOrThrow("user_domain"))
            val userIdString = cursor.getString(cursor.getColumnIndexOrThrow("user_id"))
            val type = cursor.getString(cursor.getColumnIndexOrThrow("type"))
            val itemIdString = cursor.getString(cursor.getColumnIndexOrThrow("item_id"))
            val courseIdString = cursor.getString(cursor.getColumnIndexOrThrow("course_id"))
            val dateString = cursor.getString(cursor.getColumnIndexOrThrow("date"))

            val userId = userIdString.toLongOrNull() ?: continue
            val itemId = itemIdString.toLongOrNull() ?: continue
            val courseId = courseIdString.toLongOrNull() ?: continue
            val date = Instant.parse(dateString) ?: continue


            val contentHtmlUrl = "$userDomain/courses/$courseId/assignments/$itemId"
        }

        cursor.close()
        database.close()
    }

    private fun migrateCalendarFilters() {

    }

    private fun createReminder(
        userId: Long,
        itemId: Long,
        contentHtmlUrl: String,
        title: String,
        alarmText: String,
        dateMillis: Long
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            reminderRepository.createReminder(
                userId = userId,
                contentId = itemId,
                contentHtmlUrl = contentHtmlUrl,
                title = title,
                alarmText = alarmText,
                alarmTimeInMillis = dateMillis
            )
        }
    }
}
