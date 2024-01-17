/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 */

package com.instructure.teacher.features.modules.list.ui.binders

import android.view.Gravity
import androidx.appcompat.widget.PopupMenu
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.setHidden
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.adapters.ListItemBinder
import com.instructure.teacher.databinding.AdapterModuleSubHeaderBinding
import com.instructure.teacher.features.modules.list.ui.ModuleListCallback
import com.instructure.teacher.features.modules.list.ui.ModuleListItemData

class ModuleListSubHeaderBinder : ListItemBinder<ModuleListItemData.SubHeader, ModuleListCallback>() {
    override val layoutResId = R.layout.adapter_module_sub_header

    override fun getItemId(item: ModuleListItemData.SubHeader) = item.id

    override val bindBehavior: BindBehavior<ModuleListItemData.SubHeader, ModuleListCallback> = Item { item, view, callback ->
        val binding = AdapterModuleSubHeaderBinding.bind(view)
        with(binding) {
            subHeaderTitle.text = item.title
            moduleItemPublishedIcon.setVisible(item.published == true && !item.isLoading)
            moduleItemUnpublishedIcon.setVisible(item.published == false && !item.isLoading)
            moduleItemIndent.layoutParams.width = item.indent

            moduleItemLoadingView.setVisible(item.isLoading)

            overflow.onClickWithRequireNetwork {
                val popup = PopupMenu(it.context, it, Gravity.START.and(Gravity.TOP))
                val menu = popup.menu

                when (item.published) {
                    true -> menu.add(0, 0, 0, R.string.unpublish)
                    false -> menu.add(0, 0, 0, R.string.publish)
                    else -> {
                        menu.add(0, 0, 0, R.string.unpublish)
                        menu.add(0, 1, 1, R.string.publish)
                    }
                }

                overflow.contentDescription = it.context.getString(R.string.a11y_contentDescription_moduleOptions, item.title)
                popup.show()
            }
        }
    }
}