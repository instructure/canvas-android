package com.instructure.pandautils.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.instructure.pandautils.room.entities.FileUploadInput

@Dao
interface FileUploadInputDao {

    @Query("SELECT * FROM FileUploadInput WHERE workerId=:workerId")
    suspend fun findByWorkerId(workerId: String): List<FileUploadInput>
}