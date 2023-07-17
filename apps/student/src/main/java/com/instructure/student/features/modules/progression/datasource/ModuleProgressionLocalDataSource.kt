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

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleItemSequence
import com.instructure.canvasapi2.models.ModuleItemWrapper
import com.instructure.canvasapi2.models.Quiz
import com.instructure.pandautils.room.offline.daos.QuizDao
import com.instructure.pandautils.room.offline.facade.ModuleFacade
import com.instructure.student.features.modules.progression.ModuleItemAsset

class ModuleProgressionLocalDataSource(private val moduleFacade: ModuleFacade, private val quizDao: QuizDao) : ModuleProgressionDataSource {
    override suspend fun getAllModuleItems(canvasContext: CanvasContext, moduleId: Long, forceNetwork: Boolean): List<ModuleItem> {
        return moduleFacade.getModuleItems(moduleId)
    }

    override suspend fun getModuleItemSequence(canvasContext: CanvasContext, assetType: String, assetId: String, forceNetwork: Boolean): ModuleItemSequence {
        val moduleItem = when (assetType) {
            ModuleItemAsset.MODULE_ITEM.assetType -> {
                moduleFacade.getModuleItemById(assetId.toLong())
            }
            ModuleItemAsset.PAGE.assetType -> {
                moduleFacade.getModuleItemForPage(assetId)
            }
            else -> {
                moduleFacade.getModuleItemByAssetIdAndType(assetType, assetId.toLong())
            }
        }

        if (moduleItem == null) return ModuleItemSequence()

        val moduleObject = moduleFacade.getModuleObjectById(moduleItem.moduleId)
        val modules = if (moduleObject != null) arrayOf(moduleObject) else emptyArray()

        return ModuleItemSequence(arrayOf(ModuleItemWrapper(current = moduleItem)), modules)
    }

    override suspend fun getDetailedQuiz(url: String, quizId: Long, forceNetwork: Boolean): Quiz {
        val quiz = quizDao.findById(quizId)?.toApiModel()
        return quiz ?: throw IllegalStateException("Quiz not found in database")
    }
}