/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.file.upload

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.FragmentFileUploadDialogBinding
import com.instructure.pandautils.dialogs.UploadFilesDialog
import com.instructure.pandautils.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.dialog_files_upload.*
import java.io.File
import java.util.ArrayList

@AndroidEntryPoint
open class FileUploadDialogFragment : DialogFragment() {

    private val viewModel: FileUploadDialogViewModel by viewModels()

    private lateinit var binding: FragmentFileUploadDialogBinding

    private var uploadType: FileUploadType by SerializableArg(FileUploadType.ASSIGNMENT)
    private var canvasContext: CanvasContext by ParcelableArg(ApiPrefs.user)
    private var isOneFileOnly: Boolean by BooleanArg()
    private var position: Int by IntArg()

    private var fileListAdapter: UploadFilesDialog.FileRecyclerViewAdapter? = null
    private val fileList: ArrayList<FileSubmitObject> by ParcelableArrayListArg(ArrayList())
    private var fileSubmitUri: Uri? = null
    private var cameraImageUri: Uri? = null

    private var assignment: Assignment? by NullableParcelableArg()
    private var parentFolderId: Long by LongArg()
    private var quizQuestionId: Long by LongArg()
    private var quizId: Long by LongArg()
    private var courseId: Long by LongArg()

    private val cameraPermissionContract = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isPermissionGranted ->
        if (isPermissionGranted) takePicture()
    }

    private val takePictureContract = registerForActivityResult(ActivityResultContracts.TakePicture()) { imageSaved ->
    }

    private val galleryPickerContract = registerForActivityResult(ActivityResultContracts.GetContent()) { fileUri ->
    }

    private val filePickerContract = registerForActivityResult(ActivityResultContracts.GetContent()) {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentFileUploadDialogBinding.inflate(layoutInflater, null, false)

        val title: String
        val positiveText: String

        // Get dialog headers
        when (uploadType) {
            FileUploadType.ASSIGNMENT -> {
                title = getString(R.string.assignmentHeader) + " " + assignment?.name
                positiveText = getString(R.string.turnIn)
            }
            FileUploadType.COURSE -> {
                title = getString(R.string.utils_uploadTo) + " " + getString(R.string.utils_uploadCourseFiles)
                positiveText = getString(R.string.upload)
            }
            FileUploadType.GROUP -> {
                title = getString(R.string.utils_uploadTo) + " " + getString(R.string.utils_uploadGroupFiles)
                positiveText = getString(R.string.upload)
            }
            FileUploadType.MESSAGE -> {
                title = getString(R.string.utils_attachFile)
                positiveText = getString(R.string.utils_okay)
            }
            FileUploadType.DISCUSSION -> {
                isOneFileOnly = true
                title= getString(R.string.utils_attachFile)
                positiveText = getString(R.string.utils_okay)
            }
            FileUploadType.QUIZ -> {
                isOneFileOnly = true
                title = getString(R.string.utils_uploadTo) + " " + getString(R.string.utils_uploadMyFiles)
                positiveText = getString(R.string.utils_upload)
            }
            FileUploadType.SUBMISSION_COMMENT -> {
                title = getString(R.string.utils_uploadToSubmissionComment)
                positiveText = getString(R.string.utils_upload)
            }
            else -> {
                title = getString(R.string.utils_uploadTo) + " " + getString(R.string.utils_uploadMyFiles)
                positiveText = getString(R.string.utils_upload)
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(binding.root)
                .setPositiveButton(positiveText) { _, _ ->
                    uploadClicked()
                }
                .setNegativeButton(R.string.utils_cancel) { _, _ ->
                    cancelClicked()
                }
                .create()

        return dialog
    }

    open fun uploadClicked() {
    }

    open fun cancelClicked() {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.events.observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        })
    }

    private fun handleAction(action: FileUploadAction) {
        when (action) {
            is FileUploadAction.TakePhoto -> takePicture()
            is FileUploadAction.PickPhoto -> pickFromGallery()
            is FileUploadAction.PickFile -> pickFromFiles()
        }
    }

    private fun takePicture() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionContract.launch(Manifest.permission.CAMERA)
        } else {
            val fileName = "pic_" + System.currentTimeMillis().toString() + ".jpg"
            val file = File(FileUploadUtils(requireContext(), requireContext().contentResolver).getExternalCacheDir(), fileName)

            cameraImageUri = FileProvider.getUriForFile(requireContext(), requireContext().packageName + Const.FILE_PROVIDER_AUTHORITY, file)
            takePictureContract.launch(cameraImageUri)
        }
    }

    private fun pickFromGallery() {
        galleryPickerContract.launch("image/*")
    }

    private fun pickFromFiles() {
        filePickerContract.launch("*/*")
    }

    companion object {

        private const val INVALID_ID = -1L
        private const val INVALID_ID_INT = -1

        fun newInstance(): FileUploadDialogFragment = FileUploadDialogFragment()

        fun newInstance(args: Bundle): FileUploadDialogFragment {
            return FileUploadDialogFragment().apply {
                arguments = args

                fileSubmitUri = args.getParcelable(Const.URI)
                uploadType = args.getSerializable(Const.UPLOAD_TYPE) as FileUploadType
                parentFolderId = args.getLong(Const.PARENT_FOLDER_ID, INVALID_ID)
                quizQuestionId = args.getLong(Const.QUIZ_ANSWER_ID, INVALID_ID)
                quizId = args.getLong(Const.QUIZ, INVALID_ID)
                courseId = args.getLong(Const.COURSE_ID, INVALID_ID)
                position = args.getInt(Const.POSITION, INVALID_ID_INT)
            }
        }
    }
}