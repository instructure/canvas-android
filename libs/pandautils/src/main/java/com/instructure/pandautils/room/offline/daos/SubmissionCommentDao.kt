package com.instructure.pandautils.room.offline.daos

import androidx.room.*
import com.instructure.pandautils.room.offline.entities.SubmissionCommentEntity
import com.instructure.pandautils.room.offline.model.SubmissionCommentWithAttachments

@Dao
interface SubmissionCommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(submissionComment: SubmissionCommentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(submissionComments: List<SubmissionCommentEntity>)

    @Delete
    suspend fun delete(submissionComment: SubmissionCommentEntity)

    @Update
    suspend fun update(submissionComment: SubmissionCommentEntity)

    @Transaction
    @Query("SELECT * FROM SubmissionCommentEntity WHERE id=:id")
    suspend fun findById(id: Long): SubmissionCommentWithAttachments?

    @Query("SELECT * FROM SubmissionCommentEntity WHERE submissionId=:submissionId")
    suspend fun findBySubmissionId(submissionId: Long): List<SubmissionCommentWithAttachments>
}