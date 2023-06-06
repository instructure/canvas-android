package com.instructure.pandautils.room.offline.daos

import androidx.room.*
import com.instructure.pandautils.room.offline.entities.ScheduleItemAssignmentOverrideEntity

@Dao
interface ScheduleItemAssignmentOverrideDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ScheduleItemAssignmentOverrideEntity)

    @Delete
    suspend fun delete(entity: ScheduleItemAssignmentOverrideEntity)

    @Update
    suspend fun update(entity: ScheduleItemAssignmentOverrideEntity)

    @Query("SELECT * FROM ScheduleItemAssignmentOverrideEntity WHERE scheduleItemId=:scheduleItemId")
    suspend fun findByScheduleItemId(scheduleItemId: String): List<ScheduleItemAssignmentOverrideEntity>
}