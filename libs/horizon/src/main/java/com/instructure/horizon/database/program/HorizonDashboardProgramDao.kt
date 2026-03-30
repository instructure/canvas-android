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
package com.instructure.horizon.database.program

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HorizonDashboardProgramDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(programs: List<HorizonDashboardProgramEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRefs(refs: List<HorizonDashboardProgramCourseRef>)

    @Query("SELECT * FROM horizon_dashboard_programs")
    suspend fun getAll(): List<HorizonDashboardProgramEntity>

    @Query("SELECT * FROM horizon_dashboard_program_course_refs WHERE courseId = :courseId")
    suspend fun getRefsForCourse(courseId: Long): List<HorizonDashboardProgramCourseRef>

    @Query("SELECT * FROM horizon_dashboard_program_course_refs WHERE programId = :programId")
    suspend fun getRefsForProgram(programId: String): List<HorizonDashboardProgramCourseRef>

    @Query("DELETE FROM horizon_dashboard_programs")
    suspend fun deleteAll()

    @Query("DELETE FROM horizon_dashboard_program_course_refs")
    suspend fun deleteAllRefs()
}
