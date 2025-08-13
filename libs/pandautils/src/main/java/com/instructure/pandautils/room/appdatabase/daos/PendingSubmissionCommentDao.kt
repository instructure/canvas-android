package com.instructure.pandautils.room.appdatabase.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.instructure.pandautils.room.appdatabase.entities.PendingSubmissionCommentEntity
import com.instructure.pandautils.room.appdatabase.model.PendingSubmissionCommentWithFileUploadInput
import kotlinx.coroutines.flow.Flow

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
    @Query("SELECT * FROM PendingSubmissionCommentEntity WHERE workerId=:workerId")
    suspend fun findByWorkerIdWithInputData(workerId: String): PendingSubmissionCommentWithFileUploadInput?

    @Transaction
    @Query("SELECT * FROM PendingSubmissionCommentEntity WHERE pageId=:pageId")
    suspend fun findByPageId(pageId: String): List<PendingSubmissionCommentWithFileUploadInput>?

    @Transaction
    @Query("SELECT * FROM PendingSubmissionCommentEntity WHERE pageId=:pageId")
    fun findByPageIdFlow(pageId: String): Flow<List<PendingSubmissionCommentWithFileUploadInput>?>

    @Query("SELECT * FROM PendingSubmissionCommentEntity WHERE id=:id")
    suspend fun findById(id: Long): PendingSubmissionCommentEntity?

    @Transaction
    @Query("SELECT * FROM PendingSubmissionCommentEntity WHERE status=:status AND workerId IS NOT NULL")
    suspend fun findByStatus(status: String): List<PendingSubmissionCommentWithFileUploadInput>?

    @Query("DELETE FROM PendingSubmissionCommentEntity WHERE id = :id")
    suspend fun deleteById(id: Long)
}