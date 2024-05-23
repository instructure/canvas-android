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
package com.instructure.student.features.people.list

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Enrollment.EnrollmentType
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.NaturalOrderComparator
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandarecycler.util.GroupSortedList
import com.instructure.pandarecycler.util.Types
import com.instructure.pandautils.utils.backgroundColor
import com.instructure.pandautils.utils.toast
import com.instructure.student.R
import com.instructure.student.adapter.ExpandableRecyclerAdapter
import com.instructure.student.holders.PeopleHeaderViewHolder
import com.instructure.student.holders.PeopleViewHolder
import com.instructure.student.interfaces.AdapterToFragmentCallback
import kotlinx.coroutines.CoroutineScope
import java.util.Locale

class PeopleListRecyclerAdapter(
    context: Context,
    private val lifecycleScope: CoroutineScope,
    private val repository: PeopleListRepository,
    private val canvasContext: CanvasContext,
    private val adapterToFragmentCallback: AdapterToFragmentCallback<User>
) : ExpandableRecyclerAdapter<EnrollmentType, User, RecyclerView.ViewHolder>(
    context,
    EnrollmentType::class.java,
    User::class.java
) {

    private val mCourseColor = canvasContext.backgroundColor
    private val mEnrollmentPriority = mapOf(
        EnrollmentType.Teacher to 4,
        EnrollmentType.Ta to 3,
        EnrollmentType.Student to 2,
        EnrollmentType.Observer to 1
    )

    init {
        isExpandedByDefault = true
        loadData()
    }

    override fun loadFirstPage() {
        lifecycleScope.tryLaunch {
            val teacherContext =
                if (CanvasContext.Type.isGroup(this@PeopleListRecyclerAdapter.canvasContext) && (this@PeopleListRecyclerAdapter.canvasContext as Group).courseId > 0) {
                    // We build a generic CanvasContext with type set to COURSE and give it the CourseId from the group, so that it wil use the course API not the group API
                    CanvasContext.getGenericContext(
                        CanvasContext.Type.COURSE,
                        this@PeopleListRecyclerAdapter.canvasContext.courseId,
                        ""
                    )
                } else canvasContext

            val teachers = repository.loadTeachers(teacherContext, isRefresh)
            val tas = repository.loadTAs(teacherContext, isRefresh)
            val peopleFirstPage = repository.loadFirstPagePeople(canvasContext, isRefresh)
            val result = teachers.dataOrThrow + tas.dataOrThrow + peopleFirstPage.dataOrThrow

            populateAdapter(result)

            if (peopleFirstPage is DataResult.Success<List<User>>) {
                setNextUrl(peopleFirstPage.linkHeaders.nextUrl)
            }

        } catch {
            context.toast(R.string.errorOccurred)
        }
    }

    override fun loadNextPage(nextURL: String) {
        lifecycleScope.tryLaunch {
            val peopleNextPage = repository.loadNextPagePeople(canvasContext, isRefresh, nextURL)

            populateAdapter(peopleNextPage.dataOrThrow)

            if (peopleNextPage is DataResult.Success<List<User>>) {
                setNextUrl(peopleNextPage.linkHeaders.nextUrl)
            }
        } catch {
            context.toast(R.string.errorOccurred)
        }

    }

    override val isPaginated get() = true

    private fun populateAdapter(result: List<User>) {
        val (enrolled, unEnrolled) = result.partition { it.enrollments.isNotEmpty() }
        enrolled
            .groupBy {
                it.enrollments.sortedByDescending { enrollment -> mEnrollmentPriority[enrollment.type] }[0].type
            }
            .forEach { (type, users) -> addOrUpdateAllItems(type!!, users) }
        if (CanvasContext.Type.isGroup(canvasContext)) addOrUpdateAllItems(EnrollmentType.NoEnrollment, unEnrolled)
        notifyDataSetChanged()
        adapterToFragmentCallback.onRefreshFinished()
    }

    override fun createViewHolder(v: View, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == Types.TYPE_HEADER) PeopleHeaderViewHolder(v) else PeopleViewHolder(v)

    override fun itemLayoutResId(viewType: Int): Int =
        if (viewType == Types.TYPE_HEADER) PeopleHeaderViewHolder.HOLDER_RES_ID else PeopleViewHolder.HOLDER_RES_ID

    override fun contextReady() = Unit

    override fun onBindChildHolder(holder: RecyclerView.ViewHolder, peopleGroupType: EnrollmentType, user: User) {
        val groupItemCount = getGroupItemCount(peopleGroupType)
        val itemPosition = storedIndexOfItem(peopleGroupType, user)
        (holder as PeopleViewHolder).bind(
            user,
            adapterToFragmentCallback,
            mCourseColor,
            itemPosition == 0,
            itemPosition == groupItemCount - 1
        )
    }

    override fun onBindHeaderHolder(
        holder: RecyclerView.ViewHolder,
        enrollmentType: EnrollmentType,
        isExpanded: Boolean
    ) {
        (holder as PeopleHeaderViewHolder).bind(
            enrollmentType,
            getHeaderTitle(enrollmentType),
            isExpanded,
            viewHolderHeaderClicked
        )
    }

    override fun createGroupCallback(): GroupSortedList.GroupComparatorCallback<EnrollmentType> {
        return object : GroupSortedList.GroupComparatorCallback<EnrollmentType> {
            override fun compare(o1: EnrollmentType, o2: EnrollmentType) =
                getHeaderTitle(o2).compareTo(getHeaderTitle(o1))

            override fun areContentsTheSame(oldGroup: EnrollmentType, newGroup: EnrollmentType) =
                getHeaderTitle(oldGroup) == getHeaderTitle(newGroup)

            override fun areItemsTheSame(group1: EnrollmentType, group2: EnrollmentType) =
                getHeaderTitle(group1) == getHeaderTitle(group2)

            override fun getUniqueGroupId(group: EnrollmentType) = getHeaderTitle(group).hashCode().toLong()
            override fun getGroupType(group: EnrollmentType) = Types.TYPE_HEADER
        }
    }

    override fun createItemCallback(): GroupSortedList.ItemComparatorCallback<EnrollmentType, User> {
        return object : GroupSortedList.ItemComparatorCallback<EnrollmentType, User> {
            override fun compare(group: EnrollmentType, o1: User, o2: User) = NaturalOrderComparator.compare(
                o1.sortableName?.lowercase(Locale.getDefault()).orEmpty(),
                o2.sortableName?.lowercase(Locale.getDefault()).orEmpty()
            )

            override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem.sortableName == newItem.sortableName
            override fun areItemsTheSame(item1: User, item2: User) = item1.id == item2.id
            override fun getUniqueItemId(item: User) = item.id
            override fun getChildType(group: EnrollmentType, item: User) = Types.TYPE_ITEM
        }
    }

    private fun getHeaderTitle(enrollmentType: Enrollment.EnrollmentType?): String = when (enrollmentType) {
        EnrollmentType.Student -> context.getString(R.string.students)
        EnrollmentType.Teacher, EnrollmentType.Ta -> context.getString(R.string.teachersTas)
        EnrollmentType.Observer -> context.getString(R.string.observers)
        EnrollmentType.Designer -> context.getString(R.string.enrollmentTypeDesigner)
        else -> context.getString(R.string.groupMembers) // Default
    }
}
