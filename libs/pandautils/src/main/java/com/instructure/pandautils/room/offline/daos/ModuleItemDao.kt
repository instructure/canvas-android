/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.pandautils.room.offline.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.instructure.pandautils.room.offline.entities.ModuleItemEntity

@Dao
interface ModuleItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(moduleItem: ModuleItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(moduleItems: List<ModuleItemEntity>)

    @Delete
    suspend fun delete(moduleItem: ModuleItemEntity)

    @Update
    suspend fun update(moduleItem: ModuleItemEntity)

    @Query("SELECT * FROM ModuleItemEntity WHERE moduleId = :moduleId ORDER BY position")
    suspend fun findByModuleId(moduleId: Long): List<ModuleItemEntity>

    @Query("SELECT * FROM ModuleItemEntity WHERE id = :id")
    suspend fun findById(id: Long): ModuleItemEntity?

    @Query("SELECT * FROM ModuleItemEntity WHERE type = :type AND contentId = :contentId")
    suspend fun findByTypeAndContentId(type: String, contentId: Long): ModuleItemEntity?

    @Query("SELECT * FROM ModuleItemEntity WHERE pageUrl = :pageUrl")
    suspend fun findByPageUrl(pageUrl: String): ModuleItemEntity?
}