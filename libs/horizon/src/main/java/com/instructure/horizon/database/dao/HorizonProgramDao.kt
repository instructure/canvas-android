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
import com.instructure.horizon.database.entity.HorizonProgramCourseRef
import com.instructure.horizon.database.entity.HorizonProgramEntity

@Dao
interface HorizonProgramDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(programs: List<HorizonProgramEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRefs(refs: List<HorizonProgramCourseRef>)

    @Query("SELECT * FROM horizon_programs")
    suspend fun getAll(): List<HorizonProgramEntity>

    @Query("SELECT * FROM horizon_programs WHERE programId = :programId")
    suspend fun getById(programId: String): HorizonProgramEntity?

    @Query("SELECT * FROM horizon_program_course_refs WHERE programId = :programId")
    suspend fun getRefsForProgram(programId: String): List<HorizonProgramCourseRef>

    @Query("DELETE FROM horizon_programs")
    suspend fun deleteAll()

    @Query("DELETE FROM horizon_program_course_refs")
    suspend fun deleteAllRefs()

    @Transaction
    suspend fun replaceAll(programs: List<HorizonProgramEntity>, refs: List<HorizonProgramCourseRef>) {
        deleteAllRefs()
        deleteAll()
        insertAll(programs)
        insertAllRefs(refs)
    }
}
