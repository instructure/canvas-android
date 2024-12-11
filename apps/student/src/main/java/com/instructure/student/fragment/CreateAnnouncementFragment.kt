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
import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.postmodels.DiscussionTopicPostBody
import com.instructure.canvasapi2.utils.NetworkUtils
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_CREATE_ANNOUNCEMENT
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.dialogs.UnsavedChangesExitDialog
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.databinding.FragmentCreateAnnouncementBinding
import com.instructure.student.events.DiscussionCreatedEvent
import com.instructure.student.events.DiscussionUpdatedEvent
import com.instructure.student.events.post
import kotlinx.coroutines.Job

@PageView("courses/{canvasContext}/discussion_topics/new?is_announcement=true")
@ScreenView(SCREEN_VIEW_CREATE_ANNOUNCEMENT)
class CreateAnnouncementFragment : ParentFragment() {

    private val binding by viewBinding(FragmentCreateAnnouncementBinding::bind)

    /* The announcement to be edited. This will be null if we're creating a new announcement */
    private var editAnnouncement by NullableParcelableArg<DiscussionTopicHeader>(key = DISCUSSION_TOPIC_HEADER)

    @get:PageViewUrlParam("canvasContext")
    var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    /* Menu buttons. We don't cache these because the toolbar is reconstructed on configuration change. */
    private val mSaveMenuButton get() = binding.createAnnouncementToolbar.menu.findItem(R.id.menuSaveAnnouncement)
    private val mSaveButtonTextView: TextView? get() = view?.findViewById(R.id.menuSaveAnnouncement)

    private var apiJob: Job? = null
    private var rceImageUploadJob: Job? = null

    private var isEditing = editAnnouncement != null
    private var skipUnsavedCheck = false

    /**
     * The announcement that is being edited/created. Changes should be applied directly to this
     * object. For editing mode this object should be passed to the constructor as a deep copy of
     * the original so that canceled changes are not erroneously propagated back to other pages. In
     * creation mode this object will be generated with the values necessary to distinguish it as
     * an announcement instead of a normal discussion topic header.
     */
    val announcement: DiscussionTopicHeader = editAnnouncement ?: DiscussionTopicHeader(
            announcement = true,
            published = true,
            locked = true,
            discussionType = DiscussionTopicHeader.DiscussionType.SIDE_COMMENT.apiString
    )

    //region Fragment Lifecycle Overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return layoutInflater.inflate(R.layout.fragment_create_announcement, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
                rceImageUploadJob = MediaUploadUtils.uploadRceImageJob(imageUri, canvasContext, requireActivity()) { imageUrl -> binding.announcementRCEView.insertImage(requireActivity(), imageUrl) }
            }
        }
    }
    //endregion

    //region Fragment Interaction Overrides
    override fun title() = ""

    override fun applyTheme() {
        setupToolbar()
    }
    //endregion

    //region Parent Fragment Overrides
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        announcement.message = binding.announcementRCEView.html
    }

    //endregion

    //region Setup
    private fun setupToolbar() = with(binding) {
        createAnnouncementToolbar.setupAsCloseButton {
            if (announcement.message == announcementRCEView.html) {
                skipUnsavedCheck = true
                activity?.onBackPressed()
            } else {
                UnsavedChangesExitDialog.show(requireFragmentManager()) {
                    skipUnsavedCheck = true
                    activity?.onBackPressed()
                }
            }
        }
        createAnnouncementToolbar.title = getString(if (isEditing) R.string.utils_editAnnouncementTitle else R.string.utils_createAnnouncementTitle)
        createAnnouncementToolbar.setMenu(R.menu.create_announcement) { menuItem ->
            when (menuItem.itemId) {
                R.id.menuSaveAnnouncement -> if (NetworkUtils.isNetworkAvailable) {
                    saveAnnouncement()
                }
            }
        }
        ViewStyler.themeToolbarLight(requireActivity(), createAnnouncementToolbar)
        ViewStyler.setToolbarElevationSmall(requireContext(), createAnnouncementToolbar)
        if (isEditing) with(mSaveMenuButton) {
            setIcon(0)
            setTitle(R.string.save)
        }
        mSaveButtonTextView?.setTextColor(ThemePrefs.textButtonColor)
    }

    private fun setupViews() {
        setupTitle()
        setupDescription()
        setupAllowCommentsSwitch()
        setupUsersMustPostSwitch()

        // Attempting to upload an image before description gets focus does nothing - hide the RCE toolbar initially -
        binding.announcementRCEView.apply {
            hideEditorToolbar()
            actionUploadImageCallback = { MediaUploadUtils.showPickImageDialog(this@CreateAnnouncementFragment) }
        }
    }

    private fun setupTitle() = with(binding) {
        ViewStyler.themeEditText(requireContext(), announcementNameEditText, ThemePrefs.brandColor)
        announcementNameTextInput.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL))
        announcementNameEditText.setText(announcement.title)
        announcementNameEditText.onTextChanged { announcement.title = it }
        announcementNameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) announcementRCEView.hideEditorToolbar()
        }
    }

    private fun setupDescription() = with(binding) {
        announcementRCEView.setHtml(
                announcement.message,
                getString(R.string.utils_announcementDetails),
                getString(R.string.rce_empty_description),
                ThemePrefs.brandColor, ThemePrefs.textButtonColor
        )
        // when the RCE editor has focus we want the label to be darker so it matches the title's functionality
        announcementRCEView.setLabel(announcementDescLabel, R.color.textDarkest, R.color.textDark)
    }

    private fun enableUsersMustPostSwitch(enabled: Boolean) = with(binding) {
        if (enabled) {
            usersMustPostWrapper.alpha = 1f
            usersMustPostSwitch.isEnabled = true
        } else {
            usersMustPostWrapper.alpha = 0.35f
            usersMustPostSwitch.isEnabled = false
            usersMustPostSwitch.isChecked = false
        }
    }

    private fun setupAllowCommentsSwitch() = with(binding) {
        allowCommentsSwitch.applyTheme()
        allowCommentsSwitch.isChecked = !announcement.locked
        allowCommentsSwitch.setOnCheckedChangeListener { _, isChecked ->
            announcement.locked = !isChecked
            enableUsersMustPostSwitch(isChecked)
        }
    }

    private fun setupUsersMustPostSwitch() {
        with (binding.usersMustPostSwitch) {
            applyTheme()
            isChecked = announcement.requireInitialPost
            enableUsersMustPostSwitch(!announcement.locked)
            setOnCheckedChangeListener { _, isChecked ->
                announcement.requireInitialPost = isChecked
            }
        }
    }
    //endregion

    //region Save Related
    private fun saveAnnouncement() = with(binding) {
        val description = announcementRCEView.html
        if (description.isBlank()) {
            toast(R.string.utils_createAnnouncementNoDescription)
            return
        }

        if (announcementNameEditText.text.isNullOrBlank()) {
            val noTitleString = getString(R.string.utils_noTitle)
            announcementNameEditText.setText(noTitleString)
            announcement.title = noTitleString
        }

        announcement.message = description
        saveAnnouncementAPI()
    }

    private fun onSaveStarted() = with(binding) {
        mSaveMenuButton.isVisible = false
        savingProgressBar.announceForAccessibility(getString(R.string.utils_saving))
        savingProgressBar.setVisible()
    }

    private fun onSaveError() {
        mSaveMenuButton.isVisible = true
        binding.savingProgressBar.setGone()
        toast(R.string.utils_errorSavingAnnouncement)
    }

    private fun onSaveSuccess() {
        if (isEditing) {
            toast(R.string.utils_announcementSuccessfullyUpdated)
        } else {
            toast(R.string.utils_announcementSuccessfullyCreated)
        }
        binding.announcementNameEditText.hideKeyboard() // close the keyboard
        requireActivity().onBackPressed() // close this fragment
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun saveAnnouncementAPI() {
        onSaveStarted()
        apiJob = tryWeave {
            if (isEditing) {
                val postBody = DiscussionTopicPostBody.fromAnnouncement(announcement, false)
                val updatedAnnouncement = awaitApi<DiscussionTopicHeader> { callback ->
                    DiscussionManager.editDiscussionTopic(canvasContext, announcement.id, postBody, callback)
                }
                DiscussionUpdatedEvent(updatedAnnouncement).post()
            } else {
                awaitApi<DiscussionTopicHeader> {
                    DiscussionManager.createStudentDiscussion(canvasContext, announcement, null, it)
                }
                DiscussionCreatedEvent(true).post()
            }
            onSaveSuccess()
        } catch {
            onSaveError()
        }
    }

    override fun handleBackPressed(): Boolean {
        return if (!skipUnsavedCheck && announcement.message != binding.announcementRCEView?.html) {
            UnsavedChangesExitDialog.show(requireFragmentManager()) {
                skipUnsavedCheck = true
                activity?.onBackPressed()
            }
            true
        } else false
    }
    //endregion

    companion object {
        private val DISCUSSION_TOPIC_HEADER = "discussion_topic_header"

        fun newInstance(route: Route) = if (validRoute(route)) {
                    CreateAnnouncementFragment().apply {
                        arguments = route.arguments
                    }
                } else null

        fun makeRoute(canvasContext: CanvasContext, discussionTopicHeader: DiscussionTopicHeader?): Route =
                Route(CreateAnnouncementFragment::class.java, canvasContext, canvasContext.makeBundle().apply {
                    putParcelable(DISCUSSION_TOPIC_HEADER, discussionTopicHeader)
                })

        fun validRoute(route: Route) = route.canvasContext != null
    }
}
