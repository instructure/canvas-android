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
}