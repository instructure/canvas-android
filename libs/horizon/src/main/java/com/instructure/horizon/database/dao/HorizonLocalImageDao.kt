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
import com.instructure.horizon.database.entity.HorizonLocalImageEntity

@Dao
interface HorizonLocalImageDao {

    @Query("SELECT * FROM horizon_local_images WHERE url = :url")
    suspend fun findByUrl(url: String): HorizonLocalImageEntity?

    @Query("SELECT * FROM horizon_local_images WHERE url IN (:urls)")
    suspend fun findByUrls(urls: List<String>): List<HorizonLocalImageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HorizonLocalImageEntity)

    @Query("DELETE FROM horizon_local_images")
    suspend fun deleteAll()
}
