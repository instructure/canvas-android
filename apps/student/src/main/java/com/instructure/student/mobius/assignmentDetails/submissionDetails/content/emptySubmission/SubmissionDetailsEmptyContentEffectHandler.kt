/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission

import android.app.Activity
import android.content.Context
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.student.Submission
import com.instructure.student.db.Db
import com.instructure.student.db.getInstance
import com.instructure.student.mobius.assignmentDetails.getSubmissionTypesVisibilities
import com.instructure.student.mobius.assignmentDetails.launchAudio
import com.instructure.student.mobius.assignmentDetails.launchVideo
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyContentFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyContentView
import com.instructure.student.mobius.assignmentDetails.ui.SubmissionTypesVisibilities
import com.instructure.student.mobius.assignmentDetails.uploadAudioRecording
import com.instructure.student.mobius.common.ui.EffectHandler
import com.instructure.student.util.getResourceSelectorUrl
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import com.squareup.sqldelight.Query

class SubmissionDetailsEmptyContentEffectHandler(val context: Context, val assignmentId: Long) :
    EffectHandler<SubmissionDetailsEmptyContentView, SubmissionDetailsEmptyContentEvent, SubmissionDetailsEmptyContentEffect>(),
    Query.Listener {

    private var submissionQuery: Query<Submission>? = null

    override fun connect(output: Consumer<SubmissionDetailsEmptyContentEvent>): Connection<SubmissionDetailsEmptyContentEffect> {
        val db = Db.getInstance(context)
        submissionQuery = db.submissionQueries.getSubmissionsByAssignmentId(assignmentId, ApiPrefs.user?.id ?: -1)
        submissionQuery?.addListener(this@SubmissionDetailsEmptyContentEffectHandler)

        return super.connect(output)
    }

    override fun dispose() {
        super.dispose()
        submissionQuery?.removeListener(this)
        submissionQuery = null
    }

    override fun queryResultsChanged() {
        // If we have a change in submission query, then a submission was made - go back to the Assignment details page
        view?.returnToAssignmentDetails()

        // Only want to catch the update once
        submissionQuery?.removeListener(this)
        submissionQuery = null
    }

    override fun accept(effect: SubmissionDetailsEmptyContentEffect) {
        when (effect) {
            SubmissionDetailsEmptyContentEffect.ShowVideoRecordingView -> context.launchVideo({SubmissionDetailsEmptyContentEvent.StoreVideoUri(it)}, { view?.showPermissionDeniedToast() }, consumer, SubmissionDetailsEmptyContentFragment.VIDEO_REQUEST_CODE)
            SubmissionDetailsEmptyContentEffect.ShowAudioRecordingView -> context.launchAudio({ view?.showPermissionDeniedToast() }, { view?.showAudioRecordingView() })
            SubmissionDetailsEmptyContentEffect.ShowMediaPickerView -> launchMediaPicker()
            SubmissionDetailsEmptyContentEffect.ShowVideoRecordingError -> view?.showVideoRecordingError()
            SubmissionDetailsEmptyContentEffect.ShowAudioRecordingError -> view?.showAudioRecordingError()
            SubmissionDetailsEmptyContentEffect.ShowMediaPickingError -> view?.showMediaPickingError()
            is SubmissionDetailsEmptyContentEffect.UploadVideoSubmission -> view?.launchFilePickerView(effect.uri, effect.course, effect.assignment)
            is SubmissionDetailsEmptyContentEffect.UploadMediaFileSubmission -> view?.launchFilePickerView(effect.uri, effect.course, effect.assignment)
            is SubmissionDetailsEmptyContentEffect.ShowSubmitDialogView -> {
                val studioUrl = effect.studioLTITool?.getResourceSelectorUrl(effect.course, effect.assignment)
                view?.showSubmitDialogView(
                    effect.assignment,
                    getSubmissionTypesVisibilities(effect.assignment, effect.isStudioEnabled),
                    studioUrl,
                    effect.studioLTITool?.name
                )
            }

            is SubmissionDetailsEmptyContentEffect.ShowQuizStartView -> view?.showQuizStartView(effect.course, effect.quiz)
            is SubmissionDetailsEmptyContentEffect.ShowDiscussionDetailView -> view?.showDiscussionDetailView(effect.course, effect.discussionTopicHeaderId)
            is SubmissionDetailsEmptyContentEffect.UploadAudioSubmission -> uploadAudioRecording(context, effect.file, effect.assignment, effect.course)
            is SubmissionDetailsEmptyContentEffect.ShowCreateSubmissionView -> {
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
                    Assignment.SubmissionType.EXTERNAL_TOOL, Assignment.SubmissionType.BASIC_LTI_LAUNCH -> view?.showLTIView(effect.course, effect.ltiUrl ?: "", effect.assignment.name ?: "")
                    else -> view?.showMediaRecordingView() // Assignment.SubmissionType.MEDIA_RECORDING
                }
            }
        }.exhaustive
    }

    private fun launchMediaPicker() {
        view?.getChooseMediaIntent()?.let {
            (context as Activity).startActivityForResult(it, SubmissionDetailsEmptyContentFragment.CHOOSE_MEDIA_REQUEST_CODE)
        }
    }
}
