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
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.adapter.BasicItemBinder
import com.instructure.student.adapter.BasicItemCallback
import com.instructure.student.adapter.BasicRecyclerAdapter
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadEvent
import com.instructure.student.mobius.common.ui.MobiusView
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_picker_submission_upload.*
import kotlinx.android.synthetic.main.viewholder_file_upload.view.*
import java.io.File

class PickerSubmissionUploadView(inflater: LayoutInflater, parent: ViewGroup) :
    MobiusView<PickerSubmissionUploadViewState, PickerSubmissionUploadEvent>(
        R.layout.fragment_picker_submission_upload,
        inflater,
        parent
    ) {

    private val fabRotateForward by lazy {
        AnimationUtils.loadAnimation(context, R.anim.fab_rotate_forward)
    }
    private val fabRotateBackwards by lazy {
        AnimationUtils.loadAnimation(context, R.anim.fab_rotate_backward)
    }
    private val fabReveal by lazy { AnimationUtils.loadAnimation(context, R.anim.fab_reveal) }
    private val fabHide by lazy { AnimationUtils.loadAnimation(context, R.anim.fab_hide) }

    private val adapter = PickerRecyclerAdapter(object : PickerListCallback {
        override fun deleteClicked(position: Int) {
            consumer?.accept(PickerSubmissionUploadEvent.OnFileRemoved(position))
        }
    })

    init {
        toolbar.setupAsBackButton { (context as? Activity)?.onBackPressed() }
        toolbar.title = context.getString(R.string.submission)

        filePickerRecycler.layoutManager = LinearLayoutManager(context)
        filePickerRecycler.adapter = adapter

        pickFabCamera.setOnClickListener { childFabClick(PickerSubmissionUploadEvent.CameraClicked) }
        pickFabFile.setOnClickListener { childFabClick(PickerSubmissionUploadEvent.SelectFileClicked) }
        pickFabGallery.setOnClickListener { childFabClick(PickerSubmissionUploadEvent.GalleryClicked) }
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
        renderFabOptions(state.visibilities)

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
        pickerEmptyView.setListEmpty()
        pickerEmptyView.setImageVisible(true)
        pickerEmptyView.setEmptyViewImage(context.getDrawableCompat(R.drawable.vd_panda_choosefile))
        pickerEmptyView.setTitleText(R.string.chooseFile)
        pickerEmptyView.setMessageText(R.string.chooseFileSubtext)
    }

    //region Fabs

    private fun renderFabOptions(visibilities: PickerVisibilities) {
        pickFab.setOnClickListener {
            animateFabs(visibilities)
        }

        if (visibilities.fab) {
            pickFab.show()
            pickFab.isEnabled = true
            pickFab.setVisible() // Needed for accessibility
        } else {
            pickFab.hide()
            pickFab.isEnabled = false
            pickFab.setInvisible() // Needed for accessibility
        }
    }

    private fun animateFabs(visibilities: PickerVisibilities) {
        if (isFabExpanded()) {
            closeFabs()
        } else {
            pickFab.startAnimation(fabRotateForward)
            showFabs(
                if (visibilities.fabCamera) pickFabCamera else null,
                if (visibilities.fabFile) pickFabFile else null,
                if (visibilities.fabGallery) pickFabGallery else null
            )
        }
    }

    private fun isFabExpanded(): Boolean =
        pickFabCamera.isOrWillBeShown || pickFabFile.isOrWillBeShown || pickFabGallery.isOrWillBeShown

    private fun childFabClick(event: PickerSubmissionUploadEvent) {
        closeFabs()
        consumer?.accept(event)
    }

    private fun closeFabs() {
        pickFab.startAnimation(fabRotateBackwards)
        hideFabs(pickFabFile, pickFabCamera, pickFabGallery)
    }

    private fun hideFabs(vararg fabs: View) {
        fabs.forEach { fab ->
            if (fab.isVisible) {
                fab.startAnimation(fabHide)
                fab.isEnabled = false
                fab.setInvisible() // Needed for accessibility
            }
        }
    }

    private fun showFabs(vararg fabs: View?) {
        fabs.forEach { fab ->
            fab?.startAnimation(fabReveal)
            fab?.isEnabled = true
            fab?.setVisible() // Needed for accessibility
        }
    }

    //endregion

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
    override val layoutResId = R.layout.viewholder_file_upload
    override val bindBehavior = Item { state, view, pickerListCallback ->
        with(view) {
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
}

class PickerRecyclerAdapter(callback: PickerListCallback) :
    BasicRecyclerAdapter<PickerListItemViewState, PickerListCallback>(callback) {

    override fun registerBinders() {
        register(PickerListBinder())
    }

}
