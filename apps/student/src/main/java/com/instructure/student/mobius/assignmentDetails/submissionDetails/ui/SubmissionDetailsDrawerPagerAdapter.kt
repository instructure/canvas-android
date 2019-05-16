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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.files.ui.SubmissionFilesFragment

class SubmissionDetailsDrawerPagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {

    var tabData: List<SubmissionDetailsTabData> = emptyList()
        set(value) {
            if (field.size != value.size) {
                cachedFragments = Array(value.size) { null }
            } else {
                for (idx in cachedFragments.indices) {
                    if (field[idx] != value[idx]) cachedFragments[idx] = null
                }
            }
            field = value
        }

    private var cachedFragments: Array<Fragment?> = emptyArray()

    override fun getItem(position: Int): Fragment {
        cachedFragments[position]
        var cachedFragment = cachedFragments[position]
        if (cachedFragment == null) {
            val tabData = tabData[position]
            cachedFragment = when (tabData) {
                is SubmissionDetailsTabData.FileData -> SubmissionFilesFragment().apply { data = tabData }
                else -> PlaceholderFragment().apply {
                    typeName = tabData::class.java.simpleName.substringAfterLast('.')
                    typeContents = tabData.toString()
                }
            }
            cachedFragments[position] = cachedFragment
        }
        return cachedFragment
    }

    override fun getItemPosition(`object`: Any): Int {
        val fragment: Fragment = `object` as? Fragment ?: return PagerAdapter.POSITION_UNCHANGED
        val idx = cachedFragments.indexOf(fragment)
        return idx.takeIf { it >= 0 } ?: PagerAdapter.POSITION_NONE
    }

    override fun getPageTitle(position: Int) = tabData[position].tabName

    override fun getCount() = tabData.size
}
