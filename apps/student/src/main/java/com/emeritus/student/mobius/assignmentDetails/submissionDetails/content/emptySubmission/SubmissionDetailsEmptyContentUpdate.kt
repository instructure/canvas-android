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
package com.emeritus.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission

import com.instructure.canvasapi2.models.Assignment
import com.emeritus.student.mobius.common.ui.UpdateInit
import com.spotify.mobius.First
import com.spotify.mobius.Next

class SubmissionDetailsEmptyContentUpdate : UpdateInit<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEvent, SubmissionDetailsEmptyContentEffect>() {
    override fun performInit(model: SubmissionDetailsEmptyContentModel): First<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect> {
        return First.first(model, setOf())
    }

    override fun update(
        model: SubmissionDetailsEmptyContentModel,
        event: SubmissionDetailsEmptyContentEvent
    ): Next<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect> = when (event) {
        SubmissionDetailsEmptyContentEvent.SubmitAssignmentClicked -> {
            // If a user is trying to submit something to an assignment and the assignment is null, something is terribly wrong.
            val submissionTypes = model.assignment.getSubmissionTypes()
            when {
                model.assignment.turnInType == Assignment.TurnInType.QUIZ -> Next.dispatch<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(setOf(
                    SubmissionDetailsEmptyContentEffect.ShowQuizStartView(
                        model.quiz!!,
                        model.course
                    )
                ))
                model.assignment.turnInType == Assignment.TurnInType.DISCUSSION -> Next.dispatch<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(setOf(
                    SubmissionDetailsEmptyContentEffect.ShowDiscussionDetailView(
                        model.assignment.discussionTopicHeader!!.id,
                        model.course
                    )
                ))
                submissionTypes.size == 1 && !(submissionTypes.contains(Assignment.SubmissionType.ONLINE_UPLOAD) && model.isStudioEnabled) -> Next.dispatch<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(setOf(
                    SubmissionDetailsEmptyContentEffect.ShowCreateSubmissionView(
                        submissionTypes.first(),
                        model.course,
                        model.assignment,
                        model.ltiTool
                    )
                ))
                else -> Next.dispatch<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(setOf(
                    SubmissionDetailsEmptyContentEffect.ShowSubmitDialogView(
                        model.assignment,
                        model.course,
                        model.isStudioEnabled,
                        model.studioLTITool
                    )
                ))
            }
        }

        SubmissionDetailsEmptyContentEvent.AudioRecordingClicked -> {
            Next.dispatch(setOf(SubmissionDetailsEmptyContentEffect.ShowAudioRecordingView))
        }
        SubmissionDetailsEmptyContentEvent.VideoRecordingClicked -> {
            Next.dispatch(setOf(SubmissionDetailsEmptyContentEffect.ShowVideoRecordingView))
        }
        SubmissionDetailsEmptyContentEvent.ChooseMediaClicked -> {
            Next.dispatch(setOf(SubmissionDetailsEmptyContentEffect.ShowMediaPickerView))
        }
        SubmissionDetailsEmptyContentEvent.OnMediaPickingError -> {
            Next.dispatch(setOf(SubmissionDetailsEmptyContentEffect.ShowMediaPickingError))
        }
        is SubmissionDetailsEmptyContentEvent.SendAudioRecordingClicked -> {
            if(event.file == null) {
                Next.dispatch<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(setOf(
                    SubmissionDetailsEmptyContentEffect.ShowAudioRecordingError
                ))
            } else {
                val assignment = model.assignment
                Next.dispatch<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(setOf(
                    SubmissionDetailsEmptyContentEffect.UploadAudioSubmission(
                        event.file,
                        model.course,
                        assignment
                    )
                ))
            }
        }
        is SubmissionDetailsEmptyContentEvent.SendVideoRecording -> {
            if (model.videoFileUri == null) {
                Next.dispatch<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(setOf(
                    SubmissionDetailsEmptyContentEffect.ShowVideoRecordingError
                ))
            } else {
                val assignment = model.assignment
                Next.dispatch<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(setOf(
                    SubmissionDetailsEmptyContentEffect.UploadVideoSubmission(
                        model.videoFileUri,
                        model.course,
                        assignment
                    )
                ))
            }
        }
        is SubmissionDetailsEmptyContentEvent.SendMediaFile -> {
            Next.dispatch(setOf(
                SubmissionDetailsEmptyContentEffect.UploadMediaFileSubmission(
                    event.uri,
                    model.course,
                    model.assignment
                )
            ))
        }
        is SubmissionDetailsEmptyContentEvent.OnVideoRecordingError -> {
            Next.dispatch(setOf(SubmissionDetailsEmptyContentEffect.ShowVideoRecordingError))
        }
        is SubmissionDetailsEmptyContentEvent.StoreVideoUri -> {
            Next.next(model.copy(videoFileUri = event.uri))
        }
        SubmissionDetailsEmptyContentEvent.SubmissionStarted -> Next.dispatch(setOf(
            SubmissionDetailsEmptyContentEffect.SubmissionStarted
        ))
    }
}
