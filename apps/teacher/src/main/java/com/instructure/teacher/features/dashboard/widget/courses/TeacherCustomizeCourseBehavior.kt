/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.teacher.features.dashboard.widget.courses

import com.instructure.pandautils.features.dashboard.widget.courses.customize.CustomizeCourseBehavior
import javax.inject.Inject

class TeacherCustomizeCourseBehavior @Inject constructor() : CustomizeCourseBehavior {
    override fun shouldShowColorOverlay(): Boolean {
        return true
    }
}