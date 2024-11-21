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
package com.instructure.teacher.features.assignment.details

import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.SubmissionSummary
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.*
import com.instructure.teacher.viewinterface.AssignmentDetailsView
import com.instructure.pandautils.blueprint.FragmentPresenter
import kotlinx.coroutines.Job

class AssignmentDetailsPresenter(var mAssignment: Assignment) : FragmentPresenter<AssignmentDetailsView>() {

    private var mApiCalls: Job? = null

    override fun refresh(forceNetwork: Boolean) {}

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    override fun loadData(forceNetwork: Boolean) {
        mApiCalls?.cancel()
        mApiCalls = weave {
            viewCallback?.onRefreshStarted()
            try {
                inParallel {
                    // Get Assignment
                    if (!forceNetwork) {
                        viewCallback?.populateAssignmentDetails(mAssignment)
                    } else {
                        await<Assignment>({ AssignmentManager.getAssignment(mAssignment.id, mAssignment.courseId, true, it) }) {
                            mAssignment = it
                            viewCallback?.populateAssignmentDetails(it)
                        }
                    }

                    await<SubmissionSummary>({ SubmissionManager.getSubmissionSummary(mAssignment.courseId, mAssignment.id, forceNetwork, it) }) {
                        val submissionSummary = it
                        val totalStudents = submissionSummary.graded + submissionSummary.ungraded + submissionSummary.notSubmitted
                        viewCallback?.updateSubmissionDonuts(totalStudents, submissionSummary.graded, submissionSummary.ungraded, submissionSummary.notSubmitted)
                    }
                }
            } catch (ignore: Throwable) {
                Logger.e(ignore.message)
            }
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun getAssignment(assignmentId: Long, course: Course) {
        mApiCalls = tryWeave {
            val assignment = awaitApi<Assignment> { AssignmentManager.getAssignment(assignmentId, course.id, true, it) }

            mAssignment = assignment
            loadData(false)
        } catch {
            // Show error?
            it.printStackTrace()
        }
    }

    override fun onDestroyed() {
        super.onDestroyed()
        mApiCalls?.cancel()
    }

}
