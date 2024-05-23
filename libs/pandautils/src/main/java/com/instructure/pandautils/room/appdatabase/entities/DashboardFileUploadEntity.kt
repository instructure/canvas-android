package com.instructure.pandautils.room.appdatabase.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DashboardFileUploadEntity(
    @PrimaryKey
    val workerId: String,
    val userId: Long,
    val title: String?,
    val subtitle: String?,
    val courseId: Long?,
    val assignmentId: Long?,
    val attemptId: Long?,
    val folderId: Long?
)
