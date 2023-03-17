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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui.binders

import com.instructure.pandautils.adapters.BasicItemBinder
import com.instructure.student.R
import com.instructure.student.databinding.AdapterRubricEmptyBinding
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.RubricListData
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui.RubricListCallback

class RubricListEmptyBinder : BasicItemBinder<RubricListData.Empty, RubricListCallback, AdapterRubricEmptyBinding>(
    AdapterRubricEmptyBinding::bind
) {
    override val layoutResId = R.layout.adapter_rubric_empty
    override val bindBehavior = NoBind()
}
