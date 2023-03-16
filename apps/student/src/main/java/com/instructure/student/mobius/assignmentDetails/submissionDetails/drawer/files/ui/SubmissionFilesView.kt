/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.files.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.databinding.FragmentSubmissionFilesBinding
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.files.SubmissionFilesEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.files.SubmissionFilesViewState
import com.instructure.student.mobius.common.ui.MobiusView
import com.spotify.mobius.functions.Consumer

class SubmissionFilesView(
    inflater: LayoutInflater,
    parent: ViewGroup
) : MobiusView<SubmissionFilesViewState, SubmissionFilesEvent, FragmentSubmissionFilesBinding>(
    R.layout.fragment_submission_files,
    inflater,
    FragmentSubmissionFilesBinding::inflate,
    parent) {

    private val adapter = SubmissionFilesAdapter {
        consumer?.accept(SubmissionFilesEvent.FileClicked(it))
    }

    init {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
    }

    override fun render(state: SubmissionFilesViewState) {
        when (state) {
            SubmissionFilesViewState.Empty -> {
                binding.emptyView.setVisible()
                binding.recyclerView.setGone()
            }
            is SubmissionFilesViewState.FileList -> {
                binding.emptyView.setGone()
                binding.recyclerView.setVisible()
                adapter.setData(state.files)
            }
        }
    }

    override fun onDispose() = Unit

    override fun applyTheme() = Unit

    override fun onConnect(output: Consumer<SubmissionFilesEvent>) = Unit
}

