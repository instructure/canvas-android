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
 */
package com.instructure.pandautils.room.offline.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.instructure.pandautils.room.offline.entities.StudioMediaProgressEntity

@Dao
interface StudioMediaProgressDao {

    @Insert
    suspend fun insert(fileSyncProgressEntity: StudioMediaProgressEntity): Long

    @Insert
    suspend fun insertAll(fileSyncProgressEntities: List<StudioMediaProgressEntity>)

    @Update
    suspend fun update(fileSyncProgressEntity: StudioMediaProgressEntity)

    @Query("SELECT * FROM StudioMediaProgressEntity WHERE id = :id")
    suspend fun findById(id: Long): StudioMediaProgressEntity?

    @Query("SELECT * FROM StudioMediaProgressEntity WHERE ROWID = :rowId")
    suspend fun findByRowId(rowId: Long): StudioMediaProgressEntity?

    @Query("SELECT * FROM StudioMediaProgressEntity")
    fun findAllLiveData(): LiveData<List<StudioMediaProgressEntity>>

    @Query("DELETE FROM StudioMediaProgressEntity")
    suspend fun deleteAll()
}