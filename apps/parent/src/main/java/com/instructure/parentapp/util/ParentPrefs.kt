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

package com.instructure.parentapp.util

import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.BooleanPref
import com.instructure.canvasapi2.utils.GsonPref
import com.instructure.canvasapi2.utils.NStringPref
import com.instructure.canvasapi2.utils.PrefManager
import kotlin.reflect.KMutableProperty0


object ParentPrefs : PrefManager("parentSP") {

    var currentStudent: User? by GsonPref(User::class.java, null, "current_student", false)
    var hasMigrated: Boolean by BooleanPref(false, "has_migrated_data_from_old_app")
    var gradesSortBy: String? by NStringPref(null, "grades_sort_by")

    override fun keepBaseProps(): List<KMutableProperty0<out Any?>> = listOf(
        ::hasMigrated,
        ::gradesSortBy
    )
}
