/*
 * Copyright (C) 2022 - present Instructure, Inc.
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

package com.instructure.teacher.utils

import android.app.Activity
import androidx.appcompat.widget.Toolbar
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ToolbarSetupBehavior
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.setupAsBackButton

class TeacherToolbarSetupBehavior(val activity: Activity) : ToolbarSetupBehavior {
    override fun setupToolbar(toolbar: Toolbar) {
        if (!activity.isTablet) toolbar.setupAsBackButton { activity.onBackPressed() }
        ViewStyler.themeToolbarColored(activity, toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
    }
}