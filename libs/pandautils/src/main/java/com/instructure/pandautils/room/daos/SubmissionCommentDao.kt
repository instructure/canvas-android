package com.instructure.pandautils.room.daos

import androidx.room.*
import com.instructure.canvasapi2.db.entities.SubmissionComment
import com.instructure.pandautils.room.model.SubmissionCommentWithAttachments

@Dao
interface SubmissionCommentDao {

    @Insert
    suspend fun insert(submissionComment: SubmissionComment): Long

    @Delete
    suspend fun delete(submissionComment: SubmissionComment)

    @Update
    suspend fun update(submissionComment: SubmissionComment)

    @Transaction
    @Query("SELECT * FROM SubmissionComment WHERE id=:id")
    suspend fun findById(id: Long): SubmissionCommentWithAttachments?
}