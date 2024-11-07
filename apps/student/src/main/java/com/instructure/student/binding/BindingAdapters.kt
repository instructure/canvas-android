/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.student.binding

import androidx.databinding.BindingAdapter
import com.google.android.material.tabs.TabLayout
import com.instructure.student.features.elementary.course.ElementaryCourseTab

@BindingAdapter("tabs")
fun bindCourseTabs(tabLayout: TabLayout, tabs: List<ElementaryCourseTab>?) {
    tabLayout.removeAllTabs()
    tabs?.forEach { tab ->
        tabLayout.addTab(tabLayout.newTab().apply {
            icon = tab.icon
            text = tab.text
        })
    }
}
