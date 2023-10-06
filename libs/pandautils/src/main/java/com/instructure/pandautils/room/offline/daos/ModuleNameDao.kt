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
import com.instructure.pandautils.room.offline.entities.ModuleNameEntity

@Dao
interface ModuleNameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ModuleNameEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ModuleNameEntity>)

    @Delete
    suspend fun delete(entity: ModuleNameEntity)

    @Update
    suspend fun update(entity: ModuleNameEntity)

    @Query("SELECT * FROM ModuleNameEntity WHERE lockedModuleId = :lockModuleId")
    suspend fun findByLockModuleId(lockModuleId: Long): List<ModuleNameEntity>
}