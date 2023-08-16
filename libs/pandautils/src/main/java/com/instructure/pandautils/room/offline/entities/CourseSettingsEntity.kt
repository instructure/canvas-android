package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.CourseSettings

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
data class CourseSettingsEntity(
    @PrimaryKey
    val courseId: Long,
    val courseSummary: Boolean?,
    val restrictQuantitativeData: Boolean,
) {
    constructor(courseSettings: CourseSettings, courseId: Long) : this(
        courseId,
        courseSettings.courseSummary,
        courseSettings.restrictQuantitativeData
    )

    fun toApiModel(): CourseSettings {
        return CourseSettings(courseSummary, restrictQuantitativeData)
    }
}