package com.instructure.pandautils.room.offline.daos

import androidx.room.*
import com.instructure.pandautils.room.offline.entities.MediaCommentEntity

@Dao
interface MediaCommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mediaComment: MediaCommentEntity)

    @Delete
    suspend fun delete(mediaComment: MediaCommentEntity)

    @Update
    suspend fun update(mediaComment: MediaCommentEntity)

    @Query("SELECT * FROM MediaCommentEntity WHERE mediaId = :id")
    suspend fun findById(id: String?): MediaCommentEntity?
}