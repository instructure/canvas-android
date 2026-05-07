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
package com.instructure.horizon.domain.usecase

import com.instructure.horizon.database.dao.HorizonCourseSyncPlanDao
import com.instructure.horizon.database.dao.HorizonFileSyncPlanDao
import com.instructure.horizon.database.entity.HorizonCourseSyncPlanEntity
import com.instructure.horizon.database.entity.HorizonFileSyncPlanEntity
import com.instructure.horizon.offline.sync.HorizonProgressState
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

class SaveOfflineSyncPlanUseCase @Inject constructor(
    private val courseSyncPlanDao: HorizonCourseSyncPlanDao,
    private val fileSyncPlanDao: HorizonFileSyncPlanDao,
) : BaseUseCase<SaveOfflineSyncPlanUseCase.Params, Unit>() {

    data class Params(val courses: List<Course>) {
        data class Course(
            val courseId: Long,
            val courseName: String,
            val syncFiles: Boolean,
            val files: List<File>,
        )

        data class File(
            val fileId: Long,
            val fileName: String,
        )
    }

    override suspend fun execute(params: Params) {
        courseSyncPlanDao.deleteAll()
        fileSyncPlanDao.deleteAll()
        for (course in params.courses) {
            courseSyncPlanDao.upsert(
                HorizonCourseSyncPlanEntity(
                    courseId = course.courseId,
                    courseName = course.courseName,
                    syncFiles = course.syncFiles,
                    state = HorizonProgressState.PENDING,
                )
            )
            for (file in course.files) {
                fileSyncPlanDao.upsert(
                    HorizonFileSyncPlanEntity(
                        fileId = file.fileId,
                        courseId = course.courseId,
                        fileName = file.fileName,
                        state = HorizonProgressState.PENDING,
                    )
                )
            }
        }
    }
}
