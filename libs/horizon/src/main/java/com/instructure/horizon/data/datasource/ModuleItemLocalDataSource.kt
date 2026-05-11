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

import com.instructure.canvasapi2.utils.toDate
import com.instructure.horizon.database.dao.HorizonCourseModuleDao
import com.instructure.horizon.database.dao.HorizonDashboardModuleItemDao
import com.instructure.horizon.database.entity.HorizonDashboardModuleItemEntity
import com.instructure.horizon.model.DashboardNextModuleItem
import com.instructure.horizon.model.LearningObjectType
import java.util.Date
import javax.inject.Inject

class ModuleItemLocalDataSource @Inject constructor(
    private val moduleItemDao: HorizonDashboardModuleItemDao,
    private val courseModuleDao: HorizonCourseModuleDao,
) {

    suspend fun getNextModuleItemForCourse(courseId: Long): DashboardNextModuleItem? {
        val courseModuleItem = courseModuleDao.getNextModuleItemForCourse(courseId)
        if (courseModuleItem != null) {
            return DashboardNextModuleItem(
                moduleItemId = courseModuleItem.itemId,
                courseId = courseModuleItem.courseId,
                title = courseModuleItem.title.orEmpty(),
                type = if (courseModuleItem.quizLti) LearningObjectType.ASSESSMENT
                       else LearningObjectType.fromApiString(courseModuleItem.type.orEmpty()),
                dueDate = courseModuleItem.dueAt?.toDate(),
                estimatedDuration = courseModuleItem.estimatedDuration,
                isQuizLti = courseModuleItem.quizLti,
            )
        }

        val dashboardItem = moduleItemDao.getFirstForCourse(courseId) ?: return null
        return DashboardNextModuleItem(
            moduleItemId = dashboardItem.moduleItemId,
            courseId = dashboardItem.courseId,
            title = dashboardItem.moduleItemTitle,
            type = LearningObjectType.valueOf(dashboardItem.moduleItemType),
            dueDate = dashboardItem.dueDateMs?.let { Date(it) },
            estimatedDuration = dashboardItem.estimatedDuration,
            isQuizLti = dashboardItem.isQuizLti,
        )
    }

    suspend fun saveNextModuleItem(item: DashboardNextModuleItem) {
        val entity = HorizonDashboardModuleItemEntity(
            moduleItemId = item.moduleItemId,
            courseId = item.courseId,
            moduleItemTitle = item.title,
            moduleItemType = item.type.name,
            dueDateMs = item.dueDate?.time,
            estimatedDuration = item.estimatedDuration,
            isQuizLti = item.isQuizLti,
        )
        moduleItemDao.replaceForCourse(entity)
    }
}
