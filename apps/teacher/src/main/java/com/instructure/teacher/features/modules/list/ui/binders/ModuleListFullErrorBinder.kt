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

import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.teacher.R
import com.instructure.teacher.adapters.ListItemBinder
import com.instructure.teacher.databinding.AdapterModuleListErrorFullBinding
import com.instructure.teacher.features.modules.list.ui.ModuleListCallback
import com.instructure.teacher.features.modules.list.ui.ModuleListItemData

class ModuleListFullErrorBinder : ListItemBinder<ModuleListItemData.FullError, ModuleListCallback>() {

    override val layoutResId = R.layout.adapter_module_list_error_full

    override val bindBehavior = Item { item, view, callback ->
        val binding = AdapterModuleListErrorFullBinding.bind(view)
        with(binding.retryButton) {
            backgroundTintList = android.content.res.ColorStateList.valueOf(item.buttonColor)
            setTextColor(ThemePrefs.buttonTextColor)
            setOnClickListener { callback.retryNextPage() }
        }
    }
}
