package com.instructure.pandautils.room.offline.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.instructure.pandautils.room.offline.entities.DiscussionTopicPermissionEntity

@Dao
interface DiscussionTopicPermissionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DiscussionTopicPermissionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<DiscussionTopicPermissionEntity>): List<Long>

    @Delete
    suspend fun delete(entity: DiscussionTopicPermissionEntity)

    @Update
    suspend fun update(entity: DiscussionTopicPermissionEntity)

    @Query("SELECT * FROM DiscussionTopicPermissionEntity WHERE discussionTopicHeaderId = :id")
    suspend fun findByDiscussionTopicHeaderId(id: Long): DiscussionTopicPermissionEntity?
}