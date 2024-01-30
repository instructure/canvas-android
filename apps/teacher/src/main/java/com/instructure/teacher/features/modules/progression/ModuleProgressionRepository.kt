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

package com.instructure.teacher.features.modules.progression

import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleItemSequence
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.depaginate

class ModuleProgressionRepository(
    private val moduleApi: ModuleAPI.ModuleInterface
) {

    //See com.instructure.teacher.features.modules.list.ModuleListEffectHandler.fetchPageData
    suspend fun getModulesWithItems(canvasContext: CanvasContext): List<ModuleObject> {
        val params = RestParams()
        val modules = moduleApi.getFirstPageModulesWithItems(canvasContext.apiContext(), canvasContext.id, params).depaginate {
            moduleApi.getNextPageModuleObjectList(it, params)
        }.dataOrThrow

        return modules.map {
            if (it.itemCount != it.items.size) {
                it.copy(items = getAllModuleItems(canvasContext, it.id))
            } else {
                it
            }
        }
    }

    private suspend fun getAllModuleItems(canvasContext: CanvasContext, moduleId: Long): List<ModuleItem> {
        val params = RestParams()
        return moduleApi.getFirstPageModuleItems(canvasContext.apiContext(), canvasContext.id, moduleId, params).depaginate {
            moduleApi.getNextPageModuleItemList(it, params)
        }.dataOrThrow
    }

    suspend fun getModuleItemSequence(canvasContext: CanvasContext, assetType: String, assetId: String): ModuleItemSequence {
        val params = RestParams(isForceReadFromNetwork = true)
        return moduleApi.getModuleItemSequence(canvasContext.apiContext(), canvasContext.id, assetType, assetId, params).dataOrThrow
    }
}
