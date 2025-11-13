/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.student.features.dashboard.widget.db

import android.content.Context
import androidx.room.Room
import com.google.firebase.crashlytics.FirebaseCrashlytics

private const val WIDGET_DB_PREFIX = "widget-db-"

class WidgetDatabaseProvider(
    private val context: Context,
    private val firebaseCrashlytics: FirebaseCrashlytics
) {

    private val dbMap = mutableMapOf<Long, WidgetDatabase>()

    fun getDatabase(userId: Long?): WidgetDatabase {
        if (userId == null) {
            firebaseCrashlytics.recordException(IllegalStateException("You can't access the widget database while logged out"))
            return Room.databaseBuilder(context, WidgetDatabase::class.java, "dummy-widget-db")
                .fallbackToDestructiveMigration()
                .build()
        }

        return dbMap.getOrPut(userId) {
            Room.databaseBuilder(context, WidgetDatabase::class.java, "$WIDGET_DB_PREFIX$userId")
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    fun clearDatabase(userId: Long) {
        getDatabase(userId).clearAllTables()
        dbMap.remove(userId)
        context.deleteDatabase("$WIDGET_DB_PREFIX$userId")
    }
}