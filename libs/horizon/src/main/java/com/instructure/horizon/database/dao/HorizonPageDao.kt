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
import com.instructure.horizon.database.entity.HorizonPageEntity

@Dao
interface HorizonPageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePage(page: HorizonPageEntity)

    @Query("SELECT * FROM horizon_pages WHERE courseId = :courseId AND pageUrl = :pageUrl LIMIT 1")
    suspend fun getPage(courseId: Long, pageUrl: String): HorizonPageEntity?

    @Query("DELETE FROM horizon_pages WHERE courseId = :courseId")
    suspend fun deleteByCourseId(courseId: Long)
}
