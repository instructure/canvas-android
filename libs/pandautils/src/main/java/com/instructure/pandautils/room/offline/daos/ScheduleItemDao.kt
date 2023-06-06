package com.instructure.pandautils.room.offline.daos

import androidx.room.*
import com.instructure.pandautils.room.offline.entities.ScheduleItemEntity

@Dao
interface ScheduleItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ScheduleItemEntity)

    @Delete
    suspend fun delete(entity: ScheduleItemEntity)

    @Update
    suspend fun update(entity: ScheduleItemEntity)

    @Query("SELECT * FROM ScheduleItemEntity WHERE contextCode IN (:contextCodes) AND type=:type")
    suspend fun findByItemType(contextCodes: List<String>, type: String): List<ScheduleItemEntity>

}