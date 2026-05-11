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
import com.instructure.horizon.database.entity.HorizonLearnRecommendationEntity

@Dao
interface HorizonLearnRecommendationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<HorizonLearnRecommendationEntity>)

    @Query("SELECT * FROM horizon_learn_recommendations ORDER BY displayOrder ASC")
    suspend fun getAll(): List<HorizonLearnRecommendationEntity>

    @Query("DELETE FROM horizon_learn_recommendations")
    suspend fun deleteAll()

    @Query("UPDATE horizon_learn_recommendations SET isBookmarked = :isBookmarked WHERE itemId = :id")
    suspend fun updateBookmark(id: String, isBookmarked: Boolean)

    @Transaction
    suspend fun replaceAll(items: List<HorizonLearnRecommendationEntity>) {
        deleteAll()
        insertAll(items)
    }
}
