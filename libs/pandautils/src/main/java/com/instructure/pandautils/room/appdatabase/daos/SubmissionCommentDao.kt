package com.instructure.pandautils.room.appdatabase.daos

import androidx.room.*
import com.instructure.pandautils.room.appdatabase.entities.SubmissionCommentEntity
import com.instructure.pandautils.room.appdatabase.model.SubmissionCommentWithAttachments

@Dao
interface SubmissionCommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(submissionComment: SubmissionCommentEntity): Long

    @Delete
    suspend fun delete(submissionComment: SubmissionCommentEntity)

    @Update
    suspend fun update(submissionComment: SubmissionCommentEntity)

    @Transaction
    @Query("SELECT * FROM SubmissionCommentEntity WHERE id=:id")
    suspend fun findById(id: Long): SubmissionCommentWithAttachments?
}