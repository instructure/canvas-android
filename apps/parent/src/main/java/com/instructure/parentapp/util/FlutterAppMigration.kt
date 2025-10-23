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
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.loginapi.login.model.SignedInUser
import com.instructure.loginapi.login.util.LoginPrefs
import com.instructure.loginapi.login.util.PreviousUsersUtils
import com.instructure.loginapi.login.util.SavedLoginInfo
import com.instructure.pandautils.R
import com.instructure.pandautils.dialogs.RatingDialog
import com.instructure.pandautils.features.reminder.ReminderRepository
import com.instructure.pandautils.room.calendar.daos.CalendarFilterDao
import com.instructure.pandautils.room.calendar.entities.CalendarFilterEntity
import com.instructure.pandautils.utils.fromJson
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.math.BigInteger
import java.time.Clock
import java.time.Instant

private const val LIST_IDENTIFIER = "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBhIGxpc3Qu"
private const val BIG_INTEGER_PREFIX = "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBCaWdJbnRlZ2Vy"
private const val DOUBLE_PREFIX = "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBEb3VibGUu"

private const val KEY_DARK_MODE = "flutter.dark_mode"
private const val KEY_LOGINS = "flutter.logins"
private const val KEY_CURRENT_LOGIN_UUID = "flutter.current_login_uuid"
private const val KEY_CURRENT_STUDENT = "flutter.current_student"
private const val KEY_LAST_ACCOUNT = "flutter.last_account"
private const val KEY_LAST_ACCOUNT_LOGIN_FLOW = "flutter.last_account_login_flow"
private const val KEY_RATING_DONT_SHOW_AGAIN = "flutter.dont_show_again"

data class FlutterSignedInUser(
    val uuid: String?,
    val domain: String?,
    val accessToken: String?,
    val refreshToken: String?,
    val user: User?,
    val clientId: String?,
    val clientSecret: String?,
    val selectedStudentId: Long?,
    val canMasquerade: Boolean?,
    val masqueradeUser: User?,
    val masqueradeDomain: String?,
    val isMasqueradingFromQRCode: Boolean?
) {
    fun toSignedInUser(): SignedInUser {
        val domainUri = Uri.parse(domain)
        return SignedInUser(
            user = user ?: User(),
            domain = domainUri.host.orEmpty(),
            protocol = domainUri.scheme.orEmpty(),
            token = accessToken.orEmpty(),
            accessToken = accessToken.orEmpty(),
            refreshToken = refreshToken.orEmpty(),
            clientId = clientId.orEmpty(),
            clientSecret = clientSecret.orEmpty(),
            calendarFilterPrefs = null,
            selectedStudentId = selectedStudentId
        )
    }
}

class FlutterAppMigration(
    @ApplicationContext private val context: Context,
    private val parentPrefs: ParentPrefs,
    private val loginPrefs: LoginPrefs,
    private val previousUsersUtils: PreviousUsersUtils,
    private val apiPrefs: ApiPrefs,
    private val ratingDialogPrefs: RatingDialog.Prefs,
    private val reminderRepository: ReminderRepository,
    private val calendarFilterDao: CalendarFilterDao,
    private val clock: Clock
) {
    fun migrateIfNecessary() {
        if (!parentPrefs.hasMigrated) {
            parentPrefs.hasMigrated = true
            migrateEncryptedSharedPrefs()
            migrateDatabase()
        }
    }

    private fun migrateEncryptedSharedPrefs() = try {
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
            val typeToken = object : TypeToken<List<FlutterSignedInUser>>() {}
            val logins: List<FlutterSignedInUser> = Gson().fromJson(loginListJsonString.toString(), typeToken.type)
            logins.map { it.toSignedInUser() }.forEach {
                previousUsersUtils.add(context, it, it.domain, it.user)
            }

            prefs[KEY_CURRENT_LOGIN_UUID]?.let { currentLoginUuid ->
                logins.find { it.uuid == currentLoginUuid }?.let {
                    setCurrentLoginInfo(it)
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
    } catch (e: Exception) {
        e.printStackTrace()
    }

    private fun getAllPrefs(allPrefs: Map<String, *>) = allPrefs
        .filterKeys { it.startsWith("flutter.") }
        .mapValues { (_, value) ->
            if (value is String) {
                decodeValue(value)
            } else {
                value
            }
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

    private fun setCurrentLoginInfo(flutterSignedInUser: FlutterSignedInUser) = with(apiPrefs) {
        val signedInUser = flutterSignedInUser.toSignedInUser()
        accessToken = signedInUser.accessToken.orEmpty()
        clientId = signedInUser.clientId.orEmpty()
        clientSecret = signedInUser.clientSecret.orEmpty()
        protocol = signedInUser.protocol
        refreshToken = signedInUser.refreshToken
        canBecomeUser = flutterSignedInUser.canMasquerade
        isMasquerading = flutterSignedInUser.masqueradeUser != null
        isMasqueradingFromQRCode = flutterSignedInUser.isMasqueradingFromQRCode.orDefault()
        masqueradeId = flutterSignedInUser.masqueradeUser?.id ?: -1
        domain = if (isMasquerading) Uri.parse(flutterSignedInUser.masqueradeDomain).host.orEmpty() else signedInUser.domain
        user = if (isMasquerading) flutterSignedInUser.masqueradeUser else signedInUser.user
    }

    private fun migrateDatabase() = try {
        val database = SQLiteDatabase.openDatabase(
            context.getDatabasePath("canvas_parent.db").path,
            null,
            SQLiteDatabase.OPEN_READONLY
        )

        CoroutineScope(Dispatchers.IO).launch {
            database.use {
                migrateCalendarFilters(it)
                migrateReminders(it)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    private suspend fun migrateCalendarFilters(database: SQLiteDatabase) {
        val cursor = database.rawQuery("SELECT * FROM calendar_filter", null)

        cursor.use {
            while (cursor.moveToNext()) {
                val userDomain = cursor.getString(cursor.getColumnIndexOrThrow("user_domain"))
                val userIdString = cursor.getString(cursor.getColumnIndexOrThrow("user_id"))
                val observeeIdString = cursor.getString(cursor.getColumnIndexOrThrow("observee_id"))
                val filtersString = cursor.getString(cursor.getColumnIndexOrThrow("filters"))

                val observeeId = observeeIdString.toLongOrNull() ?: continue
                val filters = filtersString.split("|").toSet()

                calendarFilterDao.insertOrUpdate(
                    CalendarFilterEntity(
                        userDomain = userDomain,
                        userId = userIdString,
                        observeeId = observeeId,
                        filters = filters
                    )
                )
            }
        }
    }

    private suspend fun migrateReminders(database: SQLiteDatabase) {
        val cursor = database.rawQuery("SELECT * FROM reminders", null)

        cursor.use {
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
                val contentHtmlUrl = if (type == "assignment") {
                    "$userDomain/courses/$courseId/assignments/$itemId"
                } else {
                    "$userDomain/courses/$courseId/calendar_events/$itemId"
                }
                val messageParam = context.getString(if (type == "assignment") R.string.assignment else R.string.a11y_calendar_event)
                val message = context.getString(R.string.reminderNotificationTitleFor, messageParam)

                if (date.isAfter(Instant.now(clock))) {
                    reminderRepository.createReminder(
                        userId = userId,
                        contentId = itemId,
                        contentHtmlUrl = contentHtmlUrl,
                        title = message,
                        alarmText = message,
                        alarmTimeInMillis = date.toEpochMilli()
                    )
                }
            }
        }
    }
}
