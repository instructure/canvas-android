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

package com.instructure.teacher.features.assignment.list

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.filterWithQuery
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.teacher.viewinterface.AssignmentListView
import com.instructure.pandautils.blueprint.SyncExpandablePresenter
import kotlinx.coroutines.Job
import retrofit2.Response
import java.util.*

class AssignmentListPresenter(private val mCanvasContext: CanvasContext) :
        SyncExpandablePresenter<AssignmentGroup, Assignment, AssignmentListView>(AssignmentGroup::class.java, Assignment::class.java) {

    private val ID_ALL_GRADING_PERIODS = -123L

    private var mSelectedPeriodId: Long? = null
    private var mGradingPeriods = ArrayList<GradingPeriod>()
    private var mDefaultGradingPeriod: GradingPeriod? = null

    private var assignmentGroups = emptyList<AssignmentGroup>()
    var searchQuery = ""
        set(value) {
            field = value
            clearData()
            populateData()
            viewCallback?.onRefreshFinished()
            viewCallback?.checkIfEmpty()
        }

    private var groupAssignmentsWithAssignmentsApiCall: Job? = null
    private var groupAssignmentsWithAssignmentsInGradingPeriodApiCall: Job? = null

    //todo add disable logic?

    override fun loadData(forceNetwork: Boolean) {
        if(forceNetwork) {
            clearData()
        } else if (data.size() > 0) {
            viewCallback?.setGradingPeriods(mGradingPeriods.toList())
            return
        }
        onRefreshStarted()
        if (mSelectedPeriodId == null) {

            if (mDefaultGradingPeriod == null) {
                mDefaultGradingPeriod = GradingPeriod(title = viewCallback?.getDefaultGradingPeriodTitle(), id = ID_ALL_GRADING_PERIODS)
            }

            CourseManager.getGradingPeriodsForCourse(object : StatusCallback<GradingPeriodResponse>() {
                override fun onResponse(response: Response<GradingPeriodResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                    mGradingPeriods.clear()
                    response.body()?.let {
                        if (!it.gradingPeriodList.isEmpty()) {
                            mGradingPeriods.add(mDefaultGradingPeriod!!)
                            mGradingPeriods.addAll(it.gradingPeriodList)
                        }
                    }

                }

                override fun onFinished(type: ApiType) {
                    mSelectedPeriodId = ID_ALL_GRADING_PERIODS
                    loadData(forceNetwork)
                }
            }, mCanvasContext.id, forceNetwork)

        } else if (mSelectedPeriodId == ID_ALL_GRADING_PERIODS) {
            getAssignmentGroupsWithAssignmentsWeave(forceNetwork)
        } else {
            getAssignmentGroupsWithAssignmentsForGradingPeriod(forceNetwork)
        }

        viewCallback?.setGradingPeriods(mGradingPeriods.toList())
    }

    private fun getAssignmentGroupsWithAssignmentsForGradingPeriod(forceNetwork: Boolean) {
        groupAssignmentsWithAssignmentsInGradingPeriodApiCall = weave {
            try {
                assignmentGroups = awaitApi<List<AssignmentGroup>> {
                    AssignmentManager.getAssignmentGroupsWithAssignmentsForGradingPeriod(
                        mCanvasContext.id,
                        mSelectedPeriodId as Long,
                        forceNetwork,
                        it
                    )
                }.filter { !it.assignments.isEmpty() }
                populateData()
            } catch (error: Throwable) {
                Logger.e(error.message)
            } finally {
                viewCallback?.onRefreshFinished()
                viewCallback?.checkIfEmpty()
            }
        }
    }

    private fun getAssignmentGroupsWithAssignmentsWeave(forceNetwork: Boolean) {
        groupAssignmentsWithAssignmentsApiCall = weave {
            try {
                assignmentGroups = awaitApi<List<AssignmentGroup>> {
                    AssignmentManager.getAssignmentGroupsWithAssignments(mCanvasContext.id, forceNetwork, it)
                }.filter { !it.assignments.isEmpty() }
                populateData()
            } catch (error: Throwable) {
                Logger.e(error.message)
            } finally {
                viewCallback?.onRefreshFinished()
                viewCallback?.checkIfEmpty()
            }
        }
    }

    private fun populateData() {
        assignmentGroups.forEach {
            data.addOrUpdateAllItems(it, it.assignments.filterWithQuery(searchQuery, Assignment::name))
        }
    }

    override fun getUniqueGroupId(group: AssignmentGroup): Long {
        return group.id
    }

    override fun refresh(forceNetwork: Boolean) {
        onRefreshStarted()
        groupAssignmentsWithAssignmentsApiCall?.cancel()
        clearData()
        loadData(forceNetwork)
    }

    override fun onDestroyed() {
        super.onDestroyed()
        groupAssignmentsWithAssignmentsApiCall?.cancel()
    }

    fun selectGradingPeriodIndex(gradingPeriodIdx: Int) {
        var newPeriodId = ID_ALL_GRADING_PERIODS

        if (gradingPeriodIdx in 0 until mGradingPeriods.size) {
            newPeriodId = mGradingPeriods[gradingPeriodIdx].id
        }

        // Refresh if selected grading period has changed
        if(mSelectedPeriodId != newPeriodId) {
            mSelectedPeriodId = newPeriodId
            refresh(false)
        }
    }

    fun clearGradingPeriodFilter() {
        mSelectedPeriodId = ID_ALL_GRADING_PERIODS
        refresh(false)
    }

    fun getSelectedGradingPeriodId() = mSelectedPeriodId!!
    fun getGradingPeriods() = mGradingPeriods
    fun getCanvasContext() = mCanvasContext

    override fun compare(group: AssignmentGroup, item1: Assignment, item2: Assignment): Int {
        var result = item1.position.compareTo(item2.position)
        if (result == 0) {
            result = item1.name!!.compareTo(item2.name!!,true)
        }
        return result
    }
}
