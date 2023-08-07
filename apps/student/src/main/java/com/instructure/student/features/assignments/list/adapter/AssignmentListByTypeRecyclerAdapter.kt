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
package com.instructure.student.features.assignments.list.adapter

import android.content.Context
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.filterWithQuery
import com.instructure.pandarecycler.util.GroupSortedList
import com.instructure.pandarecycler.util.Types
import com.instructure.student.features.assignments.list.AssignmentListRepository
import com.instructure.student.interfaces.AdapterToAssignmentsCallback
import java.util.*

class AssignmentListByTypeRecyclerAdapter(
    context: Context,
    canvasContext: CanvasContext,
    adapterToAssignmentsCallback: AdapterToAssignmentsCallback,
    isTesting: Boolean = false,
    filter: AssignmentListFilter = AssignmentListFilter.ALL,
    repository: AssignmentListRepository
) : AssignmentListRecyclerAdapter(context, canvasContext, adapterToAssignmentsCallback, isTesting, filter, repository) {

    override fun populateData() {
        assignmentGroups
            .forEach { assignmentGroup ->
            val filteredAssignments = assignmentGroup.assignments
                .filterWithQuery(searchQuery, Assignment::name)
                .filter {
                    when (filter) {
                        AssignmentListFilter.ALL -> true
                        AssignmentListFilter.MISSING -> it.isMissing()
                        AssignmentListFilter.LATE -> it.submission?.late ?: false
                        AssignmentListFilter.GRADED -> it.submission?.isGraded ?: false
                        AssignmentListFilter.UPCOMING -> !it.isSubmitted && it.dueDate?.after(Date()) ?: false
                    }
                }
            addOrUpdateAllItems(assignmentGroup, filteredAssignments)
        }
        isAllPagesLoaded = true
    }

    override fun createItemCallback() = object : GroupSortedList.ItemComparatorCallback<AssignmentGroup, Assignment> {
        private val sameCheck = compareBy<Assignment>({ it.dueAt }, { it.name })
        override fun areContentsTheSame(old: Assignment, new: Assignment) = sameCheck.compare(old, new) == 0
        override fun areItemsTheSame(item1: Assignment, item2: Assignment) = item1.id == item2.id
        override fun getChildType(group: AssignmentGroup, item: Assignment) = Types.TYPE_ITEM
        override fun getUniqueItemId(item: Assignment) = item.id
        override fun compare(group: AssignmentGroup, o1: Assignment, o2: Assignment) = o1.position - o2.position
    }
}
