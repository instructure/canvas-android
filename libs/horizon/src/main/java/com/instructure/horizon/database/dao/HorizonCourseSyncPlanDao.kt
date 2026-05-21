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
package com.instructure.horizon.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.instructure.horizon.database.entity.HorizonCourseSyncPlanEntity
import com.instructure.horizon.offline.sync.HorizonProgressState
import kotlinx.coroutines.flow.Flow

@Dao
interface HorizonCourseSyncPlanDao {

    @Query("SELECT * FROM horizon_course_sync_plan")
    suspend fun findAll(): List<HorizonCourseSyncPlanEntity>

    @Query("SELECT * FROM horizon_course_sync_plan")
    fun findAllFlow(): Flow<List<HorizonCourseSyncPlanEntity>>

    @Query("SELECT * FROM horizon_course_sync_plan WHERE courseId = :courseId")
    suspend fun findByCourseId(courseId: Long): HorizonCourseSyncPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HorizonCourseSyncPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<HorizonCourseSyncPlanEntity>)

    @Query("UPDATE horizon_course_sync_plan SET state = :state WHERE courseId = :courseId")
    suspend fun updateState(courseId: Long, state: HorizonProgressState)

    @Query("UPDATE horizon_course_sync_plan SET modulesState = :state WHERE courseId = :courseId")
    suspend fun updateModulesState(courseId: Long, state: HorizonProgressState)

    @Query("UPDATE horizon_course_sync_plan SET assignmentsState = :state WHERE courseId = :courseId")
    suspend fun updateAssignmentsState(courseId: Long, state: HorizonProgressState)

    @Query("UPDATE horizon_course_sync_plan SET pagesState = :state WHERE courseId = :courseId")
    suspend fun updatePagesState(courseId: Long, state: HorizonProgressState)

    @Query("UPDATE horizon_course_sync_plan SET scoresState = :state WHERE courseId = :courseId")
    suspend fun updateScoresState(courseId: Long, state: HorizonProgressState)

    @Query("UPDATE horizon_course_sync_plan SET filesState = :state WHERE courseId = :courseId")
    suspend fun updateFilesState(courseId: Long, state: HorizonProgressState)

    @Query("UPDATE horizon_course_sync_plan SET notesState = :state WHERE courseId = :courseId")
    suspend fun updateNotesState(courseId: Long, state: HorizonProgressState)

    @Query("DELETE FROM horizon_course_sync_plan")
    suspend fun deleteAll()
}
