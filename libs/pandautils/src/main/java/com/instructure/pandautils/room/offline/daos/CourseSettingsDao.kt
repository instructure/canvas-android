package com.instructure.pandautils.room.offline.daos

import androidx.room.*
import com.instructure.pandautils.room.offline.entities.CourseSettingsEntity

@Dao
interface CourseSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CourseSettingsEntity)

    @Delete
    suspend fun delete(entity: CourseSettingsEntity)

    @Update
    suspend fun update(entity: CourseSettingsEntity)

    @Query("SELECT * FROM CourseSettingsEntity WHERE courseId=:courseId")
    suspend fun findByCourseId(courseId: Long): CourseSettingsEntity?
}