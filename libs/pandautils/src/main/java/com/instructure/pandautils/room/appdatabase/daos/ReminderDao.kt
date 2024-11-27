/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 *
 */

package com.instructure.pandautils.room.appdatabase.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.instructure.pandautils.room.appdatabase.entities.ReminderEntity

@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity): Long

    @Query("DELETE FROM ReminderEntity WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ReminderEntity WHERE time < :time")
    suspend fun deletePastReminders(time: Long)

    @Query("SELECT * FROM ReminderEntity WHERE userId = :userId AND assignmentId = :assignmentId")
    suspend fun findByAssignmentId(userId: Long, assignmentId: Long): List<ReminderEntity>

    @Query("SELECT * FROM ReminderEntity WHERE userId = :userId AND assignmentId = :assignmentId")
    fun findByAssignmentIdLiveData(userId: Long, assignmentId: Long): LiveData<List<ReminderEntity>>

    @Query("SELECT * FROM ReminderEntity WHERE userId = :userId")
    suspend fun findByUserId(userId: Long): List<ReminderEntity>
}