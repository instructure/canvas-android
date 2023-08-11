package com.instructure.pandautils.room.offline.daos

import androidx.room.*
import com.instructure.pandautils.room.offline.entities.FileSyncSettingsEntity

@Dao
abstract class FileSyncSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(entity: FileSyncSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(entities: List<FileSyncSettingsEntity>)

    @Delete
    abstract suspend fun delete(entity: FileSyncSettingsEntity)

    @Update
    abstract suspend fun update(entity: FileSyncSettingsEntity)

    @Query("SELECT * FROM FileSyncSettingsEntity")
    abstract suspend fun findAll(): List<FileSyncSettingsEntity>

    @Query("SELECT * FROM FileSyncSettingsEntity WHERE id=:id")
    abstract suspend fun findById(id: Long): FileSyncSettingsEntity?

    @Query("DELETE FROM FileSyncSettingsEntity WHERE id=:fileId")
    abstract suspend fun deleteById(fileId: Long)

    @Query("DELETE FROM FileSyncSettingsEntity WHERE id IN (:ids)")
    abstract suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM FileSyncSettingsEntity WHERE courseId=:courseId")
    abstract suspend fun deleteByCourseId(courseId: Long)

    @Query("SELECT * FROM FileSyncSettingsEntity WHERE courseId=:courseId")
    abstract suspend fun findByCourseId(courseId: Long): List<FileSyncSettingsEntity>

    @Transaction
    open suspend fun updateCourseFiles(courseId: Long, fileSyncSettings: List<FileSyncSettingsEntity>) {
        deleteByCourseId(courseId)
        insertAll(fileSyncSettings)
    }

    @Query("DELETE FROM FileSyncSettingsEntity WHERE courseId = :courseId AND id NOT IN (:ids)")
    abstract suspend fun deleteAllExcept(courseId: Long, ids: List<Long>)

    @Query("SELECT * FROM FileSyncSettingsEntity WHERE courseId = :courseId AND id NOT IN (:ids)")
    abstract suspend fun findComplements(courseId: Long, ids: List<Long>): List<FileSyncSettingsEntity>
}