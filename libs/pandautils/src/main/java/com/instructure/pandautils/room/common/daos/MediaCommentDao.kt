package com.instructure.pandautils.room.common.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.instructure.pandautils.room.common.entities.MediaCommentEntity

@Dao
interface MediaCommentDao {

    @Insert
    suspend fun insert(mediaComment: MediaCommentEntity)

    @Delete
    suspend fun delete(mediaComment: MediaCommentEntity)

    @Update
    suspend fun update(mediaComment: MediaCommentEntity)

    @Query("SELECT * FROM MediaCommentEntity WHERE mediaId = :id")
    suspend fun findById(id: String?): MediaCommentEntity?
}