package com.instructure.pandautils.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DashboardFileUploadEntity(
    @PrimaryKey
    val workerId: String,
    val userId: Long,
    val title: String?,
    val assignmentName: String?
)
