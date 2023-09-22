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
 *
 */

package com.instructure.pandautils.room.offline.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.instructure.pandautils.room.offline.entities.SyncProgressEntity

@Dao
abstract class SyncProgressDao {

    @Insert
    abstract suspend fun insert(entity: SyncProgressEntity)

    @Insert
    abstract suspend fun insertAll(entities: List<SyncProgressEntity>)

    @Delete
    abstract suspend fun delete(entity: SyncProgressEntity)

    @Update
    abstract suspend fun update(entity: SyncProgressEntity)

    @Query("SELECT * FROM SyncProgressEntity")
    abstract suspend fun findCourseProgresses(): List<SyncProgressEntity>

    @Query("DELETE FROM SyncProgressEntity")
    abstract suspend fun deleteAll()

    @Transaction
    open suspend fun clearAndInsert(entities: List<SyncProgressEntity>) {
        deleteAll()
        insertAll(entities)
    }

}