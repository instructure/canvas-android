/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.onClick
import com.instructure.student.R
import com.instructure.student.databinding.FragmentLtiSubmissionViewBinding
import com.instructure.student.fragment.LtiLaunchFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsContentType.ExternalToolContent
import com.instructure.student.router.RouteMatcher

class LtiSubmissionViewFragment : Fragment() {

    private val binding by viewBinding(FragmentLtiSubmissionViewBinding::bind)
    private var canvasContext: CanvasContext by ParcelableArg()
    private var url: String by StringArg()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_lti_submission_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewStyler.themeButton(binding.viewLtiButton)
        binding.viewLtiButton.onClick {
            val route = LtiLaunchFragment.makeRoute(canvasContext = canvasContext, url = url)
            RouteMatcher.route(requireContext(), route)
        }
    }

    companion object {
        fun newInstance(data: ExternalToolContent) = LtiSubmissionViewFragment().apply {
            canvasContext = data.canvasContext
            url = data.url
        }
    }
}
