package com.instructure.canvasapi2.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FileUploadInput(
    @PrimaryKey
    val workerId: String,
    val courseId: Long? = null,
    val assignmentId: Long? = null,
    val quizId: Long? = null,
    val quizQuestionId: Long? = null,
    val position: Int? = null,
    val parentFolderId: Long? = null,
    val action: String,
    val userId: Long? = null,
    val attachments: List<Long> = emptyList(),
    val submissionId: Long? = null,
    var filePaths: List<String>,
)