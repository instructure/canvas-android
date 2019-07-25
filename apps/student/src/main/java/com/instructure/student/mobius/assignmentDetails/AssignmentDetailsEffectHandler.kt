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

import android.app.Activity
import android.content.Context
import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.QuizManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.weave.StatusCallbackError
import com.instructure.canvasapi2.utils.weave.awaitApiResponse
import com.instructure.pandautils.services.NotoriousUploadService
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.requestPermissions
import com.instructure.student.Submission
import com.instructure.student.db.Db
import com.instructure.student.db.getInstance
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsView
import com.instructure.student.mobius.assignmentDetails.ui.SubmissionTypesVisibilities
import com.instructure.student.mobius.common.ui.EffectHandler
import com.instructure.student.mobius.common.ui.SubmissionService
import com.instructure.student.util.getResourceSelectorUrl
import com.instructure.student.util.getStudioLTITool
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import com.squareup.sqldelight.Query
import kotlinx.coroutines.launch
import java.io.File

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
            AssignmentDetailsEffect.ShowAudioRecordingView -> launchAudio()
            AssignmentDetailsEffect.ShowAudioRecordingError -> view?.showAudioRecordingError()
            is AssignmentDetailsEffect.ShowSubmitDialogView -> {
                val studioUrl = effect.studioLTITool?.getResourceSelectorUrl(effect.course, effect.assignment)
                view?.showSubmitDialogView(effect.assignment, effect.course.id, getSubmissionTypesVisibilities(effect.assignment, effect.isStudioEnabled), studioUrl, effect.studioLTITool?.name)
            }
            is AssignmentDetailsEffect.ShowSubmissionView -> view?.showSubmissionView(effect.assignmentId, effect.course)
            is AssignmentDetailsEffect.ShowQuizStartView -> view?.showQuizStartView(effect.course, effect.quiz)
            is AssignmentDetailsEffect.ShowDiscussionDetailView -> view?.showDiscussionDetailView(effect.course, effect.discussionTopicHeaderId)
            is AssignmentDetailsEffect.ShowDiscussionAttachment -> view?.showDiscussionAttachment(effect.course, effect.discussionAttachment)
            is AssignmentDetailsEffect.UploadMediaSubmission -> uploadAudioRecording(effect.file, effect.assignment, effect.course)
            is AssignmentDetailsEffect.ShowUploadStatusView -> {
                when (effect.submission.submissionType) {
                    Assignment.SubmissionType.ONLINE_UPLOAD.apiString, Assignment.SubmissionType.MEDIA_RECORDING.apiString -> {
                        view?.showUploadStatusView(effect.submission.id)
                    }
                    // TODO: show the appropriate submission screen (text/url/etc...)
                    else -> Unit
                }
            }
            is AssignmentDetailsEffect.LoadData -> loadData(effect)
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
            val assignmentResult = try {
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
            val assignmentUrl = assignmentResult.dataOrNull?.url
            val ltiTool = if (assignmentUrl != null && assignmentResult.dataOrNull?.getSubmissionTypes()?.contains(Assignment.SubmissionType.EXTERNAL_TOOL) == true)
                SubmissionManager.getLtiFromAuthenticationUrlAsync(assignmentUrl, true).await()
            else DataResult.Fail(null)

            // We need to know if they can make submissions through Studio, only for file uploads
            val studioLTITool: DataResult<LTITool> = if (assignmentResult.isSuccess && assignmentResult.dataOrThrow.getSubmissionTypes().contains(Assignment.SubmissionType.ONLINE_UPLOAD)) {
               effect.courseId.getStudioLTITool()
            } else DataResult.Fail(null)

            val isStudioEnabled = studioLTITool.isSuccess

            val quizResult = if (assignmentResult.dataOrNull?.turnInType == (Assignment.TurnInType.QUIZ) && assignmentResult.dataOrNull?.quizId != 0L) {
                try {
                    QuizManager.getQuizAsync(effect.courseId, assignmentResult.dataOrNull?.quizId!!, effect.forceNetwork).await()
                } catch (e: StatusCallbackError) {
                    if (e.response?.code() == 401) {
                        DataResult.Fail(Failure.Authorization(e.response?.message()))
                    } else {
                        DataResult.Fail(Failure.Network(e.response?.message()))
                    }
                }
            } else null

            consumer.accept(
                AssignmentDetailsEvent.DataLoaded(
                    assignmentResult,
                    isStudioEnabled,
                    studioLTITool,
                    ltiTool,
                    dbSubmission,
                    quizResult
                )
            )
        }
    }

    private fun getSubmissionTypesVisibilities(assignment: Assignment, isStudioEnabled: Boolean): SubmissionTypesVisibilities {
        val visibilities = SubmissionTypesVisibilities()

        val submissionTypes = assignment.getSubmissionTypes()

        for (submissionType in submissionTypes) {
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (submissionType) {
                Assignment.SubmissionType.ONLINE_UPLOAD -> {
                    visibilities.fileUpload = true
                    visibilities.studioUpload = isStudioEnabled
                }
                Assignment.SubmissionType.ONLINE_TEXT_ENTRY -> visibilities.textEntry = true
                Assignment.SubmissionType.ONLINE_URL -> visibilities.urlEntry = true
                Assignment.SubmissionType.MEDIA_RECORDING -> visibilities.mediaRecording = true
            }
        }

        return visibilities
    }

    private fun launchAudio() {
        if(needsPermissions(::launchAudio, PermissionUtils.RECORD_AUDIO)) return
        view?.showAudioRecordingView()
    }

    private fun needsPermissions(successCallback: () -> Unit, vararg permissions: String): Boolean {
        if (PermissionUtils.hasPermissions(context as Activity, *permissions)) {
            return false
        }

        context.requestPermissions(setOf(*permissions)) { results ->
            if (results.isNotEmpty() && results.all { it.value }) {
                successCallback()
            } else {
                view?.showPermissionDeniedToast()
            }
        }
        return true
    }

    private fun uploadAudioRecording(file: File, assignment: Assignment, course: Course) {
        SubmissionService.startMediaSubmission(
                context = context,
                canvasContext = course,
                assignmentId = assignment.id,
                assignmentGroupCategoryId = assignment.groupCategoryId,
                assignmentName = assignment.name,
                mediaFilePath = file.path,
                notoriousAction = NotoriousUploadService.ACTION.ASSIGNMENT_SUBMISSION
        )
    }
}
