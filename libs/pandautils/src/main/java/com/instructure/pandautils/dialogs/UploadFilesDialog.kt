/*
 * Copyright (C) 2018 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.TypedValue
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_UPLOAD_FILES
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.services.FileUploadService
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.AttachmentView
import kotlinx.android.synthetic.main.adapter_file_uploads.view.*
import kotlinx.android.synthetic.main.dialog_files_upload.*
import kotlinx.android.synthetic.main.dialog_files_upload.view.*
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.util.*

@ScreenView(SCREEN_VIEW_UPLOAD_FILES)
class UploadFilesDialog : AppCompatDialogFragment() {

    init {
        retainInstance = true
    }

    enum class FileUploadType {
        ASSIGNMENT, COURSE, USER, MESSAGE, DISCUSSION, QUIZ, SUBMISSION_COMMENT, GROUP
    }

    private var getUriContentsJob: Job? = null

    private var dialogCallback: ((Int) -> Unit)? = null //(Int) -> Unit by Delegates.notNull()
    private var dialogAttachmentCallback: ((Int, FileSubmitObject?) -> Unit)? = null //-> Unit by Delegates.notNull()

    private var uploadType: FileUploadType by SerializableArg(FileUploadType.ASSIGNMENT)
    private var canvasContext: CanvasContext by ParcelableArg(ApiPrefs.user)
    private var isOneFileOnly: Boolean by BooleanArg()
    private var position: Int by IntArg()

    private var fileListAdapter: FileRecyclerViewAdapter? = null
    private val fileList: ArrayList<FileSubmitObject> by ParcelableArrayListArg(ArrayList())
    private var fileSubmitUri: Uri? = null
    private var cameraImageUri: Uri? = null

    private var assignment: Assignment? by NullableParcelableArg()
    private var parentFolderId: Long by LongArg()
    private var quizQuestionId: Long by LongArg()
    private var quizId: Long by LongArg()
    private var courseId: Long by LongArg()

    private var dialogRootView: View? = null

    //region Lifecycle

    override fun onStart() {
        EventBus.getDefault().register(this)
        super.onStart()
        // Don't dim the background when the dialog is created.
        dialog?.window?.let {
            val windowParams = it.attributes
            windowParams.dimAmount = 0f
            windowParams.flags = windowParams.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            it.attributes = windowParams

            setDialogMargins(it)
        }
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        dialog?.window?.let { setDialogMargins(it) }
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dialog?.let {
            it.window?.attributes?.windowAnimations = R.style.FileUploadDialogAnimation
            it.window?.setWindowAnimations(R.style.FileUploadDialogAnimation)
        }

        handleUriContents()
    }

    override fun onDestroy() {
        super.onDestroy()
        getUriContentsJob?.cancel()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        dialogCallback?.invoke(EVENT_DIALOG_CANCELED)
        dialogAttachmentCallback?.invoke(EVENT_DIALOG_CANCELED, null)
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe(sticky = true)
    fun onActivityResults(event: OnActivityResults) {
        event.get {
            event.remove()//Remove the event so it doesn't show up again somewhere else.

            if(it.resultCode == Activity.RESULT_OK) {
                if (it.requestCode == CAMERA_PIC_REQUEST) {
                    // Attempt to restore URI in case were were booted from memory
                    if (cameraImageUri == null) cameraImageUri = Uri.parse(FilePrefs.tempCaptureUri)

                    //if it's still null, tell the user there is an error and return.
                    if (cameraImageUri == null) {
                        Toast.makeText(activity, R.string.utils_errorGettingPhoto, Toast.LENGTH_SHORT).show()
                        return@get
                    }

                    getUriContents(cameraImageUri!!)
                } else if(it.data != null && it.data.data != null) {
                    getUriContents(it.data.data!!)
                }
            }
        }
    }

    //endregion

    //region View Setup

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

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

        dialogRootView = View.inflate(activity, R.layout.dialog_files_upload, null)
        setupViews(dialogRootView!!)

        val dialog = AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(dialogRootView)
                .setPositiveButton(positiveText, null)
                .setNegativeButton(R.string.utils_cancel, null)
                .create()

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.setOnShowListener {
            val positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            positive.setTextColor(ThemePrefs.buttonColor)
            positive.setOnClickListener { dialogPositiveButtonClick() }
            val negative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            negative.setTextColor(ThemePrefs.buttonColor)
            negative.setOnClickListener {
                onCancel(dialog)
                dismiss()
            }
        }

        return dialog
    }

    private fun setupViews(view: View) {
        //setup list view

        fileListAdapter = FileRecyclerViewAdapter { _, position ->
            fileList.removeAt(position)
            fileListAdapter!!.notifyItemRemoved(position)
            refreshFileButtonsVisibility(view)
        }.apply {
            view.fileRecyclerView.adapter = this
        }

        setOnClickListeners(view)
        setupAllowedExtensions(view)
    }

    private fun setupAllowedExtensions(view: View) {
        val allowedExtensions = view.findViewById<TextView>(R.id.allowedExtensions)
        //if there are only certain file types that are allowed, let the user know
        if (uploadType != UploadFilesDialog.FileUploadType.SUBMISSION_COMMENT && assignment != null && assignment?.allowedExtensions != null && (assignment?.allowedExtensions?.size ?: 0) > 0) {
            assignment!!.let {
                allowedExtensions.visibility = View.VISIBLE
                var extensions = getString(R.string.allowedExtensions)
                for (i in 0 until it.allowedExtensions.size) {
                    extensions += it.allowedExtensions[i]
                    if (it.allowedExtensions.size > 1 && i < it.allowedExtensions.size - 1) {
                        extensions += ","
                    }
                }
                allowedExtensions.text = extensions
            }
        } else {
            allowedExtensions.visibility = View.GONE
        }
    }

    private fun setDialogMargins(window: Window) {
        val displayMetrics = requireActivity().resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels.toFloat()
        val width = Math.round(screenWidth - convertDipsToPixels(2f, requireContext()) * 2) // * 2 for margin on each side
        window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun convertDipsToPixels(dp: Float, context: Context): Float {
        val resources = context.resources
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }

    private fun refreshFileButtonsVisibility(view: View) {
        if (isOneFileOnly && fileList.size > 0) {
            view.addButtonsContainer.setGone()
        } else {
            view.addButtonsContainer.setVisible()
        }
    }

    //endregion

    //region Click Listeners

    private fun setOnClickListeners(view: View) {
        view.fromCamera.onClick { onFromCameraClick() }
        view.fromGallery.onClick { onFromGalleryClick() }
        view.fromDevice.onClick { onFromDeviceClick() }
    }

    private fun dialogPositiveButtonClick() {
        when (uploadType) {
            FileUploadType.MESSAGE -> {
                /* Uploads for inbox messages handled as multi-part in the API POST */
                uploadFiles()
            }
            FileUploadType.DISCUSSION -> {
                /* Uploads for discussions handled as multi-part in the API POST */
                FilesSelected(fileList).post()
                if(isOneFileOnly && fileList.isNotEmpty()) {
                    dialogAttachmentCallback?.invoke(EVENT_ON_FILE_SELECTED, fileList.firstOrNull())
                }
                dismiss()
            }
            FileUploadType.QUIZ -> {
                QuizFileUploadStarted(Pair(quizQuestionId, position)).post()
                uploadFiles()
            }
            else -> uploadFiles()
        }
    }

    private fun onFromCameraClick() {
        if (!PermissionUtils.hasPermissions(requireActivity(), PermissionUtils.CAMERA)) {
            requestPermissions(PermissionUtils.makeArray(PermissionUtils.CAMERA), PermissionUtils.PERMISSION_REQUEST_CODE)
            return
        }

        val fileName = "pic_" + System.currentTimeMillis().toString() + ".jpg"
        val file = File(FileUploadUtils.getExternalCacheDir(requireContext()), fileName)

        cameraImageUri = FileProvider.getUriForFile(requireContext(), requireContext().packageName + Const.FILE_PROVIDER_AUTHORITY, file)

        if (cameraImageUri != null) {
            //save the intent information in case we get booted from memory.
            FilePrefs.tempCaptureUri = cameraImageUri.toString()
        }

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)

        if (isIntentAvailable(requireActivity(), intent.action)) {
            activity?.startActivityForResult(intent, CAMERA_PIC_REQUEST)
        }
    }

    private fun onFromGalleryClick() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        val file = File(requireContext().filesDir, "/image/*")
        intent.setDataAndType(FileProvider.getUriForFile(requireContext(),
                requireContext().packageName + Const.FILE_PROVIDER_AUTHORITY, file), "image/*")
        activity?.startActivityForResult(intent, PICK_IMAGE_GALLERY)
    }

    private fun onFromDeviceClick() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        activity?.startActivityForResult(intent, PICK_FILE_FROM_DEVICE)
    }

    //endregion

    //region Permissions

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.allPermissionsGrantedResultSummary(grantResults)) {
                onFromCameraClick()
            } else {
                Toast.makeText(activity, R.string.permissionDenied, Toast.LENGTH_LONG).show()
            }
        }
    }

    //endregion

    private fun uploadFiles() {
        if (fileList.size == 0) {
            if (isOneFileOnly) {
                addButtonsContainer.setVisible()
                dialogAttachmentCallback?.invoke(EVENT_ON_FILE_SELECTED, null)
                dismiss()
            } else Toast.makeText(activity, R.string.noFilesUploaded, Toast.LENGTH_SHORT).show()
        } else {
            if (uploadType == FileUploadType.ASSIGNMENT) {

                var uploadNotSupported = false

                if (!checkIfFileSubmissionAllowed()) { //see if we can actually submit files to this assignment
                    Toast.makeText(activity, R.string.fileUploadNotSupported, Toast.LENGTH_SHORT).show()
                    uploadNotSupported = true
                    return
                }

                //make sure that what we've uploaded can still be uploaded (allowed extensions)
                run {
                    fileList.forEach {
                        if (!isExtensionAllowed(it.fullPath)) {
                            //didn't match any of the extensions, don't upload
                            Toast.makeText(activity, R.string.oneOrMoreExtensionNotAllowed, Toast.LENGTH_SHORT).show()
                            uploadNotSupported = true
                            return@run
                        }
                    }
                }

                if(uploadNotSupported) return
            }

            // Start the upload service
            var bundle: Bundle? = null
            val intent = Intent(activity, FileUploadService::class.java)

            val parentFolderIdentifier = if(parentFolderId == INVALID_ID) null else parentFolderId

            when (uploadType) {
                FileUploadType.USER -> {
                    bundle = FileUploadService.getUserFilesBundle(fileList, parentFolderIdentifier)
                    intent.action = FileUploadService.ACTION_USER_FILE
                }
                FileUploadType.COURSE -> {
                    bundle = FileUploadService.getCourseFilesBundle(fileList, canvasContext.id, parentFolderIdentifier)
                    intent.action = FileUploadService.ACTION_COURSE_FILE
                }
                FileUploadType.GROUP -> {
                    bundle = FileUploadService.getCourseFilesBundle(fileList, canvasContext.id, parentFolderIdentifier)
                    intent.action = FileUploadService.ACTION_GROUP_FILE
                }
                FileUploadType.MESSAGE -> {
                    bundle = FileUploadService.getUserFilesBundle(fileList, null)
                    intent.action = FileUploadService.ACTION_MESSAGE_ATTACHMENTS
                }
                FileUploadType.DISCUSSION -> {
                    bundle = FileUploadService.getUserFilesBundle(fileList, null)
                    intent.action = FileUploadService.ACTION_DISCUSSION_ATTACHMENT
                }
                FileUploadType.QUIZ -> {
                    bundle = FileUploadService.getQuizFileBundle(fileList, parentFolderIdentifier, quizQuestionId, position, courseId, quizId)
                    intent.action = FileUploadService.ACTION_QUIZ_FILE
                }
                FileUploadType.SUBMISSION_COMMENT -> {
                    bundle = FileUploadService.getSubmissionCommentBundle(fileList, canvasContext.id, assignment!!)
                    intent.action = FileUploadService.ACTION_SUBMISSION_COMMENT
                }
                else -> {
                    if(assignment != null) {
                        bundle = FileUploadService.getAssignmentSubmissionBundle(fileList, canvasContext.id, assignment!!)
                        intent.action = FileUploadService.ACTION_ASSIGNMENT_SUBMISSION
                    }
                }
            }

            if(bundle != null) {
                dialogCallback?.invoke(EVENT_ON_UPLOAD_BEGIN)
                dialogAttachmentCallback?.invoke(EVENT_ON_UPLOAD_BEGIN, null)
                intent.putExtras(bundle)
                activity?.startService(intent)
                dismiss()
            }
        }
    }

    private fun handleUriContents() {
        //we only want to open the dialog in the beginning if we're not coming from an external source (sharing)
        if (uploadType == FileUploadType.MESSAGE || uploadType == FileUploadType.DISCUSSION) {
            return  // Do nothing
        } else if (fileSubmitUri != null) {
            getUriContents(fileSubmitUri!!)
        }
    }

    private fun getUriContents(fileUri: Uri) {
        getUriContentsJob = tryWeave {
            dialog?.fileLoadingContainer?.setVisible()

            val submitObject = inBackground<FileSubmitObject?> {
                val cr = requireActivity().contentResolver
                val mimeType = FileUploadUtils.getFileMimeType(cr, fileUri)
                val fileName = FileUploadUtils.getFileNameWithDefault(cr, fileUri)
                FileUploadUtils.getFileSubmitObjectFromInputStream(requireContext(), fileUri, fileName, mimeType)
            }

            submitObject?.let {
                if (it.errorMessage.isNullOrBlank()) {
                    addIfExtensionAllowed(it)
                } else {
                    Toast.makeText(activity, it.errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            dialog?.fileLoadingContainer?.setGone()
        } catch {
            Logger.e("Error with UploadFilesDialog.getUriContents: " + it.message)
            if(isAdded) dialog?.fileLoadingContainer?.setGone()
        }
    }

    private fun addIfExtensionAllowed(fileSubmitObject: FileSubmitObject): Boolean {
        if (assignment != null && (assignment?.allowedExtensions == null || assignment?.allowedExtensions?.size == 0)) {
            addToFileSubmitObjects(fileSubmitObject)
            return true
        }

        //get the extension and compare it to the list of allowed extensions
        val index = fileSubmitObject.fullPath.lastIndexOf(".")
        if (assignment != null && index != -1) {
            val ext = fileSubmitObject.fullPath.substring(index + 1)
            for (i in 0 until (assignment?.allowedExtensions?.size ?: 0)) {
                if (assignment!!.allowedExtensions[i].trim { it <= ' ' }.equals(ext, ignoreCase = true)) {
                    addToFileSubmitObjects(fileSubmitObject)
                    return true
                }
            }
            //didn't match any of the extensions, don't upload
            Toast.makeText(activity, R.string.extensionNotAllowed, Toast.LENGTH_SHORT).show()
            return false
        }

        //if we're sharing it from an external source we won't know which assignment they're trying to
        //submit to, so we won't know if there are any extension limits
        //also, the assignment and/or course could be null due to memory pressures
        if (assignment == null || canvasContext.id != 0L) {
            addToFileSubmitObjects(fileSubmitObject)
            return true
        }
        //don't want to try to upload it since it's not allowed.
        Toast.makeText(activity, R.string.extensionNotAllowed, Toast.LENGTH_SHORT).show()
        return false
    }

    private fun addToFileSubmitObjects(fileSubmitObject: FileSubmitObject) {
        fileList.add(fileSubmitObject)
        fileListAdapter?.notifyDataSetChanged()
        if(dialogRootView != null) refreshFileButtonsVisibility(dialogRootView!!)
    }

    // Used when the user hits the submit button after sharing files, we want to make sure they are allowed
    private fun isExtensionAllowed(filePath: String): Boolean {
        if (assignment != null && (assignment!!.allowedExtensions == null || assignment!!.allowedExtensions.size == 0)) {
            // There is an assignment, but no extension restriction...
            return true
        }
        // Get the extension and compare it to the list of allowed extensions
        val index = filePath.lastIndexOf(".")
        if (assignment != null && index != -1) {
            val ext = filePath.substring(index + 1)
            assignment!!.allowedExtensions.forEachIndexed { i, _ ->
                if(assignment!!.allowedExtensions[i].trim { it <= ' ' }.equals(ext, ignoreCase = true)) return true
            }
            return false
        }
        return false
    }

    private fun checkIfFileSubmissionAllowed(): Boolean {
        return if (assignment != null) {
            assignment!!.submissionTypesRaw.contains(Assignment.SubmissionType.ONLINE_UPLOAD.apiString)
        } else false
    }

    private fun isIntentAvailable(context: Context, action: String?): Boolean {
        return context.packageManager.queryIntentActivities(Intent(action), PackageManager.MATCH_DEFAULT_ONLY).size > 0
    }

    companion object {
        // Request codes, handled in Activity.onActivityResults(), activity required to implement EventBus in onActivityResults()
        const val CAMERA_PIC_REQUEST = RequestCodes.CAMERA_PIC_REQUEST
        const val PICK_IMAGE_GALLERY = RequestCodes.PICK_IMAGE_GALLERY
        const val PICK_FILE_FROM_DEVICE = 7000

        const val EVENT_DIALOG_CANCELED = 1
        const val EVENT_ON_UPLOAD_BEGIN = 2
        const val EVENT_ON_FILE_SELECTED = 3

        private const val INVALID_ID = -1L
        private const val INVALID_ID_INT = -1

        private fun getInstance(args: Bundle) : UploadFilesDialog {
            return UploadFilesDialog().apply {
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

        fun getInstance(args: Bundle, callback: (Int) -> Unit): UploadFilesDialog {
            return getInstance(args).apply { dialogCallback = callback }
        }

        fun getInstance(args: Bundle, callback: (Int, FileSubmitObject?) -> Unit): UploadFilesDialog {
            return getInstance(args).apply { dialogAttachmentCallback = callback }
        }

        /**
         * Typically what is used in Canvas Student and Teacher. Returns a status of the dialog fragment.
         * When file(s are selected the file(s) will trigger the [FileUploadService] to begin the upload.
         */
        fun show(fragmentManager: FragmentManager?, args: Bundle, callback: (Int) -> Unit) {
            if (fragmentManager != null) // Manager can be null if the user hits the back button too quickly
                UploadFilesDialog.getInstance(args, callback).show(fragmentManager, UploadFilesDialog::class.java.simpleName)
        }

        /**
         * Only used to pick a file and get an attachment object back. No file uploads are triggered when calling this function.
         */
        fun show(fragmentManager: FragmentManager?, args: Bundle, callback: (Int, FileSubmitObject?) -> Unit) {
            fragmentManager ?: return
            UploadFilesDialog.getInstance(args, callback).show(fragmentManager, UploadFilesDialog::class.java.simpleName)
        }

        fun createBundle(submitURI: Uri?, type: FileUploadType, parentFolderId: Long?): Bundle {
            val bundle = Bundle()
            if(submitURI != null) bundle.putParcelable(Const.URI, submitURI)
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

    inner class FileRecyclerViewAdapter(private val onRemovedFileCallback: (FileSubmitObject, Int) -> Unit) : RecyclerView.Adapter<FileViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder =
                FileViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_file_uploads, parent, false))

        override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
            holder.bind(fileList[position]) {
                // The position passed to onBindViewHolder could be stale at this point if the user has recently
                // added/removed items, so we need to use the current adapter position instead.
                val currentPosition = holder.adapterPosition
                onRemovedFileCallback(fileList[currentPosition], currentPosition)
            }
        }

        override fun getItemCount(): Int {
            return fileList.size
        }

        fun clear() {
            fileList.clear()
            notifyDataSetChanged()
        }
    }
}

class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(item: FileSubmitObject, onRemovedFileCallback: (FileSubmitObject) -> Unit): Unit = with(itemView) {

        AttachmentView.setColorAndIcon(context, item.contentType, item.name, null, fileIcon)

        ColorUtils.colorIt(ThemePrefs.brandColor, fileIcon)

        when {
            item.currentState == FileSubmitObject.STATE.UPLOADING -> {
                progressBar.isIndeterminate = true
                progressBar.visibility = View.VISIBLE
                fileIcon.setGone()
                removeFile.setGone()
            }
            item.currentState == FileSubmitObject.STATE.COMPLETE -> {
                fileIcon.setImageResource(R.drawable.ic_checkmark)
                removeFile.setGone()
                progressBar.isIndeterminate = false
                progressBar.visibility = View.GONE
                fileIcon.setVisible()
            }
            item.currentState == FileSubmitObject.STATE.NORMAL -> {
                removeFile.setImageResource(R.drawable.ic_close)
                removeFile.contentDescription = context.getString(R.string.utils_removeAttachment)
                removeFile.setOnClickListener {
                    onRemovedFileCallback(item)
                }
            }
        }

        fileName.text = item.name
        fileSize.text = humanReadableByteCount(item.size)
    }

    private fun humanReadableByteCount(bytes: Long): String {
        val unit = 1024
        if (bytes < unit) return bytes.toString() + " B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = "KMGTPE"[exp - 1].toString()
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }
}


