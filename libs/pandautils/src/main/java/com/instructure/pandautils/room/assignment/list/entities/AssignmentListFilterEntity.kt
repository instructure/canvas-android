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
package com.instructure.pandautils.room.assignment.list.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assignment_filter")
data class AssignmentListFilterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userDomain: String,
    val userId: Long,
    val contextId: Long,
    val groupId: Int,
    val selectedIndexes: List<Int>
)