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
package com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.files.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.emeritus.student.R
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.files.SubmissionFilesEvent
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.files.SubmissionFilesViewState
import com.emeritus.student.mobius.common.ui.MobiusView
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_submission_files.*

class SubmissionFilesView(
    inflater: LayoutInflater,
    parent: ViewGroup
) : MobiusView<SubmissionFilesViewState, SubmissionFilesEvent>(R.layout.fragment_submission_files, inflater, parent) {

    private val adapter = SubmissionFilesAdapter {
        consumer?.accept(SubmissionFilesEvent.FileClicked(it))
    }

    init {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    override fun render(state: SubmissionFilesViewState) {
        when (state) {
            SubmissionFilesViewState.Empty -> {
                emptyView.setVisible()
                recyclerView.setGone()
            }
            is SubmissionFilesViewState.FileList -> {
                emptyView.setGone()
                recyclerView.setVisible()
                adapter.setData(state.files)
            }
        }
    }

    override fun onDispose() = Unit

    override fun applyTheme() = Unit

    override fun onConnect(output: Consumer<SubmissionFilesEvent>) = Unit
}

