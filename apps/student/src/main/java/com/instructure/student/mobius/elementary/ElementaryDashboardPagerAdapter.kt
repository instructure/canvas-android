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
package com.instructure.student.mobius.elementary

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.student.mobius.elementary.grades.GradesFragment
import com.instructure.student.mobius.elementary.homeroom.HomeroomFragment
import com.instructure.student.mobius.elementary.resources.ResourcesFragment
import com.instructure.student.mobius.elementary.schedule.ScheduleFragment
import java.lang.UnsupportedOperationException

private const val PAGES_COUNT = 4

private const val HOMEROOM_TAB_POSITION = 0
private const val SCHEDULE_TAB_POSITION = 1
private const val GRADES_TAB_POSITION = 2
private const val RESOURCES_TAB_POSITION = 3

class ElementaryDashboardPagerAdapter(
    private val canvasContext: CanvasContext,
    fragmentManager: FragmentManager
) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            HOMEROOM_TAB_POSITION -> HomeroomFragment.newInstance(canvasContext)
            SCHEDULE_TAB_POSITION -> ScheduleFragment.newInstance(canvasContext)
            GRADES_TAB_POSITION -> GradesFragment.newInstance(canvasContext)
            RESOURCES_TAB_POSITION -> ResourcesFragment.newInstance(canvasContext)
            else -> throw UnsupportedOperationException()
        }
    }

    override fun getCount(): Int {
        return PAGES_COUNT
    }
}