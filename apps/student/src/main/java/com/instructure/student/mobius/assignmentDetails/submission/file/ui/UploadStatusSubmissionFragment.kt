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
package com.instructure.student.mobius.assignmentDetails.submission.file.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_UPLOAD_STATUS_SUBMISSION
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.Submission
import com.instructure.student.databinding.FragmentUploadStatusSubmissionBinding
import com.instructure.student.db.Db
import com.instructure.student.db.getInstance
import com.instructure.student.mobius.assignmentDetails.submission.file.*
import com.instructure.student.mobius.common.DBSource
import com.instructure.student.mobius.common.ui.MobiusFragment
import com.spotify.mobius.EventSource

@ScreenView(SCREEN_VIEW_UPLOAD_STATUS_SUBMISSION)
class UploadStatusSubmissionFragment :
    MobiusFragment<UploadStatusSubmissionModel, UploadStatusSubmissionEvent, UploadStatusSubmissionEffect, UploadStatusSubmissionView, UploadStatusSubmissionViewState, FragmentUploadStatusSubmissionBinding>() {

    private val submissionId by LongArg(key = Const.SUBMISSION_ID)

    override fun makeEffectHandler() =
        UploadStatusSubmissionEffectHandler(requireContext(), submissionId)

    override fun makeUpdate() = UploadStatusSubmissionUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) =
        UploadStatusSubmissionView(inflater, parent)

    override fun makePresenter() = UploadStatusSubmissionPresenter

    override fun makeInitModel() = UploadStatusSubmissionModel(submissionId)

    @Suppress("RemoveExplicitTypeArguments") // DBSource.ofSingle type arguments required, but linter thinks they aren't
    override fun getExternalEventSources(): List<EventSource<UploadStatusSubmissionEvent>> = listOf(
        DBSource.ofSingle<Submission, UploadStatusSubmissionEvent>(
            Db.getInstance(ContextKeeper.appContext)
                .submissionQueries
                .getSubmissionById(submissionId)
        ) { submission ->
            if (submission != null && !submission.errorFlag) {
                UploadStatusSubmissionEvent.OnUploadProgressChanged(submission.currentFile.toInt(), submissionId, submission.progress ?: 0.0)
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