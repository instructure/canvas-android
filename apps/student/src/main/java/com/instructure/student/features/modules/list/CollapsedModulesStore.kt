/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.features.modules.list

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.student.util.StudentPrefs

object CollapsedModulesStore {

    fun getCollapsedModuleIds(canvasContext: CanvasContext): Set<Long> {
        val keyName = getKeyName(canvasContext)
        return StudentPrefs.getString(keyName)
                .orEmpty()
                .split('|')
                .mapNotNull { it.toLongOrNull() }
                .toSet()
    }

    fun setCollapsedModuleIds(canvasContext: CanvasContext, moduleIds: Set<Long>) {
        val keyName = getKeyName(canvasContext)
        val joined = moduleIds.joinToString("|")
        StudentPrefs.putString(keyName, joined)
    }

    fun markModuleCollapsed(canvasContext: CanvasContext, moduleId: Long, isCollapsed: Boolean) {
        val collapsedIds = getCollapsedModuleIds(canvasContext).let {
            if (isCollapsed) it + moduleId else it - moduleId
        }
        setCollapsedModuleIds(canvasContext, collapsedIds)
    }

    private fun getKeyName(canvasContext: CanvasContext) = "collapsed_modules_${canvasContext.contextId}"
}