/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.pandautils.room.offline

import android.content.Context
import androidx.room.Room
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.pandautils.utils.LogoutHelper

private const val OFFLINE_DB_PREFIX = "offline-db-"

class OfflineDatabaseProvider(
    private val context: Context,
    private val logoutHelper: LogoutHelper,
    private val firebaseCrashlytics: FirebaseCrashlytics,
    private val alarmScheduler: AlarmScheduler
) : DatabaseProvider {

    private val dbMap = mutableMapOf<Long, OfflineDatabase>()

    override fun getDatabase(userId: Long?): OfflineDatabase {
        if (userId == null) {
            logoutHelper.logout(this, alarmScheduler)
            firebaseCrashlytics.recordException(IllegalStateException("You can't access the database while logged out"))
            return Room.databaseBuilder(context, OfflineDatabase::class.java, "dummy-db")
                .addMigrations(*offlineDatabaseMigrations)
                .build()
        }

        return dbMap.getOrPut(userId) {
            Room.databaseBuilder(context, OfflineDatabase::class.java, "$OFFLINE_DB_PREFIX$userId")
                .addMigrations(*offlineDatabaseMigrations)
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    override fun clearDatabase(userId: Long) {
        getDatabase(userId).clearAllTables()
        dbMap.remove(userId)
        context.deleteDatabase("$OFFLINE_DB_PREFIX$userId")
    }
}