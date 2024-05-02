/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 */package com.instructure.pandautils.room.offline.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.instructure.pandautils.room.offline.entities.DiscussionTopicRemoteFileEntity

@Dao
interface DiscussionTopicRemoteFileDao {

    @Insert
    suspend fun insert(entity: DiscussionTopicRemoteFileEntity)

    @Insert
    suspend fun insertAll(entities: List<DiscussionTopicRemoteFileEntity>)

    @Query("SELECT * FROM DiscussionTopicRemoteFileEntity WHERE discussionId = :discussionId")
    suspend fun findByDiscussionId(discussionId: Long): List<DiscussionTopicRemoteFileEntity>
}