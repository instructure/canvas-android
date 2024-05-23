package com.instructure.pandautils.room.offline.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.instructure.pandautils.room.offline.entities.GroupUserEntity

@Dao
interface GroupUserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GroupUserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<GroupUserEntity>): List<Long>

    @Delete
    suspend fun delete(entity: GroupUserEntity)

    @Update
    suspend fun update(entity: GroupUserEntity)

    @Query("SELECT t.groupId FROM GroupUserEntity t WHERE t.userId = :userId")
    suspend fun findByUserId(userId: Long): List<Long>?

}