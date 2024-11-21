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

import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupCategoriesManager
import com.instructure.canvasapi2.managers.SectionManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.inParallel
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.pandautils.blueprint.SyncPresenter
import com.instructure.teacher.events.AssignmentUpdatedEvent
import com.instructure.teacher.models.DueDateGroup
import com.instructure.teacher.utils.groupedDueDates
import com.instructure.teacher.viewinterface.DueDatesView
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class DueDatesPresenter(var mAssignment: Assignment) : SyncPresenter<DueDateGroup, DueDatesView>(DueDateGroup::class.java) {

    val sectionMap = hashMapOf<Long, Section>()
    val groupMap = hashMapOf<Long, Group>()
    val studentMap = hashMapOf<Long, User>()
    var apiCalls: Job? = null

    init {
        EventBus.getDefault().register(this)
    }

    override fun loadData(forceNetwork: Boolean) {
        if (apiCalls?.isActive ?: false) {
            apiCalls?.invokeOnCompletion { performLoad(forceNetwork) }
        } else {
            performLoad(forceNetwork)
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun performLoad(forceNetwork: Boolean) {
        apiCalls = weave {
            clearData()
            onRefreshStarted()
            if (forceNetwork) viewCallback?.hideMenu()
            try {
                val assignment = awaitApi<Assignment> { AssignmentManager.getAssignment(mAssignment.id, mAssignment.courseId, forceNetwork, it) }
                val dateGroups = assignment.groupedDueDates
                inParallel {
                    await<List<Section>>({ SectionManager.getAllSectionsForCourse(assignment.courseId, it, forceNetwork) },
                            onComplete = { sections -> sectionMap += sections.associateBy { it.id } })
                    if (assignment.groupCategoryId > 0) await<List<Group>>({ GroupCategoriesManager.getAllGroupsForCategory(assignment.groupCategoryId, it, forceNetwork) },
                            onComplete = { groups -> groupMap += groups.associateBy { it.id } })
                    dateGroups.flatMap { it.studentIds }.distinct().filter { it !in studentMap }.forEach { studentId ->
                        await<User>({ CourseManager.getCourseStudent(mAssignment.courseId, studentId, it, false) }) { studentMap += studentId to it }
                    }
                }
                mAssignment = assignment
                data.addOrUpdate(dateGroups)
                viewCallback?.showMenu(mAssignment)
            } catch (ignore: Throwable) {
            } finally {
                viewCallback?.onRefreshFinished()
                viewCallback?.checkIfEmpty()
            }
        }
    }

    override fun refresh(forceNetwork: Boolean) {
        apiCalls?.cancel()
        loadData(forceNetwork)
    }

    override fun onDestroyed() {
        super.onDestroyed()
        apiCalls?.cancel()
        EventBus.getDefault().unregister(this)
    }

    @Suppress("unused")
    @Subscribe(sticky = true)
    fun onAssignmentEdited(event: AssignmentUpdatedEvent) {
        event.once(javaClass.simpleName) {
            if (it == mAssignment.id) refresh(true)
        }
    }
}
