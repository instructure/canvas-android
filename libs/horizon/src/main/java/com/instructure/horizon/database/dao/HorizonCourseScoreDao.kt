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
import com.instructure.horizon.database.entity.HorizonCourseAssignmentEntity
import com.instructure.horizon.database.entity.HorizonCourseAssignmentGroupEntity
import com.instructure.horizon.database.entity.HorizonCourseGradeEntity

@Dao
interface HorizonCourseScoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<HorizonCourseAssignmentGroupEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<HorizonCourseAssignmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: HorizonCourseGradeEntity)

    @Query("SELECT * FROM horizon_course_assignment_groups WHERE courseId = :courseId")
    suspend fun getGroupsForCourse(courseId: Long): List<HorizonCourseAssignmentGroupEntity>

    @Query("SELECT * FROM horizon_course_assignments WHERE groupId = :groupId")
    suspend fun getAssignmentsForGroup(groupId: Long): List<HorizonCourseAssignmentEntity>

    @Query("SELECT * FROM horizon_course_grades WHERE courseId = :courseId")
    suspend fun getGradeForCourse(courseId: Long): HorizonCourseGradeEntity?

    @Query("DELETE FROM horizon_course_assignment_groups WHERE courseId = :courseId")
    suspend fun deleteGroupsForCourse(courseId: Long)

    @Query("DELETE FROM horizon_course_assignments WHERE courseId = :courseId")
    suspend fun deleteAssignmentsForCourse(courseId: Long)

    @Query("DELETE FROM horizon_course_grades WHERE courseId = :courseId")
    suspend fun deleteGradeForCourse(courseId: Long)

    @Transaction
    suspend fun replaceForCourse(
        courseId: Long,
        groups: List<HorizonCourseAssignmentGroupEntity>,
        assignments: List<HorizonCourseAssignmentEntity>,
        grade: HorizonCourseGradeEntity?,
    ) {
        deleteAssignmentsForCourse(courseId)
        deleteGroupsForCourse(courseId)
        deleteGradeForCourse(courseId)
        insertGroups(groups)
        insertAssignments(assignments)
        grade?.let { insertGrade(it) }
    }
}
