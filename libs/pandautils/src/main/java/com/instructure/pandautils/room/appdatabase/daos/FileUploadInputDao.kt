package com.instructure.pandautils.room.appdatabase.daos

import androidx.room.*
import com.instructure.pandautils.room.appdatabase.entities.FileUploadInputEntity

@Dao
interface FileUploadInputDao {

    @Insert
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