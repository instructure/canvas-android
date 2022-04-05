/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

import com.instructure.canvasapi2.models.postmodels.PendingSubmissionComment
import com.instructure.canvasapi2.utils.BooleanPref
import com.instructure.canvasapi2.utils.GsonListPref
import com.instructure.canvasapi2.utils.PrefManager
import com.instructure.canvasapi2.utils.StringPref

object TeacherPrefs : PrefManager(BaseAppManager.PREF_FILE_NAME) {

    var isConfirmedTeacher by BooleanPref()

    /* Whether the user has viewed the 'Swipe to view other students' tutorial in SpeedGrader */
    var hasViewedSwipeTutorial by BooleanPref()

    /* Whether the user has viewed the 'Tap and hold for description' Rubric tutorial in SpeedGrader */
    var hasViewedRubricTutorial by BooleanPref()

    var pendingSubmissionComments by GsonListPref(PendingSubmissionComment::class.java)

    var attendanceExternalToolId by StringPref()

    var hideCourseColorOverlay by BooleanPref()

    var commentLibraryEnabled by BooleanPref()

    var listDashboard by BooleanPref()
}
