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
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.base.BaseCanvasDialogFragment
import com.instructure.pandautils.databinding.FragmentFileUploadDialogBinding
import com.instructure.pandautils.features.shareextension.ShareExtensionActivity
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FileUploadUtils
import com.instructure.pandautils.utils.IntArg
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.NLongArg
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.SerializableArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.isCourse
import com.instructure.pandautils.utils.isGroup
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class FileUploadDialogFragment : BaseCanvasDialogFragment() {

    private val viewModel: FileUploadDialogViewModel by viewModels()

    private lateinit var binding: FragmentFileUploadDialogBinding

    private var uploadType: FileUploadType by SerializableArg(FileUploadType.ASSIGNMENT)
    private var canvasContext: CanvasContext by ParcelableArg(ApiPrefs.user)
    private var position: Int by IntArg()

    private var fileSubmitUris: ArrayList<Uri>? = arrayListOf()
    private var cameraImageUri: Uri? = null

    private var assignment: Assignment? by NullableParcelableArg()
    private var parentFolderId: Long by LongArg()
    private var quizQuestionId: Long by LongArg()
    private var quizId: Long by LongArg()
    private var courseId: Long by LongArg()
    private var userId: Long by LongArg()
    private var attemptId: Long? by NLongArg()

    private var dialogCallback: ((Int) -> Unit)? = null
    private var dialogParent: FileUploadDialogParent? = null

    private val cameraPermissionContract = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isPermissionGranted ->
        if (isPermissionGranted) {
            takePicture()
        } else if (!requireActivity().shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            toast(R.string.cameraPermissionPermanentlyDenied, Toast.LENGTH_LONG)
        }
    }

    private val takePictureContract = registerForActivityResult(ActivityResultContracts.TakePicture()) { imageSaved ->
        if (imageSaved) {
            cameraImageUri?.let {
                viewModel.addFile(it)
            }
        }
    }

    private val filePickerContract = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let {
            viewModel.addFile(it)
        }
    }

    private val multipleFilePickerContract = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) {
        it?.let {
            viewModel.addFiles(it)
        }
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
                title = getString(R.string.submission)
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
            FileUploadType.SUBMISSION_COMMENT, FileUploadType.TEACHER_SUBMISSION_COMMENT -> {
                title = getString(R.string.utils_uploadToSubmissionComment)
                positiveText = getString(R.string.utils_upload)
            }
            else -> {
                title = getString(R.string.utils_uploadTo) + " " + getString(R.string.utils_uploadMyFiles)
                positiveText = getString(R.string.utils_upload)
            }
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.AccessibleAlertDialog)
                .setTitle(title)
                .setView(binding.root)
                .setPositiveButton(positiveText, null)
                .setNegativeButton(R.string.utils_cancel, null)
                .create()

        dialog.setCanceledOnTouchOutside(false)

        dialog.setOnShowListener {
            val positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            positive.setTextColor(ThemePrefs.textButtonColor)
            positive.setOnClickListener { uploadClicked() }
            val negative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            negative.setTextColor(ThemePrefs.textButtonColor)
            negative.setOnClickListener {
                cancelClicked()
            }
        }

        if (requireActivity() is ShareExtensionActivity) {
            dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }

        return dialog
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if (requireActivity() is ShareExtensionActivity) {
            requireActivity().onBackPressed()
        }
        getParent()?.attachmentCallback(EVENT_DIALOG_CANCELED, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.events.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        }

        viewModel.setData(
            assignment, fileSubmitUris, uploadType, canvasContext, parentFolderId, quizQuestionId,
            position, quizId, userId, attemptId, dialogCallback
        )
    }

    private fun uploadClicked() {
        viewModel.uploadFiles()
    }

    private fun cancelClicked() {
        dismissAllowingStateLoss()
        viewModel.onCancelClicked()
    }

    private fun handleAction(action: FileUploadAction) {
        when (action) {
            is FileUploadAction.TakePhoto -> takePicture()
            is FileUploadAction.PickImage -> pickFromGallery()
            is FileUploadAction.PickFile -> pickFromFiles()
            is FileUploadAction.PickMultipleFile -> pickMultipleFile()
            is FileUploadAction.PickMultipleImage -> pickMultipleImage()
            is FileUploadAction.ShowToast -> Toast.makeText(requireContext(), action.toast, Toast.LENGTH_SHORT).show()
            is FileUploadAction.UploadStarted -> dismiss()
            is FileUploadAction.AttachmentSelectedAction -> getParent()?.attachmentCallback(action.event, action.attachment)
            is FileUploadAction.UploadStartedAction -> {
                getParent()?.selectedUriStringsCallback(action.selectedUris)
                getParent()?.workInfoLiveDataCallback(action.id, action.liveData)
            }
        }
    }

    private fun getParent(): FileUploadDialogParent? {
        if (dialogParent != null) {
            return dialogParent
        }
        var parent = parentFragment as? FileUploadDialogParent
        if (parent == null) {
            parent = activity as? FileUploadDialogParent
        }

        return parent
    }

    private fun takePicture() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionContract.launch(Manifest.permission.CAMERA)
        } else {
            val fileName = "pic_" + System.currentTimeMillis().toString() + ".jpg"
            val file = File(FileUploadUtils.getExternalCacheDir(requireContext()), fileName)

            cameraImageUri = FileProvider.getUriForFile(requireContext(), requireContext().packageName + Const.FILE_PROVIDER_AUTHORITY, file)
            takePictureContract.launch(cameraImageUri)
        }
    }

    private fun pickFromGallery() {
        filePickerContract.launch("image/*")
    }

    private fun pickFromFiles() {
        filePickerContract.launch("*/*")
    }

    private fun pickMultipleFile() {
        multipleFilePickerContract.launch("*/*")
    }

    private fun pickMultipleImage() {
        multipleFilePickerContract.launch("image/*")
    }

    companion object {

        const val TAG = "FileUploadDialogFragment"

        private const val INVALID_ID = -1L
        private const val INVALID_ID_INT = -1

        const val EVENT_DIALOG_CANCELED = 1
        const val EVENT_ON_UPLOAD_BEGIN = 2
        const val EVENT_ON_FILE_SELECTED = 3

        fun newInstance(): FileUploadDialogFragment = FileUploadDialogFragment()

        fun newInstance(
            args: Bundle,
            callback: ((Int) -> Unit)? = null,
            dialogParent: FileUploadDialogParent? = null
        ): FileUploadDialogFragment {
            return FileUploadDialogFragment().apply {
                arguments = args

                fileSubmitUris = args.getParcelableArrayList(Const.URIS)
                uploadType = args.getSerializable(Const.UPLOAD_TYPE) as FileUploadType
                parentFolderId = args.getLong(Const.PARENT_FOLDER_ID, INVALID_ID)
                quizQuestionId = args.getLong(Const.QUIZ_ANSWER_ID, INVALID_ID)
                quizId = args.getLong(Const.QUIZ, INVALID_ID)
                courseId = args.getLong(Const.COURSE_ID, INVALID_ID)
                position = args.getInt(Const.POSITION, INVALID_ID_INT)
                dialogCallback = callback
                this.dialogParent = dialogParent
                userId = args.getLong(Const.USER_ID, INVALID_ID)
                attemptId = args.getLong(Const.SUBMISSION_ATTEMPT, INVALID_ID).takeIf { it != INVALID_ID }
            }
        }

        fun createBundle(submitURIs: ArrayList<Uri>, type: FileUploadType, parentFolderId: Long?): Bundle {
            val bundle = Bundle()
            if (submitURIs.isNotEmpty()) bundle.putParcelableArrayList(Const.URIS, submitURIs)
            if (parentFolderId != null) bundle.putLong(Const.PARENT_FOLDER_ID, parentFolderId)
            bundle.putSerializable(Const.UPLOAD_TYPE, type)
            return bundle
        }

        fun createMessageAttachmentsBundle(defaultFileList: ArrayList<FileSubmitObject>): Bundle {
            val bundle = createBundle(arrayListOf(), FileUploadType.MESSAGE, null)
            bundle.putParcelableArrayList(Const.FILES, defaultFileList)
            return bundle
        }

        fun createDiscussionsBundle(defaultFileList: ArrayList<FileSubmitObject>): Bundle {
            val bundle = createBundle(arrayListOf(), FileUploadType.DISCUSSION, null)
            bundle.putParcelableArrayList(Const.FILES, defaultFileList)
            return bundle
        }

        fun createFilesBundle(submitUris: ArrayList<Uri>, parentFolderId: Long?): Bundle {
            return createBundle(submitUris, FileUploadType.USER, parentFolderId)
        }

        fun createContextBundle(submitURI: Uri?, context: CanvasContext, parentFolderId: Long?): Bundle {
            return when {
                context.isCourse -> createCourseBundle(submitURI, context as Course, parentFolderId)
                context.isGroup -> createGroupBundle(submitURI, context as Group, parentFolderId)
                else -> createUserBundle(submitURI, context as User, parentFolderId)
            }
        }

        private fun createCourseBundle(submitURI: Uri?, course: Course, parentFolderId: Long?): Bundle {
            val submitUris = submitURI?.let {
                arrayListOf(it)
            } ?: arrayListOf()
            val bundle = createBundle(submitUris, FileUploadType.COURSE, parentFolderId)
            bundle.putParcelable(Const.CANVAS_CONTEXT, course)
            return bundle
        }

        private fun createGroupBundle(submitURI: Uri?, group: Group, parentFolderId: Long?): Bundle {
            val submitUris = submitURI?.let {
                arrayListOf(it)
            } ?: arrayListOf()
            val bundle = createBundle(submitUris, FileUploadType.GROUP, parentFolderId)
            bundle.putParcelable(Const.CANVAS_CONTEXT, group)
            return bundle
        }

        private fun createUserBundle(submitURI: Uri?, user: User, parentFolderId: Long?): Bundle {
            val submitUris = submitURI?.let {
                arrayListOf(it)
            } ?: arrayListOf()
            val bundle = createBundle(submitUris, FileUploadType.USER, parentFolderId)
            bundle.putParcelable(Const.CANVAS_CONTEXT, user)
            return bundle
        }

        fun createAssignmentBundle(submitURIs: ArrayList<Uri>, course: Course, assignment: Assignment): Bundle {
            val bundle = createBundle(submitURIs, FileUploadType.ASSIGNMENT, null)
            bundle.putParcelable(Const.CANVAS_CONTEXT, course)
            bundle.putParcelable(Const.ASSIGNMENT, assignment)
            return bundle
        }

        fun createQuizFileBundle(quizQuestionId: Long, courseId: Long, quizId: Long, position: Int): Bundle {
            val bundle = createBundle(arrayListOf(), FileUploadType.QUIZ, null)
            bundle.putLong(Const.QUIZ_ANSWER_ID, quizQuestionId)
            bundle.putLong(Const.QUIZ, quizId)
            bundle.putLong(Const.COURSE_ID, courseId)
            bundle.putInt(Const.POSITION, position)
            return bundle
        }

        fun createSubmissionCommentBundle(course: Course, assignment: Assignment, defaultFileList: ArrayList<FileSubmitObject>): Bundle {
            val bundle = createBundle(arrayListOf(), FileUploadType.SUBMISSION_COMMENT, null)
            bundle.putParcelable(Const.CANVAS_CONTEXT, course)
            bundle.putParcelable(Const.ASSIGNMENT, assignment)
            bundle.putParcelableArrayList(Const.FILES, defaultFileList)
            return bundle
        }

        fun createAttachmentsBundle(defaultFileList: ArrayList<FileSubmitObject> = ArrayList()): Bundle {
            val bundle = createBundle(arrayListOf(), FileUploadType.MESSAGE, null)
            bundle.putParcelableArrayList(Const.FILES, defaultFileList)
            return bundle
        }

        fun createTeacherSubmissionCommentBundle(
            courseId: Long,
            assignmentId: Long,
            userId: Long,
            attemptId: Long?
        ): Bundle {
            val bundle = createBundle(arrayListOf(), FileUploadType.TEACHER_SUBMISSION_COMMENT, null)
            bundle.putParcelable(Const.ASSIGNMENT, Assignment(assignmentId, courseId = courseId))
            bundle.putLong(Const.USER_ID, userId)
            bundle.putLong(Const.SUBMISSION_ATTEMPT, attemptId.orDefault(-1))
            return bundle
        }
    }
}