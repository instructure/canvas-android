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

import android.os.Bundle
import android.view.*
import android.widget.Toast
import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.postmodels.DiscussionEntryPostBody
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.weave.awaitApiResponse
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_DISCUSSIONS_UPDATE
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.dialogs.UnsavedChangesExitDialog
import com.instructure.pandautils.discussions.DiscussionCaching
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.AttachmentView
import com.instructure.student.R
import com.instructure.student.util.Const
import kotlinx.android.synthetic.main.fragment_discussions_update.*
import kotlinx.coroutines.Job

@ScreenView(SCREEN_VIEW_DISCUSSIONS_UPDATE)
class DiscussionsUpdateFragment : ParentFragment() {

    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    // Weave
    private var updateDiscussionJob: Job? = null

    // Bundle Args
    private var discussionTopicHeaderId: Long by LongArg(default = 0L, key = DISCUSSION_TOPIC_HEADER_ID) // The topic the discussion belongs to
    private var discussionEntry: DiscussionEntry by ParcelableArg(default = DiscussionEntry(), key = DISCUSSION_ENTRY)
    private var attachmentRemoved: Boolean by BooleanArg(default = false)

    //region Fragment Lifecycle Overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            layoutInflater.inflate(R.layout.fragment_discussions_update, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rceTextEditor.setHint(R.string.rce_empty_description)
        rceTextEditor.setHtml(discussionEntry.message, "", "", ThemePrefs.brandColor, ThemePrefs.textButtonColor)

        discussionEntry.attachments?.firstOrNull()?.let {
            val attachmentView = AttachmentView(requireContext())
            attachmentView.setPendingRemoteFile(it, true) { action, attachment ->
                if (action == AttachmentView.AttachmentAction.REMOVE) {
                    attachmentRemoved = true
                    discussionEntry.attachments!!.remove(attachment)
                }
            }
            attachmentLayout.addView(attachmentView)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        updateDiscussionJob?.cancel()
    }
    //endregion

    //region Fragment Interaction Overrides
    override fun title(): String = getString(R.string.edit)

    override fun applyTheme() {
        toolbar.title = getString(R.string.edit)
        toolbar.setupAsCloseButton {
            if (discussionEntry.message == rceTextEditor?.html) {
                activity?.onBackPressed()
            } else {
                UnsavedChangesExitDialog.show(requireFragmentManager()) {
                    activity?.onBackPressed()
                }
            }
        }
        toolbar.setMenu(R.menu.menu_discussion_update, menuItemCallback)
        ViewStyler.themeToolbarLight(requireActivity(), toolbar)
        ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)
    }
    //endregion

    //region Fragment Functionality
    private fun editMessage(message: String?) {
        if (updateDiscussionJob?.isActive == true) return

        updateDiscussionJob = tryWeave {
            if (attachmentRemoved) discussionEntry.attachments = null
            val response = awaitApiResponse<DiscussionEntry> {
                DiscussionManager.updateDiscussionEntry(canvasContext, discussionTopicHeaderId, discussionEntry.id,
                        DiscussionEntryPostBody(message, discussionEntry.attachments), it)
            }

            if (response.code() in 200..299) {
                //post successful
                response.body()?.let {
                    DiscussionCaching(discussionTopicHeaderId).saveEntry(it)// Save to cache
                    DiscussionEntryEvent(it.id).postSticky()// Notify about the updated reply
                    toast(R.string.utils_discussionUpdateSuccess)
                    activity?.onBackPressed()
                }
            } else {
                //post failure
                toast(R.string.utils_discussionSentFailure)
            }

        } catch {
            //Message update failure
            toast(R.string.utils_discussionSentFailure)
        }
    }
    //endregion

    private val menuItemCallback: (MenuItem) -> Unit = { item ->
        when (item.itemId) {
            R.id.menu_save -> {
                if (APIHelper.hasNetworkConnection()) {
                    editMessage(rceTextEditor.html)
                } else {
                    Toast.makeText(requireContext(), R.string.noInternetConnectionMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        private const val DISCUSSION_TOPIC_HEADER_ID = "DISCUSSION_TOPIC_HEADER_ID"
        private const val DISCUSSION_ENTRY = "DISCUSSION_ENTRY"

        fun makeRoute(
                canvasContext: CanvasContext?,
                discussionTopicHeaderId: Long,
                discussionEntry: DiscussionEntry?): Route {
            val bundle = Bundle().apply {
                putLong(DISCUSSION_TOPIC_HEADER_ID, discussionTopicHeaderId)
                putParcelable(DISCUSSION_ENTRY, discussionEntry)
            }

            return Route(DiscussionsUpdateFragment::class.java, canvasContext, bundle)
        }

        fun newInstance(route: Route) = if (validRoute(route)) {
            DiscussionsUpdateFragment().apply {
                arguments = route.canvasContext!!.makeBundle(route.arguments)
            }
        } else null

        private fun validRoute(route: Route) = route.canvasContext != null &&
                route.arguments.containsKey(DISCUSSION_TOPIC_HEADER_ID)
    }
}
