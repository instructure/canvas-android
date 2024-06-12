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
import com.instructure.pandautils.utils.getFragmentActivity
import com.instructure.student.mobius.assignmentDetails.*
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyContentFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyContentView
import com.instructure.student.mobius.common.ui.EffectHandler
import com.instructure.student.mobius.common.ui.SubmissionHelper
import com.instructure.student.util.getResourceSelectorUrl

class SubmissionDetailsEmptyContentEffectHandler(val context: Context, val assignmentId: Long, val submissionHelper: SubmissionHelper) :
    EffectHandler<SubmissionDetailsEmptyContentView, SubmissionDetailsEmptyContentEvent, SubmissionDetailsEmptyContentEffect>() {

    override fun accept(effect: SubmissionDetailsEmptyContentEffect) {
        when (effect) {
            SubmissionDetailsEmptyContentEffect.ShowVideoRecordingView -> context.getFragmentActivity().launchVideo({SubmissionDetailsEmptyContentEvent.StoreVideoUri(it)}, { view?.showPermissionDeniedToast() }, consumer, SubmissionDetailsEmptyContentFragment.VIDEO_REQUEST_CODE)
            SubmissionDetailsEmptyContentEffect.ShowAudioRecordingView -> context.getFragmentActivity().launchAudio({ view?.showPermissionDeniedToast() }, { view?.showAudioRecordingView() })
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
            is SubmissionDetailsEmptyContentEffect.UploadAudioSubmission -> uploadAudioRecording(submissionHelper, effect.file, effect.assignment, effect.course)
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
                    Assignment.SubmissionType.STUDENT_ANNOTATION -> view?.showStudentAnnotationView(effect.assignment)
                    Assignment.SubmissionType.EXTERNAL_TOOL, Assignment.SubmissionType.BASIC_LTI_LAUNCH -> view?.showLTIView(effect.course, title = effect.assignment.name ?: "", ltiTool = effect.ltiTool)
                    else -> view?.showMediaRecordingView() // e.g. Assignment.SubmissionType.MEDIA_RECORDING
                }
            }
            SubmissionDetailsEmptyContentEffect.SubmissionStarted -> view?.returnToAssignmentDetails()
        }.exhaustive
    }

    private fun launchMediaPicker() {
       chooseMediaIntent.let {
            context.getFragmentActivity().startActivityForResult(it, SubmissionDetailsEmptyContentFragment.CHOOSE_MEDIA_REQUEST_CODE)
        }
    }
}
