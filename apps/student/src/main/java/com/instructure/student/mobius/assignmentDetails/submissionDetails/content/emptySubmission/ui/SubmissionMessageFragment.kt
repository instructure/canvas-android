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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.pandautils.blueprint.BaseCanvasFragment
import com.bumptech.glide.Glide
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.IntArg
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.databinding.FragmentSubmissionMessageBinding

class SubmissionMessageFragment : BaseCanvasFragment() {
    private var titleRes by IntArg()
    private var subtitleRes by IntArg()
    private var messageRes by IntArg()
    private var iconRes by IntArg()
    private var iconUri: Uri? by NullableParcelableArg()

    private val binding by viewBinding(FragmentSubmissionMessageBinding::bind)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_submission_message, container, false)

    override fun onResume() {
        super.onResume()
        if (titleRes != -1) binding.titleTextView.setVisible().text = requireContext().getString(titleRes)
        if (subtitleRes != -1) binding.subtitleTextView.setVisible().text = requireContext().getString(subtitleRes)
        if (messageRes != -1) binding.messageTextView.setVisible().text = requireContext().getString(messageRes)
        if (iconUri != null) {
            Glide.with(requireContext()).load(iconUri).into(binding.iconImageView.setVisible())
        } else if (iconRes > 0) {
            binding.iconImageView.setVisible().setImageResource(iconRes)
        }
    }

    companion object {
        fun newInstance(title: Int = -1, subtitle: Int = -1, message: Int = -1, iconRes: Int = 0, iconUri: Uri? = null) = SubmissionMessageFragment().apply {
            titleRes = title
            subtitleRes = subtitle
            messageRes  = message
            this.iconRes = iconRes
            this.iconUri = iconUri
        }
    }
}
