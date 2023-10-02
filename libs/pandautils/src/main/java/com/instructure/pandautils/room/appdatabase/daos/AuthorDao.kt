package com.instructure.pandautils.room.appdatabase.daos

import androidx.room.*
import com.instructure.pandautils.room.appdatabase.entities.AuthorEntity

@Dao
interface AuthorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(author: AuthorEntity)

    @Delete
    suspend fun delete(author: AuthorEntity)

    @Update
    suspend fun update(author: AuthorEntity)
}