package com.instructure.pandautils.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FileUploadInput(
    @PrimaryKey val workerId: String
)