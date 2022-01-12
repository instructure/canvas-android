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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentCommentLibraryBinding
import com.instructure.teacher.features.speedgrader.SpeedGraderViewModel
import com.instructure.teacher.utils.setupCloseButton
import com.instructure.teacher.utils.setupMenu
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_comment_library.*
import kotlinx.android.synthetic.main.fragment_discussions_reply.*
import kotlinx.android.synthetic.main.fragment_discussions_reply.toolbar
import kotlinx.android.synthetic.main.fragment_edit_syllabus.*

@AndroidEntryPoint
class CommentLibraryFragment : Fragment() {

    private val speedGraderViewModel: SpeedGraderViewModel by activityViewModels()

    private var submissionId by LongArg(key = Const.SUBMISSION_ID)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentCommentLibraryBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = speedGraderViewModel

        speedGraderViewModel.currentSubmissionId = submissionId
        speedGraderViewModel.getCommentById(submissionId).observe(viewLifecycleOwner) {
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
        toolbar.setupCloseButton(this)
        ViewStyler.themeToolbarBottomSheet(requireActivity(), resources.getBoolean(com.instructure.pandautils.R.bool.isDeviceTablet), toolbar, Color.BLACK, false)
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