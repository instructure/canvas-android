/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
package com.instructure.teacher.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.instructure.pandautils.analytics.SCREEN_VIEW_SPEED_GRADER_URL_SUBMISSION
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.onClick
import com.instructure.teacher.R
import com.instructure.teacher.activities.InternalWebViewActivity
import com.instructure.pandautils.utils.NullableStringArg
import com.instructure.pandautils.utils.StringArg
import kotlinx.android.synthetic.main.fragment_speedgrader_url_submission.*

@ScreenView(SCREEN_VIEW_SPEED_GRADER_URL_SUBMISSION)
class SpeedGraderUrlSubmissionFragment : Fragment() {

    private var mUrl by StringArg()
    private var mPreviewUrl by NullableStringArg()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_speedgrader_url_submission, container, false)
    }

    override fun onStart() {
        super.onStart()
        Glide.with(requireContext()).load(mPreviewUrl).into(urlPreviewImageView)
        urlTextView.text = mUrl
        urlTextView.setTextColor(ThemePrefs.buttonColor)
        urlTextView.onClick {
            requireActivity().startActivity(InternalWebViewActivity.createIntent(requireActivity(), mUrl, "", true))
        }
    }

    companion object {
        fun newInstance(url: String, previewUrl: String?) = SpeedGraderUrlSubmissionFragment().apply {
            mUrl = url
            mPreviewUrl = previewUrl
        }
    }
}
