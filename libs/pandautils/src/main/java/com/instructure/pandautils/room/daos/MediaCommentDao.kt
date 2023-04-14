package com.instructure.pandautils.room.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import com.instructure.pandautils.room.appdatabase.entities.MediaCommentEntity

@Dao
interface MediaCommentDao {

    @Insert
    suspend fun insert(mediaComment: MediaCommentEntity)

    @Delete
    suspend fun delete(mediaComment: MediaCommentEntity)

    @Update
    suspend fun update(mediaComment: MediaCommentEntity)

}