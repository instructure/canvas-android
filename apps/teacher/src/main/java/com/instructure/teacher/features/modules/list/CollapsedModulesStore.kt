/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.teacher.features.modules.list

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.teacher.utils.TeacherPrefs

object CollapsedModulesStore {

    fun getCollapsedModuleIds(canvasContext: CanvasContext): Set<Long> {
        val keyName = getKeyName(canvasContext)
        return TeacherPrefs.getString(keyName)
            .orEmpty()
            .split('|')
            .mapNotNull { it.toLongOrNull() }
            .toSet()
    }

    fun setCollapsedModuleIds(canvasContext: CanvasContext, moduleIds: Set<Long>) {
        val keyName = getKeyName(canvasContext)
        val joined = moduleIds.joinToString("|")
        TeacherPrefs.putString(keyName, joined)
    }

    fun markModuleCollapsed(canvasContext: CanvasContext, moduleId: Long, isCollapsed: Boolean) {
        val collapsedIds = getCollapsedModuleIds(canvasContext).let {
            if (isCollapsed) it + moduleId else it - moduleId
        }
        setCollapsedModuleIds(canvasContext, collapsedIds)
    }

    private fun getKeyName(canvasContext: CanvasContext) = "collapsed_modules_${canvasContext.contextId}"

}
