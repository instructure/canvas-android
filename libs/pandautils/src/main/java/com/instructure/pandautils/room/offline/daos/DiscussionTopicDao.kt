package com.instructure.pandautils.room.offline.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.instructure.pandautils.room.offline.entities.DiscussionTopicEntity

@Dao
interface DiscussionTopicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DiscussionTopicEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<DiscussionTopicEntity>): List<Long>

    @Delete
    suspend fun delete(entity: DiscussionTopicEntity)

    @Update
    suspend fun update(entity: DiscussionTopicEntity)

    @Query("SELECT * FROM DiscussionTopicEntity WHERE id = :id")
    suspend fun findById(id: Long): DiscussionTopicEntity?

}