package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CourseFeaturesEntity(
    @PrimaryKey
    val courseId: Long,
    val features: List<String>
)
