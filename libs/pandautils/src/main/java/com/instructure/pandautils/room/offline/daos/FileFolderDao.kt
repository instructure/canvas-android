/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 */

package com.instructure.pandautils.room.offline.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.instructure.pandautils.room.offline.entities.FileFolderEntity

@Dao
abstract class FileFolderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(fileFolder: FileFolderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(fileFolders: List<FileFolderEntity>)

    @Query("DELETE FROM FileFolderEntity")
    abstract suspend fun deleteAll()

    @Update
    abstract suspend fun update(fileFolder: FileFolderEntity)

    @Query("SELECT * FROM FileFolderEntity WHERE folderId in (SELECT id FROM FileFolderEntity WHERE contextId = :courseId)")
    abstract suspend fun findAllFilesByCourseId(courseId: Long): List<FileFolderEntity>

    @Query("SELECT * FROM FileFolderEntity WHERE id = :id")
    abstract suspend fun findById(id: Long): FileFolderEntity?

    @Query("SELECT * FROM FileFolderEntity WHERE id IN (:ids)")
    abstract suspend fun findByIds(ids: Set<Long>): List<FileFolderEntity>

    @Query("SELECT * FROM FileFolderEntity WHERE parentFolderId = :parentId")
    abstract suspend fun findFoldersByParentId(parentId: Long): List<FileFolderEntity>

    @Query("SELECT * FROM FileFolderEntity WHERE folderId = :folderId")
    abstract suspend fun findFilesByFolderId(folderId: Long): List<FileFolderEntity>

    @Query("SELECT * FROM FileFolderEntity WHERE contextId = :contextId AND parentFolderId = 0")
    abstract suspend fun findRootFolderForContext(contextId: Long): FileFolderEntity?

    @Transaction
    open suspend fun replaceAll(fileFolders: List<FileFolderEntity>) {
        deleteAll()
        insertAll(fileFolders)
    }

    @Query("SELECT * FROM FileFolderEntity WHERE folderId in (SELECT id FROM FileFolderEntity WHERE contextId = :courseId)" +
            " AND (updatedDate > IFNULL((SELECT createdDate FROM LocalFileEntity WHERE id = FileFolderEntity.id), 0) OR createdDate > IFNULL((SELECT createdDate FROM LocalFileEntity WHERE id = FileFolderEntity.id), 0))")
    abstract suspend fun findFilesToSyncFull(courseId: Long): List<FileFolderEntity>

    @Query("SELECT * FROM FileFolderEntity WHERE folderId in (SELECT id FROM FileFolderEntity WHERE contextId = :courseId)" +
            " AND (updatedDate > IFNULL((SELECT createdDate FROM LocalFileEntity WHERE id = FileFolderEntity.id), 0) OR createdDate > IFNULL((SELECT createdDate FROM LocalFileEntity WHERE id = FileFolderEntity.id), 0))" +
            " AND id in (SELECT id FROM FileSyncSettingsEntity WHERE courseId = :courseId)")
    abstract suspend fun findFilesToSyncSelected(courseId: Long): List<FileFolderEntity>

    suspend fun findFilesToSync(courseId: Long, fullSync: Boolean): List<FileFolderEntity> {
        return if (fullSync) {
            findFilesToSyncFull(courseId)
        } else {
            findFilesToSyncSelected(courseId)
        }
    }
}