/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.Logger
import com.instructure.pandautils.analytics.SCREEN_VIEW_DISCUSSIONS_REPLY
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.dialogs.UploadFilesDialog
import com.instructure.pandautils.fragments.BasePresenterFragment
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.AttachmentView
import com.instructure.teacher.R
import com.instructure.teacher.dialog.NoInternetConnectionDialog
import com.instructure.teacher.events.DiscussionEntryEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.factory.DiscussionsReplyFactory
import com.instructure.teacher.presenters.DiscussionsReplyPresenter
import com.instructure.teacher.presenters.DiscussionsReplyPresenter.Companion.REASON_MESSAGE_EMPTY
import com.instructure.teacher.presenters.DiscussionsReplyPresenter.Companion.REASON_MESSAGE_FAILED_TO_SEND
import com.instructure.teacher.presenters.DiscussionsReplyPresenter.Companion.REASON_MESSAGE_IN_PROGRESS
import com.instructure.teacher.utils.setupCloseButton
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.viewinterface.DiscussionsReplyView
import kotlinx.android.synthetic.main.fragment_discussions_reply.*

@ScreenView(SCREEN_VIEW_DISCUSSIONS_REPLY)
class DiscussionsReplyFragment : BasePresenterFragment<DiscussionsReplyPresenter, DiscussionsReplyView>(), DiscussionsReplyView {

    private var mCanvasContext: CanvasContext by ParcelableArg(default = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, -1L, ""))
    private var mDiscussionTopicHeaderId: Long by LongArg(default = 0L) // The topic the discussion belongs too
    private var mDiscussionEntryId: Long by LongArg(default = 0L) // The future parent of the discussion entry we are creating

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    override fun onRefreshFinished() {}
    override fun onRefreshStarted() {}

    override fun layoutResId(): Int = R.layout.fragment_discussions_reply

    override fun getPresenterFactory() = DiscussionsReplyFactory(mCanvasContext, mDiscussionTopicHeaderId, mDiscussionEntryId)

    override fun onPresenterPrepared(presenter: DiscussionsReplyPresenter) {}

    override fun onReadySetGo(presenter: DiscussionsReplyPresenter) {
        rceTextEditor.setHint(R.string.rce_empty_message)
        rceTextEditor.requestEditorFocus()
        rceTextEditor.showEditorToolbar()
        rceTextEditor.actionUploadImageCallback = { MediaUploadUtils.showPickImageDialog(this) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            // Get the image Uri
            when (requestCode) {
                RequestCodes.PICK_IMAGE_GALLERY -> data?.data
                RequestCodes.CAMERA_PIC_REQUEST -> MediaUploadUtils.handleCameraPicResult(requireActivity(), null)
                else -> null
            }?.let { imageUri ->
                presenter.uploadRceImage(imageUri, requireActivity())
            }
        }
    }

    override fun messageSuccess(entry: DiscussionEntry) {
        DiscussionEntryEvent(entry).post()
        requireActivity().onBackPressed()
        toast(R.string.discussion_sent_success)
    }

    override fun messageFailure(reason: Int) {
        when (reason) {
            REASON_MESSAGE_IN_PROGRESS -> {
                Logger.e("User tried to send message multiple times in a row.")
            }
            REASON_MESSAGE_EMPTY -> {
                Logger.e("User tried to send message an empty message.")
                toast(R.string.discussion_sent_empty)
            }
            REASON_MESSAGE_FAILED_TO_SEND -> {
                Logger.e("Message failed to send for some reason.")
                toast(R.string.discussion_sent_failure)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    private fun setupToolbar() {
        toolbar.title = getString(R.string.reply)
        toolbar.setupCloseButton(this)
        toolbar.setupMenu(R.menu.menu_discussion_reply, menuItemCallback)

        ViewStyler.themeToolbarBottomSheet(requireActivity(), isTablet, toolbar, Color.BLACK, false)
        ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)
    }

    val menuItemCallback: (MenuItem) -> Unit = { item ->
        when (item.itemId) {
            R.id.menu_send -> {
                if (APIHelper.hasNetworkConnection()) {
                    presenter.sendMessage(rceTextEditor.html)
                } else {
                    NoInternetConnectionDialog.show(requireFragmentManager())
                }
            }
            R.id.menu_attachment -> {
                if (APIHelper.hasNetworkConnection()) {
                    val attachments = ArrayList<FileSubmitObject>()
                    if (presenter.getAttachment() != null) {
                        attachments.add(presenter.getAttachment()!!)
                    }

                    val bundle = UploadFilesDialog.createDiscussionsBundle(attachments)
                    UploadFilesDialog.show(fragmentManager, bundle) { event, attachment ->
                        if(event == UploadFilesDialog.EVENT_ON_FILE_SELECTED) {
                            applyAttachment(attachment)
                        }
                    }
                } else {
                    NoInternetConnectionDialog.show(requireFragmentManager())
                }
            }
        }
    }

    private fun applyAttachment(file: FileSubmitObject?) {
        if(file != null) {
            presenter.setAttachment(file)
            attachments.setAttachment(file.toAttachment()) { action, _ ->
                if (action == AttachmentView.AttachmentAction.REMOVE) {
                    presenter.setAttachment(null)
                }
            }
        }
    }

    override fun insertImageIntoRCE(text: String, alt: String) = rceTextEditor.insertImage(text, alt)

    companion object {
        private const val DISCUSSION_TOPIC_HEADER_ID = "DISCUSSION_TOPIC_HEADER_ID"
        private const val DISCUSSION_ENTRY_ID = "DISCUSSION_ENTRY_ID"
        private const val IS_ANNOUNCEMENT = "IS_ANNOUNCEMENT"

        fun makeBundle(discussionTopicHeaderId: Long, discussionEntryId: Long, isAnnouncement: Boolean): Bundle =
                Bundle().apply {
                    putLong(DISCUSSION_TOPIC_HEADER_ID, discussionTopicHeaderId)
                    putLong(DISCUSSION_ENTRY_ID, discussionEntryId)
                    putBoolean(IS_ANNOUNCEMENT, isAnnouncement)
                }

        fun newInstance(canvasContext: CanvasContext, args: Bundle) =
                DiscussionsReplyFragment().apply {
                    mDiscussionTopicHeaderId = args.getLong(DISCUSSION_TOPIC_HEADER_ID)
                    mDiscussionEntryId = args.getLong(DISCUSSION_ENTRY_ID)
                    mCanvasContext = canvasContext
                }
    }
}
