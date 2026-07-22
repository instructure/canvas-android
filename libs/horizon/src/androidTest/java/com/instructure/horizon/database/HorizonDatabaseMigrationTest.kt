/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.horizon.database

import android.content.ContentValues
import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HorizonDatabaseMigrationTest {

    private lateinit var helper: SupportSQLiteOpenHelper
    private lateinit var db: SupportSQLiteDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val callback = object : SupportSQLiteOpenHelper.Callback(1) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `horizon_notification_stream_item` (" +
                        "`id` INTEGER PRIMARY KEY NOT NULL, `title` TEXT NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `horizon_assignment_details` (" +
                        "`assignmentId` INTEGER PRIMARY KEY NOT NULL, `courseId` INTEGER NOT NULL)"
                )
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(null)
            .callback(callback)
            .build()
        helper = FrameworkSQLiteOpenHelperFactory().create(config)
        db = helper.writableDatabase
    }

    @After
    fun tearDown() {
        helper.close()
    }

    @Test
    fun migration1To2AddsPendingActionsAndAllowedExtensionsWithoutDataLoss() {
        db.insert(
            "horizon_notification_stream_item",
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE,
            ContentValues().apply {
                put("id", 42L)
                put("title", "Synced notification")
            },
        )
        db.insert(
            "horizon_assignment_details",
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE,
            ContentValues().apply {
                put("assignmentId", 7L)
                put("courseId", 3L)
            },
        )

        horizonDatabaseMigrations.first { it.startVersion == 1 && it.endVersion == 2 }.migrate(db)

        db.query("SELECT title FROM horizon_notification_stream_item WHERE id = 42").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Synced notification", cursor.getString(0))
        }

        db.insert(
            "pending_actions",
            android.database.sqlite.SQLiteDatabase.CONFLICT_ABORT,
            ContentValues().apply {
                put("actionType", "submit")
                put("payload", "{}")
                put("retryCount", 0)
                put("createdAt", 1L)
            },
        )
        db.query("SELECT COUNT(*) FROM pending_actions").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }

        db.query("SELECT allowedExtensions FROM horizon_assignment_details WHERE assignmentId = 7").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("", cursor.getString(0))
        }
    }
}
