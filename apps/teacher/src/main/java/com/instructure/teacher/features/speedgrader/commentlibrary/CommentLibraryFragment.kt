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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.instructure.pandautils.base.BaseCanvasFragment
import androidx.fragment.app.activityViewModels
import com.instructure.pandautils.analytics.SCREEN_VIEW_COMMENT_LIBRARY
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.applyHorizontalSystemBarInsets
import com.instructure.teacher.databinding.FragmentCommentLibraryBinding
import com.instructure.teacher.utils.setupCloseButton
import dagger.hilt.android.AndroidEntryPoint

@ScreenView(SCREEN_VIEW_COMMENT_LIBRARY)
@AndroidEntryPoint
class CommentLibraryFragment : BaseCanvasFragment() {

    private lateinit var binding: FragmentCommentLibraryBinding

    private val commentLibraryViewModel: CommentLibraryViewModel by activityViewModels()

    private var submissionId by LongArg(key = Const.SUBMISSION_ID)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommentLibraryBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = commentLibraryViewModel

        commentLibraryViewModel.currentSubmissionId = submissionId
        commentLibraryViewModel.getCommentBySubmission(submissionId).observe(viewLifecycleOwner) {
            if (binding.commentInputContainer.commentEditText.text.toString() != it.comment) {
                binding.commentInputContainer.commentEditText.setText(it.comment)
            }
        }

        binding.root.applyHorizontalSystemBarInsets()
        setupWindowInsets()

        return binding.root
    }

    private fun setupWindowInsets() = with(binding) {
        commentLibraryToolbar.applyTopSystemBarInsets()

        ViewCompat.setOnApplyWindowInsetsListener(commentLibraryRecyclerView) { view, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = maxOf(ime.bottom, systemBars.bottom))
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(commentInputContainer.root) { view, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = maxOf(ime.bottom, systemBars.bottom))
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    private fun setupToolbar() {
        binding.commentLibraryToolbar.setupCloseButton(this)
        ViewStyler.themeToolbarLight(requireActivity(), binding.commentLibraryToolbar)
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