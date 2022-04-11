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
package com.instructure.student.mobius.assignmentDetails.submission.picker.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.pandautils.adapters.BasicItemBinder
import com.instructure.pandautils.adapters.BasicItemCallback
import com.instructure.pandautils.adapters.BasicRecyclerAdapter
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionMode
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadEvent
import com.instructure.student.mobius.common.ui.MobiusView
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_picker_submission_upload.*
import kotlinx.android.synthetic.main.viewholder_file_upload.view.*

class PickerSubmissionUploadView(inflater: LayoutInflater, parent: ViewGroup, val mode: PickerSubmissionMode) :
    MobiusView<PickerSubmissionUploadViewState, PickerSubmissionUploadEvent>(
        R.layout.fragment_picker_submission_upload,
        inflater,
        parent
    ) {

    private val adapter = PickerRecyclerAdapter(object : PickerListCallback {
        override fun deleteClicked(position: Int) {
            consumer?.accept(PickerSubmissionUploadEvent.OnFileRemoved(position))
        }
    })

    init {
        toolbar.setupAsBackButton { (context as? Activity)?.onBackPressed() }
        toolbar.title = context.getString(if (mode.isForComment) R.string.commentUpload else R.string.submission)

        filePickerRecycler.layoutManager = LinearLayoutManager(context)
        filePickerRecycler.adapter = adapter

        sourceCamera.setOnClickListener { consumer?.accept(PickerSubmissionUploadEvent.CameraClicked) }
        sourceDevice.setOnClickListener { consumer?.accept(PickerSubmissionUploadEvent.SelectFileClicked) }
        sourceGallery.setOnClickListener { consumer?.accept(PickerSubmissionUploadEvent.GalleryClicked) }
        sourceDocumentScanning.setOnClickListener { consumer?.accept(PickerSubmissionUploadEvent.DocumentScanningClicked) }
    }

    override fun onConnect(output: Consumer<PickerSubmissionUploadEvent>) {
        toolbar.setMenu(R.menu.menu_submit_generic) {
            when (it.itemId) {
                R.id.menuSubmit -> {
                    output.accept(PickerSubmissionUploadEvent.SubmitClicked)
                }
            }
        }

        toolbar.menu.findItem(R.id.menuSubmit).isVisible = false
    }

    override fun onDispose() = Unit

    override fun applyTheme() {
        ViewStyler.themeToolbarBottomSheet(context as Activity, false, toolbar, Color.BLACK, false)
    }

    override fun render(state: PickerSubmissionUploadViewState) {
        toolbar.menu.findItem(R.id.menuSubmit).isVisible = state.visibilities.submit
        fileLoading.setVisible(state.visibilities.loading)
        renderSourceOptions(state.visibilities)

        when (state) {
            is PickerSubmissionUploadViewState.Empty -> {
                filePickerRecycler.setVisible(false)
                renderEmpty()
            }
            is PickerSubmissionUploadViewState.FileList -> {
                pickerEmptyView.setVisible(false)

                filePickerRecycler.setVisible(true)
                adapter.data = state.list
            }
        }
    }

    private fun renderEmpty() {
        pickerEmptyView.setVisible(true)
        pickerEmptyView.setImageVisible(true)
        pickerEmptyView.setEmptyViewImage(context.getDrawableCompat(R.drawable.ic_panda_choosefile))
        pickerEmptyView.setListEmpty()
        pickerEmptyView.setTitleText(R.string.chooseFile)
        pickerEmptyView.setMessageText(
            if (mode.isForComment) R.string.chooseFileForCommentSubtext else R.string.chooseFileSubtext
        )
    }

    private fun renderSourceOptions(visibilities: PickerVisibilities) {
        sourcesContainer.setVisible(visibilities.sources)
        sourcesDivider.setVisible(visibilities.sources)
        sourceCamera.setVisible(visibilities.sourceCamera)
        sourceDevice.setVisible(visibilities.sourceFile)
        sourceGallery.setVisible(visibilities.sourceGallery)
    }

    fun getSelectFileIntent() = Intent(Intent.ACTION_GET_CONTENT).apply {
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        type = "*/*"
        addCategory(Intent.CATEGORY_OPENABLE)
    }

    fun getGalleryIntent(dataUri: Uri): Intent {
        return Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            setDataAndType(dataUri, "image/*")
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*"))
        }
    }

    fun getCameraIntent(dataUri: Uri) = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
        putExtra(MediaStore.EXTRA_OUTPUT, dataUri)
    }

    fun showBadExtensionDialog(allowedExtensions: List<String>) {
        PickerBadExtensionDialog.show((context as FragmentActivity).supportFragmentManager, allowedExtensions)
    }

    fun showFileErrorMessage(errorMessage: String) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }

    fun showErrorMessage(errorMessage: Int) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }

    fun closeSubmissionView() {
        (context as Activity).onBackPressed()
    }
}

interface PickerListCallback : BasicItemCallback {
    fun deleteClicked(position: Int)
}

class PickerListBinder : BasicItemBinder<PickerListItemViewState, PickerListCallback>() {
    override fun getItemId(item: PickerListItemViewState) = item.position.toLong()
    override val layoutResId = R.layout.viewholder_file_upload
    override val bindBehavior = Item { state, pickerListCallback, _ ->
        fileIcon.setImageResource(state.iconRes)
        fileName.text = state.title
        fileSize.text = state.size
        if (state.canDelete) {
            deleteButton.setVisible().setOnClickListener {
                pickerListCallback.deleteClicked(state.position)
            }
        } else {
            deleteButton.setGone().setOnClickListener(null)
        }
    }
}

class PickerRecyclerAdapter(callback: PickerListCallback) :
    BasicRecyclerAdapter<PickerListItemViewState, PickerListCallback>(callback) {

    override fun registerBinders() {
        register(PickerListBinder())
    }

}
