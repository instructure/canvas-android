/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.student.mobius.assignmentDetails.submission.file.ui

import android.os.Bundle
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionEffectHandler
import com.instructure.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionEvent
import com.instructure.student.mobius.common.LiveDataSource
import com.instructure.student.mobius.common.ui.SubmissionHelper
import com.instructure.pandautils.room.studentdb.StudentDb
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
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