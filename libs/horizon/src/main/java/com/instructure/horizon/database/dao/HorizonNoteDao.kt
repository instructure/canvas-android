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
import androidx.room.Transaction
import com.instructure.horizon.database.entity.HorizonNoteEntity

@Dao
interface HorizonNoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(notes: List<HorizonNoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: HorizonNoteEntity)

    @Query(
        "SELECT * FROM horizon_notes " +
            "WHERE (:courseId IS NULL OR courseId = :courseId) " +
            "AND (:objectType IS NULL OR objectType = :objectType) " +
            "AND (:objectId IS NULL OR objectId = :objectId) " +
            "AND (:reaction IS NULL OR reaction = :reaction) " +
            "ORDER BY " +
            "CASE WHEN :ascending = 1 THEN updatedAt END ASC, " +
            "CASE WHEN :ascending = 0 THEN updatedAt END DESC " +
            "LIMIT :limit OFFSET :offset"
    )
    suspend fun query(
        courseId: Long?,
        objectType: String?,
        objectId: String?,
        reaction: String?,
        ascending: Boolean,
        limit: Int,
        offset: Int,
    ): List<HorizonNoteEntity>

    @Query(
        "SELECT COUNT(*) FROM horizon_notes " +
            "WHERE (:courseId IS NULL OR courseId = :courseId) " +
            "AND (:objectType IS NULL OR objectType = :objectType) " +
            "AND (:objectId IS NULL OR objectId = :objectId) " +
            "AND (:reaction IS NULL OR reaction = :reaction)"
    )
    suspend fun count(
        courseId: Long?,
        objectType: String?,
        objectId: String?,
        reaction: String?,
    ): Int

    @Query("DELETE FROM horizon_notes WHERE courseId = :courseId")
    suspend fun deleteByCourseId(courseId: Long)

    @Query("DELETE FROM horizon_notes WHERE id = :noteId")
    suspend fun deleteById(noteId: String)

    @Transaction
    suspend fun replaceForCourse(courseId: Long, notes: List<HorizonNoteEntity>) {
        deleteByCourseId(courseId)
        upsertAll(notes)
    }
}
