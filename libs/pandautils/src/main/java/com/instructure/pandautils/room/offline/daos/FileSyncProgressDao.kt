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

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.instructure.pandautils.room.offline.entities.FileSyncProgressEntity

@Dao
interface FileSyncProgressDao {

    @Insert
    suspend fun insert(fileSyncProgressEntity: FileSyncProgressEntity): Long

    @Insert
    suspend fun insertAll(fileSyncProgressEntities: List<FileSyncProgressEntity>)

    @Update
    suspend fun update(fileSyncProgressEntity: FileSyncProgressEntity)

    @Query("SELECT * FROM FileSyncProgressEntity WHERE id = :id")
    suspend fun findById(id: Long): FileSyncProgressEntity?

    @Query("SELECT * FROM FileSyncProgressEntity WHERE courseId = :courseId")
    fun findByCourseIdLiveData(courseId: Long): LiveData<List<FileSyncProgressEntity>>

    @Query("SELECT * FROM FileSyncProgressEntity WHERE courseId = :courseId")
    suspend fun findByCourseId(courseId: Long): List<FileSyncProgressEntity>

    @Query("SELECT * FROM FileSyncProgressEntity WHERE fileId = :fileId")
    suspend fun findByFileId(fileId: Long): FileSyncProgressEntity?

    @Query("SELECT * FROM FileSyncProgressEntity WHERE fileId = :fileId")
    fun findByFileIdLiveData(fileId: Long): LiveData<FileSyncProgressEntity?>

    @Query("SELECT * FROM FileSyncProgressEntity WHERE additionalFile = 1 AND courseId = :courseId")
    fun findAdditionalFilesByCourseIdLiveData(courseId: Long): LiveData<List<FileSyncProgressEntity>>

    @Query("SELECT * FROM FileSyncProgressEntity WHERE additionalFile = 0 AND courseId = :courseId")
    fun findCourseFilesByCourseIdLiveData(courseId: Long): LiveData<List<FileSyncProgressEntity>>

    @Query("SELECT * FROM FileSyncProgressEntity")
    fun findAllLiveData(): LiveData<List<FileSyncProgressEntity>>

    @Query("SELECT * FROM FileSyncProgressEntity")
    suspend fun findAll(): List<FileSyncProgressEntity>

    @Query("DELETE FROM FileSyncProgressEntity")
    suspend fun deleteAll()

    @Query("SELECT * FROM FileSyncProgressEntity WHERE ROWID = :rowId")
    suspend fun findByRowId(rowId: Long): FileSyncProgressEntity?
}