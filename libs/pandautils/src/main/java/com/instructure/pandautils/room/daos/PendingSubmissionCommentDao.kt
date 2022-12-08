package com.instructure.pandautils.room.daos

import androidx.room.*
import com.instructure.pandautils.room.entities.PendingSubmissionCommentEntity
import com.instructure.pandautils.room.model.PendingSubmissionCommentWithFileUploadInput

@Dao
interface PendingSubmissionCommentDao {

    @Insert
    suspend fun insert(pendingSubmissionCommentEntity: PendingSubmissionCommentEntity): Long

    @Delete
    suspend fun delete(pendingSubmissionCommentEntity: PendingSubmissionCommentEntity)

    @Delete
    suspend fun deleteAll(pendingSubmissionCommentEntities: List<PendingSubmissionCommentEntity>)

    @Update
    suspend fun update(pendingSubmissionCommentEntity: PendingSubmissionCommentEntity)

    @Query("SELECT * FROM PendingSubmissionCommentEntity WHERE workerId=:workerId")
    suspend fun findByWorkerId(workerId: String): PendingSubmissionCommentEntity?

    @Transaction
    @Query("SELECT * FROM PendingSubmissionCommentEntity WHERE pageId=:pageId")
    suspend fun findByPageId(pageId: String): List<PendingSubmissionCommentWithFileUploadInput>?

    @Query("SELECT * FROM PendingSubmissionCommentEntity WHERE id=:id")
    suspend fun findById(id: Long): PendingSubmissionCommentEntity?

    @Query("SELECT * FROM PendingSubmissionCommentEntity WHERE status=:status AND workerId IS NOT NULL")
    suspend fun findByStatus(status: String): List<PendingSubmissionCommentWithFileUploadInput>?

    @Query("SELECT * FROM PendingSubmissionCommentEntity WHERE filePath=:filePath AND pageId=:pageId")
    suspend fun findByFilePathAndPageId(filePath: String, pageId: String): PendingSubmissionCommentEntity?

}