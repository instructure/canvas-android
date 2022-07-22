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
import android.content.Intent
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
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.FragmentFileUploadDialogBinding
import com.instructure.pandautils.services.FileUploadService
import com.instructure.pandautils.utils.*
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class FileUploadDialogFragment : DialogFragment() {

    private val viewModel: FileUploadDialogViewModel by viewModels()

    private lateinit var binding: FragmentFileUploadDialogBinding

    private var uploadType: FileUploadType by SerializableArg(FileUploadType.ASSIGNMENT)
    private var canvasContext: CanvasContext by ParcelableArg(ApiPrefs.user)
    private var position: Int by IntArg()

    private var fileSubmitUri: Uri? = null
    private var cameraImageUri: Uri? = null

    private var assignment: Assignment? by NullableParcelableArg()
    private var parentFolderId: Long by LongArg()
    private var quizQuestionId: Long by LongArg()
    private var quizId: Long by LongArg()
    private var courseId: Long by LongArg()

    private var dialogCallback: ((Int) -> Unit)? = null
    private var attachmentCallback: ((Int, FileSubmitObject?) -> Unit)? = null

    private val cameraPermissionContract = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isPermissionGranted ->
        if (isPermissionGranted) takePicture()
    }

    private val takePictureContract = registerForActivityResult(ActivityResultContracts.TakePicture()) { imageSaved ->
        if (imageSaved) {
            cameraImageUri?.let {
                viewModel.addFile(it)
            }
        }
    }

    private val galleryPickerContract = registerForActivityResult(ActivityResultContracts.GetContent()) {
        viewModel.addFile(it)
    }

    private val filePickerContract = registerForActivityResult(ActivityResultContracts.GetContent()) {
        viewModel.addFile(it)
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
                title = getString(R.string.utils_attachFile)
                positiveText = getString(R.string.utils_okay)
            }
            FileUploadType.QUIZ -> {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.events.observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        })

        viewModel.setData(assignment, fileSubmitUri, uploadType, canvasContext, parentFolderId, quizQuestionId, position, quizId)
    }

    private fun uploadClicked() {
        if (uploadType == FileUploadType.DISCUSSION) {
            attachmentCallback?.invoke(EVENT_ON_FILE_SELECTED, viewModel.getAttachmentUri())
            dismiss()
        } else {
            viewModel.uploadFiles()
        }
    }

    private fun cancelClicked() {
        dismissAllowingStateLoss()
        dialogCallback?.invoke(EVENT_DIALOG_CANCELED)
        attachmentCallback?.invoke(EVENT_DIALOG_CANCELED, null)
    }

    private fun handleAction(action: FileUploadAction) {
        when (action) {
            is FileUploadAction.TakePhoto -> takePicture()
            is FileUploadAction.PickPhoto -> pickFromGallery()
            is FileUploadAction.PickFile -> pickFromFiles()
            is FileUploadAction.ShowToast -> Toast.makeText(requireContext(), action.toast, Toast.LENGTH_SHORT).show()
            is FileUploadAction.StartUpload -> startUpload(action.bundle, action.action)
        }
    }

    private fun startUpload(bundle: Bundle, action: String) {
        val intent = Intent(requireContext(), FileUploadService::class.java)
        intent.action = action
        intent.putExtras(bundle)
        requireActivity().startService(intent)
        dialogCallback?.invoke(EVENT_ON_UPLOAD_BEGIN)
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

        const val TAG = "FileUploadDialogFragment"

        private const val INVALID_ID = -1L
        private const val INVALID_ID_INT = -1

        const val EVENT_DIALOG_CANCELED = 1
        const val EVENT_ON_UPLOAD_BEGIN = 2
        const val EVENT_ON_FILE_SELECTED = 3

        fun newInstance(): FileUploadDialogFragment = FileUploadDialogFragment()

        fun newInstance(args: Bundle, callback: ((Int) -> Unit)? = null, pickerCallback: ((Int, FileSubmitObject?) -> Unit)? = null): FileUploadDialogFragment {
            return FileUploadDialogFragment().apply {
                arguments = args

                fileSubmitUri = args.getParcelable(Const.URI)
                uploadType = args.getSerializable(Const.UPLOAD_TYPE) as FileUploadType
                parentFolderId = args.getLong(Const.PARENT_FOLDER_ID, INVALID_ID)
                quizQuestionId = args.getLong(Const.QUIZ_ANSWER_ID, INVALID_ID)
                quizId = args.getLong(Const.QUIZ, INVALID_ID)
                courseId = args.getLong(Const.COURSE_ID, INVALID_ID)
                position = args.getInt(Const.POSITION, INVALID_ID_INT)
                dialogCallback = callback
                attachmentCallback = pickerCallback
            }
        }

        fun createBundle(submitURI: Uri?, type: FileUploadType, parentFolderId: Long?): Bundle {
            val bundle = Bundle()
            if (submitURI != null) bundle.putParcelable(Const.URI, submitURI)
            if (parentFolderId != null) bundle.putLong(Const.PARENT_FOLDER_ID, parentFolderId)
            bundle.putSerializable(Const.UPLOAD_TYPE, type)
            return bundle
        }

        fun createMessageAttachmentsBundle(defaultFileList: ArrayList<FileSubmitObject>): Bundle {
            val bundle = createBundle(null, FileUploadType.MESSAGE, null)
            bundle.putParcelableArrayList(Const.FILES, defaultFileList)
            return bundle
        }

        fun createDiscussionsBundle(defaultFileList: ArrayList<FileSubmitObject>): Bundle {
            val bundle = createBundle(null, FileUploadType.DISCUSSION, null)
            bundle.putParcelableArrayList(Const.FILES, defaultFileList)
            return bundle
        }

        fun createFilesBundle(submitURI: Uri?, parentFolderId: Long?): Bundle {
            return createBundle(submitURI, FileUploadType.USER, parentFolderId)
        }

        fun createContextBundle(submitURI: Uri?, context: CanvasContext, parentFolderId: Long?): Bundle {
            return when {
                context.isCourse -> createCourseBundle(submitURI, context as Course, parentFolderId)
                context.isGroup -> createGroupBundle(submitURI, context as Group, parentFolderId)
                else -> createUserBundle(submitURI, context as User, parentFolderId)
            }
        }

        private fun createCourseBundle(submitURI: Uri?, course: Course, parentFolderId: Long?): Bundle {
            val bundle = createBundle(submitURI, FileUploadType.COURSE, parentFolderId)
            bundle.putParcelable(Const.CANVAS_CONTEXT, course)
            return bundle
        }

        private fun createGroupBundle(submitURI: Uri?, group: Group, parentFolderId: Long?): Bundle {
            val bundle = createBundle(submitURI, FileUploadType.GROUP, parentFolderId)
            bundle.putParcelable(Const.CANVAS_CONTEXT, group)
            return bundle
        }

        private fun createUserBundle(submitURI: Uri?, user: User, parentFolderId: Long?): Bundle {
            val bundle = createBundle(submitURI, FileUploadType.USER, parentFolderId)
            bundle.putParcelable(Const.CANVAS_CONTEXT, user)
            return bundle
        }

        fun createAssignmentBundle(submitURI: Uri?, course: Course, assignment: Assignment): Bundle {
            val bundle = createBundle(submitURI, FileUploadType.ASSIGNMENT, null)
            bundle.putParcelable(Const.CANVAS_CONTEXT, course)
            bundle.putParcelable(Const.ASSIGNMENT, assignment)
            return bundle
        }

        fun createQuizFileBundle(quizQuestionId: Long, courseId: Long, quizId: Long, position: Int): Bundle {
            val bundle = createBundle(null, FileUploadType.QUIZ, null)
            bundle.putLong(Const.QUIZ_ANSWER_ID, quizQuestionId)
            bundle.putLong(Const.QUIZ, quizId)
            bundle.putLong(Const.COURSE_ID, courseId)
            bundle.putInt(Const.POSITION, position)
            return bundle
        }

        fun createSubmissionCommentBundle(course: Course, assignment: Assignment, defaultFileList: java.util.ArrayList<FileSubmitObject>): Bundle {
            val bundle = createBundle(null, FileUploadType.SUBMISSION_COMMENT, null)
            bundle.putParcelable(Const.CANVAS_CONTEXT, course)
            bundle.putParcelable(Const.ASSIGNMENT, assignment)
            bundle.putParcelableArrayList(Const.FILES, defaultFileList)
            return bundle
        }

        fun createAttachmentsBundle(defaultFileList: ArrayList<FileSubmitObject> = ArrayList()): Bundle {
            val bundle = createBundle(null, FileUploadType.MESSAGE, null)
            bundle.putParcelableArrayList(Const.FILES, defaultFileList)
            return bundle
        }
    }
}