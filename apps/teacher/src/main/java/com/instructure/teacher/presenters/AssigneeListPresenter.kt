/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.teacher.presenters

import com.instructure.canvasapi2.models.CanvasComparable
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.NaturalOrderComparator
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.teacher.events.AssigneesUpdatedEvent
import com.instructure.teacher.models.AssigneeCategory
import com.instructure.teacher.models.EveryoneAssignee
import com.instructure.teacher.utils.EditDateGroups
import com.instructure.teacher.viewinterface.AssigneeListView
import com.instructure.pandautils.blueprint.SyncExpandablePresenter
import org.greenrobot.eventbus.EventBus
import java.util.*

class AssigneeListPresenter(
        val mAllDateGroups: EditDateGroups,
        targetIdx: Int,
        val sections: List<Section>,
        val groups: List<Group>,
        val students: List<User>
) : SyncExpandablePresenter<AssigneeCategory, CanvasComparable<*>, AssigneeListView>(AssigneeCategory::class.java, CanvasComparable::class.java) {

    private val mTargetDate = mAllDateGroups[targetIdx]
    private val mOtherDates = mAllDateGroups - mTargetDate

    private val mTakenStudents = mOtherDates.flatMap { it.studentIds }
    private val mTakenSections = mOtherDates.flatMap { it.sectionIds }
    private val mTakenGroups = mOtherDates.flatMap { it.groupIds }

    var isEveryone = mTargetDate.isEveryone
    val selectedStudents = ArrayList(mTargetDate.studentIds)
    val selectedSections = ArrayList(mTargetDate.sectionIds)
    val selectedGroups = ArrayList(mTargetDate.groupIds)

    private var mSectionMap = sections.associateBy { it.id }
    private var mGroupMap = groups.associateBy { it.id }
    private var mStudentMap = students.associateBy { it.id }

    private val mShowEveryoneItem = mAllDateGroups.size == 1 || mTargetDate.isEveryone || mAllDateGroups.none { it.isEveryone }

    override fun loadData(forceNetwork: Boolean) {
        data.addOrUpdateAllItems(AssigneeCategory.SECTIONS, mSectionMap.values.filter { it.id !in mTakenSections })
        data.addOrUpdateAllItems(AssigneeCategory.GROUPS, mGroupMap.values.filter { it.id !in mTakenGroups })
        data.addOrUpdateAllItems(AssigneeCategory.STUDENTS, mStudentMap.values.filter { it.id !in mTakenStudents })
        updateEveryoneItem()
        updateSelectedAssignees()
        viewCallback?.onRefreshFinished()
        viewCallback?.checkIfEmpty()
    }

    private fun updateEveryoneItem() {
        if (mShowEveryoneItem) data.addOrUpdateItem(AssigneeCategory.SECTIONS, EveryoneAssignee(mStudentMap.size, showAsEveryoneElse()))
    }

    private fun showAsEveryoneElse()
            = mOtherDates.any { it.hasOverrideAssignees }
            || selectedStudents.isNotEmpty()
            || selectedSections.isNotEmpty()
            || selectedGroups.isNotEmpty()

    private fun updateSelectedAssignees() {
        val assigneeNames = arrayListOf<CharSequence>()
        assigneeNames += selectedSections.map { mSectionMap[it]!!.name }
        assigneeNames += selectedGroups.map { mGroupMap[it]?.name ?: "" }
        assigneeNames += selectedStudents.map { Pronouns.span(mStudentMap[it]?.name, mStudentMap[it]?.pronouns) }
        viewCallback?.updateSelectedAssignees(assigneeNames, isEveryone, showAsEveryoneElse())
    }

    fun toggleStudent(id: Long, position: Int) = selectedStudents.toggle(id, position)
    fun toggleSection(id: Long, position: Int) = selectedSections.toggle(id, position)
    fun toggleGroup(id: Long, position: Int) = selectedGroups.toggle(id, position)
    fun toggleIsEveryone(position: Int) {
        isEveryone = !isEveryone
        updateEveryoneItem()
        updateSelectedAssignees()
        viewCallback?.notifyItemChanged(position)
    }

    private fun <T> ArrayList<T>.toggle(id: T, position: Int) {
        if (id in this) remove(id) else add(id)
        updateEveryoneItem()
        updateSelectedAssignees()
        viewCallback?.notifyItemChanged(position)
    }

    override fun compare(group1: AssigneeCategory, group2: AssigneeCategory): Int {
        return group1.compareTo(group2)
    }

    override fun compare(group: AssigneeCategory, item1: CanvasComparable<*>, item2: CanvasComparable<*>): Int {
        if (group == AssigneeCategory.SECTIONS) {
            if (item1 is EveryoneAssignee) return -1
            else if (item2 is EveryoneAssignee) return 1
        }

        if (group == AssigneeCategory.STUDENTS) {
            return NaturalOrderComparator.compare((item1 as User).sortableName?.lowercase(Locale.getDefault()).orEmpty(), (item2 as User).sortableName?.lowercase(Locale.getDefault()).orEmpty())
        }
        return compareValues(item1, item2)
    }

    override fun areContentsTheSame(item1: CanvasComparable<*>, item2: CanvasComparable<*>): Boolean {
        if (item1 is EveryoneAssignee && item2 is EveryoneAssignee) {
            return item1.displayAsEveryoneElse == item2.displayAsEveryoneElse
        }
        return super.areContentsTheSame(item1, item2)
    }

    override fun getUniqueItemId(item: CanvasComparable<*>) = when (item) {
        is EveryoneAssignee -> 0L
        is Section -> 100000L + item.id
        is Group -> 200000L + item.id
        is User -> 300000L + item.id
        else -> super.getUniqueItemId(item)
    }

    override fun refresh(forceNetwork: Boolean) {
        onRefreshStarted()
        clearData()
        loadData(forceNetwork)
    }

    fun save() {
        mTargetDate.isEveryone = isEveryone
        mTargetDate.sectionIds = selectedSections
        mTargetDate.groupIds = selectedGroups
        mTargetDate.studentIds = selectedStudents
        EventBus.getDefault().postSticky(AssigneesUpdatedEvent(mAllDateGroups))
    }
}
