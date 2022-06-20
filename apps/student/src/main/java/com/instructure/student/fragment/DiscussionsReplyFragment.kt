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
import android.os.Bundle
import android.view.*
import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionParticipant
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.StatusCallbackError
import com.instructure.canvasapi2.utils.weave.awaitApiResponse
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.router.Route
import com.instructure.loginapi.login.dialog.NoInternetConnectionDialog
import com.instructure.pandautils.analytics.SCREEN_VIEW_DISCUSSIONS_REPLY
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.dialogs.UploadFilesDialog
import com.instructure.pandautils.discussions.DiscussionCaching
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.AttachmentView
import com.instructure.student.R
import com.instructure.student.util.Const
import kotlinx.android.synthetic.main.fragment_discussions_reply.*
import kotlinx.coroutines.Job
import retrofit2.Response
import java.io.File

@ScreenView(SCREEN_VIEW_DISCUSSIONS_REPLY)
class DiscussionsReplyFragment : ParentFragment() {

    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    // Weave
    private var postDiscussionJob: Job? = null
    private var rceImageUploadJob: Job? = null

    // Bundle arguments
    private var discussionTopicHeaderId: Long by LongArg(default = 0L, key = DISCUSSION_TOPIC_HEADER_ID) // The topic the discussion belongs to
    private var discussionEntryId: Long by LongArg(default = 0L, key = DISCUSSION_ENTRY_ID) // The future parent of the discussion entry we are creating
    private var canAttach: Boolean by BooleanArg(key = CAN_ATTACH)

    private var attachment: FileSubmitObject? = null

    private val menuItemCallback: (MenuItem) -> Unit = { item ->
        when (item.itemId) {
            R.id.menu_send -> {
                if (APIHelper.hasNetworkConnection()) {
                    sendMessage(rceTextEditor.html)
                } else {
                    NoInternetConnectionDialog.show(requireFragmentManager())
                }
            }
            R.id.menu_attachment -> {
                if (APIHelper.hasNetworkConnection()) {
                    val attachments = ArrayList<FileSubmitObject>()
                    if (attachment != null) attachments.add(attachment!!)

                    val bundle = UploadFilesDialog.createDiscussionsBundle(attachments)
                    UploadFilesDialog.show(fragmentManager, bundle) { event, attachment ->
                        if (event == UploadFilesDialog.EVENT_ON_FILE_SELECTED) {
                            handleAttachment(attachment)
                        }
                    }
                } else {
                    NoInternetConnectionDialog.show(requireFragmentManager())
                }
            }
        }
    }

    //region Fragment Lifecycle Overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = layoutInflater.inflate(R.layout.fragment_discussions_reply, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rceTextEditor.setHint(R.string.rce_empty_message)
        rceTextEditor.actionUploadImageCallback = { MediaUploadUtils.showPickImageDialog(this) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        postDiscussionJob?.cancel()
        rceImageUploadJob?.cancel()
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
                MediaUploadUtils.uploadRceImageJob(imageUri, canvasContext, requireActivity()) { text, alt -> rceTextEditor.insertImage(text, alt) }
            }
        }
    }

    //endregion

    //region Fragment Interaction Overrides
    override fun title(): String = getString(R.string.reply)

    override fun applyTheme() {
        toolbar.title = getString(R.string.reply)
        toolbar.setupAsCloseButton(this)
        if (canAttach) {
            toolbar.setMenu(R.menu.menu_discussion_reply, menuItemCallback)
        } else {
            toolbar.setMenu(R.menu.menu_discussion_reply_no_attach, menuItemCallback)
        }
        ViewStyler.themeToolbarLight(requireActivity(), toolbar)
        ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)
    }
    //endregion

    //region Fragment Functionality
    private fun handleAttachment(file: FileSubmitObject?) {
        if (file != null) {
            this@DiscussionsReplyFragment.attachment = file
            attachments.setPendingAttachments(listOf(file.toAttachment()), true) { action, _ ->
                if (action == AttachmentView.AttachmentAction.REMOVE) {
                    this@DiscussionsReplyFragment.attachment = null
                }
            }
        } else {
            this@DiscussionsReplyFragment.attachment = null
            attachments.clearAttachmentViews()
        }
    }

    private fun sendMessage(message: String?) {
        if (postDiscussionJob?.isActive == true) return

        // Make the progress bar visible and the other buttons not there so they can't try to re-send the message multiple times
        toolbar.menu.findItem(R.id.menu_send).isVisible = false
        toolbar.menu.findItem(R.id.menu_attachment)?.isVisible = false
        savingProgressBar.announceForAccessibility(getString(R.string.sending))
        savingProgressBar.setVisible()

        postDiscussionJob = tryWeave {
            if (attachment == null) {
                if (discussionEntryId == discussionTopicHeaderId) {
                    messageSentResponse(awaitApiResponse { DiscussionManager.postToDiscussionTopic(canvasContext, discussionTopicHeaderId, message!!, it) }, topLevel = true)
                } else {
                    messageSentResponse(awaitApiResponse { DiscussionManager.replyToDiscussionEntry(canvasContext, discussionTopicHeaderId, discussionEntryId, message!!, it) })
                }
            } else {
                if (discussionEntryId == discussionTopicHeaderId) {
                    messageSentResponse(awaitApiResponse {
                        DiscussionManager.postToDiscussionTopic(canvasContext, discussionTopicHeaderId, message!!, File(attachment!!.fullPath), attachment?.contentType
                                ?: "multipart/form-data", it)
                    }, topLevel = true)
                } else {
                    messageSentResponse(awaitApiResponse {
                        DiscussionManager.replyToDiscussionEntry(canvasContext, discussionTopicHeaderId, discussionEntryId, message!!, File(attachment!!.fullPath), attachment?.contentType
                                ?: "multipart/form-data", it)
                    })
                }
            }
        } catch {
            if (isAdded && (it as StatusCallbackError).response?.code() != 400) messageFailure()
        }
    }

    private fun messageSentResponse(response: Response<DiscussionEntry>, topLevel: Boolean = false) {
        if (response.code() in 200..299 && response.body() != null) {
            val discussionEntry = response.body()

            ApiPrefs.user?.let {
                // The author does not come back in the response, we add the current user so things will display from cache properly
                discussionEntry!!.author = DiscussionParticipant(
                    id = it.id,
                    displayName = it.shortName,
                    pronouns = it.pronouns,
                    avatarImageUrl = it.avatarUrl
                )
            }

            // Post successful
            DiscussionCaching(discussionTopicHeaderId).saveEntry(discussionEntry) // Save to cache
            DiscussionEntryEvent(discussionEntry!!.id, topLevel).postSticky() // Notify about new reply
            toast(R.string.utils_discussionSentSuccess)
            activity?.onBackPressed()
        } else {
            // Post failure
            // 400 will be handled elsewhere. it means the quota has been reached
            if (response.code() != 400 && isAdded) {
                messageFailure()
            }
        }
    }

    private fun messageFailure() {
        toolbar.menu.findItem(R.id.menu_send).isVisible = true
        toolbar.menu.findItem(R.id.menu_attachment).isVisible = true
        savingProgressBar.visibility = View.GONE
        toast(R.string.utils_discussionSentFailure)
    }
    //endregion

    companion object {
        private const val DISCUSSION_TOPIC_HEADER_ID = "DISCUSSION_TOPIC_HEADER_ID"
        private const val DISCUSSION_ENTRY_ID = "DISCUSSION_ENTRY_ID"
        private const val CAN_ATTACH = "CAN_ATTACH"

        fun makeRoute(
                canvasContext: CanvasContext?,
                discussionTopicHeaderId: Long,
                discussionEntryId: Long,
                canAttach: Boolean): Route {

            val bundle = Bundle().apply {
                putLong(DISCUSSION_TOPIC_HEADER_ID, discussionTopicHeaderId)
                putLong(DISCUSSION_ENTRY_ID, discussionEntryId)
                putBoolean(CAN_ATTACH, canAttach)
            }

            return Route(DiscussionsReplyFragment::class.java, canvasContext, bundle)
        }

        fun newInstance(route: Route) = if (validRoute(route)) {
                    DiscussionsReplyFragment().apply {
                        arguments = route.canvasContext!!.makeBundle(route.arguments)
                    }
                } else null

        private fun validRoute(route: Route) = route.canvasContext != null &&
                route.arguments.containsKey(DISCUSSION_TOPIC_HEADER_ID) &&
                route.arguments.containsKey(DISCUSSION_ENTRY_ID) &&
                route.arguments.containsKey(CAN_ATTACH)
    }
}
