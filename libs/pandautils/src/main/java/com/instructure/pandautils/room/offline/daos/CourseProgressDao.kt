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
import com.instructure.pandautils.room.offline.entities.CourseProgressEntity

@Dao
interface CourseProgressDao {

    @Insert
    suspend fun insert(courseProgressEntity: CourseProgressEntity)

    @Update
    suspend fun update(courseProgressEntity: CourseProgressEntity)

    @Query("SELECT * FROM CourseProgressEntity WHERE workerId = :workerId")
    suspend fun findByWorkerId(workerId: String): CourseProgressEntity?

    @Query("SELECT * FROM CourseProgressEntity WHERE courseId IN (:courseIds)")
    fun findByCourseIdsLiveData(courseIds: List<Long>): LiveData<List<CourseProgressEntity>>
}