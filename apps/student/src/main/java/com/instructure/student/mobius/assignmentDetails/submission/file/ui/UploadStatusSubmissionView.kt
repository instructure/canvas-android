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
package com.instructure.student.mobius.assignmentDetails.submission.file.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.pandautils.adapters.BasicItemBinder
import com.instructure.pandautils.adapters.BasicItemCallback
import com.instructure.pandautils.adapters.BasicRecyclerAdapter
import com.instructure.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionEvent
import com.instructure.student.mobius.common.ui.MobiusView
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_upload_status_submission.*
import kotlinx.android.synthetic.main.fragment_upload_status_submission.toolbar
import kotlinx.android.synthetic.main.viewholder_file_upload.view.*

class UploadStatusSubmissionView(inflater: LayoutInflater, parent: ViewGroup) :
    MobiusView<UploadStatusSubmissionViewState, UploadStatusSubmissionEvent>(
        R.layout.fragment_upload_status_submission,
        inflater,
        parent
    ) {

    private var dialog: AlertDialog? = null

    private val adapter = UploadRecyclerAdapter(object : UploadListCallback {
        override fun deleteClicked(position: Int) {
            consumer?.accept(UploadStatusSubmissionEvent.OnDeleteFile(position))
        }
    })

    private fun backPress() {
        (context as? Activity)?.onBackPressed()
    }

    override fun onDispose() = Unit

    override fun applyTheme() {
        ViewStyler.themeToolbarBottomSheet(context as Activity, false, toolbar, Color.BLACK, false)
    }

    override fun onConnect(output: Consumer<UploadStatusSubmissionEvent>) {
        toolbar.setupAsBackButton { backPress() }
        toolbar.title = context.getString(R.string.submission)

        uploadStatusRecycler.layoutManager = LinearLayoutManager(context)
        uploadStatusRecycler.adapter = adapter

        uploadStatusStateDone.setOnClickListener { backPress() }
        uploadStatusStateCancel.setOnClickListener { output.accept(UploadStatusSubmissionEvent.OnRequestCancelClicked) }
        uploadStatusStateRetry.setOnClickListener { output.accept(UploadStatusSubmissionEvent.OnRetryClicked) }
    }

    override fun render(state: UploadStatusSubmissionViewState) {
        setVisibilities(state.visibilities)

        when (state) {
            UploadStatusSubmissionViewState.Loading -> Unit // Don't need to render anything
            is UploadStatusSubmissionViewState.InProgress -> renderInProgress(state)
            is UploadStatusSubmissionViewState.Succeeded -> renderSucceeded(state)
            is UploadStatusSubmissionViewState.Failed -> renderFailed(state)
        }
    }

    private fun setVisibilities(visibilities: UploadVisibilities) {
        // Set group visibilities
        statusGroup.setVisible(visibilities.failed || visibilities.succeeded)
        uploadStatusRecycler.setVisible(visibilities.failed || visibilities.inProgress)
        inProgressGroup.setVisible(visibilities.inProgress)
        uploadStatusSuccessPanda.setVisible(visibilities.succeeded)
        uploadStatusLoading.setVisible(visibilities.loading)

        // Set button visibilities
        uploadStatusStateRetry.setVisible(visibilities.failed)
        uploadStatusStateDone.setVisible(visibilities.succeeded)
        uploadStatusStateCancel.setVisible(visibilities.cancelable)
    }

    private fun renderSucceeded(state: UploadStatusSubmissionViewState.Succeeded) {
        uploadStatusStateTitle.text = state.title
        uploadStatusStateMessage.text = state.message

        dialog?.cancel()
        dialog = null
    }

    private fun renderInProgress(state: UploadStatusSubmissionViewState.InProgress) {
        uploadStatusStateProgressLabel.text = state.title
        uploadStatusStateProgress.progress = state.percentage.toInt()
        uploadStatusStateProgressPercent.text = state.percentageString
        uploadStatusStateProgressSize.text = state.sizeMessage

        renderList(state.list)
    }

    private fun renderFailed(state: UploadStatusSubmissionViewState.Failed) {
        uploadStatusStateTitle.text = state.title
        uploadStatusStateMessage.text = state.message

        renderList(state.list)
    }

    private fun renderList(list: List<UploadListItemViewState>) {
        adapter.data = list
    }

    // Effect functions
    fun submissionDeleted() {
        Toast.makeText(context, R.string.submissionDeleted, Toast.LENGTH_SHORT).show()
        backPress()
    }

    fun submissionRetrying() {
        Toast.makeText(context, R.string.submittingAssignment, Toast.LENGTH_SHORT).show()
        backPress()
    }

    fun showCancelSubmissionDialog() {
        dialog = AlertDialog.Builder(context)
            .setTitle(R.string.submissionDeleteTitle)
            .setMessage(R.string.submissionDeleteMessage)
            .setPositiveButton(R.string.yes) { _, _ ->
                consumer?.accept(UploadStatusSubmissionEvent.OnCancelClicked)
            }
            .setNegativeButton(R.string.no, null)
            .create()
        dialog?.setOnShowListener {
            dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(context, R.color.textDanger))
            dialog?.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(context, R.color.gray))
        }
        dialog?.setOnCancelListener {
            dialog = null
        }
        dialog?.show()
    }
}

interface UploadListCallback : BasicItemCallback {
    fun deleteClicked(position: Int)
}

class UploadListBinder : BasicItemBinder<UploadListItemViewState, UploadListCallback>() {
    override val layoutResId = R.layout.viewholder_file_upload
    override fun getItemId(item: UploadListItemViewState): Long {
        return item.position.toLong()
    }

    override val bindBehavior = Item { state, pickerListCallback, _ ->
        fileIcon.setImageResource(state.iconRes)
        fileIcon.imageTintList = ColorStateList.valueOf(state.iconColor)
        fileName.text = state.title
        fileSize.text = state.size

        // TODO: Error messages are useless right now, we aren't handling anything from the API right now and that has to change
        fileError.setGone()
//        if (state.errorMessage.isNullOrBlank()) {
//            fileError.setGone()
//        } else {
//            fileError.setVisible().text = state.errorMessage
//        }

        if (state.canDelete) {
            deleteButton.setVisible().setOnClickListener { pickerListCallback.deleteClicked(state.position) }
        } else {
            deleteButton.setGone().setOnClickListener(null)
        }
    }
}

class UploadRecyclerAdapter(callback: UploadListCallback) :
    BasicRecyclerAdapter<UploadListItemViewState, UploadListCallback>(callback) {

    override fun registerBinders() {
        register(UploadListBinder())
    }

}
