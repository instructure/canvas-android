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
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.mobius.assignmentDetails.submission.file.*
import com.instructure.student.mobius.common.ui.MobiusFragment

class UploadStatusSubmissionFragment :
    MobiusFragment<UploadStatusSubmissionModel, UploadStatusSubmissionEvent, UploadStatusSubmissionEffect, UploadStatusSubmissionView, UploadStatusSubmissionViewState>() {

    private val submissionId by LongArg(key = Const.SUBMISSION_ID)

    override fun makeEffectHandler() =
        UploadStatusSubmissionEffectHandler(requireContext(), submissionId)

    override fun makeUpdate() = UploadStatusSubmissionUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) =
        UploadStatusSubmissionView(inflater, parent)

    override fun makePresenter() = UploadStatusSubmissionPresenter

    override fun makeInitModel() = UploadStatusSubmissionModel(submissionId)

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