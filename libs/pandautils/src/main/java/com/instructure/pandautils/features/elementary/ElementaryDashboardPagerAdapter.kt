/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.pandautils.features.elementary

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.features.elementary.grades.GradesFragment
import com.instructure.pandautils.features.elementary.homeroom.HomeroomFragment
import com.instructure.pandautils.features.elementary.resources.ResourcesFragment
import com.instructure.pandautils.features.elementary.schedule.ScheduleFragment

class ElementaryDashboardPagerAdapter(
    private val canvasContext: CanvasContext,
    fragmentManager: FragmentManager
) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragments = listOf(
        HomeroomFragment.newInstance(),
        ScheduleFragment.newInstance(),
        GradesFragment.newInstance(),
        ResourcesFragment.newInstance()
    )

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    fun refreshHomeroomAssignments() {
        (fragments[0] as? HomeroomFragment)?.refreshAssignmentStatus()
    }
}