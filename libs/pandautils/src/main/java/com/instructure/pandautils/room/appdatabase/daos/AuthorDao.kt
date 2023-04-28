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
 */

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