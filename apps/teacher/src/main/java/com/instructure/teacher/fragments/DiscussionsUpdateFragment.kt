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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.pandautils.analytics.SCREEN_VIEW_DISCUSSIONS_UPDATE
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.dialogs.UnsavedChangesExitDialog
import com.instructure.pandautils.discussions.DiscussionUtils
import com.instructure.pandautils.fragments.BasePresenterFragment
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.MediaUploadUtils
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.Placeholder
import com.instructure.pandautils.utils.RequestCodes
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.handleLTIPlaceHolders
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.utils.withArgs
import com.instructure.pandautils.views.AttachmentView
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentDiscussionsEditBinding
import com.instructure.teacher.events.DiscussionEntryUpdatedEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.factory.DiscussionsUpdatePresenterFactory
import com.instructure.teacher.presenters.DiscussionsUpdatePresenter
import com.instructure.teacher.presenters.DiscussionsUpdatePresenter.Companion.REASON_MESSAGE_EMPTY
import com.instructure.teacher.presenters.DiscussionsUpdatePresenter.Companion.REASON_MESSAGE_FAILED_TO_SEND
import com.instructure.teacher.presenters.DiscussionsUpdatePresenter.Companion.REASON_MESSAGE_IN_PROGRESS
import com.instructure.teacher.utils.setupCloseButton
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.viewinterface.DiscussionsUpdateView

@PageView(url = "{canvasContext}/discussion_topics/{topicId}/edit")
@ScreenView(SCREEN_VIEW_DISCUSSIONS_UPDATE)
class DiscussionsUpdateFragment : BasePresenterFragment<
        DiscussionsUpdatePresenter,
        DiscussionsUpdateView,
        FragmentDiscussionsEditBinding>(),
    DiscussionsUpdateView {

    @get:PageViewUrlParam("canvasContext")
    var canvasContext: CanvasContext by ParcelableArg(default = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, -1L, ""))
    @get:PageViewUrlParam("topicId")
    var discussionTopicHeaderId: Long by LongArg(key = DISCUSSION_TOPIC_HEADER_ID, default = 0L) // The topic the discussion belongs too
    private var discussionEntry: DiscussionEntry by ParcelableArg(key = DISCUSSION_ENTRY, default = DiscussionEntry())
    private var discussionTopic: DiscussionTopic by ParcelableArg(key = DISCUSSION_TOPIC, default = DiscussionTopic())
    private var placeHolderList: ArrayList<Placeholder> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    override fun onRefreshFinished() {}
    override fun onRefreshStarted() {}

    override val bindingInflater: (layoutInflater: LayoutInflater) -> FragmentDiscussionsEditBinding = FragmentDiscussionsEditBinding::inflate

    override fun getPresenterFactory() = DiscussionsUpdatePresenterFactory(canvasContext, discussionTopicHeaderId, discussionEntry, discussionTopic)

    override fun onPresenterPrepared(presenter: DiscussionsUpdatePresenter) {}

    override fun onReadySetGo(presenter: DiscussionsUpdatePresenter): Unit = with(binding) {
        rceTextEditor.setHint(R.string.rce_empty_description)
        rceTextEditor.actionUploadImageCallback = { MediaUploadUtils.showPickImageDialog(this@DiscussionsUpdateFragment) }

        if (CanvasWebView.containsLTI(presenter.discussionEntry.message.orEmpty(), "UTF-8")) {
            rceTextEditor.setHtml(DiscussionUtils.createLTIPlaceHolders(requireContext(), presenter.discussionEntry.message.orEmpty()) { _, placeholder ->
                placeHolderList.add(placeholder)
            }, "", "", ThemePrefs.brandColor, ThemePrefs.textButtonColor)
        } else {
            rceTextEditor.setHtml(presenter.discussionEntry.message, "", "", ThemePrefs.brandColor, ThemePrefs.textButtonColor)
        }


        presenter.discussionEntry.attachments?.firstOrNull()?.let {
            val attachmentView = AttachmentView(requireContext())

            attachmentView.setPendingRemoteFile(it, true) { action, attachment ->
                if (action == AttachmentView.AttachmentAction.REMOVE) {
                    presenter.attachmentRemoved = true
                    presenter.discussionEntry.attachments?.remove(attachment)
                }
            }

            attachmentLayout.addView(attachmentView)
        }
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
        DiscussionEntryUpdatedEvent(entry).post()
        requireActivity().onBackPressed()
        toast(R.string.discussion_update_success)
    }

    override fun messageFailure(reason: Int) {
        when (reason) {
            REASON_MESSAGE_IN_PROGRESS -> {
                Logger.e("User tried to send message multiple times in a row.")
            }
            REASON_MESSAGE_EMPTY -> {
                Logger.e("User tried to send message an empty message.")
                toast(R.string.discussion_update_empty)
            }
            REASON_MESSAGE_FAILED_TO_SEND -> {
                Logger.e("Message failed to send for some reason.")
                toast(R.string.discussion_update_failure)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    private fun setupToolbar() = with(binding) {
        toolbar.title = getString(R.string.edit)
        toolbar.setupCloseButton {
            if (presenter.discussionEntry.message == rceTextEditor.html) {
                activity?.onBackPressed()
            } else {
                UnsavedChangesExitDialog.show(requireFragmentManager()) {
                    activity?.onBackPressed()
                }
            }
        }
        toolbar.setupMenu(R.menu.menu_discussion_update, menuItemCallback)

        ViewStyler.themeToolbarLight(requireActivity(), toolbar)
        ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)
    }

    override fun insertImageIntoRCE(imageUrl: String) = binding.rceTextEditor.insertImage(requireActivity(), imageUrl)

    private val menuItemCallback: (MenuItem) -> Unit = { item ->
        when (item.itemId) {
            R.id.menu_save -> {
                if(APIHelper.hasNetworkConnection()) {
                    presenter.editMessage(handleLTIPlaceHolders(placeHolderList, binding.rceTextEditor.html))
                } else {
                    Toast.makeText(requireContext(), R.string.noInternetConnectionMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        private const val DISCUSSION_TOPIC_HEADER_ID = "DISCUSSION_TOPIC_HEADER_ID"
        private const val DISCUSSION_ENTRY = "DISCUSSION_ENTRY"
        private const val DISCUSSION_TOPIC = "DISCUSSION_TOPIC"
        private const val IS_ANNOUNCEMENT = "IS_ANNOUNCEMENT"

        fun makeBundle(
                discussionTopicHeaderId: Long,
                discussionEntryId: DiscussionEntry?,
                isAnnouncement: Boolean,
                discussionTopic: DiscussionTopic): Bundle = Bundle().apply {
            putLong(DISCUSSION_TOPIC_HEADER_ID, discussionTopicHeaderId)
            putParcelable(DISCUSSION_ENTRY, discussionEntryId)
            putBoolean(IS_ANNOUNCEMENT, isAnnouncement)
            putParcelable(DISCUSSION_TOPIC, discussionTopic)
        }

        fun newInstance(canvasContext: CanvasContext, args: Bundle) =
            DiscussionsUpdateFragment().withArgs(args).apply { this.canvasContext = canvasContext }
    }
}
