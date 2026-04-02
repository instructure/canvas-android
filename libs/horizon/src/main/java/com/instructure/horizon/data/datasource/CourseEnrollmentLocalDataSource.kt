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

import com.instructure.canvasapi2.managers.graphql.horizon.DashboardEnrollment
import com.instructure.horizon.database.dao.HorizonDashboardEnrollmentDao
import com.instructure.horizon.database.dao.HorizonSyncMetadataDao
import com.instructure.horizon.database.entity.HorizonDashboardEnrollmentEntity
import com.instructure.horizon.database.entity.HorizonSyncMetadataEntity
import javax.inject.Inject

class CourseEnrollmentLocalDataSource @Inject constructor(
    private val enrollmentDao: HorizonDashboardEnrollmentDao,
    private val syncMetadataDao: HorizonSyncMetadataDao,
) {

    suspend fun getEnrollments(): List<DashboardEnrollment> {
        return enrollmentDao.getAll().map { entity ->
            DashboardEnrollment(
                enrollmentId = entity.enrollmentId,
                enrollmentState = entity.enrollmentState,
                courseId = entity.courseId,
                courseName = entity.courseName,
                courseImageUrl = entity.courseImageUrl,
                courseSyllabus = entity.courseSyllabus,
                institutionName = entity.institutionName,
                completionPercentage = entity.completionPercentage,
            )
        }
    }

    suspend fun saveEnrollments(enrollments: List<DashboardEnrollment>) {
        val entities = enrollments.map { enrollment ->
            HorizonDashboardEnrollmentEntity(
                enrollmentId = enrollment.enrollmentId,
                enrollmentState = enrollment.enrollmentState,
                courseId = enrollment.courseId,
                courseName = enrollment.courseName,
                courseImageUrl = enrollment.courseImageUrl,
                courseSyllabus = enrollment.courseSyllabus,
                institutionName = enrollment.institutionName,
                completionPercentage = enrollment.completionPercentage,
            )
        }
        enrollmentDao.replaceAll(entities)
        syncMetadataDao.upsert(
            HorizonSyncMetadataEntity(
                key = HorizonSyncMetadataEntity.KEY_DASHBOARD_ENROLLMENTS,
                lastSyncedAtMs = System.currentTimeMillis(),
            )
        )
    }

    suspend fun getAllCourseIds(): List<Long> = enrollmentDao.getAllCourseIds()
}
