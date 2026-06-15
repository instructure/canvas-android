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

import android.content.Context
import android.util.Log
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val HORIZON_DB_PREFIX = "horizon-db-"
private const val TAG = "HorizonDatabaseProvider"

@Singleton
class HorizonDatabaseProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dbMap = mutableMapOf<Long, HorizonDatabase>()
    private val listeners = mutableListOf<() -> Unit>()

    @Synchronized
    fun getDatabase(userId: Long?): HorizonDatabase {
        if (userId == null) {
            return Room.inMemoryDatabaseBuilder(context, HorizonDatabase::class.java).build()
        }
        return dbMap.getOrPut(userId) {
            Room.databaseBuilder(context, HorizonDatabase::class.java, "$HORIZON_DB_PREFIX$userId")
                .build()
        }
    }

    @Synchronized
    fun addOnDatabaseChangedListener(listener: () -> Unit) {
        listeners += listener
    }

    @Synchronized
    fun removeOnDatabaseChangedListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    @Synchronized
    fun clearDatabase(userId: Long) {
        val db = dbMap.remove(userId)
        db?.close()
        context.deleteDatabase("$HORIZON_DB_PREFIX$userId")
        notifyListeners()
    }

    @Synchronized
    fun closeDatabase(userId: Long) {
        val db = dbMap.remove(userId)
        db?.close()
        notifyListeners()
    }

    private fun notifyListeners() {
        listeners.toList().forEach { runCatching { it() } }
    }
}
