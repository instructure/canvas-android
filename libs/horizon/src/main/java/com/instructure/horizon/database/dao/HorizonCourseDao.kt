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
import com.instructure.horizon.database.entity.HorizonCourseEntity

@Dao
interface HorizonCourseDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(courses: List<HorizonCourseEntity>)

    @Query("""
        UPDATE horizon_courses SET
        name = :name, progress = :progress, imageUrl = :imageUrl,
        startAtMs = :startAtMs, endAtMs = :endAtMs,
        requirementCount = :requirementCount,
        requirementCompletedCount = :requirementCompletedCount,
        completedAtMs = :completedAtMs, grade = :grade,
        workflowState = :workflowState, lastActivityAtMs = :lastActivityAtMs,
        enrolledAtMs = :enrolledAtMs
        WHERE courseId = :courseId
    """)
    suspend fun updateEnrollmentFields(
        courseId: Long, name: String, progress: Double, imageUrl: String?,
        startAtMs: Long?, endAtMs: Long?,
        requirementCount: Int?, requirementCompletedCount: Int?,
        completedAtMs: Long?, grade: Double?,
        workflowState: String?, lastActivityAtMs: Long?, enrolledAtMs: Long?,
    )

    @Query("""
        UPDATE horizon_courses SET
        name = :name, progress = :progress, imageUrl = :imageUrl,
        courseSyllabus = :courseSyllabus
        WHERE courseId = :courseId
    """)
    suspend fun updateCourseDetailsFields(
        courseId: Long, name: String, progress: Double, imageUrl: String?, courseSyllabus: String?,
    )

    @Query("""
        UPDATE horizon_courses SET
        name = :name, startAtMs = :startAtMs, endAtMs = :endAtMs,
        moduleItemsDurations = :moduleItemsDurations
        WHERE courseId = :courseId
    """)
    suspend fun updateProgramCourseFields(
        courseId: Long, name: String, startAtMs: Long?, endAtMs: Long?, moduleItemsDurations: String,
    )

    @Query("SELECT * FROM horizon_courses WHERE courseId = :courseId")
    suspend fun getByCourseId(courseId: Long): HorizonCourseEntity?

    @Query("SELECT * FROM horizon_courses WHERE courseId IN (:courseIds)")
    suspend fun getByCourseIds(courseIds: List<Long>): List<HorizonCourseEntity>
}
