/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.moduleitemsequence

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleItemSequence
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import okhttp3.ResponseBody
import javax.inject.Inject

class ModuleItemSequenceRepository @Inject constructor(
    private val moduleApi: ModuleAPI.ModuleInterface,
    private val assignmentApi: AssignmentAPI.AssignmentInterface
) {

    suspend fun getModuleItemSequence(courseId: Long, assetType: String, assetId: String): ModuleItemSequence {
        val params = RestParams(isForceReadFromNetwork = true)
        return moduleApi.getModuleItemSequence(
            CanvasContext.Type.COURSE.apiString,
            courseId,
            assetType,
            assetId,
            params
        ).dataOrThrow
    }

    suspend fun getModulesWithItems(courseId: Long): List<ModuleObject> {
        val params = RestParams(isForceReadFromNetwork = true)
        val modules = moduleApi.getFirstPageModulesWithItems(
            CanvasContext.Type.COURSE.apiString,
            courseId,
            params,
            includes = listOf("estimated_durations")
        ).depaginate {
            moduleApi.getNextPageModuleObjectList(it, params)
        }.dataOrThrow

        return modules.map {
            if (it.itemCount != it.items.size) {
                it.copy(items = getAllModuleItems(courseId, it.id))
            } else {
                it
            }
        }
    }

    private suspend fun getAllModuleItems(courseId: Long, moduleId: Long): List<ModuleItem> {
        val params = RestParams()
        return moduleApi.getFirstPageModuleItems(
            CanvasContext.Type.COURSE.apiString,
            courseId,
            moduleId,
            params,
            includes = listOf("estimated_durations")
        ).depaginate {
            moduleApi.getNextPageModuleItemList(it, params)
        }.dataOrThrow
    }

    suspend fun getModuleItem(courseId: Long, moduleId: Long, moduleItemId: Long): ModuleItem {
        val params = RestParams(isForceReadFromNetwork = true)
        return moduleApi.getModuleItem(
            CanvasContext.Type.COURSE.apiString,
            courseId,
            moduleId,
            moduleItemId,
            params
        ).dataOrThrow
    }

    suspend fun markAsNotDone(courseId: Long, moduleItem: ModuleItem): DataResult<ResponseBody> {
        return moduleApi.markModuleItemAsNotDone(
            CanvasContext.Type.COURSE.apiString,
            courseId,
            moduleItem.moduleId,
            moduleItem.id,
            RestParams()
        )
    }

    suspend fun markAsDone(courseId: Long, moduleItem: ModuleItem): DataResult<ResponseBody> {
        return moduleApi.markModuleItemAsDone(
            CanvasContext.Type.COURSE.apiString,
            courseId,
            moduleItem.moduleId,
            moduleItem.id,
            RestParams()
        )
    }

    suspend fun markAsRead(courseId: Long, moduleId: Long, itemId: Long) {
        val restParams = RestParams(isForceReadFromNetwork = true)
        moduleApi.markModuleItemRead(CanvasContext.Type.COURSE.apiString, courseId, moduleId, itemId, restParams)
    }

    suspend fun getAssignment(assignmentId: Long, courseId: Long, forceNetwork: Boolean): Assignment {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return assignmentApi.getAssignmentWithHistory(courseId, assignmentId, params).dataOrThrow
    }
}