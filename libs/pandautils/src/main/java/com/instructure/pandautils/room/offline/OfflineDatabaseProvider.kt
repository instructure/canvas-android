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

class OfflineDatabaseProvider(private val context: Context) {

    private val dbMap = mutableMapOf<Long, OfflineDatabase>()

    fun getDatabase(userId: Long?): OfflineDatabase {
        if (userId == null) throw IllegalStateException("You can't access the database while logged out")

        return dbMap.getOrPut(userId) {
            Room.databaseBuilder(context, OfflineDatabase::class.java, "offline-db-$userId")
                .addMigrations()
                .build()
        }
    }
}