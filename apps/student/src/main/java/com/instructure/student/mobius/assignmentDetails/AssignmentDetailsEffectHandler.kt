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
package com.instructure.student.mobius.assignmentDetails

import android.content.Context
import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.weave.StatusCallbackError
import com.instructure.canvasapi2.utils.weave.awaitApiResponse
import com.instructure.student.Submission
import com.instructure.student.db.Db
import com.instructure.student.db.getInstance
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsView
import com.instructure.student.mobius.assignmentDetails.ui.SubmissionTypesVisibilities
import com.instructure.student.mobius.common.ui.EffectHandler
import com.instructure.student.util.isArcEnabled
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import com.squareup.sqldelight.Query
import kotlinx.coroutines.launch

class AssignmentDetailsEffectHandler(val context: Context, val assignmentId: Long) :
    EffectHandler<AssignmentDetailsView, AssignmentDetailsEvent, AssignmentDetailsEffect>(),
    Query.Listener {

    private var submissionQuery: Query<Submission>? = null

    override fun connect(output: Consumer<AssignmentDetailsEvent>): Connection<AssignmentDetailsEffect> {
        val db = Db.getInstance(context)
        submissionQuery = db.submissionQueries.getSubmissionsByAssignmentId(assignmentId, ApiPrefs.user!!.id)
        submissionQuery!!.addListener(this@AssignmentDetailsEffectHandler)

        return super.connect(output)
    }

    override fun dispose() {
        super.dispose()
        submissionQuery?.removeListener(this)
        submissionQuery = null
    }

    override fun queryResultsChanged() {
        launch {
            val submission = submissionQuery?.executeAsList()?.lastOrNull()
            consumer.accept(AssignmentDetailsEvent.SubmissionStatusUpdated(submission))
        }
    }

    override fun accept(effect: AssignmentDetailsEffect) {
        when (effect) {
            is AssignmentDetailsEffect.ShowSubmitDialogView -> view?.showSubmitDialogView(effect.assignment, effect.course.id, getSubmissionTypesVisibilities(effect.assignment, effect.isArcEnabled))
            is AssignmentDetailsEffect.ShowSubmissionView -> view?.showSubmissionView(effect.assignmentId, effect.course)
            is AssignmentDetailsEffect.ShowUploadStatusView -> view?.showUploadStatusView(effect.submission.id) // TODO: show upload status for files/media, otherwise show the appropriate submission screen (text/url/etc...)
            is AssignmentDetailsEffect.LoadData -> {
                loadData(effect)
            }
            is AssignmentDetailsEffect.ShowCreateSubmissionView -> {
                when (effect.submissionType) {
                    Assignment.SubmissionType.ONLINE_QUIZ -> {
                        val url = APIHelper.getQuizURL(effect.course.id, effect.assignment.quizId)
                        view?.showQuizOrDiscussionView(url)
                    }
                    Assignment.SubmissionType.DISCUSSION_TOPIC -> {
                        val url = DiscussionTopic.getDiscussionURL(ApiPrefs.protocol, ApiPrefs.domain, effect.assignment.courseId, effect.assignment.discussionTopicHeader!!.id)
                        view?.showQuizOrDiscussionView(url)
                    }
                    Assignment.SubmissionType.ONLINE_UPLOAD -> {
                        view?.showFileUploadView(effect.assignment)
                    }
                    Assignment.SubmissionType.ONLINE_TEXT_ENTRY -> {
                        view?.showOnlineTextEntryView(effect.assignment.id, effect.assignment.name, effect.assignment.submission?.body)
                    }
                    Assignment.SubmissionType.ONLINE_URL -> {
                        view?.showOnlineUrlEntryView(effect.assignment.id, effect.assignment.name, effect.course)
                    }
                    Assignment.SubmissionType.EXTERNAL_TOOL, Assignment.SubmissionType.BASIC_LTI_LAUNCH -> {
                        view?.showLTIView(effect.course, effect.ltiUrl ?: "", effect.assignment.name ?: "")
                    }
                    else -> { // Assignment.SubmissionType.MEDIA_RECORDING
                        view?.showMediaRecordingView(effect.assignment, effect.course.id)
                    }
                }
            }
            is AssignmentDetailsEffect.RouteInternally -> {
                view?.routeInternally(effect.url, ApiPrefs.domain, effect.course, effect.assignment)
            }
        }.exhaustive
    }

    private fun loadData(effect: AssignmentDetailsEffect.LoadData) {
        launch {
            val result = try {
                val assignmentResponse = awaitApiResponse<Assignment> {
                    AssignmentManager.getAssignment(effect.assignmentId, effect.courseId, effect.forceNetwork, it)
                }
                DataResult.Success(assignmentResponse.body()!!)
            } catch (e: StatusCallbackError) {
                if (e.response?.code() == 401) {
                    DataResult.Fail(Failure.Authorization(e.response?.message()))
                } else {
                    DataResult.Fail(Failure.Network(e.response?.message()))
                }
            }

            val dbSubmission = submissionQuery?.executeAsList()?.lastOrNull()

            // Determine if we need to retrieve an authenticated LTI URL based on whether this assignment accepts external tool submissions
            val assignmentUrl = result.dataOrNull?.url
            val ltiTool = if (assignmentUrl != null && result.dataOrNull?.getSubmissionTypes()?.contains(Assignment.SubmissionType.EXTERNAL_TOOL) == true)
                SubmissionManager.getLtiFromAuthenticationUrlAsync(assignmentUrl, true).await()
            else DataResult.Fail(null)

            // We need to know if they can make submissions through arc, only for file uploads
            val isArcEnabled = if (result.isSuccess && result.dataOrThrow.getSubmissionTypes().contains(Assignment.SubmissionType.ONLINE_UPLOAD)) {
                effect.courseId.isArcEnabled()
            } else false

            consumer.accept(
                AssignmentDetailsEvent.DataLoaded(
                    result,
                    isArcEnabled,
                    ltiTool,
                    dbSubmission
                )
            )
        }
    }

    private fun getSubmissionTypesVisibilities(assignment: Assignment, isArcEnabled: Boolean): SubmissionTypesVisibilities {
        val visibilities = SubmissionTypesVisibilities()

        val submissionTypes = assignment.getSubmissionTypes()

        for (submissionType in submissionTypes) {
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (submissionType) {
                Assignment.SubmissionType.ONLINE_UPLOAD -> {
                    visibilities.fileUpload = true
                    visibilities.arcUpload = isArcEnabled
                }
                Assignment.SubmissionType.ONLINE_TEXT_ENTRY -> visibilities.textEntry = true
                Assignment.SubmissionType.ONLINE_URL -> visibilities.urlEntry = true
                Assignment.SubmissionType.MEDIA_RECORDING -> visibilities.mediaRecording = true
            }
        }

        return visibilities
    }
}
