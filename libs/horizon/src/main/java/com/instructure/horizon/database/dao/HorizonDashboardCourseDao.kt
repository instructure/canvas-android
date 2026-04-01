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
import androidx.room.Transaction
import com.instructure.horizon.database.entity.HorizonDashboardCourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class HorizonDashboardCourseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(entities: List<HorizonDashboardCourseEntity>)

    @Query("SELECT * FROM horizon_dashboard_courses")
    abstract fun observeAll(): Flow<List<HorizonDashboardCourseEntity>>

    @Query("SELECT * FROM horizon_dashboard_courses")
    abstract suspend fun getAll(): List<HorizonDashboardCourseEntity>

    @Query("SELECT courseId FROM horizon_dashboard_courses")
    abstract suspend fun getAllCourseIds(): List<Long>

    @Query("SELECT COUNT(*) FROM horizon_dashboard_courses")
    abstract suspend fun count(): Int

    @Query("DELETE FROM horizon_dashboard_courses")
    abstract suspend fun deleteAll()

    @Transaction
    open suspend fun replaceAll(entities: List<HorizonDashboardCourseEntity>) {
        deleteAll()
        insertAll(entities)
    }
}
