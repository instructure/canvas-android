/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.canvas.espresso.mockcanvas.fakes

import com.instructure.canvasapi2.managers.graphql.ModuleItemCheckpoint
import com.instructure.canvasapi2.managers.graphql.ModuleItemWithCheckpoints
import com.instructure.canvasapi2.managers.graphql.ModuleManager

class FakeModuleManager : ModuleManager {

    var checkpointsMap: Map<String, List<ModuleItemWithCheckpoints>> = emptyMap()

    override suspend fun getModuleItemCheckpoints(courseId: String, forceNetwork: Boolean): List<ModuleItemWithCheckpoints> {
        return checkpointsMap[courseId] ?: emptyList()
    }

    fun setCheckpoints(courseId: String, moduleItemId: String, checkpoints: List<ModuleItemCheckpoint>) {
        val moduleItemWithCheckpoints = ModuleItemWithCheckpoints(
            moduleItemId = moduleItemId,
            checkpoints = checkpoints
        )
        checkpointsMap = checkpointsMap + (courseId to listOf(moduleItemWithCheckpoints))
    }
}