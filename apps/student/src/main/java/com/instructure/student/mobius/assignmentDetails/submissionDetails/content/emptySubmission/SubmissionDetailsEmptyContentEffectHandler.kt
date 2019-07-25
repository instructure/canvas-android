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

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyContentView
import com.instructure.student.mobius.assignmentDetails.ui.SubmissionTypesVisibilities
import com.instructure.student.mobius.common.ui.EffectHandler

class SubmissionDetailsEmptyContentEffectHandler : EffectHandler<SubmissionDetailsEmptyContentView, SubmissionDetailsEmptyContentEvent, SubmissionDetailsEmptyContentEffect>() {
    override fun accept(effect: SubmissionDetailsEmptyContentEffect) {
        when(effect) {
            is SubmissionDetailsEmptyContentEffect.ShowSubmitDialogView -> view?.showSubmitDialogView(effect.assignment, effect.course.id, getSubmissionTypesVisibilities(effect.assignment, effect.isArcEnabled))
            is SubmissionDetailsEmptyContentEffect.ShowCreateSubmissionView -> {
                val course = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, effect.course.id)
                when (effect.submissionType) {
                    Assignment.SubmissionType.ONLINE_QUIZ -> {
                        val url = APIHelper.getQuizURL(effect.course.id, effect.assignment.quizId)
                        view?.showQuizOrDiscussionView(url)
                    }
                    Assignment.SubmissionType.DISCUSSION_TOPIC -> {
                        val url = DiscussionTopic.getDiscussionURL(ApiPrefs.protocol, ApiPrefs.domain, effect.course.id, effect.assignment.discussionTopicHeader!!.id)
                        view?.showQuizOrDiscussionView(url)
                    }
                    Assignment.SubmissionType.ONLINE_UPLOAD -> {
                        view?.showFileUploadView(effect.assignment, effect.course.id)
                    }
                    Assignment.SubmissionType.ONLINE_TEXT_ENTRY -> {
                        view?.showOnlineTextEntryView(effect.assignment.id, effect.assignment.name, course)
                    }
                    Assignment.SubmissionType.ONLINE_URL -> {
                        view?.showOnlineUrlEntryView(effect.assignment.id, effect.assignment.name, course)
                    }
                    else -> { // Assignment.SubmissionType.MEDIA_RECORDING
                        view?.showMediaRecordingView(effect.assignment, effect.course.id)
                    }
                }
            }
        }.exhaustive
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

}