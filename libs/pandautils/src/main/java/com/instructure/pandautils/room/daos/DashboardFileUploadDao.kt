package com.instructure.pandautils.room.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.instructure.pandautils.room.entities.DashboardFileUploadEntity

@Dao
interface DashboardFileUploadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dashboardFileUploadEntity: DashboardFileUploadEntity)

    @Delete
    suspend fun delete(dashboardFileUploadEntity: DashboardFileUploadEntity)

    @Query("SELECT * FROM DashboardFileUploadEntity WHERE userId = :userId")
    fun getAll(userId: Long): LiveData<List<DashboardFileUploadEntity>>
}