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
import com.instructure.pandautils.room.offline.entities.ModuleCompletionRequirementEntity

@Dao
interface ModuleCompletionRequirementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ModuleCompletionRequirementEntity)

    @Delete
    suspend fun delete(entity: ModuleCompletionRequirementEntity)

    @Update
    suspend fun update(entity: ModuleCompletionRequirementEntity)

    @Query("SELECT * FROM ModuleCompletionRequirementEntity WHERE moduleId = :moduleId")
    suspend fun findByModuleId(moduleId: Long): List<ModuleCompletionRequirementEntity>

    @Query("SELECT * FROM ModuleCompletionRequirementEntity WHERE id = :id")
    suspend fun findById(id: Long): ModuleCompletionRequirementEntity?
}