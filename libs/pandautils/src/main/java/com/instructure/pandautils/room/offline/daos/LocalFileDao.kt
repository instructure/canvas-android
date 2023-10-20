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
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.instructure.pandautils.room.offline.entities.LocalFileEntity

@Dao
interface LocalFileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(localFile: LocalFileEntity)

    @Update
    suspend fun update(localFile: LocalFileEntity)

    @Delete
    suspend fun delete(localFile: LocalFileEntity)

    @Query("SELECT * FROM LocalFileEntity WHERE id = :id")
    suspend fun findById(id: Long): LocalFileEntity?

    @Query("SELECT * FROM LocalFileEntity WHERE courseId = :courseId")
    suspend fun findByCourseId(courseId: Long): List<LocalFileEntity>

    @Query("SELECT EXISTS(SELECT * FROM LocalFileEntity WHERE id = :id)")
    suspend fun existsById(id: Long): Boolean

    @Query("SELECT * FROM LocalFileEntity WHERE id IN (:ids)")
    suspend fun findByIds(ids: List<Long>): List<LocalFileEntity>

    @Query("SELECT * FROM LocalFileEntity WHERE courseId = :courseId AND id NOT IN (:ids)")
    suspend fun findRemovedFiles(courseId: Long, ids: List<Long>): List<LocalFileEntity>
}