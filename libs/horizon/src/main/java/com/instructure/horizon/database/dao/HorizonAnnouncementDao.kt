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
import com.instructure.horizon.database.entity.HorizonAnnouncementCommentEntity
import com.instructure.horizon.database.entity.HorizonAnnouncementEntity

@Dao
interface HorizonAnnouncementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<HorizonAnnouncementEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(entities: List<HorizonAnnouncementCommentEntity>)

    @Query("SELECT * FROM horizon_announcement")
    suspend fun getAll(): List<HorizonAnnouncementEntity>

    @Query("SELECT * FROM horizon_announcement WHERE isUnread = 1")
    suspend fun getUnread(): List<HorizonAnnouncementEntity>

    @Query("SELECT * FROM horizon_announcement WHERE `key` = :key LIMIT 1")
    suspend fun findByKey(key: String): HorizonAnnouncementEntity?

    @Query("SELECT * FROM horizon_announcement_comment WHERE announcementKey = :key ORDER BY position ASC")
    suspend fun getCommentsForAnnouncement(key: String): List<HorizonAnnouncementCommentEntity>

    @Query("DELETE FROM horizon_announcement")
    suspend fun deleteAll()

    @Query("DELETE FROM horizon_announcement_comment")
    suspend fun deleteAllComments()

    @Query("UPDATE horizon_announcement SET isUnread = 0 WHERE `key` = :key")
    suspend fun markRead(key: String)

    @Query("UPDATE horizon_announcement SET isClosed = 1 WHERE `key` = :key")
    suspend fun markClosed(key: String)

    @Transaction
    suspend fun replaceAll(
        announcements: List<HorizonAnnouncementEntity>,
        comments: List<HorizonAnnouncementCommentEntity>,
    ) {
        deleteAll()
        deleteAllComments()
        insertAll(announcements)
        insertComments(comments)
    }
}
