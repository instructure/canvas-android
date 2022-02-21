/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.teacher.features.speedgrader.commentlibrary

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.instructure.pandautils.analytics.SCREEN_VIEW_COMMENT_LIBRARY
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.teacher.databinding.FragmentCommentLibraryBinding
import com.instructure.teacher.utils.setupCloseButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_comment_library.*
import kotlinx.android.synthetic.main.speed_grader_comment_input_view.*

@ScreenView(SCREEN_VIEW_COMMENT_LIBRARY)
@AndroidEntryPoint
class CommentLibraryFragment : Fragment() {

    private val commentLibraryViewModel: CommentLibraryViewModel by activityViewModels()

    private var submissionId by LongArg(key = Const.SUBMISSION_ID)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentCommentLibraryBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = commentLibraryViewModel

        commentLibraryViewModel.currentSubmissionId = submissionId
        commentLibraryViewModel.getCommentBySubmission(submissionId).observe(viewLifecycleOwner) {
            if (commentEditText.text.toString() != it.comment) {
                commentEditText.setText(it.comment)
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    private fun setupToolbar() {
        commentLibraryToolbar.setupCloseButton(this)
        ViewStyler.themeToolbarBottomSheet(requireActivity(), resources.getBoolean(com.instructure.pandautils.R.bool.isDeviceTablet), commentLibraryToolbar, Color.BLACK, false)
    }

    companion object {

        fun newInstance(submissionId: Long): CommentLibraryFragment {
            val args = Bundle()
            args.putLong(Const.SUBMISSION_ID, submissionId)
            val fragment = CommentLibraryFragment()
            fragment.arguments = args
            return fragment
        }
    }
}