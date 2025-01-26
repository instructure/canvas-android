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
package com.instructure.teacher.features.modules.list.ui.binders

import android.view.Gravity
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.PopupMenu
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.utils.isValid
import com.instructure.pandautils.binding.setTint
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.setTextForVisibility
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.adapters.ListItemBinder
import com.instructure.teacher.databinding.AdapterModuleItemBinding
import com.instructure.teacher.features.modules.list.ui.ModuleListCallback
import com.instructure.teacher.features.modules.list.ui.ModuleListItemData

class ModuleListItemBinder :
    ListItemBinder<ModuleListItemData.ModuleItemData, ModuleListCallback>() {

    override val layoutResId = R.layout.adapter_module_item

    override fun getItemId(item: ModuleListItemData.ModuleItemData) = item.id

    override val bindBehavior = Item { item, view, callback ->
        val binding = AdapterModuleItemBinding.bind(view)
        with(binding) {
            moduleItemIcon.setVisible(item.iconResId != null)
            item.iconResId?.let {
                moduleItemIcon.setImageResource(it)
            }
            moduleItemIndent.layoutParams.width = item.indent
            moduleItemTitle.setTextForVisibility(item.title)
            moduleItemSubtitle.setTextForVisibility(item.subtitle)
            moduleItemSubtitle2.setTextForVisibility(item.subtitle2)

            root.setOnClickListener { callback.moduleItemClicked(item.id) }
            root.isEnabled = item.enabled

            val statusIcon = getStatusIcon(item)
            moduleItemStatusIcon.apply {
                contentDescription = context.getString(statusIcon.contentDescription)
                setImageResource(statusIcon.icon)
                setTint(statusIcon.tint)
                setVisible(!item.isLoading)
                alpha = if (item.unpublishable || item.type == ModuleItem.Type.File) 1f else 0.5f
            }
            moduleItemLoadingView.setVisible(item.isLoading)

            publishActions.contentDescription = publishActions.context.getString(R.string.a11y_contentDescription_moduleOptions, item.title)
            publishActions.onClickWithRequireNetwork {
                if (item.type == ModuleItem.Type.File) {
                    item.contentId?.let {
                        callback.updateFileModuleItem(
                            item.contentId,
                            item.contentDetails ?: ModuleContentDetails()
                        )
                    }
                } else {
                    if (!item.unpublishable) {
                        callback.showSnackbar(
                            R.string.error_unpublishable_module_item,
                            arrayOf(item.title.orEmpty())
                        )
                    } else {
                        showModuleItemActions(it, item, callback)
                    }
                }
            }


        }
    }

    private fun getStatusIcon(data: ModuleListItemData.ModuleItemData): StatusIcon {
        val icon: Int
        val tint: Int
        val contentDescription: Int
        when (data.type) {
            ModuleItem.Type.File -> {
                if (data.contentDetails?.hidden == true) {
                    icon = R.drawable.ic_eye_off
                    tint = R.color.textWarning
                    contentDescription = R.string.a11y_hidden
                } else if (data.contentDetails?.lockAt.isValid() || data.contentDetails?.unlockAt.isValid()) {
                    icon = R.drawable.ic_calendar_month
                    tint = R.color.textWarning
                    contentDescription = R.string.a11y_scheduled
                } else {
                    icon =
                        if (data.isPublished == true) R.drawable.ic_complete_solid else R.drawable.ic_no
                    tint = if (data.isPublished == true) R.color.textSuccess else R.color.textDark
                    contentDescription =
                        if (data.isPublished == true) R.string.a11y_published else R.string.a11y_unpublished
                }
            }

            else -> {
                icon =
                    if (data.isPublished == true) R.drawable.ic_complete_solid else R.drawable.ic_no
                tint = if (data.isPublished == true) R.color.textSuccess else R.color.textDark
                contentDescription =
                    if (data.isPublished == true) R.string.a11y_published else R.string.a11y_unpublished
            }
        }

        return StatusIcon(icon, tint, contentDescription)
    }

    private fun showModuleItemActions(
        view: View,
        item: ModuleListItemData.ModuleItemData,
        callback: ModuleListCallback
    ) {
        val popup = PopupMenu(view.context, view, Gravity.START.and(Gravity.TOP))
        val menu = popup.menu

        when (item.isPublished) {
            true -> menu.add(0, 0, 0, R.string.unpublishModuleItemAction)
            false -> menu.add(0, 1, 1, R.string.publishModuleItemAction)
            else -> {
                menu.add(0, 0, 0, R.string.unpublishModuleItemAction)
                menu.add(0, 1, 1, R.string.publishModuleItemAction)
            }
        }

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                0 -> {
                    callback.updateModuleItem(item.id, false)
                    true
                }

                1 -> {
                    callback.updateModuleItem(item.id, true)
                    true
                }

                else -> false
            }
        }

        popup.show()
    }
}

data class StatusIcon(
    @DrawableRes val icon: Int,
    @ColorRes val tint: Int,
    @StringRes val contentDescription: Int
)
