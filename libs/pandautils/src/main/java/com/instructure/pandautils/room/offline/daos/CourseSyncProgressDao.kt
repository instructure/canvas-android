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
import com.instructure.pandautils.room.offline.entities.CourseSyncProgressEntity

@Dao
interface CourseSyncProgressDao {

    @Insert
    suspend fun insert(courseSyncProgressEntity: CourseSyncProgressEntity)

    @Insert
    suspend fun insertAll(entities: List<CourseSyncProgressEntity>)

    @Update
    suspend fun update(courseSyncProgressEntity: CourseSyncProgressEntity)

    @Query("SELECT * FROM CourseSyncProgressEntity")
    suspend fun findAll(): List<CourseSyncProgressEntity>

    @Query("SELECT * FROM CourseSyncProgressEntity")
    fun findAllLiveData(): LiveData<List<CourseSyncProgressEntity>>

    @Query("SELECT * FROM CourseSyncProgressEntity WHERE courseId = :courseId")
    suspend fun findByCourseId(courseId: Long): CourseSyncProgressEntity?

    @Query("SELECT * FROM CourseSyncProgressEntity WHERE workerId = :workerId")
    fun findByWorkerIdLiveData(workerId: String): LiveData<CourseSyncProgressEntity?>

    @Query("DELETE FROM CourseSyncProgressEntity")
    suspend fun deleteAll()
}