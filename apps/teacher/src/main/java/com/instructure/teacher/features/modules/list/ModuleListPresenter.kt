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

import android.content.Context
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.tryOrNull
import com.instructure.pandautils.utils.DP
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.color
import com.instructure.teacher.R
import com.instructure.teacher.features.modules.list.ui.ModuleListItemData
import com.instructure.teacher.features.modules.list.ui.ModuleListViewState
import com.instructure.teacher.mobius.common.ui.Presenter

object ModuleListPresenter : Presenter<ModuleListModel, ModuleListViewState> {

    override fun present(model: ModuleListModel, context: Context): ModuleListViewState {

        val items = mutableListOf<ModuleListItemData>()

        val indentWidth = context.DP(10).toInt()

        val courseColor = model.course.color

        items += model.modules.map { module ->
            val moduleItems = module.items.map { item ->
                if (item.type.equals(ModuleItem.Type.SubHeader.name, ignoreCase = true)) {
                    ModuleListItemData.ModuleItemData(
                        item.id,
                        null,
                        item.title,
                        null,
                        item.published,
                        item.indent * indentWidth,
                        0
                    )
                } else {
                    createModuleItemData(item, context, indentWidth, courseColor)
                }
            }
            ModuleListItemData.ModuleData(
                module.id,
                module.name.orEmpty(),
                module.published,
                moduleItems
            )
        }

        if (model.pageData.lastPageResult?.isFail == true) {
            if (model.modules.isEmpty()) {
                items += ModuleListItemData.FullError(ThemePrefs.buttonColor)
            } else {
                items += ModuleListItemData.InlineError(ThemePrefs.buttonColor)
            }
        } else if (!model.isLoading && !model.pageData.hasMorePages && model.modules.isEmpty()) {
            items += ModuleListItemData.Empty
        }

        if (model.modules.isNotEmpty() && model.pageData.hasMorePages) {
            items += ModuleListItemData.Loading
        }

        val collapsedModuleIds = CollapsedModulesStore.getCollapsedModuleIds(model.course)

        return ModuleListViewState(
            showRefreshing = model.isLoading && model.modules.isEmpty(),
            items = items,
            collapsedModuleIds = collapsedModuleIds
        )
    }

    private fun createModuleItemData(
        item: ModuleItem,
        context: Context,
        indentWidth: Int,
        courseColor: Int
    ): ModuleListItemData.ModuleItemData {
        val subtitle = item.moduleDetails?.dueDate?.let {
            context.getString(
                R.string.due,
                DateHelper.getMonthDayTimeMaybeMinutesMaybeYear(context, it, R.string.at)
            )
        }

        val iconRes: Int? = when (tryOrNull { ModuleItem.Type.valueOf(item.type.orEmpty()) }) {
            ModuleItem.Type.Assignment -> R.drawable.vd_assignment
            ModuleItem.Type.Discussion -> R.drawable.vd_discussion
            ModuleItem.Type.File -> R.drawable.vd_attachment
            ModuleItem.Type.Page -> R.drawable.vd_pages
            ModuleItem.Type.Quiz -> R.drawable.vd_quiz
            ModuleItem.Type.ExternalUrl -> R.drawable.vd_link
            ModuleItem.Type.ExternalTool -> R.drawable.vd_lti
            else -> null
        }

        return ModuleListItemData.ModuleItemData(
            item.id,
            item.title,
            subtitle,
            iconRes,
            item.published,
            item.indent * indentWidth,
            courseColor
        )
    }

}
