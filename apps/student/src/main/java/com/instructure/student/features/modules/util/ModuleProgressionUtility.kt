/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
package com.instructure.student.features.modules.util

import android.content.Context
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.student.fragment.CourseModuleProgressionFragment.Companion.shouldAddModuleItem
import java.util.*

object ModuleProgressionUtility {
    fun prepareModulesForCourseProgression(
        context: Context,
        moduleItemId: Long,
        modules: ArrayList<ModuleObject>,
        moduleItems: ArrayList<ArrayList<ModuleItem>>
    ): ModuleHelper {
        // We want to give CourseModuleProgressionFragment an arrayList without SubHeaders and ExternalTool moduleItems.
        // We currently don't display them and there isn't a good way to just skip over them during the progression.
        // This makes it easier to keep track of which item we're on in the progression without having to do lots of
        // checks/math to account for skipping over sub-headers and external tools.

        // Remove all the subHeaders and external tools items from the children list. We won't display them in module progression
        val headerlessItems = ArrayList<ArrayList<ModuleItem>>()
        for (i in modules.indices) {
            headerlessItems.add(ArrayList())
            for (k in moduleItems[i].indices) {
                if (shouldAddModuleItem(context, moduleItems[i][k])) {
                    headerlessItems[i].add(moduleItems[i][k])
                }
            }
        }

        // At this point it's possible that we removed some sub-headers or external tools from the arrayList,
        // which could throw off the groupPosition and childPosition. This will get the new, correct group
        // and child position of the module item
        var newGroupPos = 0
        var newChildPos = 0
        for (i in headerlessItems.indices) {
            for (k in headerlessItems[i].indices) {
                if (moduleItemId == headerlessItems[i][k].id) {
                    newGroupPos = i
                    newChildPos = k
                    break
                }
            }
        }
        return ModuleHelper(headerlessItems, newGroupPos, newChildPos)
    }

    data class ModuleHelper(
        var strippedModuleItems: ArrayList<ArrayList<ModuleItem>>,
        var newGroupPosition: Int = 0,
        var newChildPosition: Int = 0
    )
}
