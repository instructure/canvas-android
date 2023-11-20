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
import com.instructure.pandautils.room.offline.entities.PageEntity

@Dao
interface PageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<PageEntity>)

    @Delete
    suspend fun delete(entity: PageEntity)

    @Query("DELETE FROM PageEntity WHERE courseId=:id")
    suspend fun deleteAllByCourseId(id: Long)

    @Update
    suspend fun update(entity: PageEntity)

    @Query("SELECT * FROM PageEntity")
    suspend fun findAll(): List<PageEntity>

    @Query("SELECT * FROM PageEntity WHERE id=:id")
    suspend fun findById(id: Long): PageEntity?

    @Query("SELECT * FROM PageEntity WHERE url=:url AND courseId=:courseId")
    suspend fun findByUrlAndCourse(url: String, courseId: Long): PageEntity?

    @Query("SELECT * FROM PageEntity WHERE frontPage=1 AND courseId=:courseId")
    suspend fun getFrontPage(courseId: Long): PageEntity?

    @Query("SELECT * FROM PageEntity WHERE courseId=:courseId")
    suspend fun findByCourseId(courseId: Long): List<PageEntity>

    @Query("SELECT * FROM PageEntity WHERE courseId=:courseId AND (url=:pageId OR title=:pageId)")
    suspend fun getPageDetails(courseId: Long, pageId: String): PageEntity?
}