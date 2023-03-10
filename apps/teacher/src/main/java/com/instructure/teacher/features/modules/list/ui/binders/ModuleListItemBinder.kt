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
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.instructure.pandautils.utils.setTextForVisibility
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.adapters.ListItemBinder
import com.instructure.teacher.databinding.AdapterModuleItemBinding
import com.instructure.teacher.features.modules.list.ui.ModuleListCallback
import com.instructure.teacher.features.modules.list.ui.ModuleListItemData

class ModuleListItemBinder : ListItemBinder<ModuleListItemData.ModuleItemData, ModuleListCallback>() {

    override fun bindingInflater(viewType: Int): (LayoutInflater, ViewGroup, Boolean) -> ViewBinding = AdapterModuleItemBinding::inflate

    override fun getItemId(item: ModuleListItemData.ModuleItemData) = item.id

    override val bindBehavior = Item { item, callback ->
        with(binding as AdapterModuleItemBinding) {
            moduleItemIcon.setVisible(item.iconResId != null)
            item.iconResId?.let {
                moduleItemIcon.setImageResource(it)
                moduleItemIcon.imageTintList = ColorStateList.valueOf(item.tintColor)
            }
            moduleItemIndent.layoutParams.width = item.indent
            moduleItemTitle.setTextForVisibility(item.title)
            moduleItemSubtitle.setTextForVisibility(item.subtitle)
            moduleItemPublishedIcon.setVisible(item.isPublished == true)
            moduleItemUnpublishedIcon.setVisible(item.isPublished == false)
            moduleItemLoadingView.setVisible(item.isLoading)
            root.setOnClickListener { callback.moduleItemClicked(item.id) }
            root.isEnabled = item.enabled
        }
    }
}
