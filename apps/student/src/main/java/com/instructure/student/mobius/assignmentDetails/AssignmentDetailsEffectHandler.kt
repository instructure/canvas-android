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
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.QuizManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.weave.StatusCallbackError
import com.instructure.canvasapi2.utils.weave.awaitApiResponse
import com.instructure.student.Submission
import com.instructure.student.db.Db
import com.instructure.student.db.getInstance
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsFragment
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsView
import com.instructure.student.mobius.common.ui.EffectHandler
import com.instructure.student.util.getResourceSelectorUrl
import com.instructure.student.util.getStudioLTITool
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
            AssignmentDetailsEffect.ShowVideoRecordingView -> context.launchVideo({ AssignmentDetailsEvent.StoreVideoUri(it) }, { view?.showPermissionDeniedToast() }, consumer, AssignmentDetailsFragment.VIDEO_REQUEST_CODE)
            AssignmentDetailsEffect.ShowAudioRecordingView -> context.launchAudio({ view?.showPermissionDeniedToast() }, { view?.showAudioRecordingView() })
            AssignmentDetailsEffect.ShowMediaPickerView -> launchMediaPicker()
            AssignmentDetailsEffect.ShowVideoRecordingError -> view?.showVideoRecordingError()
            AssignmentDetailsEffect.ShowAudioRecordingError -> view?.showAudioRecordingError()
            AssignmentDetailsEffect.ShowMediaPickingError -> view?.showMediaPickingError()
            is AssignmentDetailsEffect.UploadVideoSubmission -> view?.launchFilePickerView(effect.uri, effect.course, effect.assignment)
            is AssignmentDetailsEffect.UploadMediaFileSubmission -> view?.launchFilePickerView(effect.uri, effect.course, effect.assignment)
            AssignmentDetailsEffect.ShowBookmarkDialog -> view?.showBookmarkDialog()
            is AssignmentDetailsEffect.ShowSubmitDialogView -> {
                val studioUrl = effect.studioLTITool?.getResourceSelectorUrl(effect.course, effect.assignment)
                view?.showSubmitDialogView(
                    effect.assignment,
                    effect.course.id,
                    getSubmissionTypesVisibilities(effect.assignment, effect.isStudioEnabled),
                    studioUrl,
                    effect.studioLTITool?.name
                )
            }
            is AssignmentDetailsEffect.ShowSubmissionView -> view?.showSubmissionView(effect.assignmentId, effect.course, effect.isObserver)
            is AssignmentDetailsEffect.ShowQuizStartView -> view?.showQuizStartView(effect.course, effect.quiz)
            is AssignmentDetailsEffect.ShowDiscussionDetailView -> view?.showDiscussionDetailView(effect.course, effect.discussionTopicHeaderId)
            is AssignmentDetailsEffect.ShowDiscussionAttachment -> view?.showDiscussionAttachment(effect.course, effect.discussionAttachment)
            is AssignmentDetailsEffect.UploadAudioSubmission -> uploadAudioRecording(context, effect.file, effect.assignment, effect.course)
            is AssignmentDetailsEffect.ShowUploadStatusView -> {
                when (effect.submission.submissionType) {
                    Assignment.SubmissionType.ONLINE_UPLOAD.apiString, Assignment.SubmissionType.MEDIA_RECORDING.apiString -> view?.showUploadStatusView(effect.submission.id)
                    Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString -> {
                        view?.showOnlineTextEntryView(
                            effect.submission.assignmentId,
                            effect.submission.assignmentName,
                            effect.submission.submissionEntry,
                            effect.submission.errorFlag
                        )
                    }
                    Assignment.SubmissionType.ONLINE_URL.apiString -> {
                        view?.showOnlineUrlEntryView(
                            effect.submission.assignmentId,
                            effect.submission.assignmentName,
                            effect.submission.canvasContext,
                            effect.submission.submissionEntry,
                            effect.submission.errorFlag
                        )
                    }
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
                    Assignment.SubmissionType.ONLINE_UPLOAD -> view?.showFileUploadView(effect.assignment)
                    Assignment.SubmissionType.ONLINE_TEXT_ENTRY -> view?.showOnlineTextEntryView(effect.assignment.id, effect.assignment.name)
                    Assignment.SubmissionType.ONLINE_URL -> view?.showOnlineUrlEntryView(effect.assignment.id, effect.assignment.name, effect.course)
                    Assignment.SubmissionType.EXTERNAL_TOOL, Assignment.SubmissionType.BASIC_LTI_LAUNCH -> view?.showLTIView(effect.course, effect.ltiUrl
                            ?: "", effect.assignment.name ?: "")
                    else -> view?.showMediaRecordingView(effect.assignment) // Assignment.SubmissionType.MEDIA_RECORDING
                }
            }
            is AssignmentDetailsEffect.RouteInternally -> view?.routeInternally(effect.url, ApiPrefs.domain, effect.course, effect.assignment)
        }.exhaustive
    }

    private fun loadData(effect: AssignmentDetailsEffect.LoadData) {
        launch {
            // In order to handle observers, we need to fetch the course and its enrollments
            val courseResult = try {
                CourseManager.getCourseWithGradeAsync(effect.courseId, true).await()
            } catch (e: StatusCallbackError) {
                DataResult.Fail(null)
            }

            val isObserver = courseResult.isSuccess && courseResult.dataOrNull != null && courseResult.dataOrNull!!.enrollments!!.firstOrNull { it.isObserver } != null
            val assignmentResult = try {
                if (isObserver) {
                    // Valid observer enrollment, this means we need to include observers in our assignment response
                    val assignmentResponse = awaitApiResponse<ObserveeAssignment> {
                        AssignmentManager.getAssignmentIncludeObservees(effect.assignmentId, effect.courseId, effect.forceNetwork, it)
                    }
                    val assignmentWithObserverSubmission = assignmentResponse.body()!!.toAssignmentForObservee()
                    if (assignmentWithObserverSubmission != null) {
                        DataResult.Success(assignmentWithObserverSubmission)
                    } else {
                        DataResult.Fail(null)
                    }
                } else {
                    // Something went wrong with the course fetch, or there was no valid observer, so we fetch the student assignment as normal
                    val assignmentResponse = awaitApiResponse<Assignment> {
                        AssignmentManager.getAssignment(effect.assignmentId, effect.courseId, effect.forceNetwork, it)
                    }
                    DataResult.Success(assignmentResponse.body()!!)
                }
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

            logEvent(getAnalyticsString(quizResult, assignmentResult))

            consumer.accept(
                AssignmentDetailsEvent.DataLoaded(
                    assignmentResult,
                    isStudioEnabled,
                    studioLTITool,
                    ltiTool,
                    dbSubmission,
                    quizResult,
                    isObserver
                )
            )
        }
    }

    private fun launchMediaPicker() {
        chooseMediaIntent.let {
            (context as Activity).startActivityForResult(it, AssignmentDetailsFragment.CHOOSE_MEDIA_REQUEST_CODE)
        }
    }

    private fun getAnalyticsString(quizResult: DataResult<Quiz>?, assignmentResult: DataResult<Assignment>): String {
        return when {
            quizResult != null -> AnalyticsEventConstants.ASSIGNMENT_DETAIL_QUIZ
            assignmentResult.dataOrNull?.discussionTopicHeader != null -> AnalyticsEventConstants.ASSIGNMENT_DETAIL_DISCUSSION
            else -> AnalyticsEventConstants.ASSIGNMENT_DETAIL_ASSIGNMENT
        }
    }
}
