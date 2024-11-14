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
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.LtiType
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.lti.LtiLaunchFragment
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.SerializableArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.student.R
import com.instructure.student.databinding.FragmentLtiSubmissionViewBinding
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsContentType.ExternalToolContent
import com.instructure.student.router.RouteMatcher

class LtiSubmissionViewFragment : Fragment() {

    private val binding by viewBinding(FragmentLtiSubmissionViewBinding::bind)
    private var canvasContext: CanvasContext by ParcelableArg()
    private var url: String by StringArg()
    private var ltiType: LtiType by SerializableArg(LtiType.EXTERNAL_TOOL)
    private var title: String by StringArg()
    private var ltiTool: LTITool? by NullableParcelableArg()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_lti_submission_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewStyler.themeButton(binding.viewLtiButton)
        setUpViews()
        binding.viewLtiButton.onClickWithRequireNetwork {
            RouteMatcher.route(
                requireActivity(),
                LtiLaunchFragment.makeRoute(
                    canvasContext,
                    url,
                    title,
                    sessionLessLaunch = false,
                    assignmentLti = true,
                    ltiTool = ltiTool,
                    openInternally = ltiType.openInternally
                )
            )
        }
    }

    private fun setUpViews() {
        binding.viewLtiButton.text = getString(ltiType.openButtonRes)
        binding.ltiSubmissionTitle.text = getString(ltiType.ltiTitleRes)
        binding.ltiSubmissionSubtitle.text = getString(ltiType.ltiDescriptionRes)
    }

    companion object {
        fun newInstance(data: ExternalToolContent) = LtiSubmissionViewFragment().apply {
            canvasContext = data.canvasContext
            url = data.ltiTool?.url.orEmpty()
            ltiTool = data.ltiTool
            title = data.title
            ltiType = data.ltiType
        }
    }
}
