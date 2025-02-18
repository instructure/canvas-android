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
import androidx.appcompat.widget.PopupMenu
import com.instructure.pandautils.utils.accessibilityClassName
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.adapters.ListItemBinder
import com.instructure.teacher.databinding.AdapterModuleBinding
import com.instructure.teacher.features.modules.list.ui.ModuleListCallback
import com.instructure.teacher.features.modules.list.ui.ModuleListItemData

class ModuleListModuleBinder : ListItemBinder<ModuleListItemData.ModuleData, ModuleListCallback>() {

    override val layoutResId = R.layout.adapter_module

    override fun getItemId(item: ModuleListItemData.ModuleData) = item.id

    override val bindBehavior = Header(
        onExpand = { item, isExpanded, callback -> callback.markModuleExpanded(item.id, isExpanded) },
        onBind = { item, view, isCollapsed, callback ->
            val binding = AdapterModuleBinding.bind(view)
            with(binding) {
                moduleName.text = item.name
                publishedIcon.setVisible(item.isPublished == true && !item.isLoading)
                unpublishedIcon.setVisible(item.isPublished == false && !item.isLoading)
                collapseIcon.rotation = if (isCollapsed) 180f else 0f

                loadingView.setVisible(item.isLoading)

                publishActions.contentDescription = publishActions.context.getString(R.string.a11y_contentDescription_moduleOptions, item.name)
                publishActions.onClickWithRequireNetwork {
                    val popup = PopupMenu(it.context, it, Gravity.START.and(Gravity.TOP))
                    popup.inflate(R.menu.menu_module)

                    popup.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.publishModuleItems -> {
                                callback.publishModuleAndItems(item.id)
                                true
                            }
                            R.id.publishModule -> {
                                callback.publishModule(item.id)
                                true
                            }
                            R.id.unpublishModuleItems -> {
                                callback.unpublishModuleAndItems(item.id)
                                true
                            }
                            else -> false
                        }
                    }

                    popup.show()
                }

                //Can't use the binding adapter due to how the view holder is set up
                publishActions.accessibilityClassName("android.widget.Button")
                root.accessibilityClassName("android.widget.Button")

            }
        }
    )
}
