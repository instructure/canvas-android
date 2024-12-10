/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.student.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.postmodels.DiscussionTopicPostBody
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.NetworkUtils
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.canvasapi2.utils.weave.*
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_CREATE_DISCUSSION
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.dialogs.DatePickerDialogFragment
import com.instructure.pandautils.dialogs.TimePickerDialogFragment
import com.instructure.pandautils.models.DueDateGroup
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.databinding.FragmentCreateDiscussionBinding
import com.instructure.student.dialog.UnsavedChangesExitDialog
import com.instructure.student.events.DiscussionCreatedEvent
import com.instructure.student.events.post
import com.instructure.student.view.AssignmentOverrideView
import kotlinx.coroutines.Job
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.*

@PageView("courses/{canvasContext}/discussion_topics/new")
@ScreenView(SCREEN_VIEW_CREATE_DISCUSSION)
class CreateDiscussionFragment : ParentFragment() {

    private val binding by viewBinding(FragmentCreateDiscussionBinding::bind)

    @get:PageViewUrlParam("canvasContext")
    var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private val sendButton: TextView? get() = view?.findViewById(R.id.menuSaveDiscussion)
    private val saveButton: TextView? get() = view?.findViewById(R.id.menuSave)
//    private val mAttachmentButton: TextView? get() = view?.findViewById(R.id.menuAddAttachment) BLOCKED COMMS 868

    // Bundle Args
    private var discussionTopicHeader: DiscussionTopicHeader? by NullableParcelableArg(key = DISCUSSION_TOPIC_HEADER) // Null if new discussion, not null if editing; editing not available in Student

    // Other state
    private var allowThreaded: Boolean by BooleanArg(false)
    private var usersMustPost: Boolean by BooleanArg(false)
    private var description by NullableStringArg()
    private var hasLoadedDataForEdit by BooleanArg()

    private var editDateGroups: MutableList<DueDateGroup> = arrayListOf()

    private var createDiscussionCall: Job? = null
    private var rceImageJob: Job? = null

    /**
     * (Creation mode only) An attachment to be uploaded alongside the discussion. Note that this
     * can only be used when creating new discussions. Setting/changing attachments on existing
     * discussions (editing mode) is currently unsupported.
     */
    var attachment: FileSubmitObject? = null

    /** (Editing mode only) Set to *true* if the existing discussions's attachment should be removed */
    private var attachmentRemoved = false

    private val datePickerOnClick: (date: Date?, (Int, Int, Int) -> Unit) -> Unit = { date, callback ->
        DatePickerDialogFragment.getInstance(requireActivity().supportFragmentManager, date) { year, month, dayOfMonth ->
            callback(year, month, dayOfMonth)
        }.show(requireActivity().supportFragmentManager, DatePickerDialogFragment::class.java.simpleName)
    }

    private val timePickerOnClick: (date: Date?, (Int, Int) -> Unit) -> Unit = { date, callback ->
        TimePickerDialogFragment.getInstance(requireActivity().supportFragmentManager, date) { hour, min ->
            callback(hour, min)
        }.show(requireActivity().supportFragmentManager, TimePickerDialogFragment::class.java.simpleName)
    }

    //region Fragment Lifecycle Overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        discussionTopicHeader = nonNullArgs.getParcelable(DISCUSSION_TOPIC_HEADER)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return layoutInflater.inflate(R.layout.fragment_create_discussion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupViews()
        binding.attachmentLayout.setGone()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            // Get the image Uri
            when (requestCode) {
                RequestCodes.PICK_IMAGE_GALLERY -> data?.data
                RequestCodes.CAMERA_PIC_REQUEST -> MediaUploadUtils.handleCameraPicResult(requireActivity(), null)
                else -> null
            }?.let { imageUri ->
                // If the image Uri is not null, upload it
                rceImageJob = MediaUploadUtils.uploadRceImageJob(imageUri, canvasContext, requireActivity()) { imageUrl -> binding.descriptionRCEView.insertImage(requireActivity(), imageUrl) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        createDiscussionCall?.cancel()
        rceImageJob?.cancel()
    }

    //endregion

    //region Fragment Interaction Overrides
    override fun title() = ""
    override fun applyTheme() = setupToolbar()
    //endregion

    //region Setup
    private fun setupToolbar() {
        with (binding) {
            createDiscussionToolbar.setupAsCloseButton {
                if (discussionTopicHeader == null) {
                    activity?.onBackPressed()
                } else {
                    if (discussionTopicHeader?.message == descriptionRCEView.html) {
                        activity?.onBackPressed()
                    } else {
                        UnsavedChangesExitDialog.show(requireFragmentManager()) {
                            activity?.onBackPressed()
                        }
                    }
                }
            }

            createDiscussionToolbar.title =
                if (discussionTopicHeader == null) getString(R.string.utils_createDiscussion) else getString(R.string.utils_editDiscussion)
            createDiscussionToolbar.setMenu(if (discussionTopicHeader == null) R.menu.create_discussion else R.menu.menu_save_generic) { menuItem ->
                when (menuItem.itemId) {
                    R.id.menuSaveDiscussion, R.id.menuSave -> if (NetworkUtils.isNetworkAvailable) saveDiscussion()
                    //R.id.menuAddAttachment -> if (discussionTopicHeader == null) addAttachment() BLOCKED COMMS 868
                }
            }
            ViewStyler.themeToolbarLight(requireActivity(), createDiscussionToolbar)
            ViewStyler.setToolbarElevationSmall(requireContext(), createDiscussionToolbar)
            sendButton?.setTextColor(ThemePrefs.textButtonColor)
            saveButton?.setTextColor(ThemePrefs.textButtonColor)
        }
    }

    fun setupViews() = with(binding) {
        (view as? ViewGroup)?.descendants<TextInputLayout>()?.forEach {
            it.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }

        descriptionRCEView.setHtml(description ?: discussionTopicHeader?.message,
                getString(R.string.utils_discussionDetails),
                getString(R.string.rce_empty_description),
                ThemePrefs.brandColor, ThemePrefs.textButtonColor)

        descriptionRCEView.hideEditorToolbar()
        discussionNameTextInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) descriptionRCEView.hideEditorToolbar()
        }

        descriptionRCEView.actionUploadImageCallback = { MediaUploadUtils.showPickImageDialog(this@CreateDiscussionFragment) }

        // When the RCE editor has focus we want the label to be darker so it matches the title's functionality
        descriptionRCEView.setLabel(discussionDescLabel, R.color.textDarkest, R.color.textDark)

        if (!hasLoadedDataForEdit) discussionTopicHeader?.let {
            editDiscussionName.setText(it.title)
            allowThreaded = it.type == DiscussionTopicHeader.DiscussionType.THREADED
            usersMustPost = it.requireInitialPost
            hasLoadedDataForEdit = true
        }

        ViewStyler.themeEditText(requireContext(), editDiscussionName, ThemePrefs.brandColor)

        setupAllowThreadedSwitch()
        setupUsersMustPostSwitch()
//        updateAttachmentUI() BLOCKED COMMS 868

        if (editDateGroups.isEmpty()) {
            // If the dateGroups is empty, we want to add a due date so that we can set the available from and to fields
            editDateGroups.clear()
            val dueDateGroup = DueDateGroup()
            if (discussionTopicHeader != null) {
                // Populate the availability dates if we have them, the assignment is null, so this is an ungraded assignment
                dueDateGroup.coreDates.lockDate = (discussionTopicHeader as DiscussionTopicHeader).lockAt
                dueDateGroup.coreDates.unlockDate = (discussionTopicHeader as DiscussionTopicHeader).delayedPostDate
            }
            editDateGroups.add(dueDateGroup)
        }

        setupOverrides()

        setupDelete()
    }

    private fun setupOverrides() {
        binding.overrideContainer.removeAllViews()

        // Load in overrides
        editDateGroups.forEachIndexed { index, dueDateGroup ->
            val assignees = ArrayList<String>()
            val v = AssignmentOverrideView(requireActivity())

            v.toAndFromDatesOnly()

            v.setupOverride(index, dueDateGroup, editDateGroups.size > 1, assignees, datePickerOnClick, timePickerOnClick, {
                if (editDateGroups.contains(it)) editDateGroups.remove(it)
                setupOverrides()
            }) { }

            binding.overrideContainer.addView(v)
        }
    }

    private fun setupAllowThreadedSwitch() {
        binding.threadedSwitch.apply {
            applyTheme()
            isChecked = allowThreaded
            setOnCheckedChangeListener { _, isChecked -> allowThreaded = isChecked }
        }
    }

    private fun setupUsersMustPostSwitch() {
        binding.usersMustPostSwitch.apply {
            applyTheme()
            isChecked = usersMustPost
            setOnCheckedChangeListener { _, isChecked -> usersMustPost = isChecked }
        }
    }

    private fun setupDelete() {
        // TODO - For now we set it to be gone, in the future we will revisit after COMMS-868
        binding.deleteWrapper.setGone()
        /*
        deleteWrapper.setVisible(discussionTopicHeader != null)
        deleteWrapper.onClickWithRequireNetwork {
            AlertDialog.Builder(requireContext())
                    .setTitle(R.string.discussionsDeleteTitle)
                    .setMessage(R.string.discussionsDeleteMessage)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        if(discussionTopicHeader != null) {
                            deleteDiscussionTopicHeader(discussionTopicHeader!!.id)
                        }
                    }
                    .setNegativeButton(R.string.cancel) { _, _ -> }
                    .showThemed()
        }
        */
    }
    //endregion

    //region Functionality
    private fun saveDiscussion() = with(binding) {
        if (discussionTopicHeader != null) {
            val postData = DiscussionTopicPostBody()
            // Discussion title isn't required
            if (editDiscussionName.text.isNullOrBlank()) {
                postData.title = getString(R.string.utils_noTitle)
            } else {
                postData.title = editDiscussionName.text?.toString() ?: getString(R.string.utils_noTitle)
            }
            postData.message = descriptionRCEView.html
            postData.discussionType = if (allowThreaded) {
                DiscussionTopicHeader.DiscussionType.THREADED.toString().lowercase(Locale.getDefault())
            } else {
                DiscussionTopicHeader.DiscussionType.SIDE_COMMENT.toString().lowercase(Locale.getDefault())
            }
            postData.requireInitialPost = usersMustPost

            editDiscussion((discussionTopicHeader as DiscussionTopicHeader).id, postData)
        } else {
            val discussionTopicHeader = DiscussionTopicHeader()

            if (editDiscussionName.text.isNullOrBlank()) {
                discussionTopicHeader.title = getString(R.string.utils_noTitle)
            } else {
                discussionTopicHeader.title = editDiscussionName.text.toString()
            }
            discussionTopicHeader.message = descriptionRCEView.html
            discussionTopicHeader.type = if (allowThreaded) DiscussionTopicHeader.DiscussionType.THREADED else DiscussionTopicHeader.DiscussionType.SIDE_COMMENT
            discussionTopicHeader.requireInitialPost = usersMustPost

            if (editDateGroups[0].coreDates.unlockDate != null) {
                discussionTopicHeader.delayedPostDate = editDateGroups[0].coreDates.unlockDate
            }

            if (editDateGroups[0].coreDates.lockDate != null) {
                discussionTopicHeader.lockAt = editDateGroups[0].coreDates.lockDate
            }

            saveDiscussion(discussionTopicHeader)
        }

    }

    private fun saveDiscussion(discussionTopicHeader: DiscussionTopicHeader) {
        startSavingDiscussion()
        @Suppress("EXPERIMENTAL_FEATURE_WARNING")
        createDiscussionCall = tryWeave {
            var filePart: MultipartBody.Part? = null
            attachment?.let {
                val file = File(it.fullPath)
                val requestBody = file.asRequestBody(it.contentType.toMediaTypeOrNull())
                filePart = MultipartBody.Part.createFormData("attachment", file.name, requestBody)
            }
            awaitApi<DiscussionTopicHeader> { DiscussionManager.createStudentDiscussion(canvasContext, discussionTopicHeader, filePart, it) }
            discussionSavedSuccessfully(null)

        } catch {
            if (it is StatusCallbackError) {
                val statusCode = it.response?.code()
                if (statusCode == 500) { // Quota has been reached. Likely the discussion as indeed created.
                    errorSavingDiscussionAttachment()
                } else {
                    errorSavingDiscussion()
                }
            }
        }
    }

    private fun editDiscussion(topicId: Long, discussionTopicPostBody: DiscussionTopicPostBody) {
        startSavingDiscussion()
        @Suppress("EXPERIMENTAL_FEATURE_WARNING")
        createDiscussionCall = weave {
            try {
                if (attachmentRemoved) discussionTopicPostBody.removeAttachment = ""
                val discussionTopic = awaitApi<DiscussionTopicHeader> { DiscussionManager.editDiscussionTopic(canvasContext, topicId, discussionTopicPostBody, it) }
                discussionSavedSuccessfully(discussionTopic)

            } catch (e: Throwable) {
                errorSavingDiscussion()
            }
        }
    }

    /* Revisit after COMMS-868
    private fun deleteDiscussionTopicHeader(discussionTopicHeaderId: Long) {
        DiscussionManager.deleteDiscussionTopicHeader(canvasContext, discussionTopicHeaderId, object : StatusCallback<Void>() {
            override fun onResponse(response: Response<Void>, linkHeaders: LinkHeaders, type: ApiType) {
                if (response.code() in 200..299) {
//                    DiscussionTopicHeaderDeletedEvent(discussionTopicHeaderId, (DiscussionsDetailsFragment::class.java.toString() + ".onResume()")).post() // Todo -re-add after COMSS-868
                    discussionDeletedSuccessfully()
                }
            }
        })
    }
    */

    private fun startSavingDiscussion() {
        sendButton?.setGone()
//        mAttachmentButton?.setGone() Blocked COMMS 868
        binding.savingProgressBar.announceForAccessibility(getString(R.string.utils_saving))
        binding.savingProgressBar.setVisible()
    }

    private fun errorSavingDiscussion() {
        sendButton?.setVisible()
        /* blocked COMMS 868
        val quantity = if(attachment == null) 0 else 1
        mAttachmentButton?.text = resources.getQuantityString(R.plurals.utils_addAttachment, quantity, quantity)
        mAttachmentButton?.setVisible()
        */
        binding.savingProgressBar.setGone()
    }

    private fun errorSavingDiscussionAttachment() {
        toast(R.string.utils_discussionSuccessfulAttachmentNot)
        binding.editDiscussionName.hideKeyboard() // Close the keyboard
        navigation?.popCurrentFragment()
    }

    private fun discussionSavedSuccessfully(discussionTopic: DiscussionTopicHeader?) {
        if (discussionTopic == null) {
            DiscussionCreatedEvent(true).post() // Post bus event
            toast(R.string.utils_discussionSuccessfullyCreated) // Let the user know the discussion was saved
        } else {
//            discussionTopic.assignment = getAssignment()
//            DiscussionUpdatedEvent(discussionTopic).post() TODO - re-add after COMMS-868
            toast(R.string.utils_discussionSuccessfullyUpdated)
        }

        binding.editDiscussionName.hideKeyboard() // Close the keyboard
        navigation?.popCurrentFragment()
    }

    /* Uncomment after COMMS 868
    fun discussionDeletedSuccessfully() {
        activity?.onBackPressed()
    } */

    /* Blocked COMMS 868
    private fun updateAttachmentUI() {
        updateAttachmentButton()
        attachmentLayout.clearAttachmentViews()

        // Show attachment waiting to upload (if any)
        attachment?.let { attachment ->
            val attachmentView = AttachmentView(requireContext())
            attachmentView.setPendingAttachment(attachment.toAttachment(), true) { action, _ ->
                if (action == AttachmentView.AttachmentAction.REMOVE) {
                    this@CreateDiscussionFragment.attachment = null
                    updateAttachmentButton()
                }
            }
            attachmentLayout.addView(attachmentView)
        }

        // Show existing attachment (if any)
        discussionTopicHeader?.attachments?.firstOrNull()?.let {
            val attachmentView = AttachmentView(requireContext())
            attachmentView.setPendingRemoteFile(it, true) { action, attachment ->
                if (action == AttachmentView.AttachmentAction.REMOVE) {
                    this@CreateDiscussionFragment.attachmentRemoved = true
                    discussionTopicHeader?.attachments?.remove(attachment)
                }
            }
            attachmentLayout.addView(attachmentView)
        }
    }
    */

    /* blocked COMMS 868
    private fun updateAttachmentButton(show: Boolean = true) {
        val quantity = if(attachment == null) 0 else 1
        mAttachmentButton?.text = resources.getQuantityString(R.plurals.utils_addAttachment, quantity, quantity)
        // Only show if (1) we're in creation mode and (2) we don't already have an attachment
        mAttachmentButton?.setVisible(show && discussionTopicHeader == null && attachment == null)
    }
    */

    /* Blocked COMMS 868
    private fun addAttachment() {
        val bundle = UploadFilesDialog.createDiscussionsBundle(ArrayList())
        UploadFilesDialog.show(fragmentManager, bundle, { event, attachment ->
            if(event == UploadFilesDialog.EVENT_ON_FILE_SELECTED) {
                this.attachment = attachment
                updateAttachmentUI()
            }
        })
    }
    */
    //endregion

    companion object {
        private val DISCUSSION_TOPIC_HEADER = "discussion_topic_header"

        fun makeRoute(canvasContext: CanvasContext, discussionTopicHeader: DiscussionTopicHeader? = null): Route {
            val bundle = Bundle().apply {
                putParcelable(DISCUSSION_TOPIC_HEADER, discussionTopicHeader)
            }

            return Route(CreateDiscussionFragment::class.java, canvasContext, bundle)
        }

        fun newInstance(route: Route) = if (validRoute(route)) {
            CreateDiscussionFragment().apply {
                arguments = route.canvasContext!!.makeBundle(route.arguments)
            }
        } else null

        private fun validRoute(route: Route) = route.canvasContext != null
    }
}
