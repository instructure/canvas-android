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

    @Query("SELECT * FROM DashboardFileUploadEntity")
    fun getAll(): LiveData<List<DashboardFileUploadEntity>>
}