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
package com.instructure.teacher.features.postpolicies.ui

import com.instructure.pandautils.adapters.BasicItemBinder
import com.instructure.pandautils.adapters.BasicItemCallback
import com.instructure.pandautils.adapters.BasicRecyclerAdapter
import com.instructure.pandautils.utils.applyTheme
import com.instructure.teacher.R
import com.instructure.teacher.adapters.ListItemCallback
import com.instructure.teacher.databinding.AdapterPostPolicySectionBinding
import com.instructure.teacher.features.postpolicies.PostSection

interface PostGradeSectionCallback : BasicItemCallback, ListItemCallback {
    fun sectionToggled(sectionId: Long)
}

class PostGradeSectionRecyclerAdapter(callback: PostGradeSectionCallback) : BasicRecyclerAdapter<PostSection, PostGradeSectionCallback>(callback) {
    override fun registerBinders() {
        register(PostGradeSectionBinder())
    }
}

private class PostGradeSectionBinder : BasicItemBinder<PostSection, PostGradeSectionCallback>() {
    override val layoutResId = R.layout.adapter_post_policy_section
    override fun getItemId(item: PostSection) = item.section.id

    override val bindBehavior = Item { item, callback, _ ->
        val binding = AdapterPostPolicySectionBinding.bind(this)
        binding.postPolicySectionTitle.text = item.section.name
        binding.postPolicySectionToggle.applyTheme(item.courseColor)
        binding.postPolicySectionToggle.setOnCheckedChangeListener(null)
        binding.postPolicySectionToggle.isChecked = item.selected
        binding.postPolicySectionToggle.setOnCheckedChangeListener { _, _ ->
            callback.sectionToggled(item.section.id)
        }
    }
}
