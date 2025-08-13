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

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.countCustomGradeStatus
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.pandautils.blueprint.FragmentPresenter
import com.instructure.teacher.viewinterface.AssignmentDetailsView
import kotlinx.coroutines.Job

class AssignmentDetailsPresenter(
    var mAssignment: Assignment,
    private val assignmentsApi: AssignmentAPI.AssignmentInterface
) : FragmentPresenter<AssignmentDetailsView>() {

    private var mApiCalls: Job? = null

    override fun refresh(forceNetwork: Boolean) {}

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    override fun loadData(forceNetwork: Boolean) {
        mApiCalls?.cancel()
        mApiCalls = weave {
            viewCallback?.onRefreshStarted()
            try {
                // Get Assignment
                if (!forceNetwork) {
                    mAssignment
                } else {
                    mAssignment = awaitApi {
                        AssignmentManager.getAssignment(
                            mAssignment.id,
                            mAssignment.courseId,
                            true,
                            it
                        )
                    }
                }
                viewCallback?.populateAssignmentDetails(mAssignment)

                val submissionSummary = awaitApi { SubmissionManager.getSubmissionSummary(mAssignment.courseId, mAssignment.id, forceNetwork, it) }

                val restParams = RestParams(isForceReadFromNetwork = forceNetwork, usePerPageQueryParam = true)
                val submissions = assignmentsApi.getFirstPageSubmissionsForAssignment(
                    mAssignment.courseId, mAssignment.id, restParams
                ).depaginate {
                    assignmentsApi.getNextPageSubmissions(it, restParams)
                }.dataOrNull.orEmpty()

                val customGradeStatedSubmittedCount = submissions.countCustomGradeStatus(
                    "submitted", "pending_review", "graded"
                )

                val customGradeStatedUnsubmittedCount = submissions.countCustomGradeStatus(
                    "unsubmitted",
                    requireNoGradeMatch = false
                )

                val graded = submissionSummary.graded + customGradeStatedSubmittedCount + customGradeStatedUnsubmittedCount
                val ungraded = (submissionSummary.ungraded - customGradeStatedSubmittedCount).coerceAtLeast(0)
                val unsubmitted = (submissionSummary.notSubmitted - customGradeStatedUnsubmittedCount).coerceAtLeast(0)
                val totalStudents = graded + ungraded + unsubmitted

                viewCallback?.updateSubmissionDonuts(
                    totalStudents,
                    graded,
                    ungraded,
                    unsubmitted
                )
            } catch (ignore: Throwable) {
                Logger.e(ignore.message)
            }
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun getAssignment(assignmentId: Long, course: Course) {
        mApiCalls = tryWeave {
            val assignment = awaitApi { AssignmentManager.getAssignment(assignmentId, course.id, true, it) }

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
