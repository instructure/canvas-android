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

import android.content.res.ColorStateList
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.setHidden
import com.instructure.pandautils.utils.setTextForVisibility
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.adapters.ListItemBinder
import com.instructure.teacher.databinding.AdapterModuleItemBinding
import com.instructure.teacher.features.modules.list.ui.ModuleListCallback
import com.instructure.teacher.features.modules.list.ui.ModuleListItemData

class ModuleListItemBinder : ListItemBinder<ModuleListItemData.ModuleItemData, ModuleListCallback>() {

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
            moduleItemPublishedIcon.setVisible(item.isPublished == true && !item.isLoading)
            moduleItemUnpublishedIcon.setVisible(item.isPublished == false && !item.isLoading)
            root.setOnClickListener { callback.moduleItemClicked(item.id) }
            root.isEnabled = item.enabled

            moduleItemLoadingView.setVisible(item.isLoading)

            overflow.onClickWithRequireNetwork {
                val popup = PopupMenu(it.context, it, Gravity.START.and(Gravity.TOP))
                val menu = popup.menu

                when (item.isPublished) {
                    true -> menu.add(0, 0, 0, R.string.unpublish)
                    false -> menu.add(0, 1, 1, R.string.publish)
                    else -> {
                        menu.add(0, 0, 0, R.string.unpublish)
                        menu.add(0, 1, 1, R.string.publish)
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

                overflow.contentDescription = it.context.getString(R.string.a11y_contentDescription_moduleOptions, item.title)
                popup.show()
            }
        }
    }
}
