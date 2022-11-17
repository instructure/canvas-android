package com.instructure.pandautils.room.daos

import androidx.room.*
import com.instructure.canvasapi2.db.entities.Attachment

@Dao
interface AttachmentDao {

    @Insert
    suspend fun insert(attachment: Attachment)

    @Insert
    suspend fun insertAll(attachments: List<Attachment>)

    @Delete
    suspend fun delete(attachment: Attachment)

    @Delete
    suspend fun deleteAll(attachments: List<Attachment>)

    @Update
    suspend fun update(attachment: Attachment)

    @Query("SELECT * FROM Attachment WHERE workerId=:parentId")
    suspend fun findByParentId(parentId: String): List<Attachment>?
}