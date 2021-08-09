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

package com.instructure.pandautils.features.elementary.schedule.pager

import com.instructure.pandautils.features.elementary.schedule.ScheduleFragment

data class SchedulePagerViewData(val fragments: List<ScheduleFragment>)

sealed class SchedulePagerAction {
    data class SelectPage(val position: Int, val smoothScroll: Boolean = false) : SchedulePagerAction()
    object MoveToNext : SchedulePagerAction()
    object MoveToPrevious: SchedulePagerAction()
}