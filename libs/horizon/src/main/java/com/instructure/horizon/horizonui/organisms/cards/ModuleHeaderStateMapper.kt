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
package com.instructure.horizon.horizonui.organisms.cards

import android.content.Context
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.horizon.R
import com.instructure.pandautils.utils.formatDayMonthYear
import com.instructure.pandautils.utils.formatMinuteDuration
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject

class ModuleHeaderStateMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun mapModuleObjectToHeaderState(
        module: ModuleObject,
        allModules: List<ModuleObject>,
        onClick: (Long) -> Unit,
    ): ModuleHeaderState {
        val status = when {
            module.items.all { it.completionRequirement == null } -> ModuleStatus.OPTIONAL
            module.state == ModuleObject.State.Locked.apiString -> ModuleStatus.LOCKED
            module.state == ModuleObject.State.Completed.apiString -> ModuleStatus.COMPLETED
            module.state == ModuleObject.State.Started.apiString -> ModuleStatus.IN_PROGRESS
            else -> ModuleStatus.NOT_STARTED
        }
        val filteredItems = module.items.filter {
            it.type == ModuleItem.Type.Assignment.name
                    || it.type == ModuleItem.Type.Page.name
                    || it.type == ModuleItem.Type.ExternalTool.name
                    || it.type == ModuleItem.Type.ExternalUrl.name
                    || it.type == ModuleItem.Type.File.name
                    || it.type == ModuleItem.Type.Quiz.name

        }
        return ModuleHeaderState(
            title = module.name.orEmpty(),
            status = status,
            expanded = status == ModuleStatus.IN_PROGRESS,
            onClick = { onClick(module.id) },
            subtitle = getModuleSubtitle(module, allModules),
            itemCount = filteredItems.size,
            pastDueCount = filteredItems.count { it.overDue },
            remainingMinutes = module.estimatedDuration?.formatMinuteDuration(context)
        )
    }

    private fun getModuleSubtitle(module: ModuleObject, allModules: List<ModuleObject>): String? {
        return lockedExplanation(module, allModules) ?: requirementExplanation(module)
    }

    private fun requirementExplanation(module: ModuleObject): String? {
        if (module.items.all { it.completionRequirement != null }) {
            return context.getString(R.string.moduleItemCompleteAllItems)
        }

        if (module.items.all { it.completionRequirement == null }) {
            return null
        }

        if (module.items.any { it.completionRequirement == null }) {
            return context.getString(R.string.moduleItemCompleteAllOfTheRequiredItems)
        }

        return null
    }

    private fun lockedExplanation(module: ModuleObject, allModules: List<ModuleObject>): String? {
        if (module.state != ModuleObject.State.Locked.apiString) {
            return null
        }

        val unlockDate = module.unlockDate
        if (unlockDate != null && unlockDate > Date()) {
            return context.getString(R.string.moduleWillUnlockExplanation, unlockDate.formatDayMonthYear())
        }

        val prerequisiteIds = module.prerequisiteIds
        if (prerequisiteIds != null && prerequisiteIds.isNotEmpty()) {
            val prerequisiteModules = allModules.filter { prerequisiteIds.contains(it.id) }
            val moduleNames = prerequisiteModules.joinToString { it.name.orEmpty() }
            return context.getString(R.string.prerequisiteExplanation, moduleNames)
        }

        return null
    }
}