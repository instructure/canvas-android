/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.features.calendar

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.instructure.pandautils.analytics.SCREEN_VIEW_CALENDAR
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.features.calendar.BaseCalendarFragment
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.studentColor
import com.instructure.parentapp.features.dashboard.SelectedStudentHolder
import com.instructure.parentapp.util.ParentPrefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@ScreenView(SCREEN_VIEW_CALENDAR)
@AndroidEntryPoint
class ParentCalendarFragment : BaseCalendarFragment() {

    @Inject
    lateinit var selectedStudentHolder: SelectedStudentHolder

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            selectedStudentHolder.selectedStudentChangedFlow.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collectLatest {
                delay(100)
                refreshCalendar()
            }
        }
    }

    override fun applyTheme() {
        val student = ParentPrefs.currentStudent
        val color = student.studentColor
        ViewStyler.setStatusBarDark(requireActivity(), color)
    }

    override fun showToolbar(): Boolean = false
}