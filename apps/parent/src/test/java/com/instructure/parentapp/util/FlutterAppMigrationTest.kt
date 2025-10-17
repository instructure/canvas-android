/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.parentapp.util

import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
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
import com.instructure.pandautils.room.calendar.entities.CalendarFilterEntity
import com.instructure.parentapp.R
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId


class FlutterAppMigrationTest {

    private val context: Context = mockk(relaxed = true)
    private val parentPrefs: ParentPrefs = mockk(relaxed = true)
    private val loginPrefs: LoginPrefs = mockk(relaxed = true)
    private val previousUsersUtils: PreviousUsersUtils = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val ratingDialogPrefs: RatingDialog.Prefs = mockk(relaxed = true)
    private val reminderRepository: ReminderRepository = mockk(relaxed = true)
    private val calendarFilterDao: CalendarFilterDao = mockk(relaxed = true)
    private val clock = Clock.fixed(Instant.parse("2024-01-05T00:00:00.00Z"), ZoneId.systemDefault())
    private val mockUri: Uri = mockk(relaxed = true)

    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockDatabase: SQLiteDatabase

    private val flutterAppMigration = FlutterAppMigration(
        context,
        parentPrefs,
        loginPrefs,
        previousUsersUtils,
        apiPrefs,
        ratingDialogPrefs,
        reminderRepository,
        calendarFilterDao,
        clock
    )

    @Before
    fun setup() {
        mockSharedPreferences = mockk(relaxed = true)
        mockDatabase = mockk(relaxed = true)
        mockkStatic(Uri::class)
        mockkStatic(MasterKeys::class)
        mockkStatic(EncryptedSharedPreferences::class)
        mockkStatic(SQLiteDatabase::class)

        every { MasterKeys.getOrCreate(any()) } returns "masterKey"
        every {
            EncryptedSharedPreferences.create(
                "FlutterEncryptedSharedPreferences",
                "masterKey",
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } returns mockSharedPreferences
        every {
            SQLiteDatabase.openDatabase(
                any(),
                null,
                SQLiteDatabase.OPEN_READONLY
            )
        } returns mockDatabase
        every { Uri.parse(any()) } returns mockUri
        every { parentPrefs.hasMigrated } returns false
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Migrating if has not migrated yet`() {
        every { parentPrefs.hasMigrated } returns false

        flutterAppMigration.migrateIfNecessary()

        coVerify(exactly = 1) { parentPrefs.hasMigrated = true }
    }

    @Test
    fun `Not migrating if already has migrated`() {
        every { parentPrefs.hasMigrated } returns true

        flutterAppMigration.migrateIfNecessary()

        coVerify(exactly = 1) { parentPrefs.hasMigrated }
        coVerify(exactly = 0) { parentPrefs.hasMigrated = true }
    }

    @Test
    fun `Migrate last user`() {
        every { mockSharedPreferences.all } returns mapOf(
            "flutter.last_account" to """{"domain":"domain.com","name":"name","authentication_provider":null}""".trimIndent(),
            "flutter.last_account_login_flow" to 1L
        )

        flutterAppMigration.migrateIfNecessary()

        coVerify(exactly = 1) {
            loginPrefs.lastSavedLogin = SavedLoginInfo(AccountDomain("domain.com", "name"), 1)
        }
    }

    @Test
    fun `Migrate logins`() {
        val logins = listOf(
            """{"uuid":"uuid1","domain":"https://domain.com","accessToken":"access","refreshToken":"refresh","user":{"id":"1","name":"P","sortable_name":"P"
                |,"short_name":"P","pronouns":"He/Him","avatar_url":"https://avatar.com/1","primary_email":"p@p.com","locale":null,"effective_locale":"en",
                |"permissions":{"become_user":false,"can_update_name":true,"can_update_avatar":true,"limit_parent_app_web_access":false},"login_id":"p"},
                |"clientId":"clientId","clientSecret":"clientSecret","selectedStudentId":null,"canMasquerade":false,"masqueradeUser":null,"masqueradeDomain":null
                |,"isMasqueradingFromQRCode":null}""".trimMargin(),
            """{"uuid":"uuid2","domain":"https://domain2.com","accessToken":"access","refreshToken":"refresh","user":{"id":"2","name":"P2","sortable_name":"P2"
                |,"short_name":"P2","pronouns":"He/Him","avatar_url":"https://avatar.com/2","primary_email":"p2@p.com","locale":null,"effective_locale":"en",
                |"permissions":{"become_user":false,"can_update_name":true,"can_update_avatar":true,"limit_parent_app_web_access":false},"login_id":"p"},
                |"clientId":"clientId2","clientSecret":"clientSecret2","selectedStudentId":null,"canMasquerade":false,"masqueradeUser":null,"masqueradeDomain":null
                |,"isMasqueradingFromQRCode":null}""".trimMargin()
        )
        every { mockSharedPreferences.all } returns mapOf(
            "flutter.logins" to logins,
            "flutter.current_login_uuid" to "uuid2"
        )
        every { mockUri.scheme } returns "https"
        every { mockUri.host } returnsMany listOf("domain.com", "domain2.com")

        val expectedUsers = listOf(
            User(
                id = 1,
                name = "P",
                sortableName = "P",
                shortName = "P",
                pronouns = "He/Him",
                avatarUrl = "https://avatar.com/1"
            ),
            User(
                id = 2,
                name = "P2",
                sortableName = "P2",
                shortName = "P2",
                pronouns = "He/Him",
                avatarUrl = "https://avatar.com/2"
            )
        )

        flutterAppMigration.migrateIfNecessary()

        coVerify(exactly = 1) {
            previousUsersUtils.add(
                context, SignedInUser(
                    expectedUsers[0],
                    "domain.com",
                    "https",
                    "access",
                    "access",
                    "refresh",
                    "clientId",
                    "clientSecret",
                    null
                ),
                "domain.com",
                expectedUsers[0]
            )
        }

        coVerify(exactly = 1) {
            previousUsersUtils.add(
                context, SignedInUser(
                    expectedUsers[1],
                    "domain2.com",
                    "https",
                    "access",
                    "access",
                    "refresh",
                    "clientId2",
                    "clientSecret2",
                    null
                ),
                "domain2.com",
                expectedUsers[1]
            )
        }

        coVerify(exactly = 1) {
            apiPrefs.accessToken = "access"
            apiPrefs.clientId = "clientId2"
            apiPrefs.clientSecret = "clientSecret2"
            apiPrefs.protocol = "https"
            apiPrefs.refreshToken = "refresh"
            apiPrefs.canBecomeUser = false
            apiPrefs.isMasquerading = false
            apiPrefs.isMasqueradingFromQRCode = false
            apiPrefs.masqueradeId = -1
            apiPrefs.domain = "domain2.com"
            apiPrefs.user = expectedUsers[1]
        }
    }

    @Test
    fun `Migrate rating dialog`() {
        every { mockSharedPreferences.all } returns mapOf(
            "flutter.dont_show_again" to true
        )

        flutterAppMigration.migrateIfNecessary()

        coVerify { ratingDialogPrefs.dontShowAgain = true }
    }

    @Test
    fun `Migrate current student`() {
        every { mockSharedPreferences.all } returns mapOf(
            "flutter.current_student" to """{"id":"1","name":"Student","sortable_name":"Sortable, Name","short_name":"S","pronouns":null,
                "avatar_url":"https://avatar.com/1.png","primary_email":null,"locale":null,"effective_locale":null,"permissions":null,"login_id":"s"}""".trimIndent()
        )

        flutterAppMigration.migrateIfNecessary()

        coVerify(exactly = 1) {
            parentPrefs.currentStudent = User(
                id = 1,
                name = "Student",
                sortableName = "Sortable, Name",
                shortName = "S",
                avatarUrl = "https://avatar.com/1.png",
                loginId = "s"
            )
        }
    }

    // TODO: fix
    fun `Migrate calendar filters`() {
        val cursor: Cursor = mockk(relaxed = true)
        every { mockDatabase.rawQuery("SELECT * FROM calendar_filter", null) } returns cursor
        every { cursor.getString(any()) } returnsMany listOf("domain.com", "1", "11", "filter1|filter2")
        every { cursor.moveToNext() } returnsMany listOf(true, false)

        flutterAppMigration.migrateIfNecessary()

        coVerify(exactly = 1) {
            calendarFilterDao.insertOrUpdate(
                CalendarFilterEntity(
                    userDomain = "domain.com",
                    userId = "1",
                    observeeId = 11,
                    filters = setOf("filter1", "filter2")
                )
            )
        }
    }

    // TODO: fix
    fun `Migrate assignment reminder`() {
        val cursor: Cursor = mockk(relaxed = true)
        every { mockDatabase.rawQuery("SELECT * FROM reminders", null) } returns cursor
        every { cursor.getColumnIndexOrThrow("user_domain") } returns 0
        every { cursor.getString(0) } returns "domain.com"
        every { cursor.getColumnIndexOrThrow("user_id") } returns 1
        every { cursor.getString(1) } returns "1"
        every { cursor.getColumnIndexOrThrow("type") } returns 2
        every { cursor.getString(2) } returns "assignment"
        every { cursor.getColumnIndexOrThrow("item_id") } returns 3
        every { cursor.getString(3) } returns "101"
        every { cursor.getColumnIndexOrThrow("course_id") } returns 4
        every { cursor.getString(4) } returns "202"
        every { cursor.getColumnIndexOrThrow("date") } returns 5
        every { cursor.getString(5) } returns "2025-01-01T00:00:00Z"
        every { cursor.moveToNext() } returnsMany listOf(true, false)
        every { context.getString(R.string.assignment) } returns "Assignment"
        every {
            context.getString(eq(R.string.reminderNotificationTitleFor), any())
        } returns "Reminder for Assignment"

        flutterAppMigration.migrateIfNecessary()

        coVerify(exactly = 1) {
            reminderRepository.createReminder(
                userId = 1,
                contentId = 101,
                contentHtmlUrl = "domain.com/courses/202/assignments/101",
                title = "Reminder for Assignment",
                alarmText = "Reminder for Assignment",
                alarmTimeInMillis = Instant.parse("2025-01-01T00:00:00Z").toEpochMilli()
            )
        }
    }

    // TODO: fix
    fun `Migrate event reminder`() {
        val cursor: Cursor = mockk(relaxed = true)
        every { mockDatabase.rawQuery("SELECT * FROM reminders", null) } returns cursor
        every { cursor.getColumnIndexOrThrow("user_domain") } returns 0
        every { cursor.getString(0) } returns "domain2.com"
        every { cursor.getColumnIndexOrThrow("user_id") } returns 1
        every { cursor.getString(1) } returns "1"
        every { cursor.getColumnIndexOrThrow("type") } returns 2
        every { cursor.getString(2) } returns "event"
        every { cursor.getColumnIndexOrThrow("item_id") } returns 3
        every { cursor.getString(3) } returns "102"
        every { cursor.getColumnIndexOrThrow("course_id") } returns 4
        every { cursor.getString(4) } returns "203"
        every { cursor.getColumnIndexOrThrow("date") } returns 5
        every { cursor.getString(5) } returns "2025-01-01T00:00:00Z"
        every { cursor.moveToNext() } returnsMany listOf(true, false)
        every { context.getString(R.string.a11y_calendar_event) } returns "Event"
        every {
            context.getString(eq(R.string.reminderNotificationTitleFor), any())
        } returns "Reminder for Event"

        flutterAppMigration.migrateIfNecessary()

        coVerify(exactly = 1) {
            reminderRepository.createReminder(
                userId = 1,
                contentId = 102,
                contentHtmlUrl = "domain2.com/courses/203/calendar_events/102",
                title = "Reminder for Event",
                alarmText = "Reminder for Event",
                alarmTimeInMillis = Instant.parse("2025-01-01T00:00:00Z").toEpochMilli()
            )
        }
    }

    // TODO: fix
    fun `Ignore past date reminder`() {
        val cursor: Cursor = mockk(relaxed = true)
        every { mockDatabase.rawQuery("SELECT * FROM reminders", null) } returns cursor
        every { cursor.getColumnIndexOrThrow("user_domain") } returns 0
        every { cursor.getString(0) } returns "domain2.com"
        every { cursor.getColumnIndexOrThrow("user_id") } returns 1
        every { cursor.getString(1) } returns "1"
        every { cursor.getColumnIndexOrThrow("type") } returns 2
        every { cursor.getString(2) } returns "event"
        every { cursor.getColumnIndexOrThrow("item_id") } returns 3
        every { cursor.getString(3) } returns "102"
        every { cursor.getColumnIndexOrThrow("course_id") } returns 4
        every { cursor.getString(4) } returns "203"
        every { cursor.getColumnIndexOrThrow("date") } returns 5
        every { cursor.getString(5) } returns "2024-01-01T00:00:00Z"
        every { cursor.moveToNext() } returnsMany listOf(true, false)

        flutterAppMigration.migrateIfNecessary()

        coVerify(exactly = 0) {
            reminderRepository.createReminder(any(), any(), any(), any(), any(), any())
        }
    }
}
