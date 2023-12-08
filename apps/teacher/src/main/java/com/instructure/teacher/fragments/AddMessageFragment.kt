/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.teacher.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.AdapterView
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkInfo
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Message
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_INBOX_COMPOSE
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.dialogs.UnsavedChangesExitDialog
import com.instructure.pandautils.features.file.upload.FileUploadDialogFragment
import com.instructure.pandautils.features.file.upload.FileUploadDialogParent
import com.instructure.pandautils.fragments.BasePresenterFragment
import com.instructure.pandautils.room.appdatabase.daos.AttachmentDao
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.ColorUtils
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FileUploadEvent
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.NullableStringArg
import com.instructure.pandautils.utils.ParcelableArrayListArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.nonNullArgs
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.utils.withArgs
import com.instructure.pandautils.views.AttachmentView
import com.instructure.teacher.R
import com.instructure.teacher.adapters.CanvasContextSpinnerAdapter
import com.instructure.teacher.adapters.NothingSelectedSpinnerAdapter
import com.instructure.teacher.databinding.FragmentAddMessageBinding
import com.instructure.teacher.events.ChooseMessageEvent
import com.instructure.teacher.events.MessageAddedEvent
import com.instructure.teacher.factory.AddMessagePresenterFactory
import com.instructure.teacher.presenters.AddMessagePresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.setupCloseButton
import com.instructure.teacher.viewinterface.AddMessageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.UUID
import javax.inject.Inject

@PageView(url = "conversations/compose")
@ScreenView(SCREEN_VIEW_INBOX_COMPOSE)
@AndroidEntryPoint
class AddMessageFragment : BasePresenterFragment<
        AddMessagePresenter,
        AddMessageView,
        FragmentAddMessageBinding>(),
    AddMessageView,
    FileUploadDialogParent {

    private var currentMessage: Message? by NullableParcelableArg(null, Const.MESSAGE_TO_USER)
    private var selectedCourse: CanvasContext? = null
    private var isNewMessage by BooleanArg(false, Const.COMPOSE_FRAGMENT)
    private var sendIndividually = false
    private var isMessageStudentsWho by BooleanArg(false, MESSAGE_STUDENTS_WHO)
    private var isPersonalMessage by BooleanArg(false, MESSAGE_STUDENTS_WHO_CONTEXT_IS_PERSONAL)
    private var messageStudentsWhoContextId by NullableStringArg(MESSAGE_STUDENTS_WHO_CONTEXT_ID)
    private var shouldAllowExit = false
    private var participants: ArrayList<Recipient> by ParcelableArrayListArg(key = KEY_PARTICIPANTS)

    @Inject
    lateinit var attachmentDao: AttachmentDao

    private val isValidNewMessage: Boolean
        get() {
            if (isNewMessage) {
                if (selectedCourse == null) {
                    showToast(R.string.no_course_selected)
                    return false
                }
            }

            if (binding.chips.recipients.isEmpty()) {
                showToast(R.string.message_has_no_recipients)
                return false
            } else if (TextUtils.getTrimmedLength(binding.message.text) == 0) {
                showToast(R.string.empty_message)
                return false
            }

            return true
        }

    private val recipientsFromRecipientEntries: ArrayList<Recipient>
        get() = ArrayList(binding.chips.recipients)

    override val bindingInflater: (layoutInflater: LayoutInflater) -> FragmentAddMessageBinding = FragmentAddMessageBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        if (savedInstanceState != null && !isNewMessage) {

        } else if (isNewMessage) {
            // Composing a new message
            spinnerWrapper.setVisible()
            editSubject.setVisible()
            sendIndividualMessageWrapper.setVisible()
            sendIndividualDivider.setVisible()
            recipientWrapper.setGone()
            subjectView.setGone()

            ViewStyler.themeSwitch(requireContext(), sendIndividualSwitch, ThemePrefs.brandColor)
            sendIndividualSwitch.setOnCheckedChangeListener { _, isChecked -> sendIndividually = isChecked }

            if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_COURSE)) {
                selectedCourse = savedInstanceState.getParcelable(SELECTED_COURSE)
            }
        } else {
            if (isMessageStudentsWho) {
                // Set up selected course for 'message students who' to allow searching and editing recipients
                selectedCourse = CanvasContext.fromContextCode(messageStudentsWhoContextId)
                chips.canvasContext = selectedCourse
            }

            val vto = chips.viewTreeObserver
            vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {

                override fun onGlobalLayout() {
                    if (presenter.isReply) {
                        if (currentMessage == null && presenter.conversation?.participants != null && presenter.conversation!!.participants.size == 1) {
                            // This is the result of replyAll to a monologue
                            addInitialRecipients(listOf(presenter.conversation!!.participants.first().id))
                        } else {
                            addInitialRecipients(
                                currentMessage?.participatingUserIds
                                    ?: presenter.conversation?.audience ?: emptyList()
                            )
                        }
                    } else if (isMessageStudentsWho) {
                        addRecipients(participants)
                    }

                    val obs = chips.viewTreeObserver
                    obs.removeOnGlobalLayoutListener(this)
                }
            })
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (selectedCourse != null) {
            outState.putParcelable(SELECTED_COURSE, selectedCourse)
        }
    }

    override fun onPresenterPrepared(presenter: AddMessagePresenter) {}

    override fun getPresenterFactory(): AddMessagePresenterFactory {
        val conversation = arguments?.getParcelable<Conversation>(Const.CONVERSATION)
        val participants = arguments?.getParcelableArrayList<Recipient>(KEY_PARTICIPANTS)
        val messages = arguments?.getParcelableArrayList<Message>(Const.MESSAGE)
        val isReply = arguments?.getBoolean(KEY_IS_REPLY, false) ?: false
        return AddMessagePresenterFactory(conversation, participants, messages, isReply)
    }

    override fun onReadySetGo(presenter: AddMessagePresenter) = with(binding) {
        setupToolbar()

        // Set conversation subject
        if (!isNewMessage && !isMessageStudentsWho) {
            subjectView.text = presenter.conversation!!.subject
        } else if (isMessageStudentsWho) {
            if (isPersonalMessage) {
                subjectView.setGone()
                editSubject.setVisible()
                editSubject.setText(nonNullArgs.getString(MESSAGE_STUDENTS_WHO_SUBJECT))
            } else {
                subjectView.text = nonNullArgs.getString(MESSAGE_STUDENTS_WHO_SUBJECT)
            }
        }

        // Set up recipients view
        var previousCheckState = false
        chips.onRecipientsChanged = { recipients: List<Recipient> ->
            val entryCount = recipients.sumOf { it.userCount.coerceAtLeast(1) }
            if (entryCount >= 100) {
                if (sendIndividualSwitch.isEnabled) {
                    sendIndividualMessageWrapper.alpha = 0.3f
                    previousCheckState = binding.sendIndividualSwitch.isChecked
                    sendIndividualSwitch.isEnabled = false
                    sendIndividualSwitch.isChecked = true
                }
            } else if (!sendIndividualSwitch.isEnabled) {
                sendIndividualMessageWrapper.alpha = 1f
                sendIndividualSwitch.isEnabled = true
                sendIndividualSwitch.isChecked = previousCheckState
            }
        }

        if (presenter.course!!.id != 0L) {
            chips.canvasContext = presenter.course
        } else if (selectedCourse != null) {
            courseWasSelected()
        }

        ColorUtils.colorIt(ThemePrefs.textButtonColor, contactsImageButton)

        // Don't show the contacts button if there is no selected course and there is no context_code from the conversation (shouldn't happen, but it does)
        if (selectedCourse == null && presenter.course != null && presenter.course!!.id == 0L) {
            contactsImageButton.visibility = View.INVISIBLE
        }

        contactsImageButton.setOnClickListener {
            val canvasContext: CanvasContext? =
                if (presenter.course != null && presenter.course!!.id == 0L) {
                    // Presenter doesn't know what the course is, use the selectedCourse instead
                    selectedCourse
                } else {
                    presenter.course
                }

            RouteMatcher.route(
                requireActivity(),
                Route(
                    ChooseRecipientsFragment::class.java,
                    canvasContext,
                    ChooseRecipientsFragment.createBundle(canvasContext!!, recipientsFromRecipientEntries)
                )
            )
        }

        // Ensure attachments are up to date
        refreshAttachments()

        // Get courses and groups if this is a new compose message
        if (isNewMessage) {
            presenter.getAllCoursesAndGroups(false)
        }
    }

    override fun addCoursesAndGroups(courses: ArrayList<Course>, groups: ArrayList<Group>) = with(binding) {
        val adapter = CanvasContextSpinnerAdapter.newAdapterInstance(requireContext(), courses, groups)
        courseSpinner.adapter = NothingSelectedSpinnerAdapter(
            adapter = adapter,
            nothingSelectedLayout = R.layout.spinner_item_nothing_selected,
            context = requireContext()
        )
        if (selectedCourse != null) {
            courseSpinner.onItemSelectedListener = null // Prevent listener from firing when the selection is placed
            courseSpinner.setSelection(
                adapter.getPosition(selectedCourse) + 1,
                false
            ) //  + 1 is for the nothingSelected position
            courseWasSelected()
        }

        courseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position != 0) { // Position zero is nothingSelected prompt
                    val canvasContext = adapter.getItem(position - 1) // -1 to account for nothingSelected item
                    if (selectedCourse == null || selectedCourse!!.id != canvasContext!!.id) {
                        chips.clearRecipients()
                        selectedCourse = canvasContext
                        courseWasSelected()
                        courseSpinner.contentDescription =
                            getString(R.string.a11y_content_description_inbox_course_spinner, selectedCourse?.name)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun courseWasSelected() = with(binding) {
        recipientWrapper.setVisible()
        contactsImageButton.setVisible()
        requireActivity().invalidateOptionsMenu()
        chips.canvasContext = selectedCourse
    }

    private fun setupToolbar() = with(binding) {
        if (isNewMessage || isMessageStudentsWho && isPersonalMessage) {
            toolbar.setTitle(R.string.newMessage)
        } else if (isMessageStudentsWho) {
            toolbar.setTitle(R.string.messageStudentsWho)
        } else {
            toolbar.setTitle(if (presenter.isReply) R.string.reply_to_message else R.string.forward_message)
        }

        if (toolbar.menu.size() == 0) {
            toolbar.inflateMenu(R.menu.menu_compose_message_activity)
        }

        toolbar.menu.findItem(R.id.menu_attachment).isVisible = true
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_send -> {
                    sendMessage()
                    true
                }

                R.id.menu_attachment -> {
                    val bundle = FileUploadDialogFragment.createAttachmentsBundle(ArrayList())
                    FileUploadDialogFragment.newInstance(bundle)
                        .show(childFragmentManager, FileUploadDialogFragment.TAG)
                    true
                }

                else -> false
            }
        }

        ViewStyler.themeToolbarLight(requireActivity(), toolbar)
        toolbar.setupCloseButton { handleExit() }
    }

    fun handleExit() {
        // Check to see if the user has made any changes
        if (selectedCourse != null || binding.editSubject.text?.isNotEmpty() == true || binding.message.text?.isNotEmpty() == true || presenter.attachments.isNotEmpty()) {
            shouldAllowExit = false
            UnsavedChangesExitDialog.show(requireActivity().supportFragmentManager) {
                shouldAllowExit = true
                requireActivity().onBackPressed()
            }
        } else {
            shouldAllowExit = true
            requireActivity().onBackPressed()
        }
    }

    fun shouldAllowExit(): Boolean = shouldAllowExit

    override fun onHandleBackPressed(): Boolean {
        // See if they have unsent changes
        if (!shouldAllowExit()) {
            handleExit()
            return true
        }

        return super.onHandleBackPressed()
    }


    override fun messageSuccess() {
        showToast(R.string.message_sent_successfully)
        shouldAllowExit = true

        // Post a unique skip id in case they come back to this message and send another message
        EventBus.getDefault().postSticky(MessageAddedEvent(true, null))

        requireActivity().onBackPressed()
    }

    override fun messageFailure() = with(binding) {
        toolbar.menu.findItem(R.id.menu_send).isVisible = true
        toolbar.menu.findItem(R.id.menu_attachment).isVisible = true

        savingProgressBar.setGone()

        showToast(R.string.error_sending_message)
    }

    override fun refreshAttachments() {
        binding.attachments.setPendingAttachments(presenter.attachments, true) { action, attachment ->
            if (action == AttachmentView.AttachmentAction.REMOVE) presenter.removeAttachment(attachment)
        }
    }

    internal fun sendMessage() = with(binding) {
        // Validate inputs
        if (!isValidNewMessage) return

        // Ensure network is available
        if (!APIHelper.hasNetworkConnection()) {
            showToast(this@AddMessageFragment.getString(R.string.not_available_offline))
            return
        }

        // Make the progress bar visible and the other buttons not there so they can't try to re-send the message multiple times
        toolbar.menu.findItem(R.id.menu_send).isVisible = false
        toolbar.menu.findItem(R.id.menu_attachment).isVisible = false

        ViewStyler.themeProgressBar(savingProgressBar, requireContext().getColor(R.color.textDarkest))
        savingProgressBar.announceForAccessibility(getString(R.string.sendingSimple))
        savingProgressBar.setVisible()

        // Send message
        if (isNewMessage || isMessageStudentsWho) {
            // Send bulk if recipient count exceeds 99, OR if count exceeds one AND 'send individually' is checked
            val recipients = chips.recipients
            val recipientCount = recipients.sumOf { it.userCount.coerceAtLeast(1) }
            var isBulk = recipientCount >= 100 || (recipientCount > 1 && sendIndividually)

            val contextId: String
            val subject: String

            if (isMessageStudentsWho) {
                sendIndividually = false
                isBulk = true // We always want to send these as bulk individual messages
                contextId = nonNullArgs.getString(MESSAGE_STUDENTS_WHO_CONTEXT_ID, "")
                subject = if (isPersonalMessage) editSubject.text.toString() else subjectView.text.toString()
            } else {
                contextId = selectedCourse!!.contextId
                subject = editSubject.text.toString()
            }
            // isBulk controls the group vs individual messages, so group message flag is hardcoded to true at the api call
            presenter.sendNewMessage(chips.recipients, message.text.toString(), subject, contextId, isBulk)
        } else {
            presenter.sendMessage(chips.recipients, message.text.toString())
        }
    }


    private fun addInitialRecipients(initialRecipientIds: List<Long>) {
        val selectedRecipients = binding.chips.recipients
        val recipients = initialRecipientIds
            .map { it.toString() }
            .filter { id ->
                // Skip existing recipients
                selectedRecipients.none { it.stringId == id }
            }
            .mapNotNull { presenter.getParticipantById(it) }
        binding.chips.addRecipients(recipients)
    }

    private fun addRecipients(newRecipients: List<Recipient>) {
        val selectedRecipients = binding.chips.recipients
        val recipients = newRecipients.filter { recipient ->
            // Skip existing recipients
            val stringId = recipient.stringId
            selectedRecipients.none { it.stringId == stringId }
        }
        binding.chips.addRecipients(recipients)
    }

    override fun onRefreshFinished() {}

    override fun onRefreshStarted() {}

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onRecipientsUpdated(event: ChooseMessageEvent) {
        event.once(javaClass.simpleName) { recipients ->
            // We're going to add all the recipients that the user has selected. They may have removed a user previously selected,
            // so clear the view so we only add the users selected
            binding.chips.clearRecipients()
            addRecipients(recipients)
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFileUploadEvent(event: FileUploadEvent) {
        // Update the adapter item
        event.once(AddMessageFragment::class.java.simpleName) {
            EventBus.getDefault().removeStickyEvent(it)
            presenter.addAttachments(it.attachments)
            refreshAttachments()
        }
    }

    override fun workInfoLiveDataCallback(uuid: UUID?, workInfoLiveData: LiveData<WorkInfo>) {
        workInfoLiveData.observe(viewLifecycleOwner) { workInfo ->
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                lifecycleScope.launch {
                    uuid?.let { uuid ->
                        attachmentDao.findByParentId(uuid.toString())?.let { attachmentEntities ->
                            val attachments = attachmentEntities.map { it.toApiModel() }
                            presenter.addAttachments(attachments)
                            refreshAttachments()
                            attachmentDao.deleteAll(attachmentEntities)
                        } ?: toast(R.string.errorUploadingFile)
                    } ?: toast(R.string.errorUploadingFile)
                }
            }
        }
    }

    companion object {

        /* Bundle key for boolean indicating whether the user is replying or forwarding */
        private const val KEY_IS_REPLY = "is_reply"
        private const val SELECTED_COURSE = "selected_course"

        /* Bundle key for list of participants */
        private const val KEY_PARTICIPANTS = "participants"
        private const val MESSAGE_STUDENTS_WHO = "message_students_who"
        private const val MESSAGE_STUDENTS_WHO_SUBJECT = "message_students_who_subject"
        private const val MESSAGE_STUDENTS_WHO_CONTEXT_ID = "message_students_context_id"
        private const val MESSAGE_STUDENTS_WHO_CONTEXT_IS_PERSONAL = "message_students_is_personal"

        fun createBundle(
            isReply: Boolean,
            conversation: Conversation,
            participants: List<Recipient>,
            messages: List<Message>,
            currentMessage: Message?
        ): Bundle =
            Bundle().apply {
                putBoolean(KEY_IS_REPLY, isReply)
                putParcelable(Const.CONVERSATION, conversation)
                putParcelableArrayList(KEY_PARTICIPANTS, ArrayList(participants))
                putParcelableArrayList(Const.MESSAGE, ArrayList(messages))
                putParcelable(Const.MESSAGE_TO_USER, currentMessage)
            }

        fun createBundle(): Bundle =
            Bundle().apply {
                putBoolean(Const.COMPOSE_FRAGMENT, true)
            }

        fun createBundle(users: List<Recipient>, subject: String, contextId: String, isPersonal: Boolean): Bundle =
            Bundle().apply {
                putBoolean(MESSAGE_STUDENTS_WHO_CONTEXT_IS_PERSONAL, isPersonal)
                putBoolean(MESSAGE_STUDENTS_WHO, true)
                putParcelableArrayList(KEY_PARTICIPANTS, ArrayList(users))
                putString(MESSAGE_STUDENTS_WHO_SUBJECT, subject)
                putString(MESSAGE_STUDENTS_WHO_CONTEXT_ID, contextId)
            }

        fun newInstance(bundle: Bundle) = AddMessageFragment().withArgs(bundle)
    }
}
