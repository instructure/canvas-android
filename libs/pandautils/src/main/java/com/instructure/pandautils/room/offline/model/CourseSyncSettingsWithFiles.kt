package com.instructure.pandautils.room.offline.model

import androidx.room.Embedded
import androidx.room.Relation
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.FileSyncSettingsEntity

data class CourseSyncSettingsWithFiles(
    @Embedded
    val courseSyncSettings: CourseSyncSettingsEntity,

    @Relation(
        entity = FileSyncSettingsEntity::class,
        parentColumn = "courseId",
        entityColumn = "courseId"
    )
    val files: List<FileSyncSettingsEntity>
) {
    fun isFileSelected(fileId: Long): Boolean {
        return courseSyncSettings.fullFileSync || files.find { it.id == fileId } != null
    }
}