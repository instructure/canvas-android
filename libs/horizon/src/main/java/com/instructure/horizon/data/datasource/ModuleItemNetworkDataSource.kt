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
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleItemSequence
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.horizon.model.DashboardNextModuleItem
import com.instructure.horizon.model.LearningObjectType
import okhttp3.ResponseBody
import javax.inject.Inject

class ModuleItemNetworkDataSource @Inject constructor(
    private val moduleApi: ModuleAPI.ModuleInterface,
) {

    suspend fun getNextModuleItemForCourse(courseId: Long, forceRefresh: Boolean): DashboardNextModuleItem? {
        val params = RestParams(
            usePerPageQueryParam = true,
            isForceReadFromNetwork = forceRefresh,
        )
        val modules = moduleApi.getFirstPageModulesWithItems(
            CanvasContext.Type.COURSE.apiString,
            courseId,
            params,
            includes = listOf("estimated_durations"),
        ).depaginate { moduleApi.getNextPageModuleObjectList(it, params) }.dataOrThrow

        val item = modules.firstNotNullOfOrNull { module ->
            module.items.firstOrNull { it.completionRequirement?.completed == false }
                ?: if (module.itemCount > module.items.size) {
                    getAllModuleItems(courseId, module.id, params)
                        .firstOrNull { it.completionRequirement?.completed == false }
                } else null
        } ?: return null

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

    private suspend fun getAllModuleItems(
        courseId: Long,
        moduleId: Long,
        params: RestParams,
    ): List<ModuleItem> {
        return moduleApi.getFirstPageModuleItems(
            CanvasContext.Type.COURSE.apiString,
            courseId,
            moduleId,
            params,
            includes = listOf("estimated_durations"),
        ).depaginate { moduleApi.getNextPageModuleItemList(it, params) }.dataOrThrow
    }

    suspend fun getModuleItemSequence(courseId: Long, assetType: String, assetId: String): ModuleItemSequence {
        return moduleApi.getModuleItemSequence(
            CanvasContext.Type.COURSE.apiString,
            courseId,
            assetType,
            assetId,
            RestParams(isForceReadFromNetwork = true),
        ).dataOrThrow
    }

    suspend fun getModuleItem(courseId: Long, moduleId: Long, moduleItemId: Long): ModuleItem {
        return moduleApi.getModuleItem(
            CanvasContext.Type.COURSE.apiString,
            courseId,
            moduleId,
            moduleItemId,
            RestParams(isForceReadFromNetwork = true),
        ).dataOrThrow
    }

    suspend fun markAsDone(courseId: Long, moduleItem: ModuleItem): DataResult<ResponseBody> {
        return markAsDone(courseId, moduleItem.moduleId, moduleItem.id)
    }

    suspend fun markAsNotDone(courseId: Long, moduleItem: ModuleItem): DataResult<ResponseBody> {
        return markAsNotDone(courseId, moduleItem.moduleId, moduleItem.id)
    }

    suspend fun markAsDone(
        courseId: Long,
        moduleId: Long,
        itemId: Long,
        params: RestParams = RestParams(),
    ): DataResult<ResponseBody> {
        return moduleApi.markModuleItemAsDone(
            CanvasContext.Type.COURSE.apiString,
            courseId,
            moduleId,
            itemId,
            params,
        )
    }

    suspend fun markAsNotDone(
        courseId: Long,
        moduleId: Long,
        itemId: Long,
        params: RestParams = RestParams(),
    ): DataResult<ResponseBody> {
        return moduleApi.markModuleItemAsNotDone(
            CanvasContext.Type.COURSE.apiString,
            courseId,
            moduleId,
            itemId,
            params,
        )
    }

    suspend fun markAsRead(
        courseId: Long,
        moduleId: Long,
        itemId: Long,
        params: RestParams = RestParams(isForceReadFromNetwork = true),
    ): DataResult<ResponseBody> {
        return moduleApi.markModuleItemRead(
            CanvasContext.Type.COURSE.apiString,
            courseId,
            moduleId,
            itemId,
            params,
        )
    }
}
