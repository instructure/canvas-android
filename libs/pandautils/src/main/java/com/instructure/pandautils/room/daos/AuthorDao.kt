package com.instructure.pandautils.room.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Update
import com.instructure.pandautils.room.entities.AuthorEntity

@Dao
interface AuthorDao {

    @Insert(onConflict = REPLACE)
    suspend fun insert(author: AuthorEntity)

    @Delete
    suspend fun delete(author: AuthorEntity)

    @Update
    suspend fun update(author: AuthorEntity)
}