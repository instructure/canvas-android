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
package com.instructure.horizon.data.repository

import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.horizon.database.moduleitem.HorizonDashboardModuleItemDao
import com.instructure.horizon.database.moduleitem.HorizonDashboardModuleItemEntity
import com.instructure.horizon.model.LearningObjectType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class ModuleItemOfflineRepository @Inject constructor(
    private val moduleItemDao: HorizonDashboardModuleItemDao,
) {

    suspend fun getModuleItemsForCourse(courseId: Long): List<ModuleObject> {
        val entity = moduleItemDao.getFirstForCourse(courseId) ?: return emptyList()
        val moduleItem = ModuleItem(
            id = entity.moduleItemId,
            moduleId = 0L,
            title = entity.moduleItemTitle,
            type = entity.moduleItemType,
            quizLti = entity.isQuizLti,
            estimatedDuration = entity.estimatedDuration,
            moduleDetails = entity.dueDateMs?.let { ms ->
                ModuleContentDetails(dueAt = isoFormatter.format(Date(ms)))
            },
        )
        return listOf(ModuleObject(items = listOf(moduleItem)))
    }

    suspend fun saveModuleItem(courseId: Long, modules: List<ModuleObject>) {
        val firstItem = modules.flatMap { it.items }.firstOrNull() ?: return
        val entity = HorizonDashboardModuleItemEntity(
            moduleItemId = firstItem.id,
            courseId = courseId,
            moduleItemTitle = firstItem.title.orEmpty(),
            moduleItemType = if (firstItem.quizLti) LearningObjectType.ASSESSMENT.name
                             else LearningObjectType.fromApiString(firstItem.type.orEmpty()).name,
            dueDateMs = firstItem.moduleDetails?.dueDate?.time,
            estimatedDuration = firstItem.estimatedDuration,
            isQuizLti = firstItem.quizLti,
        )
        moduleItemDao.insertAll(listOf(entity))
    }

    companion object {
        private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+00:00", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
}
