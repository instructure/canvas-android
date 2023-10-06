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

import android.app.Activity.RESULT_OK
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.interactions.Identity
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_MESSAGE_THREAD
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.AttachmentView
import com.instructure.teacher.R
import com.instructure.teacher.activities.InitActivity
import com.instructure.teacher.adapters.MessageAdapter
import com.instructure.teacher.adapters.StudentContextFragment
import com.instructure.teacher.databinding.FragmentMessageThreadBinding
import com.instructure.teacher.databinding.RecyclerSwipeRefreshLayoutBinding
import com.instructure.teacher.events.ConversationDeletedEvent
import com.instructure.teacher.events.ConversationUpdatedEvent
import com.instructure.teacher.events.MessageAddedEvent
import com.instructure.teacher.factory.MessageThreadPresenterFactory
import com.instructure.teacher.features.inbox.list.TeacherInboxRouter
import com.instructure.teacher.holders.MessageHolder
import com.instructure.teacher.interfaces.MessageAdapterCallback
import com.instructure.teacher.presenters.MessageThreadPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.MediaDownloader
import com.instructure.teacher.utils.RecyclerViewUtils
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.utils.view
import com.instructure.teacher.viewinterface.MessageThreadView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@ScreenView(SCREEN_VIEW_MESSAGE_THREAD)
class MessageThreadFragment : BaseSyncFragment<Message, MessageThreadPresenter, MessageThreadView, MessageHolder, MessageAdapter>(), MessageThreadView, Identity {

    private val binding by viewBinding(FragmentMessageThreadBinding::bind)

    private var conversationScope: String? = null

    override val identity: Long?
        get() = nonNullArgs.getParcelable<Conversation>(Const.CONVERSATION)?.id

    override val skipCheck: Boolean
        get() = false

    private val toolbar: Toolbar
        get() = binding.root.findViewById(R.id.toolbar)

    private lateinit var swipeRefreshLayoutContainerBinding: RecyclerSwipeRefreshLayoutBinding

    private val menuListener = Toolbar.OnMenuItemClickListener { item ->
        when (item.itemId) {
            R.id.archive, R.id.unarchive -> {
                presenter.toggleArchived()
                return@OnMenuItemClickListener true
            }

            R.id.delete -> {
                val dialog = AlertDialog.Builder(requireContext())
                        .setView(R.layout.dialog_delete_conversation)
                        .setNegativeButton(R.string.teacher_cancel) { dialogInterface, _ -> dialogInterface.dismiss() }
                        .setPositiveButton(R.string.delete) { _, _ -> presenter.deleteConversation() }
                        .create()

                dialog.setOnShowListener {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
                }

                dialog.show()
                return@OnMenuItemClickListener true
            }

            R.id.reply -> {
                val topMessage = presenter.data.get(0)
                addMessage(topMessage, true)
                return@OnMenuItemClickListener true
            }
            R.id.replyAll -> {
                replyAllMessage()
                return@OnMenuItemClickListener true
            }
            R.id.markAsUnread -> {
                presenter.markConversationUnread()
                return@OnMenuItemClickListener true
            }
            R.id.forward -> {
                val forwardMessage = presenter.data.get(presenter.data.size() - 1)
                addMessage(forwardMessage, false)
                return@OnMenuItemClickListener true
            }
        }
        false
    }

    private val adapterCallback = object : MessageAdapterCallback {
        override fun onAvatarClicked(user: BasicUser) {
            val canvasContext = CanvasContext.fromContextCode(conversation!!.contextCode)

            if (canvasContext != null && canvasContext is Course) {
                val bundle = StudentContextFragment.makeBundle(user.id, canvasContext.id, false)
                RouteMatcher.route(requireActivity(), Route(StudentContextFragment::class.java, null, bundle))
            }
        }

        override fun onAttachmentClicked(action: AttachmentView.AttachmentAction, attachment: Attachment) {
            if (action == AttachmentView.AttachmentAction.PREVIEW) {
                attachment.view(requireActivity())
            } else if (action == AttachmentView.AttachmentAction.DOWNLOAD) {
                if (PermissionUtils.hasPermissions(requireActivity(), PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                    // Download media
                    MediaDownloader.download(requireContext(), attachment.url, attachment.filename!!, attachment.filename!!)
                } else {
                    requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE), PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE)
                }
            }
        }

        override fun onMessageAction(action: MessageAdapterCallback.MessageClickAction, message: Message) {
            when (action) {
                MessageAdapterCallback.MessageClickAction.REPLY -> addMessage(message, true)

                MessageAdapterCallback.MessageClickAction.REPLY_ALL -> replyAllMessage(message)

                MessageAdapterCallback.MessageClickAction.FORWARD -> addMessage(message, false)

                MessageAdapterCallback.MessageClickAction.DELETE -> {
                    val dialog = AlertDialog.Builder(requireContext())
                            .setView(R.layout.dialog_delete_message)
                            .setNegativeButton(R.string.teacher_cancel) { dialogInterface, _ -> dialogInterface.dismiss() }
                            .setPositiveButton(R.string.delete) { _, _ -> presenter.deleteMessage(message) }
                            .create()

                    dialog.setOnShowListener {
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
                        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
                    }

                    dialog.show()
                }
            }
        }

        override fun getParticipantById(id: Long): BasicUser? = presenter.getParticipantById(id)
    }

    private val conversation: Conversation?
        get() = presenter.getConversation()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        swipeRefreshLayoutContainerBinding = RecyclerSwipeRefreshLayoutBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun layoutResId(): Int = R.layout.fragment_message_thread

    override fun onCreateView(view: View) {}

    override fun onReadySetGo(presenter: MessageThreadPresenter) = with(binding) {
        if (recyclerView.adapter == null && conversation != null) {
            recyclerView.adapter = adapter
        }

        starred.onClick {
            presenter.toggleStarred()
        }

        // Set to true so we actually mark the conversation as read
        presenter.loadData(true)
        swipeRefreshLayoutContainerBinding.emptyPandaView.setLoading()
    }

    override fun getPresenterFactory(): MessageThreadPresenterFactory {
        if (arguments?.containsKey(Const.CONVERSATION_ID) == true) {
            // We are coming from a push notification
            // No way to know from the push notification where in the conversation we should be; position = 0
            return MessageThreadPresenterFactory(conversationId = nonNullArgs.getLong(Const.CONVERSATION_ID, 0))
        }

        return MessageThreadPresenterFactory(nonNullArgs.getParcelable<Parcelable>(Const.CONVERSATION) as Conversation)
    }

    override fun onPresenterPrepared(presenter: MessageThreadPresenter) {
        conversationScope = nonNullArgs.getString(Const.SCOPE)

        initToolbar()

        // We may not have the conversation yet (we don't when coming from a push notification)
        if (conversation != null) {
            setupConversationDetails()
            setupRecyclerView()
        }
    }

    private fun setupRecyclerView() {
        RecyclerViewUtils.buildRecyclerView(requireActivity().window.decorView.rootView, requireContext(), adapter,
                presenter, R.id.swipeRefreshLayout, R.id.recyclerView, R.id.emptyPandaView, getString(R.string.no_items_to_display_short))
        recyclerView.also {
            val linearLayoutManager = LinearLayoutManager(requireContext())
            it.layoutManager = linearLayoutManager

            val dividerItemDecoration = DividerItemDecoration(
                it.context,
                (it.layoutManager as LinearLayoutManager).orientation
            )
            dividerItemDecoration.setDrawable(requireContext().getDrawableCompat(R.drawable.item_decorator_gray))
            it.removeAllItemDecorations()
            it.addItemDecoration(dividerItemDecoration)
            addSwipeToRefresh(swipeRefreshLayoutContainerBinding.swipeRefreshLayout)
        }
    }

    private fun initToolbar() {
        toolbar.setTitle(R.string.message)

        ViewStyler.themeToolbarColored(requireActivity(), toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)

        if (activity is InitActivity && resources.getBoolean(R.bool.isDeviceTablet)) {
            // Don't have an arrow because going back will close the app
        } else {
            toolbar.setupBackButton(this@MessageThreadFragment)
        }

        toolbar.inflateMenu(R.menu.message_thread)

        if (conversationScope != null && conversationScope == "sent") {
            // We can't archive sent conversations
            val archive = toolbar.menu.findItem(R.id.archive)
            if (archive != null) {
                archive.isVisible = false
            }

            val unarchive = toolbar.menu.findItem(R.id.unarchive)
            if (unarchive != null) {
                unarchive.isVisible = false
            }
        }

        toolbar.setOnMenuItemClickListener(menuListener)
    }

    override fun setupConversationDetails() = with(binding) {
        if(recyclerView.adapter == null) {
            // If we didn't setup the adapter initially (we didn't start off with the conversation), then do it now
            recyclerView.adapter = adapter
        }

        setupRecyclerView()

        if (conversation!!.subject == null || conversation!!.subject!!.trim().isEmpty()) {
            subjectView.setText(R.string.no_subject)
        } else {
            subjectView.text = conversation!!.subject
        }

        starred.setImageResource(if (conversation!!.isStarred) R.drawable.ic_star_filled else R.drawable.ic_star_outline)
        ColorUtils.colorIt(ThemePrefs.brandColor, starred.drawable)

        val menu = toolbar.menu
        // we don't want the archive option when it is in the sent folder, we've already toggled the visibility of those in initToolbar
        val isArchived = conversation!!.workflowState == Conversation.WorkflowState.ARCHIVED
        if(conversationScope == null || !conversationScope.equals("sent")) {
            menu.findItem(R.id.archive).isVisible = !isArchived
            menu.findItem(R.id.unarchive).isVisible = isArchived
        }

        // Set theme after menu changes, otherwise menu icons may retain original tint
        val textColor = ThemePrefs.primaryTextColor
        ToolbarColorizeHelper.colorizeToolbar(toolbar, textColor, requireActivity())
    }

    private fun initConversationDetails() = with(binding) {
        if (conversation!!.subject == null || conversation!!.subject!!.isBlank() || conversation!!.subject!!.trim { it <= ' ' }.isEmpty()) {
            subjectView.setText(R.string.no_subject)
        } else {
            subjectView.text = conversation!!.subject
        }

        starred.setImageResource(if (conversation!!.isStarred) R.drawable.ic_star_filled else R.drawable.ic_star_outline)
        ColorUtils.colorIt(ThemePrefs.brandColor, starred.drawable)

        val menu = toolbar.menu
        // We don't want the archive option when it is in the sent folder, we've already toggled the visibility of those in initToolbar
        val isArchived = conversation!!.workflowState == Conversation.WorkflowState.ARCHIVED
        if (conversationScope == null || conversationScope != "sent") {
            menu.findItem(R.id.archive).isVisible = !isArchived
            menu.findItem(R.id.unarchive).isVisible = isArchived
        }

        // Set theme after menu changes, otherwise menu icons may retain original tint
        val textColor = ThemePrefs.primaryTextColor
        ToolbarColorizeHelper.colorizeToolbar(toolbar, textColor, requireActivity())
    }

    override fun createAdapter(): MessageAdapter {
        return MessageAdapter(requireContext(), presenter, conversation!!, adapterCallback)
    }

    private fun replyAllMessage() {
        val args = AddMessageFragment.createBundle(
            isReply = true,
            conversation = conversation!!,
            participants = presenter.participants.map { Recipient.from(it) },
            messages = presenter.getMessageChainForMessage(null),
            currentMessage = null
        )
        RouteMatcher.route(requireActivity(), Route(AddMessageFragment::class.java, null, args))
    }

    // Same as reply all but scoped to a message
    private fun replyAllMessage(message: Message) {
        val args = AddMessageFragment.createBundle(
            isReply = true,
            conversation = conversation!!,
            participants = getMessageRecipientsForReplyAll(message).map { Recipient.from(it) },
            messages = presenter.getMessageChainForMessage(null),
            currentMessage = message
        )
        RouteMatcher.route(requireActivity(), Route(AddMessageFragment::class.java, null, args))
    }

    private fun addMessage(message: Message, isReply: Boolean) {
        val args = AddMessageFragment.createBundle(
            isReply = isReply,
            conversation = conversation!!,
            participants = getMessageRecipientsForReply(message).map { Recipient.from(it) },
            messages = presenter.getMessageChainForMessage(message),
            currentMessage = message
        )
        RouteMatcher.route(requireActivity(), Route(AddMessageFragment::class.java, null, args))
    }

    private fun getMessageRecipientsForReplyAll(message: Message): ArrayList<BasicUser> {
        return ArrayList(message.participatingUserIds
                // Map the conversations participating users to the messages participating users
                .mapNotNull { participatingUserId ->
                    presenter.participants.find { basicUser ->
                        basicUser.id == participatingUserId
                    }
                })

    }

    private fun getMessageRecipientsForReply(message: Message): ArrayList<BasicUser>  {
        // If the author is self, we default to all other participants
        return if (message.authorId == ApiPrefs.user!!.id) {
            presenter.participants
        } else {
            arrayListOf((presenter.participants.first { it.id == message.authorId }))
        }
    }

    override val recyclerView: RecyclerView get() = swipeRefreshLayoutContainerBinding.recyclerView

    override fun onRefreshFinished() {
        swipeRefreshLayoutContainerBinding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onRefreshStarted() {
        swipeRefreshLayoutContainerBinding.emptyPandaView.setLoading()
    }

    override fun checkIfEmpty() {
        RecyclerViewUtils.checkIfEmpty(
            swipeRefreshLayoutContainerBinding.emptyPandaView,
            recyclerView,
            swipeRefreshLayoutContainerBinding.swipeRefreshLayout,
            adapter,
            presenter.isEmpty
        )
    }

    override fun refreshConversationData() {
        initConversationDetails()
    }

    override fun onConversationDeleted() {
        EventBus.getDefault().postSticky(ConversationDeletedEvent(TeacherInboxRouter::class.java.simpleName))

        // Only go back a screen on phones
        if (!isTablet) {
            requireActivity().onBackPressed()
        }
    }

    override fun onConversationMarkedAsUnread() {
        EventBus.getDefault().postSticky(ConversationUpdatedEvent(conversation!!, InboxApi.Scope.UNREAD, null))

        // Only go back a screen on phones
        if (!isTablet) {
            requireActivity().onBackPressed()
        }
    }

    override fun onConversationRead() {
        EventBus.getDefault().postSticky(ConversationUpdatedEvent(conversation!!, InboxApi.Scope.UNREAD, null))
    }

    override fun onMessageDeleted() {
        // Update the thread so the reply button is at the top thread
        presenter.refresh(true)
    }

    override fun onConversationArchived() {
        EventBus.getDefault().postSticky(ConversationUpdatedEvent(conversation!!, InboxApi.Scope.ARCHIVED, null))

        // Only go back a screen on phones
        if (!isTablet) {
            requireActivity().onBackPressed()
        }
    }

    override fun onConversationStarred() {
        EventBus.getDefault().postSticky(ConversationUpdatedEvent(conversation!!, InboxApi.Scope.STARRED, null))
    }

    override fun onConversationLoadFailed() {
        toast(R.string.errorOccurred)
        activity?.onBackPressed()
    }

    override fun showUserMessage(userMessageResId: Int) {
        showToast(userMessageResId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RequestCodes.COMPOSE_MESSAGE && resultCode == RESULT_OK) {
            if (presenter != null) {
                swipeRefreshLayoutContainerBinding.swipeRefreshLayout.isRefreshing = true
                presenter.refresh(true)
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onMessageEdited(event: MessageAddedEvent) {
        event.once(javaClass.simpleName + conversation!!.id + "_" + conversation!!.messageCount) { aBoolean ->
            if (aBoolean) {
                swipeRefreshLayoutContainerBinding.swipeRefreshLayout.isRefreshing = true
                presenter.refresh(true)
            }
        }
    }

    override fun perPageCount(): Int = ApiPrefs.perPageCount

    companion object {
        fun createBundle(conversation: Conversation, scope: String): Bundle =
                Bundle().apply {
                    putParcelable(Const.CONVERSATION, conversation)
                    putString(Const.SCOPE, scope)
                }

        fun createBundle(conversationId: Long): Bundle =
                Bundle().apply {
                    // For when we get a push notification for a new message in a conversation
                    putLong(Const.CONVERSATION_ID, conversationId)
                }

        fun newInstance(bundle: Bundle): MessageThreadFragment =
                MessageThreadFragment().apply {
                    arguments = bundle
                }
    }
}
