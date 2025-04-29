/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.room.appdatabase.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDownloadProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fileDownloadProgressEntity: FileDownloadProgressEntity)

    @Delete
    suspend fun delete(fileDownloadProgressEntity: FileDownloadProgressEntity)

    @Update
    suspend fun update(fileDownloadProgressEntity: FileDownloadProgressEntity)

    @Query("SELECT * FROM FileDownloadProgressEntity WHERE workerId=:workerId")
    suspend fun findByWorkerId(workerId: String): FileDownloadProgressEntity?

    @Query("SELECT * FROM FileDownloadProgressEntity WHERE workerId=:workerId")
    fun findByWorkerIdFlow(workerId: String): Flow<FileDownloadProgressEntity?>

    @Query("DELETE FROM FileDownloadProgressEntity WHERE workerId = :workerId")
    suspend fun deleteByWorkerId(workerId: String)
}