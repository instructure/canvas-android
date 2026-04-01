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
package com.instructure.horizon.data.datasource

import com.instructure.canvasapi2.GetCoursesQuery
import com.instructure.canvasapi2.type.EnrollmentWorkflowState
import com.instructure.horizon.database.dao.HorizonDashboardCourseDao
import com.instructure.horizon.database.dao.HorizonSyncMetadataDao
import com.instructure.horizon.database.entity.HorizonDashboardCourseEntity
import com.instructure.horizon.database.entity.HorizonSyncMetadataEntity
import javax.inject.Inject

class CourseEnrollmentLocalDataSource @Inject constructor(
    private val courseDao: HorizonDashboardCourseDao,
    private val syncMetadataDao: HorizonSyncMetadataDao,
) {

    suspend fun getEnrollments(): List<GetCoursesQuery.Enrollment> {
        return courseDao.getAll().map { entity ->
            GetCoursesQuery.Enrollment(
                id = entity.enrollmentId.toString(),
                state = EnrollmentWorkflowState.safeValueOf(entity.enrollmentState),
                lastActivityAt = null,
                course = GetCoursesQuery.Course(
                    id = entity.courseId.toString(),
                    name = entity.courseName,
                    image_download_url = entity.courseImageUrl,
                    syllabus_body = null,
                    account = null,
                    usersConnection = GetCoursesQuery.UsersConnection(
                        nodes = listOf(
                            GetCoursesQuery.Node(
                                courseProgression = GetCoursesQuery.CourseProgression(
                                    requirements = GetCoursesQuery.Requirements(
                                        completionPercentage = entity.completionPercentage,
                                    ),
                                    incompleteModulesConnection = null,
                                )
                            )
                        )
                    ),
                )
            )
        }
    }

    suspend fun saveEnrollments(enrollments: List<GetCoursesQuery.Enrollment>) {
        val entities = enrollments.mapNotNull { enrollment ->
            val course = enrollment.course ?: return@mapNotNull null
            val completionPercentage = course.usersConnection?.nodes
                ?.firstOrNull()?.courseProgression?.requirements?.completionPercentage ?: 0.0
            HorizonDashboardCourseEntity(
                enrollmentId = enrollment.id?.toLongOrNull() ?: return@mapNotNull null,
                courseId = course.id.toLongOrNull() ?: return@mapNotNull null,
                courseName = course.name,
                courseImageUrl = course.image_download_url,
                completionPercentage = completionPercentage,
                enrollmentState = enrollment.state.rawValue,
            )
        }
        courseDao.replaceAll(entities)
        syncMetadataDao.upsert(
            HorizonSyncMetadataEntity(
                key = HorizonSyncMetadataEntity.KEY_DASHBOARD_COURSES,
                lastSyncedAtMs = System.currentTimeMillis(),
            )
        )
    }
}
