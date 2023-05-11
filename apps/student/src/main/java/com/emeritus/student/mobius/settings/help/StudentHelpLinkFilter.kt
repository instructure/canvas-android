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
package com.emeritus.student.mobius.settings.help

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.HelpLink
import com.instructure.pandautils.features.help.HelpLinkFilter

class StudentHelpLinkFilter : HelpLinkFilter {

    override fun isLinkAllowed(link: HelpLink, favoriteCourses: List<Course>): Boolean {
        return ((link.availableTo.contains("student") || link.availableTo.contains("user"))
            && (link.url != "#teacher_feedback" || favoriteCourses.filter { !it.isTeacher }.count() > 0))
    }
}