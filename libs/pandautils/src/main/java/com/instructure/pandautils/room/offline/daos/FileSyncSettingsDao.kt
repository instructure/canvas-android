package com.instructure.pandautils.room.offline.daos

import androidx.room.*
import com.instructure.pandautils.room.offline.entities.FileSyncSettingsEntity

@Dao
interface FileSyncSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FileSyncSettingsEntity)

    @Delete
    suspend fun delete(entity: FileSyncSettingsEntity)

    @Update
    suspend fun update(entity: FileSyncSettingsEntity)

    @Query("SELECT * FROM FileSyncSettingsEntity")
    suspend fun findAll(): List<FileSyncSettingsEntity>

    @Query("SELECT * FROM FileSyncSettingsEntity WHERE id=:id")
    suspend fun findById(id: Long): FileSyncSettingsEntity?

    @Query("DELETE FROM FileSyncSettingsEntity WHERE id=:fileId")
    suspend fun deleteById(fileId: Long)

    @Query("DELETE FROM FileSyncSettingsEntity WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}