package com.instructure.pandautils.room.daos

import androidx.room.*
import com.instructure.pandautils.room.appdatabase.entities.AttachmentEntity

@Dao
interface AttachmentDao {

    @Insert
    suspend fun insert(attachment: AttachmentEntity)

    @Insert
    suspend fun insertAll(attachments: List<AttachmentEntity>)

    @Delete
    suspend fun delete(attachment: AttachmentEntity)

    @Delete
    suspend fun deleteAll(attachments: List<AttachmentEntity>)

    @Update
    suspend fun update(attachment: AttachmentEntity)

    @Query("SELECT * FROM AttachmentEntity WHERE workerId=:parentId")
    suspend fun findByParentId(parentId: String): List<AttachmentEntity>?
}