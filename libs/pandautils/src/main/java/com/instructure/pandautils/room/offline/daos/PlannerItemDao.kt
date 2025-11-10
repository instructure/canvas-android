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
 *
 *
 */

package com.instructure.pandautils.room.offline.daos

import androidx.room.*
import com.instructure.pandautils.room.offline.entities.PlannerItemEntity

@Dao
interface PlannerItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PlannerItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<PlannerItemEntity>)

    @Delete
    suspend fun delete(entity: PlannerItemEntity)

    @Query("DELETE FROM PlannerItemEntity WHERE courseId=:courseId")
    suspend fun deleteAllByCourseId(courseId: Long)

    @Update
    suspend fun update(entity: PlannerItemEntity)

    @Query("SELECT * FROM PlannerItemEntity WHERE id=:id")
    suspend fun findById(id: Long): PlannerItemEntity?

    @Query("SELECT * FROM PlannerItemEntity WHERE courseId IN (:courseIds)")
    suspend fun findByCourseIds(courseIds: List<Long>): List<PlannerItemEntity>

    @Query("SELECT * FROM PlannerItemEntity WHERE courseId=:courseId")
    suspend fun findByCourseId(courseId: Long): List<PlannerItemEntity>
}