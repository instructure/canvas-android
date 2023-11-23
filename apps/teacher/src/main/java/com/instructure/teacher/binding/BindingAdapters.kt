/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.teacher.binding

import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.instructure.teacher.features.modules.progression.ModuleProgressionAdapter

@BindingAdapter("moduleFragments", "initialPosition", requireAll = true)
fun setModuleFragments(viewPager: ViewPager, fragments: List<Fragment>?, initialPosition: Int) {
    val adapter = viewPager.adapter as? ModuleProgressionAdapter
    adapter?.addFragments(fragments.orEmpty())
    viewPager.setCurrentItem(initialPosition, false)
}

@BindingAdapter("onPageChangeListener")
fun addOnPageChangeListener(viewPager: ViewPager, listener: OnPageChangeListener?) {
    listener?.let { viewPager.addOnPageChangeListener(it) }
}
