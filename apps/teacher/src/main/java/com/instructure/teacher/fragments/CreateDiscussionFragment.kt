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
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.textfield.TextInputLayout
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.models.postmodels.AssignmentPostBody
import com.instructure.canvasapi2.models.postmodels.DiscussionTopicPostBody
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.interactions.Identity
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_CREATE_DISCUSSION
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.dialogs.DatePickerDialogFragment
import com.instructure.pandautils.dialogs.TimePickerDialogFragment
import com.instructure.pandautils.dialogs.UnsavedChangesExitDialog
import com.instructure.pandautils.discussions.DiscussionUtils
import com.instructure.pandautils.features.file.upload.FileUploadDialogFragment
import com.instructure.pandautils.features.file.upload.FileUploadDialogParent
import com.instructure.pandautils.fragments.BasePresenterFragment
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.MediaUploadUtils
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.NullableStringArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.Placeholder
import com.instructure.pandautils.utils.RequestCodes
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyTheme
import com.instructure.pandautils.utils.descendants
import com.instructure.pandautils.utils.handleLTIPlaceHolders
import com.instructure.pandautils.utils.hideKeyboard
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.showThemed
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.utils.withArgs
import com.instructure.pandautils.views.AttachmentView
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentCreateDiscussionBinding
import com.instructure.teacher.dialog.ConfirmRemoveAssignmentOverrideDialog
import com.instructure.teacher.events.AssigneesUpdatedEvent
import com.instructure.teacher.events.DiscussionCreatedEvent
import com.instructure.teacher.events.DiscussionUpdatedEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.factory.CreateDiscussionPresenterFactory
import com.instructure.teacher.models.DueDateGroup
import com.instructure.teacher.presenters.CreateDiscussionPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.EditDateGroups
import com.instructure.teacher.utils.groupedDueDates
import com.instructure.teacher.utils.setGroupedDueDates
import com.instructure.teacher.utils.setupCloseButton
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.utils.withRequireNetwork
import com.instructure.teacher.view.AssignmentOverrideView
import com.instructure.teacher.viewinterface.CreateDiscussionView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Date
import java.util.Locale

@PageView("courses/{canvasContext}/discussion_topics/new")
@ScreenView(SCREEN_VIEW_CREATE_DISCUSSION)
class CreateDiscussionFragment : BasePresenterFragment<
        CreateDiscussionPresenter,
        CreateDiscussionView,
        FragmentCreateDiscussionBinding>(),
    CreateDiscussionView,
    Identity,
    FileUploadDialogParent {

    @get:PageViewUrlParam("canvasContext")
    var canvasContext: CanvasContext by ParcelableArg(Course(), CANVAS_CONTEXT)
    private var discussionTopicHeader: DiscussionTopicHeader? by NullableParcelableArg(null, DISCUSSION_TOPIC_HEADER)
    private val sendButton: TextView? get() = view?.findViewById(R.id.menuSaveDiscussion)
    private val saveButton: TextView? get() = view?.findViewById(R.id.menuSave)
    private val attachmentButton get() = binding.toolbar.menu.findItem(R.id.menuAddAttachment)
    private var isPublished: Boolean by BooleanArg(false)
    private var isSubscribed: Boolean by BooleanArg(true)
    private var allowThreaded: Boolean by BooleanArg(false)
    private var usersMustPost: Boolean by BooleanArg(false)
    private var hasLoadedDataForEdit by BooleanArg()
    private var displayGradeAs: String? by NullableStringArg()
    private var description by NullableStringArg()

    private var scrollToDates: Boolean by BooleanArg(false, SHOULD_SCROLL_TO_DATES)
    private var rceHasFocus = false

    private var placeHolderList: ArrayList<Placeholder> = ArrayList()

    //region Graded Discussion variables

    // We maintain a copy of the groupedDueDates to manipulate and use to display
    // overrides. When pushing changes, we update the original assignment object
    // with the changes in the copy.
    private var editDateGroups: EditDateGroups = arrayListOf()
    private val groupsMapped = hashMapOf<Long, Group>()
    private val sectionsMapped = hashMapOf<Long, Section>()
    private val studentsMapped = hashMapOf<Long, User>()

    // Keeps track of which override we were editing so we can scroll back to it when the user returns from editing assignees
    private var scrollBackToOverride: AssignmentOverrideView? = null

    private var scrollHandler: Handler = Handler()

    private var scrollToRunnable: Runnable = Runnable {
        if(isAdded) binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN)
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
            if (editDateGroups.contains(callback)) editDateGroups.remove(callback)
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
        scrollHandler.removeCallbacks(scrollToRunnable)
    }

    override fun onRefreshFinished() { }

    override fun onRefreshStarted() { }

    override val identity: Long? get() = 0
    override val skipCheck: Boolean get() = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            @Suppress("UNCHECKED_CAST")
            editDateGroups = (savedInstanceState.getSerializable(EDIT_DATE_GROUPS) as ArrayList<DueDateGroup>)
            rceHasFocus = savedInstanceState.getBoolean(RCE_HAS_FOCUS)
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

    override fun insertImageIntoRCE(imageUrl: String) = binding.descriptionRCEView.insertImage(requireActivity(), imageUrl)

    override fun onReadySetGo(presenter: CreateDiscussionPresenter) {
        // If we already have something in the edit date groups we already have the full assignment and don't need to get it again.
        discussionTopicHeader?.assignment?.let {
            // Get the full assignment with overrides
            if (editDateGroups.size == 0) presenter.getFullAssignment(it.id)
        }

        setupToolbar()
        setupViews()

        if(rceHasFocus) {
            binding.descriptionRCEView.requestEditorFocus()
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    override fun getPresenterFactory() = CreateDiscussionPresenterFactory(canvasContext, discussionTopicHeader?.assignment)

    override fun onPresenterPrepared(presenter: CreateDiscussionPresenter) { }

    override val bindingInflater: (layoutInflater: LayoutInflater) -> FragmentCreateDiscussionBinding = FragmentCreateDiscussionBinding::inflate

    override fun updateDueDateGroups(groups: HashMap<Long, Group>, sections: HashMap<Long, Section>, students: HashMap<Long, User>) {
        groupsMapped += groups
        sectionsMapped += sections
        studentsMapped += students

        setupOverrides()

        if (scrollToDates) {
            scrollToDates = false
            // We came from the Dates page, scroll to the dates for editing
            scrollHandler.postDelayed(scrollToRunnable, 300)
        }

        scrollBackToOverride?.let {
            if (!scrollToDates)
                binding.scrollView.post {
                    binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                }
            scrollBackToOverride = null
        }
    }

    override fun errorOccurred() {
        toast(R.string.error_occurred)
    }

    override fun updatedAssignment() {
        editDateGroups.clear()
        setupViews()
    }

    fun setupToolbar() = with(binding) {
        toolbar.setupCloseButton {
            if(discussionTopicHeader == null) {
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

        toolbar.title = if(discussionTopicHeader == null) getString(R.string.createDiscussion) else getString(R.string.editDiscussion)
        toolbar.setupMenu(if (discussionTopicHeader == null) R.menu.create_discussion else R.menu.menu_save_generic) { menuItem ->
            when (menuItem.itemId) {
                R.id.menuSaveDiscussion, R.id.menuSave -> withRequireNetwork { saveDiscussion() }
                R.id.menuAddAttachment -> if (discussionTopicHeader == null) addAttachment()
            }
        }

        ViewStyler.themeToolbarLight(requireActivity(), toolbar)
        ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)

        sendButton?.setTextColor(ThemePrefs.textButtonColor)
        saveButton?.setTextColor(ThemePrefs.textButtonColor)
    }

    fun setupViews() = with(binding) {
        (view as? ViewGroup)?.descendants<TextInputLayout>()?.forEach {
            it.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }

        if(CanvasWebView.containsLTI(description ?: discussionTopicHeader?.message ?: "", "UTF-8")) {
            descriptionRCEView.setHtml(DiscussionUtils.createLTIPlaceHolders(requireContext(), description ?: discussionTopicHeader?.message ?: "") { _, placeholder ->
                placeHolderList.add(placeholder)
            },
                    getString(R.string.discussion_details),
                    getString(R.string.rce_empty_description),
                    ThemePrefs.brandColor, ThemePrefs.textButtonColor)
        } else {
            descriptionRCEView.setHtml(description ?: discussionTopicHeader?.message,
                    getString(R.string.discussion_details),
                    getString(R.string.rce_empty_description),
                    ThemePrefs.brandColor, ThemePrefs.textButtonColor)
        }

        // When the RCE editor has focus we want the label to be darker so it matches the title's functionality
        descriptionRCEView.setLabel(discussionDescLabel, R.color.textDarkest, R.color.textDark)

        if (!hasLoadedDataForEdit)
            discussionTopicHeader?.let {
                editDiscussionName.setText(it.title)
                isPublished = it.published
                allowThreaded = it.type == DiscussionTopicHeader.DiscussionType.THREADED
                usersMustPost = it.requireInitialPost
                isSubscribed = it.subscribed
                hasLoadedDataForEdit = true
            }

        ViewStyler.themeEditText(requireContext(), editDiscussionName, ThemePrefs.brandColor)
        ViewStyler.themeEditText(requireContext(), editGradePoints, ThemePrefs.brandColor)

        setupPublishSwitch()
        setupSubscribeSwitch()
        setupAllowThreadedSwitch()
        setupUsersMustPostSwitch()
        updateAttachmentUI()

        if(presenter.getAssignment() == null) {
            if(editDateGroups.isEmpty()) {
                // If the dateGroups is empty, we want to add a due date so that we can set the available from and to fields
                editDateGroups.clear()

                val dueDateGroup = DueDateGroup()

                if(discussionTopicHeader != null) {
                    // Populate the availability dates if we have them, the assignment is null, so this is an ungraded assignment
                    dueDateGroup.coreDates.lockDate = (discussionTopicHeader as DiscussionTopicHeader).lockAt
                    dueDateGroup.coreDates.unlockDate = (discussionTopicHeader as DiscussionTopicHeader).delayedPostDate
                }

                editDateGroups.add(dueDateGroup)
            }

            //Make the graded things gone, we can't create a graded discussion
            gradeWrapper.setGone()
            addOverride.setGone()
            subscribeWrapper.setGone()
        } else {
            // Points possible
            val pointsPossible = (presenter.getAssignment() as Assignment).pointsPossible
            editGradePoints.setText(NumberHelper.formatDecimal(pointsPossible, 2, true))

            if(displayGradeAs == null) {
                displayGradeAs = (presenter.getAssignment() as Assignment).gradingType
            }

            setupDisplayGradeAs()

            if (editDateGroups.isEmpty()) editDateGroups.addAll((presenter.getAssignment() as Assignment).groupedDueDates)

            if (groupsMapped.isEmpty() && sectionsMapped.isEmpty() && studentsMapped.isEmpty()) {
                presenter.getDueDateInfo((presenter.getAssignment() as Assignment).groupCategoryId)
            }

            addOverride.setOnClickListener {
                editDateGroups.add(DueDateGroup())
                setupOverrides()
                scrollView.post {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                }
                // This opens the assignees page to save the user a click.
                overrideContainer.descendants<AssignmentOverrideView>().last().findViewById<AppCompatEditText>(R.id.assignTo).performClick()
            }
        }
        setupOverrides()
        setupDelete()

        descriptionRCEView.hideEditorToolbar()
        descriptionRCEView.actionUploadImageCallback = { MediaUploadUtils.showPickImageDialog(this@CreateDiscussionFragment) }

        editDiscussionName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) descriptionRCEView.hideEditorToolbar()
        }
    }

    private fun setupPublishSwitch()  {
        // If a student has submitted something, we can't let the teacher unpublish the discussion
        if (presenter.getAssignment()?.unpublishable == true) {
            binding.publishWrapper.setGone()
            isPublished = true
            return
        }

        // Publish status
        with(binding.publishSwitch) {
            applyTheme()
            isChecked = isPublished
            setOnCheckedChangeListener { _, isChecked -> isPublished = isChecked }
        }
    }

    private fun setupSubscribeSwitch()  {
        with(binding.subscribeSwitch) {
            applyTheme()
            isChecked = isSubscribed
            setOnCheckedChangeListener { _, isChecked -> isSubscribed = isChecked }
        }
    }

    private fun setupAllowThreadedSwitch()  {
        with (binding.threadedSwitch) {
            applyTheme()
            isChecked = allowThreaded
            setOnCheckedChangeListener { _, isChecked -> allowThreaded = isChecked }
        }
    }

    private fun setupUsersMustPostSwitch()  {
        with(binding.usersMustPostSwitch) {
            applyTheme()
            isChecked = usersMustPost
            setOnCheckedChangeListener { _, isChecked -> usersMustPost = isChecked }
        }
    }

    private fun setupOverrides() {
        binding.overrideContainer.removeAllViews()

        if(presenter.getAssignment() == null) {
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
        } else {
            // Load in overrides
            if(groupsMapped.isNotEmpty() || sectionsMapped.isNotEmpty() || studentsMapped.isNotEmpty()) {
                editDateGroups.forEachIndexed { index, dueDateGroup ->
                    val assignees = ArrayList<CharSequence>()
                    val v = AssignmentOverrideView(requireActivity())
                    if (dueDateGroup.isEveryone) {
                        assignees += getString(if (editDateGroups.any { it.hasOverrideAssignees }) R.string.everyone_else else R.string.everyone)
                    }

                    dueDateGroup.groupIds.forEach { assignees.add(groupsMapped[it]?.name!!) }
                    dueDateGroup.sectionIds.forEach { assignees.add(sectionsMapped[it]?.name!!) }
                    dueDateGroup.studentIds.forEach {
                        assignees.add(studentsMapped[it]!!.let { user -> Pronouns.span(user.name, user.pronouns) })
                    }

                    v.setupOverride(index, dueDateGroup, editDateGroups.size > 1, assignees, datePickerOnClick, timePickerOnClick, removeOverrideClick) {
                        val args = AssigneeListFragment.makeBundle(
                                editDateGroups,
                                index,
                                sectionsMapped.values.toList(),
                                groupsMapped.values.toList(),
                                studentsMapped.values.toList())
                        RouteMatcher.route(requireActivity(), Route(AssigneeListFragment::class.java, canvasContext, args))
                        scrollBackToOverride = v
                    }

                    binding.overrideContainer.addView(v)
                }
            }
        }

        binding.overrideContainer.descendants<TextInputLayout>().forEach {
            it.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
    }

    private fun setupDisplayGradeAs() = with(binding) {
        // Filters spinner
        val spinnerAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.display_grade_as_types_discussion, R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        displayGradeAsSpinner.adapter = spinnerAdapter
        ViewStyler.themeSpinner(requireContext(), displayGradeAsSpinner, ThemePrefs.brandColor)
        displayGradeAsSpinner.onItemSelectedListener = null

        when(displayGradeAs) {
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
                    getString(R.string.points) -> displayGradeAs = Assignment.POINTS_TYPE
                    getString(R.string.gpa_scale) -> displayGradeAs = Assignment.GPA_SCALE_TYPE
                    getString(R.string.letter_grade) -> displayGradeAs = Assignment.LETTER_GRADE_TYPE
                    getString(R.string.complete_incomplete) -> displayGradeAs = Assignment.PASS_FAIL_TYPE
                    getString(R.string.percentage) -> displayGradeAs = Assignment.PERCENT_TYPE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        })
    }

    private fun setupDelete() {
        binding.deleteWrapper.setVisible(discussionTopicHeader != null)
        binding.deleteWrapper.onClickWithRequireNetwork {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.discussions_delete_title)
                .setMessage(R.string.discussions_delete_message)
                .setPositiveButton(R.string.delete) { _, _ ->
                    if(discussionTopicHeader != null) {
                        presenter.deleteDiscussionTopicHeader(discussionTopicHeader!!.id)
                    }
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .showThemed()
        }
    }

    private fun updateAttachmentUI() = with(binding) {
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
        discussionTopicHeader?.attachments?.firstOrNull()?.let {
            val attachmentView = AttachmentView(requireContext())
            attachmentView.setPendingRemoteFile(it, true) { action, attachment ->
                if (action == AttachmentView.AttachmentAction.REMOVE) {
                    presenter.attachmentRemoved = true
                    discussionTopicHeader?.attachments?.remove(attachment)
                }
            }

            attachmentLayout.addView(attachmentView)
        }
    }

    private fun updateAttachmentButton(show: Boolean = true) {
        // Only show if (1) we're in creation mode and (2) we don't already have an attachment
        attachmentButton?.isVisible = show && discussionTopicHeader == null && presenter.attachment == null
    }

    private fun addAttachment() {
        // set the description here. When we ask for permission to use the camera the app can call readySetGo and reset the description
        description = binding.descriptionRCEView.html

        val bundle = FileUploadDialogFragment.createDiscussionsBundle(ArrayList())
        FileUploadDialogFragment.newInstance(bundle).show(childFragmentManager, FileUploadDialogFragment.TAG)
    }

    override fun attachmentCallback(event: Int, attachment: FileSubmitObject?) {
        if(event == FileUploadDialogFragment.EVENT_ON_FILE_SELECTED) {
            presenter.attachment = attachment
            updateAttachmentUI()
        }
    }

    override fun startSavingDiscussion() {
        sendButton?.setGone()
        binding.savingProgressBar.announceForAccessibility(getString(R.string.saving))
        binding.savingProgressBar.setVisible()
    }

    override fun errorSavingDiscussion() {
        sendButton?.setVisible()
        binding.savingProgressBar.setGone()
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

        binding.editDiscussionName.hideKeyboard() // Close the keyboard
        requireActivity().onBackPressed() // Close this fragment
    }

    private fun saveDiscussion() = with(binding) {
        if(discussionTopicHeader != null) {
            val postData = DiscussionTopicPostBody()

            // Discussion title isn't required
            if(editDiscussionName.text.isNullOrBlank()) {
                postData.title = getString(R.string.no_title)
            } else {
                postData.title = editDiscussionName.text?.toString() ?: getString(R.string.no_title)
            }
            postData.message = handleLTIPlaceHolders(placeHolderList, descriptionRCEView.html)
            postData.published = isPublished
            postData.discussionType = if (allowThreaded) {
                DiscussionTopicHeader.DiscussionType.THREADED.toString().lowercase(Locale.getDefault())
            } else {
                DiscussionTopicHeader.DiscussionType.SIDE_COMMENT.toString().lowercase(Locale.getDefault())
            }
            postData.requireInitialPost = usersMustPost

            if (presenter.getAssignment() == null) {
                postData.delayedPostAt = editDateGroups[0].coreDates.unlockDate.toApiString()
                postData.lockAt = editDateGroups[0].coreDates.lockDate
            } else {
                val assignmentPostData = AssignmentPostBody()
                assignmentPostData.gradingType = displayGradeAs
                assignmentPostData.setGroupedDueDates(editDateGroups)
                assignmentPostData.pointsPossible = editGradePoints.text.toString().toDouble()

                postData.assignment = assignmentPostData
            }

            presenter.editDiscussion((discussionTopicHeader as DiscussionTopicHeader).id, postData)
        } else {
            val discussionTopicHeader = DiscussionTopicHeader()

            if(editDiscussionName.text.isNullOrBlank()) {
                discussionTopicHeader.title = getString(R.string.no_title)
            } else {
                discussionTopicHeader.title = editDiscussionName.text.toString()
            }

            discussionTopicHeader.message = descriptionRCEView.html
            discussionTopicHeader.published = isPublished
            discussionTopicHeader.subscribed = isSubscribed
            discussionTopicHeader.type = if (allowThreaded) DiscussionTopicHeader.DiscussionType.THREADED else DiscussionTopicHeader.DiscussionType.SIDE_COMMENT
            discussionTopicHeader.requireInitialPost = usersMustPost

            // If the assignment is null, that means we're creating/editing a discussion. When we do this we initialize mEditDateGroups with an empty DueDateGroup
            if (presenter.getAssignment() == null) {
                discussionTopicHeader.delayedPostDate = editDateGroups[0].coreDates.unlockDate
                discussionTopicHeader.lockAt = editDateGroups[0].coreDates.lockDate
            }
            presenter.saveDiscussion(discussionTopicHeader)
        }
    }

    override fun discussionDeletedSuccessfully(discussionTopicHeaderId: Long) {
        activity?.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(EDIT_DATE_GROUPS, ArrayList(editDateGroups))
        outState.putBoolean(RCE_HAS_FOCUS, binding.descriptionRCEView.hasFocus())
        description = binding.descriptionRCEView.html
        super.onSaveInstanceState(outState)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onAssigneesChanged(event: AssigneesUpdatedEvent) {
        // Update grouped due dates (EditDateGroups)
        event.once(javaClass.simpleName) { dates ->
            editDateGroups = dates
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
