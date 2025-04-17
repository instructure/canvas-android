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
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.horizon.R
import com.instructure.pandautils.utils.formatDayMonthYear
import com.instructure.pandautils.utils.formatIsoDuration
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModuleHeaderStateMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun mapModuleObjectToHeaderState(
        module: ModuleObject,
        allModules: List<ModuleObject>,
        onClick: (Long) -> Unit,
    ): ModuleHeaderState {
        return ModuleHeaderState(
            title = module.name.orEmpty(),
            status = when {
                module.state == ModuleObject.State.Locked.apiString -> ModuleStatus.LOCKED
                module.state == ModuleObject.State.Completed.apiString -> ModuleStatus.COMPLETED
                module.state == ModuleObject.State.Started.apiString -> ModuleStatus.IN_PROGRESS
                module.items.all { it.completionRequirement == null } -> ModuleStatus.OPTIONAL
                else -> ModuleStatus.NOT_STARTED
            },
            onClick = { onClick(module.id) },
            subtitle = lockedExplanation(module, allModules),
            itemCount = module.items.size,
            pastDueCount = module.items.count { it.overDue },
            remainingMinutes = module.estimatedDuration?.formatIsoDuration(context)
        )
    }

    private fun lockedExplanation(module: ModuleObject, allModules: List<ModuleObject>): String? {
        val unlockDate = module.unlockDate
        if (unlockDate != null && unlockDate > Date()) {
            return context.getString(R.string.moduleWillUnlockExplanation, unlockDate.formatDayMonthYear())
        }

        val prerequisiteIds = module.prerequisiteIds
        if (prerequisiteIds != null && prerequisiteIds.isNotEmpty()) {
            val prerequisiteModules = allModules.filter { prerequisiteIds.contains(it.id) }
            val moduleNames = prerequisiteModules.joinToString(", ") { it.name.orEmpty() }
            return context.getString(R.string.willUnlockAfterCompletingExplanation, moduleNames)
        }

        return null
    }
}