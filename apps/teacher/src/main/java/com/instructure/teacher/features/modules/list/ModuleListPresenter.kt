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

        val iconTint = model.course.color

        items += model.modules.map { module ->
            val moduleItems: List<ModuleListItemData> = if (module.items.isNotEmpty()) {
                module.items.map { item ->
                    if (item.type.equals(ModuleItem.Type.SubHeader.name, ignoreCase = true)) {
                        ModuleListItemData.SubHeader(
                            id = item.id,
                            title = item.title,
                            indent = item.indent * indentWidth,
                            enabled = false,
                            published = item.published,
                            isLoading = item.id in model.loadingModuleItemIds
                        )
                    } else {
                        createModuleItemData(
                            item,
                            context,
                            indentWidth,
                            iconTint,
                            item.id in model.loadingModuleItemIds
                        )
                    }
                }
            } else {
                listOf(ModuleListItemData.EmptyItem(module.id))
            }
            ModuleListItemData.ModuleData(
                id = module.id,
                name = module.name.orEmpty(),
                isPublished = module.published,
                moduleItems = moduleItems,
                isLoading = module.id in model.loadingModuleItemIds
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
        courseColor: Int,
        loading: Boolean
    ): ModuleListItemData.ModuleItemData {
        val subtitle = item.moduleDetails?.dueDate?.let {
            DateHelper.getMonthDayTimeMaybeMinutesMaybeYear(context, it, R.string.at)
        }

        val pointsPossible = item.moduleDetails?.pointsPossible?.toFloatOrNull()
        val subtitle2 =
            pointsPossible?.let { context.resources.getQuantityString(R.plurals.moduleItemPoints, it.toInt(), it) }

        val iconRes: Int? = when (tryOrNull { ModuleItem.Type.valueOf(item.type.orEmpty()) }) {
            ModuleItem.Type.Assignment -> if (item.quizLti) R.drawable.ic_quiz else R.drawable.ic_assignment
            ModuleItem.Type.Discussion -> R.drawable.ic_discussion
            ModuleItem.Type.File -> R.drawable.ic_attachment
            ModuleItem.Type.Page -> R.drawable.ic_pages
            ModuleItem.Type.Quiz -> R.drawable.ic_quiz
            ModuleItem.Type.ExternalUrl -> R.drawable.ic_link
            ModuleItem.Type.ExternalTool -> R.drawable.ic_lti
            else -> null
        }

        return ModuleListItemData.ModuleItemData(
            id = item.id,
            title = item.title,
            subtitle = subtitle,
            subtitle2 = subtitle2,
            iconResId = iconRes,
            isPublished = item.published,
            indent = item.indent * indentWidth,
            tintColor = courseColor,
            enabled = !loading,
            isLoading = loading,
            type = tryOrNull { ModuleItem.Type.valueOf(item.type.orEmpty()) } ?: ModuleItem.Type.Assignment,
            contentDetails = item.moduleDetails,
            contentId = item.contentId,
            unpublishable = item.unpublishable
        )
    }

}
