package com.instructure.pandautils.room.offline.daos

import androidx.room.*
import com.instructure.pandautils.room.offline.entities.AssignmentOverrideEntity

@Dao
interface AssignmentOverrideDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AssignmentOverrideEntity)

    @Delete
    suspend fun delete(entity: AssignmentOverrideEntity)

    @Update
    suspend fun update(entity: AssignmentOverrideEntity)

    @Query("SELECT * FROM AssignmentOverrideEntity WHERE id IN (:assignmentOverrideIds)")
    suspend fun findByIds(assignmentOverrideIds: List<Long>): List<AssignmentOverrideEntity>
}