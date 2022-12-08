package com.instructure.pandautils.room.daos

import androidx.room.*
import com.instructure.pandautils.room.entities.FileUploadInputEntity

@Dao
interface FileUploadInputDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fileUploadInput: FileUploadInputEntity)

    @Delete
    suspend fun delete(fileUploadInput: FileUploadInputEntity)

    @Delete
    suspend fun deleteAll(fileUploadInputs: List<FileUploadInputEntity>)

    @Update
    suspend fun update(fileUploadInput: FileUploadInputEntity)

    @Query("SELECT * FROM FileUploadInputEntity WHERE workerId=:workerId")
    suspend fun findByWorkerId(workerId: String): FileUploadInputEntity?
}