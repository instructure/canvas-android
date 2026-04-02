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

import com.instructure.horizon.database.dao.HorizonDashboardModuleItemDao
import com.instructure.horizon.database.entity.HorizonDashboardModuleItemEntity
import com.instructure.horizon.model.DashboardNextModuleItem
import com.instructure.horizon.model.LearningObjectType
import java.util.Date
import javax.inject.Inject

class ModuleItemLocalDataSource @Inject constructor(
    private val moduleItemDao: HorizonDashboardModuleItemDao,
) {

    suspend fun getNextModuleItemForCourse(courseId: Long): DashboardNextModuleItem? {
        val entity = moduleItemDao.getFirstForCourse(courseId) ?: return null
        return DashboardNextModuleItem(
            moduleItemId = entity.moduleItemId,
            courseId = entity.courseId,
            title = entity.moduleItemTitle,
            type = LearningObjectType.valueOf(entity.moduleItemType),
            dueDate = entity.dueDateMs?.let { Date(it) },
            estimatedDuration = entity.estimatedDuration,
            isQuizLti = entity.isQuizLti,
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
