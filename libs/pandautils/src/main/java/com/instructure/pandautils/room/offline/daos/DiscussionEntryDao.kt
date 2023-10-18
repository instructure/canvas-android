package com.instructure.pandautils.room.offline.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.instructure.pandautils.room.offline.entities.DiscussionEntryEntity

@Dao
interface DiscussionEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DiscussionEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<DiscussionEntryEntity>): List<Long>

    @Delete
    suspend fun delete(entity: DiscussionEntryEntity)

    @Update
    suspend fun update(entity: DiscussionEntryEntity)

    @Query("SELECT * FROM DiscussionEntryEntity WHERE id = :id")
    suspend fun findById(id: Long): DiscussionEntryEntity?

}