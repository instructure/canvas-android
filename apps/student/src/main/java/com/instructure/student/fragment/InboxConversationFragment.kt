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

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkManager
import com.instructure.canvasapi2.managers.InboxManager
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.analytics.SCREEN_VIEW_INBOX_CONVERSATION
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.file.download.FileDownloadWorker
import com.instructure.pandautils.utils.ColorUtils
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ConversationUpdatedEvent
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.NullableStringArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ToolbarColorizeHelper
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.getDrawableCompat
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.R
import com.instructure.student.adapter.InboxConversationAdapter
import com.instructure.student.databinding.FragmentInboxConversationBinding
import com.instructure.student.databinding.PandaRecyclerRefreshLayoutBinding
import com.instructure.student.events.MessageAddedEvent
import com.instructure.student.interfaces.MessageAdapterCallback
import com.instructure.student.router.RouteMatcher
import com.instructure.student.view.AttachmentView
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_INBOX_CONVERSATION)
@PageView(url = "conversations")
@AndroidEntryPoint
class InboxConversationFragment : ParentFragment() {

    @Inject
    lateinit var workManager: WorkManager

    private val binding by viewBinding(FragmentInboxConversationBinding::bind)
    private lateinit var recyclerBinding: PandaRecyclerRefreshLayoutBinding

    private var scope by NullableStringArg(Const.SCOPE)
    private var conversation by ParcelableArg<Conversation>(key = Const.CONVERSATION)
    private var conversationId by LongArg(0L, Const.CONVERSATION_ID)

    private var conversationCall: WeaveJob? = null
    private var starCall: WeaveJob? = null
    private var archiveCall: WeaveJob? = null
    private var deleteConversationCall: WeaveJob? = null
    private var deleteMessageCall: WeaveJob? = null
    private var unreadCall: WeaveJob? = null

    private val adapter: InboxConversationAdapter by lazy {
        InboxConversationAdapter(requireContext(), conversation, mAdapterCallback)
    }

    private val menuListener = Toolbar.OnMenuItemClickListener { item ->
        when (item.itemId) {
            R.id.archive, R.id.unarchive -> toggleArchived()
            R.id.reply -> addMessage(adapter.topMessage, true)
            R.id.replyAll -> replyAllMessage()
            R.id.markAsUnread -> markConversationUnread()
            R.id.forward -> addMessage(adapter.forwardMessage, false)
            R.id.delete -> {
                val dialog = AlertDialog.Builder(requireContext())
                        .setMessage(R.string.confirmDeleteConversation)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.delete) { _, _ -> deleteConversation() }
                        .create()

                dialog.setOnShowListener {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
                }

                dialog.show()
            }
            else -> return@OnMenuItemClickListener false
        }
        true
    }

    private val mAdapterCallback = object : MessageAdapterCallback {
        override fun onAvatarClicked(user: BasicUser) = Unit

        override fun onAttachmentClicked(action: AttachmentView.AttachmentAction, attachment: Attachment) {
            when (action) {
                AttachmentView.AttachmentAction.REMOVE -> Unit // Do nothing

                AttachmentView.AttachmentAction.PREVIEW -> openMedia(attachment.contentType, attachment.url, attachment.filename, attachment.id.toString(), ApiPrefs.user!!)

                AttachmentView.AttachmentAction.DOWNLOAD -> {
                    if (PermissionUtils.hasPermissions(requireActivity(), PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                        workManager.enqueue(FileDownloadWorker.createOneTimeWorkRequest(attachment.displayName.orEmpty(), attachment.url.orEmpty()))
                    } else {
                        requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE), PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE)
                    }
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
                            .setMessage(R.string.confirmDeleteMessage)
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(R.string.delete) { _, _ -> deleteMessage(message) }
                            .create()

                    dialog.setOnShowListener {
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
                        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
                    }

                    dialog.show()
                }
            }
        }

        override fun getParticipantById(id: Long): BasicUser? = adapter.participants[id]

        override fun onRefreshFinished() {
            setRefreshing(false)
        }
    }

    override fun title(): String = getString(R.string.inbox)

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_inbox_conversation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerBinding = PandaRecyclerRefreshLayoutBinding.bind(binding.root)
        when {
        // Setup from conversation ID
            conversationId != 0L -> {
                conversationCall = tryWeave {
                    conversation = awaitApi { InboxManager.getConversation(conversationId, true, it) }
                    conversationId = 0L
                    setupViews()
                } catch {
                    it.cause?.printStackTrace()
                    toast(R.string.errorConversationGeneric)
                    requireActivity().onBackPressed()
                }
            }
        // Set up from conversation object
            else -> {
                setupViews()
            }
        }
    }

    override fun onDestroy() {
        starCall?.cancel()
        archiveCall?.cancel()
        deleteConversationCall?.cancel()
        deleteMessageCall?.cancel()
        unreadCall?.cancel()
        conversationCall?.cancel()
        super.onDestroy()
    }

    override fun applyTheme() {
        ViewStyler.themeToolbarColored(requireActivity(), binding.toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
    }

    private fun setupViews() {
        initToolbar()
        initConversationDetails()
        initAdapter()
    }

    private fun initAdapter() {
        configureRecyclerView(requireView(), requireContext(), adapter, R.id.swipeRefreshLayout, R.id.emptyView, R.id.listView)
        val dividerItemDecoration = DividerItemDecoration(
            recyclerBinding.listView.context,
            LinearLayoutManager.VERTICAL
        )
        dividerItemDecoration.setDrawable(requireContext().getDrawableCompat(R.drawable.item_decorator_gray))
        recyclerBinding.listView.addItemDecoration(dividerItemDecoration)
    }

    private fun initToolbar() = with(binding) {
        toolbar.setupAsBackButton(this@InboxConversationFragment)
        toolbar.setTitle(R.string.message)
        toolbar.inflateMenu(R.menu.message_thread)

        if ("sent" == scope) {
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

        toolbar.menu.findItem(R.id.reply)?.isVisible = !conversation.cannotReply
        toolbar.menu.findItem(R.id.replyAll)?.isVisible = !conversation.cannotReply

        toolbar.setOnMenuItemClickListener(menuListener)
    }

    private fun initConversationDetails() = with(binding) {
        val conversation = conversation

        if (conversation.subject == null || conversation.subject?.trim { it <= ' ' }?.isEmpty() == true) {
            subjectView.setText(R.string.noSubject)
        } else {
            subjectView.text = conversation.subject
        }

        starred.setOnClickListener { toggleStarred() }
        starred.setImageResource(if (conversation.isStarred) R.drawable.ic_star_filled else R.drawable.ic_star_outline)
        ColorUtils.colorIt(ThemePrefs.brandColor, starred.drawable)
        starred.alpha = 1f
        starred.isEnabled = true

        val menu = toolbar.menu
        // We don't want the archive option when it is in the sent folder, we've already toggled the visibility of those in initToolbar
        val isArchived = conversation.workflowState == Conversation.WorkflowState.ARCHIVED
        if (scope == null || scope != "sent") {
            menu.findItem(R.id.archive).isVisible = !isArchived
            menu.findItem(R.id.unarchive).isVisible = isArchived
        }

        // Set theme after menu changes, otherwise menu icons may retain original tint
        val textColor = ThemePrefs.primaryTextColor
        ToolbarColorizeHelper.colorizeToolbar(toolbar, textColor, requireActivity())
    }

    private fun toggleStarred() = with(binding) {
        starCall?.cancel()
        val shouldStar = !conversation.isStarred
        tryWeave {
            starred.setImageResource(if (shouldStar) R.drawable.ic_star_filled else R.drawable.ic_star_outline)
            ColorUtils.colorIt(ThemePrefs.brandColor, starred.drawable)
            starred.alpha = 0.35f
            starred.isEnabled = false
            awaitApi<Conversation> { InboxManager.starConversation(conversation.id, shouldStar, conversation.workflowState!!, it) }
            conversation.isStarred = shouldStar
            refreshConversationData()
            onConversationUpdated(false)
        } catch {
            toast(R.string.errorConversationGeneric)
            starred.setImageResource(if (!shouldStar) R.drawable.ic_star_filled else R.drawable.ic_star_outline)
            ColorUtils.colorIt(ThemePrefs.brandColor, starred.drawable)
            refreshConversationData()
        }
    }

    private fun toggleArchived() {
        archiveCall?.cancel()
        val archive = conversation.workflowState != Conversation.WorkflowState.ARCHIVED
        archiveCall = tryWeave {
            awaitApi<Conversation> { InboxManager.archiveConversation(conversation.id, archive, it) }
            toast(if (archive) R.string.conversationArchived else R.string.conversationUnarchived)
            onConversationUpdated(true)
        } catch {
            toast(R.string.errorConversationGeneric)
        }
    }

    private fun deleteConversation() {
        deleteConversationCall?.cancel()
        deleteConversationCall = tryWeave {
            awaitApi<Conversation> { InboxManager.deleteConversation(conversation.id, it) }
            toast(R.string.deleted)
            onConversationUpdated(true)
        } catch {
            toast(R.string.errorConversationGeneric)
        }
    }

    private fun markConversationUnread() {
        unreadCall?.cancel()
        unreadCall = tryWeave {
            awaitApi<Void> { InboxManager.markConversationAsUnread(conversation.id, it) }
            onConversationUpdated(true)
        } catch {
            toast(R.string.errorConversationGeneric)
        }
    }

    private fun deleteMessage(message: Message) {
        deleteMessageCall?.cancel()
        deleteMessageCall = tryWeave {
            awaitApi<Conversation> { InboxManager.deleteMessages(conversation.id, listOf(message.id), it) }
            adapter.remove(message)
            if (adapter.size() > 0) {
                toast(R.string.deleted)
                onConversationUpdated(false)
            } else {
                onConversationUpdated(true)
            }
        } catch {
            toast(R.string.errorConversationGeneric)
        }
    }

    private fun replyAllMessage() {
        val users = if (adapter.participants.size == 1) {
            adapter.participants.values
        } else {
            adapter.participants.values.filter { it.id != ApiPrefs.user?.id }
        }
        val route = InboxComposeMessageFragment.makeRoute(
                true,
                conversation,
                users.map { Recipient.from(it) },
                longArrayOf(),
                null)
        RouteMatcher.route(requireActivity(), route)
    }

    // Same as reply all but scoped to a message
    private fun replyAllMessage(message: Message) {
        val route = InboxComposeMessageFragment.makeRoute(
                true,
                conversation,
                getMessageRecipientsForReplyAll(message).map { Recipient.from(it) },
                longArrayOf(),
                message)
        RouteMatcher.route(requireActivity(), route)
    }

    private fun addMessage(message: Message, isReply: Boolean) {
        val route = InboxComposeMessageFragment.makeRoute(
                isReply,
                conversation,
                getMessageRecipientsForReply(message).map { Recipient.from(it) },
                adapter.getMessageChainIdsForMessage(message),
                message)
        RouteMatcher.route(requireActivity(), route)
    }

    private fun getMessageRecipientsForReplyAll(message: Message): List<BasicUser> {
        val userIds = if (message.participatingUserIds.size == 1) {
            message.participatingUserIds
        } else {
            message.participatingUserIds.filter { it != ApiPrefs.user?.id }
        }
        return userIds
            // Map the conversations participating users to the messages participating users
            .mapNotNull { participatingUserId ->
                adapter.participants.values.find { basicUser ->
                    basicUser.id == participatingUserId
                }
            }
    }

    private fun getMessageRecipientsForReply(message: Message): List<BasicUser>  {
        // If the author is self, we default to all other participants
        return if (message.authorId == ApiPrefs.user!!.id) {
            if (adapter.participants.size == 1) {
                adapter.participants.values.toList()
            } else {
                adapter.participants.values.filter { it.id != ApiPrefs.user?.id }
            }
        } else {
            listOf(adapter.participants.values.first { it.id == message.authorId })
        }
    }

    private fun refreshConversationData() {
        if (view != null) initConversationDetails()
    }

    private fun onConversationUpdated(goBack: Boolean) {
        EventBus.getDefault().postSticky(ConversationUpdatedEvent(conversation))
        if (goBack) requireActivity().onBackPressed()
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onMessageChanged(event: MessageAddedEvent) {
        event.once(javaClass.simpleName + conversation.id + "_" + conversation.messageCount) { shouldUpdate ->
            if (shouldUpdate) adapter.refresh()
        }
    }

    companion object {

        fun makeRoute(conversation: Conversation, scope: String?): Route {
            val bundle = Bundle().apply {
                putParcelable(Const.CONVERSATION, conversation)
                putString(Const.SCOPE, scope)
            }
            return Route(null, InboxConversationFragment::class.java, null, bundle)
        }

        fun makeRoute(conversationId: Long): Route {
            val bundle = Bundle().apply { putLong(Const.CONVERSATION_ID, conversationId) }
            return Route(null, InboxConversationFragment::class.java, null, bundle)
        }

        fun validateRoute(route: Route): Boolean {
            return route.arguments.containsKey(Const.CONVERSATION)
                    || route.arguments.containsKey(Const.CONVERSATION_ID)
                    || route.paramsHash.containsKey(RouterParams.CONVERSATION_ID)
        }

        fun newInstance(route: Route) : InboxConversationFragment? {
            if (!validateRoute(route)) return null
            route.paramsHash[RouterParams.CONVERSATION_ID]?.let {
                route.arguments.putLong(Const.CONVERSATION_ID, it.toLong())
            }
            return InboxConversationFragment().withArgs(route.arguments)
        }

    }
}
