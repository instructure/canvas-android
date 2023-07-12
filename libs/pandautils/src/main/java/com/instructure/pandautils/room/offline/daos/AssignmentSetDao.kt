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
import com.instructure.pandautils.room.offline.entities.AssignmentSetEntity

@Dao
interface AssignmentSetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assignmentSetEntity: AssignmentSetEntity)

    @Delete
    suspend fun delete(assignmentSetEntity: AssignmentSetEntity)

    @Update
    suspend fun update(assignmentSetEntity: AssignmentSetEntity)

    @Query("SELECT * FROM AssignmentSetEntity WHERE masteryPathId = :masteryPathId")
    suspend fun findByMasteryPathId(masteryPathId: Long): List<AssignmentSetEntity>
}