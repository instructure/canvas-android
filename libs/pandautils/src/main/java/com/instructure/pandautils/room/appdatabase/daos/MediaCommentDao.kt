package com.instructure.pandautils.room.appdatabase.daos

import androidx.room.*
import com.instructure.pandautils.room.appdatabase.entities.MediaCommentEntity

@Dao
interface MediaCommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mediaComment: MediaCommentEntity)

    @Delete
    suspend fun delete(mediaComment: MediaCommentEntity)

    @Update
    suspend fun update(mediaComment: MediaCommentEntity)
}