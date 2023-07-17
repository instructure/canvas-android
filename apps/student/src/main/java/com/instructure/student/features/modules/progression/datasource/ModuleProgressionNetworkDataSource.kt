/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.features.modules.progression.datasource

import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.apis.QuizAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleItemSequence
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import okhttp3.ResponseBody

class ModuleProgressionNetworkDataSource(private val moduleApi: ModuleAPI.ModuleInterface, private val quizApi: QuizAPI.QuizInterface) : ModuleProgressionDataSource {
    override suspend fun getAllModuleItems(canvasContext: CanvasContext, moduleId: Long, forceNetwork: Boolean): List<ModuleItem> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return moduleApi.getFirstPageModuleItems(canvasContext.apiContext(), canvasContext.id, moduleId, params).depaginate {
            moduleApi.getNextPageModuleItemList(it, params)
        }.dataOrThrow
    }

    override suspend fun getModuleItemSequence(canvasContext: CanvasContext, assetType: String, assetId: String, forceNetwork: Boolean): ModuleItemSequence {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return moduleApi.getModuleItemSequence(canvasContext.apiContext(), canvasContext.id, assetType, assetId, params).dataOrThrow
    }

    override suspend fun getDetailedQuiz(url: String, quizId: Long, forceNetwork: Boolean): Quiz {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return quizApi.getDetailedQuizByUrl(url, params).dataOrThrow
    }

    suspend fun markAsNotDone(canvasContext: CanvasContext, moduleItem: ModuleItem): DataResult<ResponseBody> {
        return moduleApi.markModuleItemAsNotDone(canvasContext.apiContext(), canvasContext.id, moduleItem.moduleId, moduleItem.id, RestParams())
    }

    suspend fun markAsDone(canvasContext: CanvasContext, moduleItem: ModuleItem): DataResult<ResponseBody> {
        return moduleApi.markModuleItemAsDone(canvasContext.apiContext(), canvasContext.id, moduleItem.moduleId, moduleItem.id, RestParams())
    }

    suspend fun markAsRead(canvasContext: CanvasContext, moduleItem: ModuleItem): DataResult<ResponseBody> {
        return moduleApi.markModuleItemRead(canvasContext.apiContext(), canvasContext.id, moduleItem.moduleId, moduleItem.id, RestParams())
    }
}