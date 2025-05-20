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

    private var title by StringArg()
    private var subtitle by StringArg()
    private var message by StringArg()
    private var iconRes by IntArg()
    private var iconUri: Uri? by NullableParcelableArg()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_speedgrader_empty, container, false)
    }

    override fun onResume(): Unit = with(binding) {
        super.onResume()
        if (title.isNotBlank()) titleTextView.setVisible().text = title
        if (subtitle.isNotBlank()) subtitleTextView.setVisible().text = subtitle
        if (message.isNotBlank()) messageTextView.setVisible().text = message
        if (iconUri != null) {
            Glide.with(requireContext()).load(iconUri).into(iconImageView.setVisible())
        } else if (iconRes > 0) {
            iconImageView.setVisible().setImageResource(iconRes)
        }
    }

    companion object {

        const val ICON_RES = "iconRes"
        const val ICON_URI = "iconUri"
        const val TITLE = "title"
        const val SUBTITLE = "subtitle"
        const val MESSAGE = "message"

        fun newInstance(title: String = "", subtitle: String = "", message: String = "", iconRes: Int = 0, iconUri: Uri? = null) = SpeedGraderEmptyFragment().apply {
            this.title = title
            this.subtitle = subtitle
            this.message = message
            this.iconRes = iconRes
            this.iconUri = iconUri
        }

        fun createBundle(title: String = "", subtitle: String = "", message: String = "", iconRes: Int = 0, iconUri: Uri? = null) = Bundle().apply {
            putString(TITLE, title)
            putString(SUBTITLE, subtitle)
            putString(MESSAGE, message)
            putInt(ICON_RES, iconRes)
            putParcelable(ICON_URI, iconUri)
        }
    }
}
