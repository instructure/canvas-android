/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui.binders

import com.emeritus.student.R
import com.instructure.pandautils.adapters.BasicItemBinder
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.RubricListData
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui.RubricListCallback
import kotlinx.android.synthetic.main.adapter_rubric_grade.view.*

class RubricListGradeBinder : BasicItemBinder<RubricListData.Grade, RubricListCallback>() {
    override val layoutResId = R.layout.adapter_rubric_grade
    override val bindBehavior = Item { gradeData, _, _ ->
        gradeCell.setState(gradeData.state)
    }
}
