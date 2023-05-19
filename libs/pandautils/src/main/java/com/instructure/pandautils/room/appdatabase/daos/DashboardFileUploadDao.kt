package com.instructure.pandautils.room.appdatabase.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.instructure.pandautils.room.appdatabase.entities.DashboardFileUploadEntity

@Dao
interface DashboardFileUploadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dashboardFileUploadEntity: DashboardFileUploadEntity)

    @Delete
    suspend fun delete(dashboardFileUploadEntity: DashboardFileUploadEntity)

    @Query("SELECT * FROM DashboardFileUploadEntity WHERE userId = :userId")
    fun getAllForUser(userId: Long): LiveData<List<DashboardFileUploadEntity>>

    @Query("DELETE FROM DashboardFileUploadEntity WHERE workerId = :workerId")
    suspend fun deleteByWorkerId(workerId: String)
}