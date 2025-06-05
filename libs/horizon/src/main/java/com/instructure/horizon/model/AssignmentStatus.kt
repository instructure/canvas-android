/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.model

import androidx.annotation.StringRes
import com.instructure.canvasapi2.models.Assignment
import com.instructure.horizon.R
import com.instructure.pandautils.utils.orDefault

enum class AssignmentStatus(@StringRes val label: Int) {
    NotSubmitted(R.string.notSubmittedStatusLabel),
    Submitted(R.string.submittedStatusLabel),
    Graded(R.string.gradedStatusLabel),
    Excused(R.string.excusedStatusLabel),
    Missing(R.string.missingStatusLabel),
    Late(R.string.lateStatusLabel),
}

fun Assignment.getStatus(): AssignmentStatus {
    return when {
        this.isGraded() -> AssignmentStatus.Graded
        this.submission?.excused.orDefault() -> AssignmentStatus.Excused
        this.submission?.missing.orDefault() -> AssignmentStatus.Missing
        this.submission?.late.orDefault() -> AssignmentStatus.Late
        this.isSubmitted.orDefault() -> AssignmentStatus.Submitted
        else -> AssignmentStatus.NotSubmitted
    }
}