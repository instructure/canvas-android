package com.instructure.pandautils.room.daos

import androidx.room.*
import com.instructure.canvasapi2.db.entities.FileUploadInput

@Dao
interface FileUploadInputDao {

    @Insert
    suspend fun insert(fileUploadInput: FileUploadInput)

    @Delete
    suspend fun delete(fileUploadInput: FileUploadInput)

    @Update
    suspend fun update(fileUploadInput: FileUploadInput)

    @Query("SELECT * FROM FileUploadInput WHERE workerId=:workerId")
    suspend fun findByWorkerId(workerId: String): FileUploadInput?
}