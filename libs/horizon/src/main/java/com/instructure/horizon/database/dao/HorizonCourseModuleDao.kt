/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.horizon.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.instructure.horizon.database.entity.HorizonCourseModuleEntity
import com.instructure.horizon.database.entity.HorizonCourseModuleItemEntity

@Dao
interface HorizonCourseModuleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModules(modules: List<HorizonCourseModuleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<HorizonCourseModuleItemEntity>)

    @Query("SELECT * FROM horizon_course_modules WHERE courseId = :courseId ORDER BY position")
    suspend fun getModulesForCourse(courseId: Long): List<HorizonCourseModuleEntity>

    @Query("SELECT * FROM horizon_course_module_items WHERE moduleId = :moduleId ORDER BY position")
    suspend fun getItemsForModule(moduleId: Long): List<HorizonCourseModuleItemEntity>

    @Query("SELECT * FROM horizon_course_module_items WHERE courseId = :courseId ORDER BY moduleId, position")
    suspend fun getItemsForCourse(courseId: Long): List<HorizonCourseModuleItemEntity>

    @Query("SELECT * FROM horizon_course_module_items WHERE courseId = :courseId ORDER BY moduleId, position LIMIT 1")
    suspend fun getNextModuleItemForCourse(courseId: Long): HorizonCourseModuleItemEntity?

    @Query("SELECT * FROM horizon_course_module_items WHERE itemId = :itemId LIMIT 1")
    suspend fun getItemById(itemId: Long): HorizonCourseModuleItemEntity?

    @Query("DELETE FROM horizon_course_modules WHERE courseId = :courseId")
    suspend fun deleteModulesForCourse(courseId: Long)

    @Query("DELETE FROM horizon_course_module_items WHERE courseId = :courseId")
    suspend fun deleteItemsForCourse(courseId: Long)

    @Transaction
    suspend fun replaceForCourse(
        courseId: Long,
        modules: List<HorizonCourseModuleEntity>,
        items: List<HorizonCourseModuleItemEntity>,
    ) {
        deleteItemsForCourse(courseId)
        deleteModulesForCourse(courseId)
        insertModules(modules)
        insertItems(items)
    }
}
