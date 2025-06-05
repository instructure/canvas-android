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
import com.instructure.pandautils.room.offline.entities.EnrollmentEntity

@Dao
interface EnrollmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: EnrollmentEntity): Long

    @Upsert(entity = EnrollmentEntity::class)
    suspend fun insertOrUpdate(entity: EnrollmentEntity)

    @Delete
    suspend fun delete(entity: EnrollmentEntity)

    @Update
    suspend fun update(entity: EnrollmentEntity)

    @Query("SELECT * FROM EnrollmentEntity WHERE courseId = :courseId")
    suspend fun findByCourseId(courseId: Long): List<EnrollmentEntity>

    @Query("SELECT * FROM EnrollmentEntity WHERE courseId = :courseId AND userId = :userId")
    suspend fun findByCourseIdAndUserId(courseId: Long, userId: Long): List<EnrollmentEntity>

    @Query("SELECT * FROM EnrollmentEntity")
    suspend fun findAll(): List<EnrollmentEntity>

    @Query("SELECT * FROM EnrollmentEntity WHERE currentGradingPeriodId = :gradingPeriodId")
    suspend fun findByGradingPeriodId(gradingPeriodId: Long): List<EnrollmentEntity>

    @Query("SELECT * FROM EnrollmentEntity WHERE courseId = :courseId AND role = :role")
    suspend fun findByCourseIdAndRole(courseId: Long, role: String): List<EnrollmentEntity>

    @Query("SELECT * FROM EnrollmentEntity WHERE userId = :userId")
    suspend fun findByUserId(userId: Long): EnrollmentEntity?
}