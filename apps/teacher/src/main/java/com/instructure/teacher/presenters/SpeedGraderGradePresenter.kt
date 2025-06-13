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
package com.instructure.teacher.presenters

import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.teacher.events.AssignmentGradedEvent
import com.instructure.teacher.events.SubmissionUpdatedEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.viewinterface.SpeedGraderGradeView
import com.instructure.pandautils.blueprint.FragmentPresenter
import kotlinx.coroutines.*

class SpeedGraderGradePresenter(var submission: Submission?, val assignment: Assignment, val course: Course, val assignee: Assignee) : FragmentPresenter<SpeedGraderGradeView>() {

    private var mPostGradeAPICall: Job? = null
    private var refreshSubmissionApiCall: Job? = null

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun  updateGrade(grade: String, isExcused: Boolean) {
        mPostGradeAPICall = weave {
            try {
                viewCallback?.onRefreshStarted()
                val assigneeId = when (assignee) {
                    is StudentAssignee -> assignee.student.id
                    is GroupAssignee -> assignee.students.firstOrNull()?.id ?: assignee.group.id
                }
                val newSubmission = when (isExcused) {
                // TODO: Marking as EXCUSED only seems to work for the specified student and not the whole group.
                    true -> awaitApi<Submission> { SubmissionManager.postSubmissionExcusedStatus(course.id, assignment.id, assigneeId, true, true, it) }
                    else -> awaitApi { SubmissionManager.postSubmissionGrade(course.id, assignment.id, assigneeId, grade, false, true, it) }
                }

                /* The submission in the API response does not include the rubric assessment, so we
                need to transfer any existing assessment from the original submission */
                submission?.rubricAssessment?.let { newSubmission.rubricAssessment = it }

                updateSubmission(newSubmission)
                viewCallback?.onRefreshFinished()
            } catch (e: Throwable) {
                viewCallback?.updateGradeError()
            }
        }
    }

    fun refreshSubmission() {
        refreshSubmissionApiCall = GlobalScope.launch(Dispatchers.Main) {
            try {
                viewCallback?.onRefreshStarted()

                // Try to update our submission for post/hide grades
                val newSubmission = SubmissionManager.getSingleSubmissionAsync(
                    course.id,
                    assignment.id,
                    submission?.userId ?: return@launch,
                    true
                ).await().dataOrNull ?: return@launch

                submission = newSubmission
                viewCallback?.updateGradeText()
                viewCallback?.onRefreshFinished()
            } catch (e: Throwable) {
                viewCallback?.updateGradeError()
            }
        }
    }

    override fun onDestroyed() {
        super.onDestroyed()
        mPostGradeAPICall?.cancel()
        refreshSubmissionApiCall?.cancel()
    }

    override fun loadData(forceNetwork: Boolean) { }
    override fun refresh(forceNetwork: Boolean) { }

    fun updateSubmission(newSubmission: Submission) {
        submission = newSubmission
        viewCallback?.updateGradeText()
        AssignmentGradedEvent(assignment.id).post() //post bus event
        SubmissionUpdatedEvent(newSubmission).post()
        CanvasRestAdapter.clearCacheUrls("courses/${course.id}/assignment_groups")
        CanvasRestAdapter.clearCacheUrls("courses/${course.id}/assignments/${assignment.id}")
    }

}
