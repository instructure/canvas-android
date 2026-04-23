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
import com.instructure.horizon.database.entity.HorizonLearnItemEntity

@Dao
interface HorizonLearnItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<HorizonLearnItemEntity>)

    @Query("SELECT * FROM horizon_learn_items WHERE queryKey = :queryKey")
    suspend fun getByQueryKey(queryKey: String): List<HorizonLearnItemEntity>

    @Query("DELETE FROM horizon_learn_items WHERE queryKey = :queryKey")
    suspend fun deleteByQueryKey(queryKey: String)

    @Transaction
    suspend fun replaceByQueryKey(entities: List<HorizonLearnItemEntity>, queryKey: String) {
        deleteByQueryKey(queryKey)
        insertAll(entities)
    }
}
