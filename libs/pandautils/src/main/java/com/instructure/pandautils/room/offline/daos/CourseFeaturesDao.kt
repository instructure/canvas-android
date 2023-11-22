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

import androidx.room.*
import com.instructure.pandautils.room.offline.entities.CourseFeaturesEntity

@Dao
interface CourseFeaturesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CourseFeaturesEntity)

    @Delete
    suspend fun delete(entity: CourseFeaturesEntity)

    @Update
    suspend fun update(entity: CourseFeaturesEntity)

    @Query("SELECT * FROM CourseFeaturesEntity WHERE id = :courseId")
    suspend fun findByCourseId(courseId: Long): CourseFeaturesEntity?
}
