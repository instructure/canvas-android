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
import com.instructure.horizon.database.entity.HorizonLearnBrowseItemEntity

@Dao
interface HorizonLearnBrowseItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<HorizonLearnBrowseItemEntity>)

    @Query("SELECT * FROM horizon_learn_browse_items")
    suspend fun getAll(): List<HorizonLearnBrowseItemEntity>

    @Query("SELECT * FROM horizon_learn_browse_items WHERE id = :id")
    suspend fun findById(id: String): HorizonLearnBrowseItemEntity?

    @Query(
        "SELECT * FROM horizon_learn_browse_items " +
            "WHERE (:searchQuery IS NULL OR courseName LIKE '%' || :searchQuery || '%') " +
            "AND (:itemType IS NULL OR itemType = :itemType) " +
            "AND (:bookmarkedOnly = 0 OR isBookmarked = 1) " +
            "AND (:completedOnly = 0 OR completionPercentage = 100.0) " +
            "ORDER BY " +
            "CASE WHEN :sortMode = 'MOST_RECENT' THEN updatedAtMs END DESC, " +
            "CASE WHEN :sortMode = 'LEAST_RECENT' THEN updatedAtMs END ASC, " +
            "CASE WHEN :sortMode = 'NAME_A_Z' THEN courseName END ASC, " +
            "CASE WHEN :sortMode = 'NAME_Z_A' THEN courseName END DESC, " +
            "CASE WHEN :sortMode IS NULL THEN displayOrder END ASC"
    )
    suspend fun queryFiltered(
        searchQuery: String?,
        itemType: String?,
        bookmarkedOnly: Boolean,
        completedOnly: Boolean,
        sortMode: String?,
    ): List<HorizonLearnBrowseItemEntity>

    @Query("DELETE FROM horizon_learn_browse_items")
    suspend fun deleteAll()

    @Query("UPDATE horizon_learn_browse_items SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmark(id: String, isBookmarked: Boolean)

    @Transaction
    suspend fun replaceAll(items: List<HorizonLearnBrowseItemEntity>) {
        deleteAll()
        insertAll(items)
    }
}
