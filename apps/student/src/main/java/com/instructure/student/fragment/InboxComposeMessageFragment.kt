/*
 * Copyright (C) 2018 - present  Instructure, Inc.
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
package com.instructure.student.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkInfo
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.managers.InboxManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.weave.*
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_INBOX_COMPOSE
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.file.upload.FileUploadDialogFragment
import com.instructure.pandautils.features.file.upload.FileUploadDialogParent
import com.instructure.pandautils.room.appdatabase.daos.AttachmentDao
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.adapter.CanvasContextSpinnerAdapter
import com.instructure.student.adapter.NothingSelectedSpinnerAdapter
import com.instructure.student.databinding.FragmentInboxComposeMessageBinding
import com.instructure.student.dialog.UnsavedChangesExitDialog
import com.instructure.student.events.ChooseRecipientsEvent
import com.instructure.student.events.MessageAddedEvent
import com.instructure.student.router.RouteMatcher
import com.instructure.student.view.AttachmentView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import javax.inject.Inject

@PageView(url = "conversations/compose")
@ScreenView(SCREEN_VIEW_INBOX_COMPOSE)
@AndroidEntryPoint
class InboxComposeMessageFragment : ParentFragment(), FileUploadDialogParent {

    private val binding by viewBinding(FragmentInboxComposeMessageBinding::bind)

    private val conversation by NullableParcelableArg<Conversation>(key = Const.CONVERSATION)
    private val participants by ParcelableArrayListArg<Recipient>(key = PARTICIPANTS)
    private val includedMessageIds by LongArrayArg(key = Const.MESSAGE)
    private val isReply by BooleanArg(key = IS_REPLY)
    private val currentMessage by NullableParcelableArg<Message>(key = Const.MESSAGE_TO_USER)
    private val homeroomMessage by BooleanArg(key = HOMEROOM_MESSAGE)

    private var selectedContext by NullableParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)
    private val isNewMessage by lazy { conversation == null }
    private val attachments = mutableListOf<Attachment>()

    private var sendIndividually = false
    private var shouldAllowExit = false

    private var coursesCall: WeaveJob? = null
    private var sendCall: WeaveJob? = null

    @Inject
    lateinit var attachmentDao: AttachmentDao

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        coursesCall?.cancel()
        sendCall?.cancel()
        super.onDestroy()
    }

    override fun title(): String = getString(R.string.composeMessage)

    override fun applyTheme() {
        with (binding) {
            ColorUtils.colorIt(ThemePrefs.textButtonColor, contactsImageButton)
            ViewStyler.themeSwitch(requireContext(), sendIndividualSwitch, ThemePrefs.brandColor)
            ViewStyler.themeToolbarLight(requireActivity(), toolbar)
            ViewStyler.themeProgressBar(savingProgressBar, requireContext().getColor(R.color.textDarkest))
        }
    }

    private fun validateMessage() = when {
        isNewMessage && selectedContext == null -> {
            toast(R.string.noCourseSelected)
            false
        }
        binding.chips.recipients.isEmpty() -> {
            toast(R.string.noRecipients)
            false
        }
        TextUtils.getTrimmedLength(binding.message.text) == 0 -> {
            toast(R.string.emptyMessage)
            false
        }
        else -> true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedContext = nonNullArgs.getParcelable(Const.CANVAS_CONTEXT)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_inbox_compose_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isNewMessage) {
            // Composing a new message
            binding.spinnerWrapper.setVisible()
            binding.recipientWrapper.setGone()
            binding.subjectView.setGone()
            binding.editSubject.setVisible()
            binding.sendIndividualMessageWrapper.setVisible()
            binding.sendIndividualDivider.setVisible()
            binding.sendIndividualSwitch.setOnCheckedChangeListener { _, isChecked -> sendIndividually = isChecked }
            if (participants.isNotEmpty()) {
                globalAddRecipients()
            }
        } else {
            globalAddRecipients()
        }
        setupToolbar()
        setupViews()
    }

    private fun globalAddRecipients() {
        binding.chips.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (isReply) {
                    if (currentMessage == null && conversation?.participants != null && conversation!!.participants.size == 1) {
                        // This is the result of replyAll to a monologue
                        addInitialRecipients(listOf(conversation!!.participants.first().id))
                    } else {
                        addInitialRecipients(
                            currentMessage?.participatingUserIds
                                ?: conversation?.audience ?: emptyList()
                        )
                    }
                } else if (participants.isNotEmpty()) {
                    addRecipients(participants)
                }
                binding.chips.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun setupViews() = with(binding) {
        // Set conversation subject
        conversation?.let { subjectView.text = it.subject }

        // Set up recipients view
        if (selectedContext != null) courseWasSelected()

        var previousCheckState = false
        chips.onRecipientsChanged = { recipients: List<Recipient> ->
            val entryCount = recipients.sumOf { it.userCount.coerceAtLeast(1) }
            if (entryCount >= 100) {
                if (sendIndividualSwitch.isEnabled) {
                    sendIndividualMessageWrapper.alpha = 0.3f
                    previousCheckState = sendIndividualSwitch.isChecked
                    sendIndividualSwitch.isEnabled = false
                    sendIndividualSwitch.isChecked = true
                }
            } else if (!sendIndividualSwitch.isEnabled) {
                sendIndividualMessageWrapper.alpha = 1f
                sendIndividualSwitch.isEnabled = true
                sendIndividualSwitch.isChecked = previousCheckState
            }
        }


        // Don't show the contacts button if there is no selected course
        contactsImageButton.setVisible(selectedContext != null || conversation?.contextCode.isValid())
        contactsImageButton.onClick {
            if (selectedContext == null && conversation == null) {
                toast(R.string.noCourseSelected)
            } else {
                val canvasContext =
                    selectedContext ?: CanvasContext.fromContextCode(conversation?.contextCode) ?: return@onClick
                RouteMatcher.route(requireActivity(), InboxRecipientsFragment.makeRoute(canvasContext, chips.recipients))
            }
        }

        // Ensure attachments are up to date
        refreshAttachments()

        // Get courses and groups if this is a new compose message
        if (isNewMessage) getAllCoursesAndGroups()

        if (homeroomMessage) {
            hideFieldsForHomeroomMessage()
        }
    }

    private fun hideFieldsForHomeroomMessage() {
        binding.spinnerWrapper.setGone()
        binding.recipientWrapper.setGone()
        binding.sendIndividualMessageWrapper.setGone()
    }

    private fun getAllCoursesAndGroups() {
        coursesCall = weave {
            try {
                val (courses, groups) = awaitApis<List<Course>, List<Group>>(
                    { CourseManager.getAllFavoriteCourses(true, it) },
                    { GroupManager.getAllGroups(it, true) }
                )
                addCoursesAndGroups(courses, groups)
            } catch (ignore: Throwable) {
            }
        }
    }

    private fun addCoursesAndGroups(courses: List<Course>, groups: List<Group>) = with(binding) {
        val adapter = CanvasContextSpinnerAdapter.newAdapterInstance(requireContext(), courses, groups)
        courseSpinner.adapter = NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_item_nothing_selected, context)
        if (selectedContext != null) {
            courseSpinner.onItemSelectedListener = null // Prevent listener from firing the when selection is placed
            courseSpinner.setSelection(
                adapter.getPosition(selectedContext) + 1,
                false
            ) // + 1 is for the nothingSelected position
            courseWasSelected()
        }
        courseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position != 0) { // Position zero is nothingSelected prompt
                    val canvasContext = adapter.getItem(position - 1) // -1 to account for nothingSelected item
                    if (selectedContext == null || selectedContext!!.id != canvasContext!!.id) {
                        chips.clearRecipients()
                        selectedContext = canvasContext
                        courseWasSelected()
                        courseSpinner.contentDescription =
                            getString(R.string.a11y_content_description_inbox_course_spinner, selectedContext?.name)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }
    }

    private fun courseWasSelected() = with(binding) {
        if (!homeroomMessage) {
            recipientWrapper.visibility = View.VISIBLE
            contactsImageButton.visibility = View.VISIBLE
        }
        requireActivity().invalidateOptionsMenu()
        chips.canvasContext = selectedContext
    }

    private fun setupToolbar() = with(binding) {
        toolbar.setTitle(
            when {
                isNewMessage -> R.string.newMessage
                isReply -> R.string.reply
                else -> R.string.forwardMessage
            }
        )

        if (toolbar.menu.size() == 0) toolbar.inflateMenu(R.menu.menu_add_message)
        toolbar.menu.findItem(R.id.menu_attachment).isVisible = true
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_send -> {
                    sendMessage()
                }
                R.id.menu_attachment -> {
                    val bundle = FileUploadDialogFragment.createMessageAttachmentsBundle(arrayListOf())
                    FileUploadDialogFragment.newInstance(bundle)
                        .show(childFragmentManager, FileUploadDialogFragment.TAG)
                }
                else -> return@setOnMenuItemClickListener false
            }
            true
        }

        toolbar.setupAsBackButton(this@InboxComposeMessageFragment)
    }

    private fun handleExit() {
        // Check to see if the user has made any changes
        if (binding.editSubject.text?.isNotBlank() == true || binding.message.text?.isNotBlank() == true || attachments.isNotEmpty()) {
            shouldAllowExit = false
            // Use childFragmentManager so that exiting the compose fragment also dismisses the dialog
            UnsavedChangesExitDialog.show(childFragmentManager) {
                shouldAllowExit = true
                requireActivity().onBackPressed()
            }
        } else {
            shouldAllowExit = true
            requireActivity().onBackPressed()
        }
    }

    override fun handleBackPressed(): Boolean {
        // See if they have unsent changes
        if (!shouldAllowExit) {
            handleExit()
            return true
        }
        return super.handleBackPressed()
    }

    private fun messageSuccess(conversation: Conversation) {
        toast(R.string.messageSentSuccessfully)
        shouldAllowExit = true
        EventBus.getDefault().postSticky(MessageAddedEvent(true, null))
        EventBus.getDefault().postSticky(ConversationUpdatedEvent(conversation))
        requireActivity().onBackPressed()
    }

    private fun messageFailure(@StringRes message: Int = R.string.errorSendingMessage) {
        binding.toolbar.menu.findItem(R.id.menu_send).isVisible = true
        binding.toolbar.menu.findItem(R.id.menu_attachment).isVisible = true
        binding.savingProgressBar.visibility = View.GONE
        toast(message)
    }

    private fun refreshAttachments() {
        binding.attachmentLayout.setPendingAttachments(attachments, true) { action, attachment ->
            if (action == AttachmentView.AttachmentAction.REMOVE) {
                attachments -= attachment
            }
        }
    }

    private fun sendMessage() = with(binding) {
        // Validate inputs
        if (!validateMessage()) return

        // Ensure network is available
        if (!APIHelper.hasNetworkConnection()) {
            Toast.makeText(
                requireContext(),
                this@InboxComposeMessageFragment.getString(R.string.notAvailableOffline),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Make the progress bar visible and the other buttons not there so they can't try to re-send the message multiple times
        toolbar.menu.findItem(R.id.menu_send).isVisible = false
        toolbar.menu.findItem(R.id.menu_attachment).isVisible = false
        savingProgressBar.announceForAccessibility(getString(R.string.sending))
        savingProgressBar.visibility = View.VISIBLE

        // Send message
        if (isNewMessage) {
            val recipients = chips.recipients
            val recipientCount = recipients.sumOf { it.userCount.coerceAtLeast(1) }
            val isBulk = recipientCount >= 100 || (recipientCount > 1 && sendIndividually)
            val contextId = selectedContext!!.contextId
            val subject = editSubject.text.toString()
            // isBulk controls the group vs individual messages, so group message flag is hardcoded to true at the api call
            createConversation(recipients, message.text.toString(), subject, contextId, isBulk)
        } else {
            sendMessage(chips.recipients, message.text.toString())
        }
    }

    private fun sendMessage(selectedRecipients: List<Recipient>, message: String) {

        sendCall = tryWeave {
            val attachmentIds = attachments.map { it.id }.toLongArray()
            val recipientIds = selectedRecipients.mapNotNull { it.stringId }
            val conversation = awaitApi<Conversation> {
                InboxManager.addMessage(
                    conversation?.id ?: 0,
                    message,
                    recipientIds,
                    includedMessageIds,
                    attachmentIds,
                    conversation?.contextCode,
                    it
                )
            }
            messageSuccess(conversation)
        } catch {
            it.cause?.printStackTrace()
            messageFailure()
        }
    }

    private fun createConversation(
        selectedRecipients: List<Recipient>,
        message: String,
        subject: String,
        contextId: String,
        isBulk: Boolean
    ) {
        sendCall?.cancel()
        sendCall = tryWeave {
            val attachmentIds = attachments.map { it.id }.toLongArray()
            val recipientIds = selectedRecipients.mapNotNull { it.stringId }
            val conversation = awaitApi<List<Conversation>> {
                InboxManager.createConversation(recipientIds, message, subject, contextId, attachmentIds, isBulk, it)
            }.first()
            messageSuccess(conversation)
        } catch { error ->
            error.cause?.printStackTrace()
            val canvasErrors = (error as? StatusCallbackError)?.canvasErrors
            if (canvasErrors?.any { it.attribute == "recipients" && it.message == "invalid" } == true) {
                messageFailure(R.string.invalidRecipients)
            } else {
                messageFailure()
            }
        }
    }

    private fun addInitialRecipients(initialRecipientIds: List<Long>) {
        val recipients = initialRecipientIds
            .mapNotNull { longId ->
                val id = longId.toString()
                participants.find {
                    // Map IDs to participants
                    it.stringId == id
                }
            }
            .minus(binding.chips.recipients)
        binding.chips.addRecipients(recipients)
    }

    private fun addRecipients(newRecipients: List<Recipient>) {
        val selectedRecipients = binding.chips.recipients
        val recipients = newRecipients.filter { recipient ->
            // Skip existing recipients
            selectedRecipients.none { it.stringId == recipient.stringId }
        }
        binding.chips.addRecipients(recipients)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onRecipientsChosen(event: ChooseRecipientsEvent) {
        event.once(javaClass.simpleName) { recipients ->
            // We're going to add all the recipients that the user has selected. They may have removed a user previously selected,
            // so clear the view so we only add the users selected
            binding.chips.clearRecipients()
            addRecipients(recipients)
        }
    }

    override fun workInfoLiveDataCallback(uuid: UUID?, workInfoLiveData: LiveData<WorkInfo>) {
        workInfoLiveData.observe(viewLifecycleOwner) { workInfo ->
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                lifecycleScope.launch {
                    uuid?.let { uuid ->
                        val attachmentEntities = attachmentDao.findByParentId(uuid.toString())
                        attachmentEntities?.let { attachmentList ->
                            attachments.addAll(attachmentList.map { it.toApiModel() })
                            refreshAttachments()
                            attachmentDao.deleteAll(attachmentList)
                        } ?: toast(R.string.errorUploadingFile)
                    } ?: toast(R.string.errorUploadingFile)

                }
            }
        }
    }

    companion object {

        private const val IS_REPLY = "is_reply"
        private const val PARTICIPANTS = "participants"
        private const val HOMEROOM_MESSAGE = "homeroom_message"

        fun makeRoute(
            isReply: Boolean,
            conversation: Conversation,
            participants: List<Recipient>,
            includedMessageIds: LongArray,
            currentMessage: Message?
        ): Route {
            val bundle = Bundle().apply {
                putBoolean(IS_REPLY, isReply)
                putParcelable(Const.CONVERSATION, conversation)
                putParcelableArrayList(PARTICIPANTS, ArrayList(participants))
                putLongArray(Const.MESSAGE, includedMessageIds)
                putParcelable(Const.MESSAGE_TO_USER, currentMessage)
            }
            return Route(InboxComposeMessageFragment::class.java, null, bundle)
        }

        fun makeRoute() = Route(InboxComposeMessageFragment::class.java, null, Bundle())

        fun makeRoute(
            canvasContext: CanvasContext,
            participants: ArrayList<Recipient>,
            homeroomMessage: Boolean = false
        ): Route {
            val bundle = Bundle().apply {
                putParcelableArrayList(PARTICIPANTS, participants)
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putBoolean(HOMEROOM_MESSAGE, homeroomMessage)
            }
            return Route(InboxComposeMessageFragment::class.java, canvasContext, bundle)
        }

        private fun validateRoute(route: Route): Boolean {
            val args = route.arguments
            return when {
                args.containsKey(Const.CONVERSATION) -> args.containsKey(IS_REPLY)
                        && args.containsKey(PARTICIPANTS)
                        && args.containsKey(Const.MESSAGE)
                        && args.containsKey(Const.MESSAGE_TO_USER)
                args.containsKey(PARTICIPANTS) -> route.canvasContext != null
                else -> true
            }
        }

        fun newInstance(route: Route): InboxComposeMessageFragment? {
            if (!validateRoute(route)) return null
            return InboxComposeMessageFragment().apply {
                arguments = route.arguments
            }
        }
    }
}
