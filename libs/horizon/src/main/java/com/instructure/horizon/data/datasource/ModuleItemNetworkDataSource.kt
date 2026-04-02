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

import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.horizon.model.DashboardNextModuleItem
import com.instructure.horizon.model.LearningObjectType
import javax.inject.Inject

class ModuleItemNetworkDataSource @Inject constructor(
    private val moduleApi: ModuleAPI.ModuleInterface,
) {

    suspend fun getNextModuleItemForCourse(courseId: Long): DashboardNextModuleItem? {
        val params = RestParams(isForceReadFromNetwork = true)
        val modules = moduleApi.getFirstPageModulesWithItems(
            CanvasContext.Type.COURSE.apiString,
            courseId,
            params,
            includes = listOf("estimated_durations"),
        ).dataOrThrow
        val item = modules.flatMap { it.items }.firstOrNull() ?: return null
        return DashboardNextModuleItem(
            moduleItemId = item.id,
            courseId = courseId,
            title = item.title.orEmpty(),
            type = if (item.quizLti) LearningObjectType.ASSESSMENT
                   else LearningObjectType.fromApiString(item.type.orEmpty()),
            dueDate = item.moduleDetails?.dueDate,
            estimatedDuration = item.estimatedDuration,
            isQuizLti = item.quizLti,
        )
    }
}
