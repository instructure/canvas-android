package com.instructure.pandautils.room.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import com.instructure.canvasapi2.db.entities.MediaComment

@Dao
interface MediaCommentDao {

    @Insert
    suspend fun insert(mediaComment: MediaComment)

    @Delete
    suspend fun delete(mediaComment: MediaComment)

    @Update
    suspend fun update(mediaComment: MediaComment)

}