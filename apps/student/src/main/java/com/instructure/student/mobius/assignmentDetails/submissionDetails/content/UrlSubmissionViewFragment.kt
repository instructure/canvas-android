/*
 * Copyright (C) 2019 - present  Instructure, Inc.
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
 */
package com.instructure.student.mobius.assignmentDetails.submissionDetails.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.pandautils.blueprint.BaseCanvasFragment
import com.bumptech.glide.Glide
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.NullableStringArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.onClick
import com.instructure.student.R
import com.instructure.student.activity.InternalWebViewActivity
import com.instructure.student.databinding.FragmentUrlSubmissionViewBinding

class UrlSubmissionViewFragment : BaseCanvasFragment() {

    private val binding by viewBinding(FragmentUrlSubmissionViewBinding::bind)

    private var url by StringArg()
    private var previewUrl by NullableStringArg()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_url_submission_view, container, false)
    }

    override fun onStart() = with(binding) {
        super.onStart()
        Glide.with(requireContext()).load(previewUrl).into(urlPreviewImageView)
        urlTextView.text = url
        urlTextView.setTextColor(ThemePrefs.textButtonColor)
        val launchUrl = { _: View ->
            val title = getString(R.string.urlSubmission)
            val intent = InternalWebViewActivity.createIntent(requireActivity(), url, title, true)
            requireActivity().startActivity(intent)
        }
        urlTextView.onClick(launchUrl)
        urlPreviewImageView.onClick(launchUrl)
    }

    companion object {
        fun newInstance(url: String, previewUrl: String?) = UrlSubmissionViewFragment().apply {
            this.url = url
            this.previewUrl = previewUrl
        }
    }
}
