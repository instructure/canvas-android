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

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.pandautils.base.BaseCanvasFragment
import com.bumptech.glide.Glide
import com.instructure.pandautils.analytics.SCREEN_VIEW_SPEED_GRADER_EMPTY
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.IntArg
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentSpeedgraderEmptyBinding

@ScreenView(SCREEN_VIEW_SPEED_GRADER_EMPTY)
class SpeedGraderEmptyFragment : BaseCanvasFragment() {

    private val binding by viewBinding(FragmentSpeedgraderEmptyBinding::bind)

    private var mTitle by StringArg()
    private var mSubtitle by StringArg()
    private var mMessage by StringArg()
    private var mIconRes by IntArg()
    private var mIconUri: Uri? by NullableParcelableArg()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_speedgrader_empty, container, false)
    }

    override fun onResume(): Unit = with(binding) {
        super.onResume()
        if (mTitle.isNotBlank()) titleTextView.setVisible().text = mTitle
        if (mSubtitle.isNotBlank()) subtitleTextView.setVisible().text = mSubtitle
        if (mMessage.isNotBlank()) messageTextView.setVisible().text = mMessage
        if (mIconUri != null) {
            Glide.with(requireContext()).load(mIconUri).into(iconImageView.setVisible())
        } else if (mIconRes > 0) {
            iconImageView.setVisible().setImageResource(mIconRes)
        }
    }

    companion object {
        fun newInstance(title: String = "", subtitle: String = "", message: String = "", iconRes: Int = 0, iconUri: Uri? = null) = SpeedGraderEmptyFragment().apply {
            mTitle = title
            mSubtitle = subtitle
            mMessage = message
            mIconRes = iconRes
            mIconUri = iconUri
        }
    }
}
