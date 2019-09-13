/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.parentapp.util

import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.IntPref
import com.instructure.canvasapi2.utils.NBooleanPref
import com.instructure.canvasapi2.utils.PrefManager
import com.instructure.parentapp.R


object ParentPrefs : PrefManager(Const.CANVAS_PARENT_SP) {

    @JvmStatic
    var currentColor = ContextCompat.getColor(ContextKeeper.appContext, R.color.parent_colorPrimary)

    var selectedStudentIndex by IntPref()

    var selectedTab by IntPref()

    var isObserver by NBooleanPref()

}
