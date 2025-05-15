/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.room.assignment.list.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.instructure.pandautils.room.assignment.list.entities.AssignmentListSelectedFiltersEntity

@Dao
interface AssignmentListSelectedFiltersEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AssignmentListSelectedFiltersEntity)

    @Upsert
    suspend fun insertOrUpdate(entity: AssignmentListSelectedFiltersEntity)

    @Delete
    suspend fun delete(entity: AssignmentListSelectedFiltersEntity)

    @Update
    suspend fun update(entity: AssignmentListSelectedFiltersEntity)

    @Query("SELECT * FROM assignment_filter WHERE userDomain = :userDomain AND userId = :userId AND contextId = :contextId")
    suspend fun findAssignmentListSelectedFiltersEntity(userDomain: String, userId: Long, contextId: Long): AssignmentListSelectedFiltersEntity?
}