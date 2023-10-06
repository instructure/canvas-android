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
import com.instructure.pandautils.room.offline.entities.DiscussionTopicHeaderEntity

@Dao
interface DiscussionTopicHeaderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DiscussionTopicHeaderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<DiscussionTopicHeaderEntity>): List<Long>

    @Delete
    suspend fun delete(entity: DiscussionTopicHeaderEntity)

    @Update
    suspend fun update(entity: DiscussionTopicHeaderEntity)

    @Query("SELECT * FROM DiscussionTopicHeaderEntity WHERE id = :id")
    suspend fun findById(id: Long): DiscussionTopicHeaderEntity?

    @Query("SELECT * FROM DiscussionTopicHeaderEntity WHERE announcement = 0 AND courseId = :courseId")
    suspend fun findAllDiscussionsForCourse(courseId: Long): List<DiscussionTopicHeaderEntity>

    @Query("SELECT * FROM DiscussionTopicHeaderEntity WHERE announcement = 1 AND courseId = :courseId ORDER BY postedDate DESC")
    suspend fun findAllAnnouncementsForCourse(courseId: Long): List<DiscussionTopicHeaderEntity>

    @Query("DELETE FROM DiscussionTopicHeaderEntity WHERE courseId = :courseId AND announcement = :isAnnouncement")
    suspend fun deleteAllByCourseId(courseId: Long, isAnnouncement: Boolean)
}