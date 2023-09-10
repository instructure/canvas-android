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

package com.instructure.pandautils.room.offline.daos

import androidx.room.*
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.room.offline.model.CourseSyncSettingsWithFiles

@Dao
interface CourseSyncSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CourseSyncSettingsEntity)

    @Delete
    suspend fun delete(entity: CourseSyncSettingsEntity)

    @Update
    suspend fun update(entity: CourseSyncSettingsEntity)

    @Query("SELECT * FROM CourseSyncSettingsEntity")
    suspend fun findAll(): List<CourseSyncSettingsEntity>

    @Query("SELECT * FROM CourseSyncSettingsEntity WHERE courseId=:courseId")
    suspend fun findById(courseId: Long): CourseSyncSettingsEntity?

    @Query("SELECT * FROM CourseSyncSettingsEntity WHERE courseId IN (:courseIds)")
    suspend fun findByIds(courseIds: List<Long>): List<CourseSyncSettingsEntity>

    @Query("SELECT * FROM CourseSyncSettingsEntity WHERE courseId=:courseId")
    suspend fun findWithFilesById(courseId: Long): CourseSyncSettingsWithFiles?

    @Query("SELECT * FROM CourseSyncSettingsEntity WHERE courseId IN (:courseIds)")
    suspend fun findWithFilesByIds(courseIds: List<Long>): List<CourseSyncSettingsWithFiles>

    @Query("SELECT * FROM CourseSyncSettingsEntity")
    suspend fun findAllWithFiles(): List<CourseSyncSettingsWithFiles>
}