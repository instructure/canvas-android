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
package com.instructure.teacher.mobius.common.modules

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.utils.DataResult

sealed class ModulesListEvent {
    object PullToRefresh : ModulesListEvent()
    data class ModuleClicked(val moduleItem: ModuleItem) : ModulesListEvent()
    data class DataLoaded(val modulesResult: DataResult<ModuleItem>) : ModulesListEvent()
}

sealed class ModulesListEffect {
    data class ShowModuleDetailView(val moduleItem: ModuleItem) : ModulesListEffect()
    data class LoadData(val canvasContext: CanvasContext, val forceNetwork: Boolean) : ModulesListEffect()
}

data class ModulesListModel(
    val course: CanvasContext,
    val isLoading: Boolean = false,
    val modulesResult: DataResult<ModuleItem>? = null
)