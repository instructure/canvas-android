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
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.parcelCopy
import com.instructure.interactions.Identity
import com.instructure.pandautils.analytics.SCREEN_VIEW_CREATE_OR_EDIT_ANNOUNCEMENT
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.dialogs.DatePickerDialogFragment
import com.instructure.pandautils.dialogs.TimePickerDialogFragment
import com.instructure.pandautils.dialogs.UnsavedChangesExitDialog
import com.instructure.pandautils.dialogs.UploadFilesDialog
import com.instructure.pandautils.discussions.DiscussionUtils
import com.instructure.pandautils.fragments.BasePresenterFragment
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.AttachmentView
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.R
import com.instructure.teacher.dialog.SectionPickerDialog
import com.instructure.teacher.events.SectionsUpdatedEvent
import com.instructure.teacher.factory.CreateOrEditAnnouncementPresenterFactory
import com.instructure.teacher.presenters.CreateOrEditAnnouncementPresenter
import com.instructure.teacher.utils.setupCloseButton
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.utils.withRequireNetwork
import com.instructure.teacher.viewinterface.CreateOrEditAnnouncementView
import kotlinx.android.synthetic.main.fragment_create_or_edit_announcement.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.collections.ArrayList

@ScreenView(SCREEN_VIEW_CREATE_OR_EDIT_ANNOUNCEMENT)
class CreateOrEditAnnouncementFragment :
        BasePresenterFragment<CreateOrEditAnnouncementPresenter, CreateOrEditAnnouncementView>(),
        CreateOrEditAnnouncementView,
        Identity {

    /* The course this announcement belongs to */
    private var mCanvasContext by ParcelableArg<CanvasContext>(Course())

    /* The announcement to be edited. This will be null if we're creating a new announcement */
    private var mEditAnnouncement by NullableParcelableArg<DiscussionTopicHeader>()

    /* Menu buttons. We don't cache these because the toolbar is reconstructed on configuration change. */
    private val mSaveMenuButton get() = toolbar.menu.findItem(R.id.menuSaveAnnouncement)
    private val mAttachmentButton get() = toolbar.menu.findItem(R.id.menuAddAttachment)
    private val mSaveButtonTextView: TextView? get() = view?.findViewById(R.id.menuSaveAnnouncement)

    /* Formats for displaying the delayed post date */
    private val mDateFormat by lazy { DateHelper.fullMonthNoLeadingZeroDateFormat }
    private val mTimeFormat by lazy { DateHelper.getPreferredTimeFormat(requireContext()) }

    private var placeHolderList: ArrayList<Placeholder> = ArrayList()

    /* The default date to show when the user enables delayed posting (the current date just before midnight) */
    private val mDefaultDate: Date
        get() = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

    override val identity = 0L
    override val skipCheck = false
    override fun onRefreshFinished() {}
    override fun onRefreshStarted() {}
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { }
    override fun onPresenterPrepared(presenter: CreateOrEditAnnouncementPresenter) {}
    override fun layoutResId(): Int = R.layout.fragment_create_or_edit_announcement

    override fun getPresenterFactory() = CreateOrEditAnnouncementPresenterFactory(mCanvasContext, mEditAnnouncement?.parcelCopy())

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
    }

    override fun onReadySetGo(presenter: CreateOrEditAnnouncementPresenter) {
        setupToolbar()
        setupViews()
        presenter.loadData(true)
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

    @Suppress("UNUSED_PARAMETER", "unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun sectionsUpdate(sectionsUpdateEvent: SectionsUpdatedEvent) {
        setSectionText()
    }

    fun setupToolbar() {
        toolbar.setupCloseButton {
            if(presenter.announcement.message == announcementRCEView?.html) {
                activity?.onBackPressed()
            } else {
                UnsavedChangesExitDialog.show(requireFragmentManager()) {
                    activity?.onBackPressed()
                }
            }
        }

        toolbar.title = getString(if (presenter.isEditing) R.string.editAnnouncementTitle else R.string.create_announcement_title)
        toolbar.setupMenu(R.menu.create_announcement) { menuItem ->
            when (menuItem.itemId) {
                R.id.menuSaveAnnouncement -> withRequireNetwork { saveAnnouncement() }
                R.id.menuAddAttachment -> if (!presenter.isEditing) addAttachment()
            }
        }

        ViewStyler.themeToolbarBottomSheet(requireActivity(), isTablet, toolbar, Color.BLACK, false)
        ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)

        if (presenter.isEditing) with(mSaveMenuButton) {
            setIcon(0)
            setTitle(R.string.save)
        }

        mSaveButtonTextView?.setTextColor(ThemePrefs.buttonColor)
    }

    private fun setupViews() {
        setupTitle()
        setupDescription()
        setupDeleteButton()
        setupDelaySwitch()
        setupAllowCommentsSwitch()
        setupUsersMustPostSwitch()
        updateAttachmentUI()
        setSectionText()

        announcementRCEView.hideEditorToolbar()
        announcementRCEView.actionUploadImageCallback = { MediaUploadUtils.showPickImageDialog(this) }
    }

    private fun setupTitle() {
        ViewStyler.themeEditText(requireContext(), announcementNameEditText, ThemePrefs.brandColor)
        announcementNameTextInput.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        announcementNameEditText.setText(presenter.announcement.title)
        announcementNameEditText.onTextChanged { presenter.announcement.title = it }
        announcementNameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) announcementRCEView.hideEditorToolbar()
        }
    }

    private fun setupDescription() {
        if (CanvasWebView.containsLTI(presenter.announcement.message.orEmpty(), "UTF-8")) {
            announcementRCEView.setHtml(
                    DiscussionUtils.createLTIPlaceHolders(requireContext(), presenter.announcement.message.orEmpty()) { _, placeholder ->
                        placeHolderList.add(placeholder)
                    },
                    getString(R.string.announcementDetails),
                    getString(R.string.rce_empty_description),
                    ThemePrefs.brandColor, ThemePrefs.buttonColor
            )
        } else {
            announcementRCEView.setHtml(
                    presenter.announcement.message,
                    getString(R.string.announcementDetails),
                    getString(R.string.rce_empty_description),
                    ThemePrefs.brandColor, ThemePrefs.buttonColor
            )
        }
        // When the RCE editor has focus we want the label to be darker so it matches the title's functionality
        announcementRCEView.setLabel(announcementDescLabel, R.color.defaultTextDark, R.color.defaultTextGray)
    }

    private fun setupDeleteButton() {
        // Only show delete button in editing mode
        deleteAnnouncementButton
                .setVisible(presenter.isEditing)
                .onClickWithRequireNetwork {
                    AlertDialog.Builder(requireContext())
                            .setTitle(R.string.deleteAnnouncementDialogTitle)
                            .setMessage(R.string.deleteAnnouncementDialogMessage)
                            .setPositiveButton(R.string.delete) { _, _ ->
                                presenter.deleteAnnouncement()
                            }
                            .setNegativeButton(R.string.cancel, null)
                            .showThemed()
                }
    }

    private fun setupDelaySwitch() {
        delaySwitch.applyTheme()
        delaySwitch.isChecked = presenter.announcement.delayedPostDate != null
        updatePostDate()

        delaySwitch.setOnCheckedChangeListener { _, isChecked ->
            presenter.announcement.delayedPostDate = if (isChecked) mDefaultDate else null
            updatePostDate()
        }

        postDate.onClick {
            DatePickerDialogFragment.getInstance(requireActivity().supportFragmentManager) { year, month, dayOfMonth ->
                val date = Calendar.getInstance().apply {
                    time = presenter.announcement.delayedPostDate
                    set(year, month, dayOfMonth)
                }.time
                presenter.announcement.delayedPostDate = date
                updatePostDate()
            }.show(requireActivity().supportFragmentManager, DatePickerDialogFragment::class.java.simpleName)
        }

        postTime.onClick {
            TimePickerDialogFragment.getInstance(requireActivity().supportFragmentManager) { hour, min ->
                val date = Calendar.getInstance().apply {
                    time = presenter.announcement.delayedPostDate
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, min)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                presenter.announcement.delayedPostDate = date
                updatePostDate()
            }.show(requireActivity().supportFragmentManager, TimePickerDialogFragment::class.java.simpleName)
        }
    }

    private fun updateAttachmentUI() {
        updateAttachmentButton()
        attachmentLayout.clearAttachmentViews()

        // Show attachment waiting to upload (if any)
        presenter.attachment?.let { attachment ->
            val attachmentView = AttachmentView(requireContext())
            attachmentView.setPendingAttachment(attachment.toAttachment(), true) { action, _ ->
                    if (action == AttachmentView.AttachmentAction.REMOVE) {
                        presenter.attachment = null
                        updateAttachmentButton()
                    }
                }

            attachmentLayout.addView(attachmentView)
        }

        // Show existing attachment (if any)
        presenter.announcement.attachments.firstOrNull()?.let {
            val attachmentView = AttachmentView(requireContext())
            attachmentView.setPendingRemoteFile(it, true) { action, attachment ->
                if (action == AttachmentView.AttachmentAction.REMOVE) {
                    presenter.attachmentRemoved = true
                    presenter.announcement.attachments.remove(attachment)
                }
            }

            attachmentLayout.addView(attachmentView)
        }
    }

    private fun updatePostDate() {
        val date = presenter.announcement.delayedPostDate
        if (date == null) {
            postDateWrapper.setGone()
        } else {
            postDateWrapper.setVisible()
            postDate.setText(mDateFormat.format(date))
            postTime.setText(mTimeFormat.format(date))
        }
    }

    private fun enableUsersMustPostSwitch(enabled: Boolean) {
        if (enabled) {
            usersMustPostWrapper.alpha = 1f
            usersMustPostSwitch.isEnabled = true
        } else {
            usersMustPostWrapper.alpha = 0.35f
            usersMustPostSwitch.isEnabled = false
            usersMustPostSwitch.isChecked = false
        }
    }

    private fun setupAllowCommentsSwitch() {
        allowCommentsSwitch.applyTheme()
        allowCommentsSwitch.isChecked = !presenter.announcement.locked
        allowCommentsSwitch.setOnCheckedChangeListener { _, isChecked ->
            presenter.announcement.locked = !isChecked
            enableUsersMustPostSwitch(isChecked)
        }
    }

    private fun setupUsersMustPostSwitch() {
        with (usersMustPostSwitch) {
            applyTheme()
            isChecked = presenter.announcement.requireInitialPost
            enableUsersMustPostSwitch(!presenter.announcement.locked)
            setOnCheckedChangeListener { _, isChecked ->
                presenter.announcement.requireInitialPost = isChecked
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.announcement.message = announcementRCEView.html
    }

    private fun saveAnnouncement() {
        val description = announcementRCEView.html
        if (description.isBlank()) {
            toast(R.string.create_announcement_no_description)
            return
        }

        if (announcementNameEditText.text.isNullOrBlank()) {
            val noTitleString = getString(R.string.no_title)
            announcementNameEditText.setText(noTitleString)
            presenter.announcement.title = noTitleString
        }

        presenter.announcement.message = handleLTIPlaceHolders(placeHolderList, description)
        presenter.saveAnnouncement()
    }

    override fun onSaveStarted() {
        mSaveMenuButton.isVisible = false
        updateAttachmentButton(show = false)
        savingProgressBar.announceForAccessibility(getString(R.string.saving))
        savingProgressBar.setVisible()
    }

    private fun updateAttachmentButton(show: Boolean = true) {
        // Only show if (1) we're in creation mode and (2) we don't already have an attachment
        mAttachmentButton?.isVisible = show && !presenter.isEditing && presenter.attachment == null
    }

    override fun onSaveError() {
        mSaveMenuButton.isVisible = true
        updateAttachmentButton()
        savingProgressBar.setGone()
        toast(R.string.errorSavingAnnouncement)
    }

    override fun onDeleteError() {
        mSaveMenuButton.isVisible = true
        updateAttachmentButton()
        savingProgressBar.setGone()
        toast(R.string.errorDeletingAnnouncement)
    }

    override fun onSaveSuccess() {
        if (presenter.isEditing) {
            toast(R.string.announcementSuccessfullyUpdated)
        } else {
            toast(R.string.announcementSuccessfullyCreated)
        }

        announcementNameEditText.hideKeyboard() // Close the keyboard
        requireActivity().onBackPressed() // Close this fragment
    }

    override fun onDeleteSuccess() {
        toast(R.string.announcementDeleted)
        announcementNameEditText.hideKeyboard() // Close the keyboard
        requireActivity().onBackPressed() // Close this fragment
    }

    private fun addAttachment() {
        val bundle = UploadFilesDialog.createDiscussionsBundle(ArrayList())
        UploadFilesDialog.show(fragmentManager, bundle) { event, attachment ->
            if(event == UploadFilesDialog.EVENT_ON_FILE_SELECTED) {
                presenter.attachment = attachment
                updateAttachmentUI()
            }
        }
    }

    override fun onSectionsLoaded() {
        setSectionText()
        sections?.onClick { _ ->
            SectionPickerDialog.show(requireFragmentManager(), presenter.courseSections, presenter.getSelectedSections()) {
                presenter.announcement.specificSections = if (it.isNotEmpty()) it else "all"
                EventBus.getDefault().post(SectionsUpdatedEvent())
            }
        }
    }

    private fun setSectionText() {
        sections.setText(
                if (presenter.getSelectedSections().isNotEmpty())
                    presenter.getSelectedSections().joinToString(", ") { it.name }
                else getString(R.string.allSections))
    }

    override fun insertImageIntoRCE(text: String, alt: String) = announcementRCEView.insertImage(text, alt)

    companion object {
        fun newInstance(bundle: Bundle) =
                CreateOrEditAnnouncementFragment().apply {
                    arguments = bundle
                }

        fun newInstanceCreate(canvasContext: CanvasContext) =
                CreateOrEditAnnouncementFragment().apply {
                    mCanvasContext = canvasContext
                }

        fun newInstanceEdit(canvasContext: CanvasContext, editAnnouncement: DiscussionTopicHeader) =
                CreateOrEditAnnouncementFragment().apply {
                    mCanvasContext = canvasContext
                    mEditAnnouncement = editAnnouncement
                }
    }
}
