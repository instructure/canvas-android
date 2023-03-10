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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.adapters.ListItemBinder
import com.instructure.teacher.databinding.AdapterModuleBinding
import com.instructure.teacher.features.modules.list.ui.ModuleListCallback
import com.instructure.teacher.features.modules.list.ui.ModuleListItemData

class ModuleListModuleBinder : ListItemBinder<ModuleListItemData.ModuleData, ModuleListCallback>() {

    override fun bindingInflater(viewType: Int): (LayoutInflater, ViewGroup, Boolean) -> ViewBinding = AdapterModuleBinding::inflate

    override fun getItemId(item: ModuleListItemData.ModuleData) = item.id

    override val bindBehavior = Header(
        onExpand = { item, isExpanded, callback -> callback.markModuleExpanded(item.id, isExpanded) },
        onBind = { item, isCollapsed, _ ->
            with(binding as AdapterModuleBinding) {
                moduleName.text = item.name
                publishedIcon.setVisible(item.isPublished == true)
                unpublishedIcon.setVisible(item.isPublished == false)
                collapseIcon.rotation = if (isCollapsed) 0f else 180f
            }
        }
    )
}
