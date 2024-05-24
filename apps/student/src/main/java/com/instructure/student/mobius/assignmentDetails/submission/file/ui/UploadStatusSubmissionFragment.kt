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
 */package com.instructure.student.mobius.assignmentDetails.submission.file.ui

import android.os.Bundle
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionEffectHandler
import com.instructure.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionEvent
import com.instructure.student.mobius.common.LiveDataSource
import com.instructure.student.mobius.common.ui.SubmissionHelper
import com.instructure.student.room.StudentDb
import com.instructure.student.room.entities.CreateSubmissionEntity
import com.spotify.mobius.EventSource
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UploadStatusSubmissionFragment : BaseUploadStatusSubmissionFragment() {

    @Inject
    lateinit var submissionHelper: SubmissionHelper

    @Inject
    lateinit var studentDb: StudentDb

    override fun makeEffectHandler() =
        UploadStatusSubmissionEffectHandler(submissionId, submissionHelper, studentDb)

    override fun getExternalEventSources(): List<EventSource<UploadStatusSubmissionEvent>> = listOf(
        LiveDataSource.of<CreateSubmissionEntity, UploadStatusSubmissionEvent>(
            studentDb.submissionDao().findSubmissionByIdLiveData(submissionId)
        ) { submission ->
            if (submission != null && !submission.errorFlag) {
                UploadStatusSubmissionEvent.OnUploadProgressChanged(
                    submission.currentFile.toInt(),
                    submissionId,
                    submission.progress?.toDouble() ?: 0.0
                )
            } else UploadStatusSubmissionEvent.RequestLoad
        }
    )

    companion object {
        private fun validRoute(route: Route) = route.arguments.containsKey(Const.SUBMISSION_ID)

        fun makeRoute(submissionId: Long): Route {
            val bundle = Bundle().apply {
                putLong(Const.SUBMISSION_ID, submissionId)
            }

            return Route(UploadStatusSubmissionFragment::class.java, null, bundle)
        }

        fun newInstance(route: Route): UploadStatusSubmissionFragment? {
            if (!validRoute(route)) return null
            return UploadStatusSubmissionFragment().withArgs(route.arguments)
        }
    }
}