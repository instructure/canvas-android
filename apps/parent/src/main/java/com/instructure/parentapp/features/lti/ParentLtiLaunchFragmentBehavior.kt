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
package com.instructure.parentapp.features.lti

import androidx.fragment.app.FragmentActivity
import com.instructure.pandautils.features.lti.LtiLaunchFragmentBehavior
import com.instructure.pandautils.utils.studentColor
import com.instructure.parentapp.R
import com.instructure.parentapp.util.ParentPrefs

class ParentLtiLaunchFragmentBehavior(parentPrefs: ParentPrefs, activity: FragmentActivity) : LtiLaunchFragmentBehavior {
    override val toolbarColor: Int = parentPrefs.currentStudent.studentColor

    override val toolbarTextColor: Int = activity.getColor(R.color.textLightest)

    override fun closeLtiLaunchFragment(activity: FragmentActivity) {
        activity.supportFragmentManager.popBackStack()
    }
}