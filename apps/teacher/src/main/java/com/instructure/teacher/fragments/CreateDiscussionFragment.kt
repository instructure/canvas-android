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
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputLayout
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.postmodels.AssignmentPostBody
import com.instructure.canvasapi2.models.postmodels.DiscussionTopicPostBody
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.interactions.Identity
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_CREATE_DISCUSSION
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
import com.instructure.teacher.dialog.ConfirmRemoveAssignmentOverrideDialog
import com.instructure.teacher.events.AssigneesUpdatedEvent
import com.instructure.teacher.events.DiscussionCreatedEvent
import com.instructure.teacher.events.DiscussionUpdatedEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.factory.CreateDiscussionPresenterFactory
import com.instructure.teacher.models.DueDateGroup
import com.instructure.teacher.presenters.CreateDiscussionPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.*
import com.instructure.teacher.view.AssignmentOverrideView
import com.instructure.teacher.viewinterface.CreateDiscussionView
import kotlinx.android.synthetic.main.fragment_create_discussion.*
import kotlinx.android.synthetic.main.view_assignment_override.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.any
import kotlin.collections.arrayListOf
import kotlin.collections.firstOrNull
import kotlin.collections.forEach
import kotlin.collections.forEachIndexed
import kotlin.collections.hashMapOf
import kotlin.collections.isNotEmpty
import kotlin.collections.last
import kotlin.collections.plusAssign
import kotlin.collections.toList

@ScreenView(SCREEN_VIEW_CREATE_DISCUSSION)
class CreateDiscussionFragment : BasePresenterFragment<
        CreateDiscussionPresenter,
        CreateDiscussionView>(), CreateDiscussionView, Identity {

    private var mCanvasContext: CanvasContext by ParcelableArg(Course(), CANVAS_CONTEXT)
    private var mDiscussionTopicHeader: DiscussionTopicHeader? by NullableParcelableArg(null, DISCUSSION_TOPIC_HEADER)
    private val sendButton: TextView? get() = view?.findViewById(R.id.menuSaveDiscussion)
    private val saveButton: TextView? get() = view?.findViewById(R.id.menuSave)
    private val mAttachmentButton get() = toolbar.menu.findItem(R.id.menuAddAttachment)
    private var mIsPublished: Boolean by BooleanArg(false)
    private var mIsSubscribed: Boolean by BooleanArg(true)
    private var mAllowThreaded: Boolean by BooleanArg(false)
    private var mUsersMustPost: Boolean by BooleanArg(false)
    private var mHasLoadedDataForEdit by BooleanArg()
    private var mDisplayGradeAs: String? by NullableStringArg()
    private var mDescription by NullableStringArg()

    private var mScrollToDates: Boolean by BooleanArg(false, SHOULD_SCROLL_TO_DATES)
    private var mRCEHasFocus = false

    private var placeHolderList: ArrayList<Placeholder> = ArrayList()

    //region Graded Discussion variables

    // We maintain a copy of the groupedDueDates to manipulate and use to display
    // overrides. When pushing changes, we update the original assignment object
    // with the changes in the copy.
    private var mEditDateGroups: EditDateGroups = arrayListOf()
    private val groupsMapped = hashMapOf<Long, Group>()
    private val sectionsMapped = hashMapOf<Long, Section>()
    private val studentsMapped = hashMapOf<Long, User>()

    // Keeps track of which override we were editing so we can scroll back to it when the user returns from editing assignees
    private var scrollBackToOverride: AssignmentOverrideView? = null

    private var mScrollHandler: Handler = Handler()

    private var mScrollToRunnable: Runnable = Runnable {
        if(isAdded) scrollView.fullScroll(ScrollView.FOCUS_DOWN)
    }

    //endregion

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

    private val removeOverrideClick: (DueDateGroup) -> Unit = { callback ->
        // Show confirmation dialog
        ConfirmRemoveAssignmentOverrideDialog.show(requireActivity().supportFragmentManager) {
            if (mEditDateGroups.contains(callback)) mEditDateGroups.remove(callback)
            setupOverrides()
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mScrollHandler.removeCallbacks(mScrollToRunnable)
    }

    override fun onRefreshFinished() { }

    override fun onRefreshStarted() { }

    override val identity: Long? get() = 0
    override val skipCheck: Boolean get() = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            @Suppress("UNCHECKED_CAST")
            mEditDateGroups = (savedInstanceState.getSerializable(EDIT_DATE_GROUPS) as ArrayList<DueDateGroup>)
            mRCEHasFocus = savedInstanceState.getBoolean(RCE_HAS_FOCUS)
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

    override fun insertImageIntoRCE(text: String, alt: String) = descriptionRCEView.insertImage(text, alt)

    override fun onReadySetGo(presenter: CreateDiscussionPresenter) {
        // If we already have something in the edit date groups we already have the full assignment and don't need to get it again.
        mDiscussionTopicHeader?.assignment?.let {
            // Get the full assignment with overrides
            if (mEditDateGroups.size == 0) presenter.getFullAssignment(it.id)
        }

        setupToolbar()
        setupViews()

        if(mRCEHasFocus) {
            descriptionRCEView.requestEditorFocus()
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    override fun getPresenterFactory() = CreateDiscussionPresenterFactory(mCanvasContext, mDiscussionTopicHeader?.assignment)

    override fun onPresenterPrepared(presenter: CreateDiscussionPresenter) { }

    override fun layoutResId(): Int = R.layout.fragment_create_discussion

    override fun updateDueDateGroups(groups: HashMap<Long, Group>, sections: HashMap<Long, Section>, students: HashMap<Long, User>) {
        groupsMapped += groups
        sectionsMapped += sections
        studentsMapped += students

        setupOverrides()

        if (mScrollToDates) {
            mScrollToDates = false
            // We came from the Dates page, scroll to the dates for editing
            mScrollHandler.postDelayed(mScrollToRunnable, 300)
        }

        scrollBackToOverride?.let {
            if (!mScrollToDates)
                scrollView.post {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                }
            scrollBackToOverride = null
        }
    }

    override fun errorOccurred() {
        toast(R.string.error_occurred)
    }

    override fun updatedAssignment() {
        mEditDateGroups.clear()
        setupViews()
    }

    fun setupToolbar() {
        toolbar.setupCloseButton {
            if(mDiscussionTopicHeader == null) {
                activity?.onBackPressed()
            } else {
                if (mDiscussionTopicHeader?.message == descriptionRCEView?.html) {
                    activity?.onBackPressed()
                } else {
                    UnsavedChangesExitDialog.show(requireFragmentManager()) {
                        activity?.onBackPressed()
                    }
                }
            }
        }

        toolbar.title = if(mDiscussionTopicHeader == null) getString(R.string.createDiscussion) else getString(R.string.editDiscussion)
        toolbar.setupMenu(if (mDiscussionTopicHeader == null) R.menu.create_discussion else R.menu.menu_save_generic) { menuItem ->
            when (menuItem.itemId) {
                R.id.menuSaveDiscussion, R.id.menuSave -> withRequireNetwork { saveDiscussion() }
                R.id.menuAddAttachment -> if (mDiscussionTopicHeader == null) addAttachment()
            }
        }

        ViewStyler.themeToolbarBottomSheet(requireActivity(), isTablet, toolbar, Color.BLACK, false)
        ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)

        sendButton?.setTextColor(ThemePrefs.buttonColor)
        saveButton?.setTextColor(ThemePrefs.buttonColor)
    }

    fun setupViews() {
        (view as? ViewGroup)?.descendants<TextInputLayout>()?.forEach {
            it.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }

        if(CanvasWebView.containsLTI(mDescription ?: mDiscussionTopicHeader?.message ?: "", "UTF-8")) {
            descriptionRCEView.setHtml(DiscussionUtils.createLTIPlaceHolders(requireContext(), mDescription ?: mDiscussionTopicHeader?.message ?: "") { _, placeholder ->
                placeHolderList.add(placeholder)
            },
                    getString(R.string.discussion_details),
                    getString(R.string.rce_empty_description),
                    ThemePrefs.brandColor, ThemePrefs.buttonColor)
        } else {
            descriptionRCEView.setHtml(mDescription ?: mDiscussionTopicHeader?.message,
                    getString(R.string.discussion_details),
                    getString(R.string.rce_empty_description),
                    ThemePrefs.brandColor, ThemePrefs.buttonColor)
        }

        // When the RCE editor has focus we want the label to be darker so it matches the title's functionality
        descriptionRCEView.setLabel(discussionDescLabel, R.color.defaultTextDark, R.color.defaultTextGray)

        if (!mHasLoadedDataForEdit)
            mDiscussionTopicHeader?.let {
                editDiscussionName.setText(it.title)
                mIsPublished = it.published
                mAllowThreaded = it.type == DiscussionTopicHeader.DiscussionType.THREADED
                mUsersMustPost = it.requireInitialPost
                mIsSubscribed = it.subscribed
                mHasLoadedDataForEdit = true
            }

        ViewStyler.themeEditText(requireContext(), editDiscussionName, ThemePrefs.brandColor)
        ViewStyler.themeEditText(requireContext(), editGradePoints, ThemePrefs.brandColor)

        setupPublishSwitch()
        setupSubscribeSwitch()
        setupAllowThreadedSwitch()
        setupUsersMustPostSwitch()
        updateAttachmentUI()

        if(presenter.getAssignment() == null) {
            if(mEditDateGroups.isEmpty()) {
                // If the dateGroups is empty, we want to add a due date so that we can set the available from and to fields
                mEditDateGroups.clear()

                val dueDateGroup = DueDateGroup()

                if(mDiscussionTopicHeader != null) {
                    // Populate the availability dates if we have them, the assignment is null, so this is an ungraded assignment
                    dueDateGroup.coreDates.lockDate = (mDiscussionTopicHeader as DiscussionTopicHeader).lockAt
                    dueDateGroup.coreDates.unlockDate = (mDiscussionTopicHeader as DiscussionTopicHeader).delayedPostDate
                }

                mEditDateGroups.add(dueDateGroup)
            }

            //Make the graded things gone, we can't create a graded discussion
            gradeWrapper.setGone()
            addOverride.setGone()
            subscribeWrapper.setGone()
        } else {
            // Points possible
            val pointsPossible = (presenter.getAssignment() as Assignment).pointsPossible
            editGradePoints.setText(NumberHelper.formatDecimal(pointsPossible, 2, true))

            if(mDisplayGradeAs == null) {
                mDisplayGradeAs = (presenter.getAssignment() as Assignment).gradingType
            }

            setupDisplayGradeAs()

            if (mEditDateGroups.isEmpty()) mEditDateGroups.addAll((presenter.getAssignment() as Assignment).groupedDueDates)

            if (groupsMapped.isEmpty() && sectionsMapped.isEmpty() && studentsMapped.isEmpty()) {
                presenter.getDueDateInfo((presenter.getAssignment() as Assignment).groupCategoryId)
            }

            addOverride.setOnClickListener {
                mEditDateGroups.add(DueDateGroup())
                setupOverrides()
                scrollView.post {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                }
                // This opens the assignees page to save the user a click.
                overrideContainer.descendants<AssignmentOverrideView>().last().assignTo.performClick()
            }
        }
        setupOverrides()
        setupDelete()

        descriptionRCEView.hideEditorToolbar()
        descriptionRCEView.actionUploadImageCallback = { MediaUploadUtils.showPickImageDialog(this) }

        editDiscussionName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) descriptionRCEView.hideEditorToolbar()
        }
    }

    private fun setupPublishSwitch()  {
        // If a student has submitted something, we can't let the teacher unpublish the discussion
        if (presenter.getAssignment()?.unpublishable == true) {
            publishWrapper.setGone()
            mIsPublished = true
            return
        }

        // Publish status
        with(publishSwitch) {
            applyTheme()
            isChecked = mIsPublished
            setOnCheckedChangeListener { _, isChecked -> mIsPublished = isChecked }
        }
    }

    private fun setupSubscribeSwitch()  {
        with(subscribeSwitch) {
            applyTheme()
            isChecked = mIsSubscribed
            setOnCheckedChangeListener { _, isChecked -> mIsSubscribed = isChecked }
        }
    }

    private fun setupAllowThreadedSwitch()  {
        with (threadedSwitch) {
            applyTheme()
            isChecked = mAllowThreaded
            setOnCheckedChangeListener { _, isChecked -> mAllowThreaded = isChecked }
        }
    }

    private fun setupUsersMustPostSwitch()  {
        with(usersMustPostSwitch) {
            applyTheme()
            isChecked = mUsersMustPost
            setOnCheckedChangeListener { _, isChecked -> mUsersMustPost = isChecked }
        }
    }

    private fun setupOverrides() {
        overrideContainer.removeAllViews()

        if(presenter.getAssignment() == null) {
            // Load in overrides
            mEditDateGroups.forEachIndexed { index, dueDateGroup ->
                val assignees = ArrayList<String>()
                val v = AssignmentOverrideView(requireActivity())

                v.toAndFromDatesOnly()
                v.setupOverride(index, dueDateGroup, mEditDateGroups.size > 1, assignees, datePickerOnClick, timePickerOnClick, {
                    if (mEditDateGroups.contains(it)) mEditDateGroups.remove(it)
                    setupOverrides()
                }) { }

                overrideContainer.addView(v)
            }
        } else {
            // Load in overrides
            if(groupsMapped.isNotEmpty() || sectionsMapped.isNotEmpty() || studentsMapped.isNotEmpty()) {
                mEditDateGroups.forEachIndexed { index, dueDateGroup ->
                    val assignees = ArrayList<CharSequence>()
                    val v = AssignmentOverrideView(requireActivity())
                    if (dueDateGroup.isEveryone) {
                        assignees += getString(if (mEditDateGroups.any { it.hasOverrideAssignees }) R.string.everyone_else else R.string.everyone)
                    }

                    dueDateGroup.groupIds.forEach { assignees.add(groupsMapped[it]?.name!!) }
                    dueDateGroup.sectionIds.forEach { assignees.add(sectionsMapped[it]?.name!!) }
                    dueDateGroup.studentIds.forEach {
                        assignees.add(studentsMapped[it]!!.let { user -> Pronouns.span(user.name, user.pronouns) })
                    }

                    v.setupOverride(index, dueDateGroup, mEditDateGroups.size > 1, assignees, datePickerOnClick, timePickerOnClick, removeOverrideClick) {
                        val args = AssigneeListFragment.makeBundle(
                                mEditDateGroups,
                                index,
                                sectionsMapped.values.toList(),
                                groupsMapped.values.toList(),
                                studentsMapped.values.toList())
                        RouteMatcher.route(requireContext(), Route(AssigneeListFragment::class.java, mCanvasContext, args))
                        scrollBackToOverride = v
                    }

                    overrideContainer.addView(v)
                }
            }
        }

        overrideContainer.descendants<TextInputLayout>().forEach {
            it.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
    }

    private fun setupDisplayGradeAs() {
        // Filters spinner
        val spinnerAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.display_grade_as_types_discussion, R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        displayGradeAsSpinner.adapter = spinnerAdapter
        ViewStyler.themeSpinner(requireContext(), displayGradeAsSpinner, ThemePrefs.brandColor)
        displayGradeAsSpinner.onItemSelectedListener = null

        when(mDisplayGradeAs) {
            Assignment.POINTS_TYPE -> displayGradeAsSpinner.setSelection(spinnerAdapter.getPosition(getString(R.string.points)))
            Assignment.GPA_SCALE_TYPE -> displayGradeAsSpinner.setSelection(spinnerAdapter.getPosition(getString(R.string.gpa_scale)))
            Assignment.LETTER_GRADE_TYPE -> displayGradeAsSpinner.setSelection(spinnerAdapter.getPosition(getString(R.string.letter_grade)))
            Assignment.PASS_FAIL_TYPE -> displayGradeAsSpinner.setSelection(spinnerAdapter.getPosition(getString(R.string.complete_incomplete)))
            Assignment.PERCENT_TYPE -> displayGradeAsSpinner.setSelection(spinnerAdapter.getPosition(getString(R.string.percentage)))
        }

        displayGradeAsSpinner.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                view ?: return
                when((view as TextView).text.toString()) {
                    getString(R.string.points) -> mDisplayGradeAs = Assignment.POINTS_TYPE
                    getString(R.string.gpa_scale) -> mDisplayGradeAs = Assignment.GPA_SCALE_TYPE
                    getString(R.string.letter_grade) -> mDisplayGradeAs = Assignment.LETTER_GRADE_TYPE
                    getString(R.string.complete_incomplete) -> mDisplayGradeAs = Assignment.PASS_FAIL_TYPE
                    getString(R.string.percentage) -> mDisplayGradeAs = Assignment.PERCENT_TYPE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        })
    }

    private fun setupDelete() {
        deleteWrapper.setVisible(mDiscussionTopicHeader != null)
        deleteWrapper.onClickWithRequireNetwork {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.discussions_delete_title)
                .setMessage(R.string.discussions_delete_message)
                .setPositiveButton(R.string.delete) { _, _ ->
                    if(mDiscussionTopicHeader != null) {
                        presenter.deleteDiscussionTopicHeader(mDiscussionTopicHeader!!.id)
                    }
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .showThemed()
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
        mDiscussionTopicHeader?.attachments?.firstOrNull()?.let {
            val attachmentView = AttachmentView(requireContext())
            attachmentView.setPendingRemoteFile(it, true) { action, attachment ->
                if (action == AttachmentView.AttachmentAction.REMOVE) {
                    presenter.attachmentRemoved = true
                    mDiscussionTopicHeader?.attachments?.remove(attachment)
                }
            }

            attachmentLayout.addView(attachmentView)
        }
    }

    private fun updateAttachmentButton(show: Boolean = true) {
        // Only show if (1) we're in creation mode and (2) we don't already have an attachment
        mAttachmentButton?.isVisible = show && mDiscussionTopicHeader == null && presenter.attachment == null
    }

    private fun addAttachment() {
        // set the description here. When we ask for permission to use the camera the app can call readySetGo and reset the description
        mDescription = descriptionRCEView.html

        val bundle = UploadFilesDialog.createDiscussionsBundle(ArrayList())
        UploadFilesDialog.show(fragmentManager, bundle) { event, attachment ->
            if(event == UploadFilesDialog.EVENT_ON_FILE_SELECTED) {
                presenter.attachment = attachment
                updateAttachmentUI()
            }
        }
    }

    override fun startSavingDiscussion() {
        sendButton?.setGone()
        savingProgressBar.announceForAccessibility(getString(R.string.saving))
        savingProgressBar.setVisible()
    }

    override fun errorSavingDiscussion() {
        sendButton?.setVisible()
        savingProgressBar.setGone()
    }

    override fun discussionSavedSuccessfully(discussionTopic: DiscussionTopicHeader?) {
        if(discussionTopic == null) {
            DiscussionCreatedEvent(true).post() // Post bus event
            toast(R.string.discussionSuccessfullyCreated) // Let the user know the discussion was saved
        } else {
            discussionTopic.assignment = presenter.getAssignment()
            DiscussionUpdatedEvent(discussionTopic).post()
            toast(R.string.discussionSuccessfullyUpdated)
        }

        editDiscussionName.hideKeyboard() // Close the keyboard
        requireActivity().onBackPressed() // Close this fragment
    }

    private fun saveDiscussion() {
        if(mDiscussionTopicHeader != null) {
            val postData = DiscussionTopicPostBody()

            // Discussion title isn't required
            if(editDiscussionName.text.isNullOrBlank()) {
                postData.title = getString(R.string.no_title)
            } else {
                postData.title = editDiscussionName.text?.toString() ?: getString(R.string.no_title)
            }
            postData.message = handleLTIPlaceHolders(placeHolderList, descriptionRCEView.html)
            postData.published = mIsPublished
            postData.discussionType = if (mAllowThreaded) {
                DiscussionTopicHeader.DiscussionType.THREADED.toString().lowercase(Locale.getDefault())
            } else {
                DiscussionTopicHeader.DiscussionType.SIDE_COMMENT.toString().lowercase(Locale.getDefault())
            }
            postData.requireInitialPost = mUsersMustPost

            if (presenter.getAssignment() == null) {
                postData.delayedPostAt = mEditDateGroups[0].coreDates.unlockDate.toApiString()
                postData.lockAt = mEditDateGroups[0].coreDates.lockDate
            } else {
                val assignmentPostData = AssignmentPostBody()
                assignmentPostData.gradingType = mDisplayGradeAs
                assignmentPostData.setGroupedDueDates(mEditDateGroups)
                assignmentPostData.pointsPossible = editGradePoints.text.toString().toDouble()

                postData.assignment = assignmentPostData
            }

            presenter.editDiscussion((mDiscussionTopicHeader as DiscussionTopicHeader).id, postData)
        } else {
            val discussionTopicHeader = DiscussionTopicHeader()

            if(editDiscussionName.text.isNullOrBlank()) {
                discussionTopicHeader.title = getString(R.string.no_title)
            } else {
                discussionTopicHeader.title = editDiscussionName.text.toString()
            }

            discussionTopicHeader.message = descriptionRCEView.html
            discussionTopicHeader.published = mIsPublished
            discussionTopicHeader.subscribed = mIsSubscribed
            discussionTopicHeader.type = if (mAllowThreaded) DiscussionTopicHeader.DiscussionType.THREADED else DiscussionTopicHeader.DiscussionType.SIDE_COMMENT
            discussionTopicHeader.requireInitialPost = mUsersMustPost

            // If the assignment is null, that means we're creating/editing a discussion. When we do this we initialize mEditDateGroups with an empty DueDateGroup
            if (presenter.getAssignment() == null) {
                discussionTopicHeader.delayedPostDate = mEditDateGroups[0].coreDates.unlockDate
                discussionTopicHeader.lockAt = mEditDateGroups[0].coreDates.lockDate
            }
            presenter.saveDiscussion(discussionTopicHeader)
        }
    }

    override fun discussionDeletedSuccessfully(discussionTopicHeaderId: Long) {
        activity?.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(EDIT_DATE_GROUPS, ArrayList<DueDateGroup>(mEditDateGroups))
        outState.putBoolean(RCE_HAS_FOCUS, descriptionRCEView.hasFocus())
        mDescription = descriptionRCEView.html
        super.onSaveInstanceState(outState)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onAssigneesChanged(event: AssigneesUpdatedEvent) {
        // Update grouped due dates (EditDateGroups)
        event.once(javaClass.simpleName) { dates ->
            mEditDateGroups = dates
            setupOverrides()
            // Remove it so when we go to another assignment or discussion it won't show up there too
            EventBus.getDefault().removeStickyEvent(event)
        }
    }

    companion object {
        private const val EDIT_DATE_GROUPS = "editDateGroups"
        private const val RCE_HAS_FOCUS = "rceHasFocus"

        @JvmStatic private val CANVAS_CONTEXT = "canvas_context"
        @JvmStatic private val DISCUSSION_TOPIC_HEADER = "discussion_topic_header"
        @JvmStatic private val SHOULD_SCROLL_TO_DATES = "shouldScrollToDates"

        fun newInstance(args: Bundle) = CreateDiscussionFragment().withArgs(args)

        fun makeBundle(canvasContext: CanvasContext): Bundle =
                Bundle().apply {
                    putParcelable(CANVAS_CONTEXT, canvasContext)
                }

        fun makeBundle(canvasContext: CanvasContext, discussionTopicHeader: DiscussionTopicHeader): Bundle =
                Bundle().apply {
                    putParcelable(CANVAS_CONTEXT, canvasContext)
                    putParcelable(DISCUSSION_TOPIC_HEADER, discussionTopicHeader)
                }


        fun makeBundle(canvasContext: CanvasContext, discussionTopicHeader: DiscussionTopicHeader, shouldScrollToDates: Boolean): Bundle =
                Bundle().apply {
                    putParcelable(CANVAS_CONTEXT, canvasContext)
                    putParcelable(DISCUSSION_TOPIC_HEADER, discussionTopicHeader)
                    putBoolean(SHOULD_SCROLL_TO_DATES, shouldScrollToDates)
                }
    }
}
