package com.instructure.pandautils.room.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Update
import com.instructure.canvasapi2.db.entities.Author

@Dao
interface AuthorDao {

    @Insert(onConflict = REPLACE)
    suspend fun insert(author: Author)

    @Delete
    suspend fun delete(author: Author)

    @Update
    suspend fun update(author: Author)
}