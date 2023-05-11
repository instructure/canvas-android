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
package com.emeritus.student.mobius.assignmentDetails.submissionDetails.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.utils.*
import com.emeritus.student.R
import com.emeritus.student.fragment.LtiLaunchFragment
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsContentType.ExternalToolContent
import com.emeritus.student.router.RouteMatcher
import kotlinx.android.synthetic.main.fragment_lti_submission_view.*

class LtiSubmissionViewFragment : Fragment() {
    private var canvasContext: CanvasContext by ParcelableArg()
    private var url: String by StringArg()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_lti_submission_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewStyler.themeButton(viewLtiButton)
        viewLtiButton.onClick {
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
