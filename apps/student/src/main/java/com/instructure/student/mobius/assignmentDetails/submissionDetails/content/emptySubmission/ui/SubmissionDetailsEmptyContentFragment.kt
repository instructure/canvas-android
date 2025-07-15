/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 */package com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsEmptyContentEventBusSource
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentEffectHandler
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentEvent
import com.instructure.student.mobius.common.LiveDataSource
import com.instructure.student.mobius.common.ui.SubmissionHelper
import com.instructure.pandautils.room.studentdb.StudentDb
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SubmissionDetailsEmptyContentFragment : BaseSubmissionDetailsEmptyContentFragment() {

    @Inject
    lateinit var submissionHelper: SubmissionHelper

    @Inject
    lateinit var studentDb: StudentDb

    override fun makeEffectHandler() = SubmissionDetailsEmptyContentEffectHandler(requireContext(), assignment.id, submissionHelper)

    override fun getExternalEventSources() = listOf(
        SubmissionDetailsEmptyContentEventBusSource(),
        LiveDataSource.of<CreateSubmissionEntity, SubmissionDetailsEmptyContentEvent>(
            studentDb.submissionDao()
                .findSubmissionByAssignmentIdLiveData(assignment.id, ApiPrefs.user?.id ?: -1)
        ) { submission ->
            if (submission != null && submission.progress == null) {
                // Submission was just inserted - back out to the assignment details screen
                SubmissionDetailsEmptyContentEvent.SubmissionStarted
            } else // Submission is either currently being uploaded, or there is no submission being uploaded - do nothing
                null
        }
    )

    companion object {
        const val VIDEO_REQUEST_CODE = 45520
        const val CHOOSE_MEDIA_REQUEST_CODE = 45521

        fun newInstance(course: Course, assignment: Assignment, isStudioEnabled: Boolean, quiz: Quiz? = null, studioLTITool: LTITool? = null, isObserver: Boolean = false, ltiTool: LTITool? = null): SubmissionDetailsEmptyContentFragment {
            val bundle = course.makeBundle {
                putBoolean(Const.IS_OBSERVER, isObserver)
                putParcelable(Const.ASSIGNMENT, assignment)
                putParcelable(Const.QUIZ, quiz)
                putBoolean(Const.IS_STUDIO_ENABLED, isStudioEnabled)
                putParcelable(Const.STUDIO_LTI_TOOL, studioLTITool)
                putParcelable(Const.LTI_TOOL, ltiTool)
            }

            return SubmissionDetailsEmptyContentFragment().withArgs(bundle)
        }

        fun isFileRequest(requestCode: Int): Boolean {
            return requestCode in listOf(
                VIDEO_REQUEST_CODE,
                CHOOSE_MEDIA_REQUEST_CODE
            )
        }
    }
}