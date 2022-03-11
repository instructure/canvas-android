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
package com.instructure.student.adapter

import android.content.Context
import android.view.View
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.RecipientManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.weave.*
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.toast
import com.instructure.student.R
import com.instructure.student.holders.RecipientViewHolder
import com.instructure.student.interfaces.AdapterToFragmentCallback
import java.util.*


open class InboxRecipientAdapter(
    context: Context,
    private val canvasContext: CanvasContext,
    private val selectedRecipients: HashSet<Recipient>,
    private val adapterToFragmentCallback: AdapterToFragmentCallback<Recipient>
) : BaseListRecyclerAdapter<Recipient, RecipientViewHolder>(context, Recipient::class.java) {

    private data class StackEntry(val recipientGroup: Recipient)

    private val backStack = Stack<StackEntry>()

    private var recipientCall: WeaveJob? = null

    private var canSendMessagesAll = false

    private val adapterCallback = { recipient: Recipient, position: Int, isCheckbox: Boolean ->
        when (recipient.recipientType) {
        // Select and deselect individuals.
            Recipient.Type.Person -> {
                addOrRemoveRecipient(recipient, position)
                notifyItemChanged(position)
            }

        // Always go to a metagroup - Canvas won't let you send a message to an entire metagroup
            Recipient.Type.Metagroup -> setContextRecipient(recipient)

        // If it's a group, make sure there are actually users in that group.
            Recipient.Type.Group ->
                if (recipient.userCount > 0) {
                    if (isCheckbox) {
                        addOrRemoveRecipient(recipient, position)
                    } else {
                        // Filter down to the group
                        if (selectedRecipients.contains(recipient)) {
                            context.toast(R.string.entireGroupSelected)
                        } else {
                            setContextRecipient(recipient)
                        }
                    }
                } else {
                    context.toast(R.string.noUsersInGroup)
                }

            else -> {}
        }
    }

    init {
        itemCallback = inboxItemCallback

        // Create root recipient group, add to back stack
        val rootContextRecipient = Recipient(
                stringId = canvasContext.contextId
        )

        backStack.add(StackEntry(rootContextRecipient))

        loadData()
    }

    private fun addOrRemoveRecipient(recipient: Recipient, position: Int) {
        if (!selectedRecipients.add(recipient)) {
            selectedRecipients.remove(recipient)
        }
        notifyItemChanged(position, recipient)
    }

    private fun setContextRecipient(recipient: Recipient) {
        backStack.add(StackEntry(recipient))
        refresh()
    }

    fun clearBackStack() {
        backStack.clear()
    }

    fun popBackStack(): Boolean {
        if (backStack.size > 1) {
            backStack.pop()
            refresh()
            return true
        }
        return false
    }

    override val isPaginated get() = true

    override fun loadFirstPage() {
        recipientCall?.cancel()
        recipientCall = tryWeave {
            if (canvasContext.type == CanvasContext.Type.COURSE) {
                canSendMessagesAll = awaitApi<CanvasContextPermission> {
                    CourseManager.getCoursePermissions(
                        canvasContext.id,
                        listOf(CanvasContextPermission.SEND_MESSAGES_ALL),
                        it,
                        true
                    )
                }.send_messages_all
            }
            awaitPaginated<List<Recipient>> {
                onRequest { RecipientManager.searchRecipients(null, backStack.peek().recipientGroup.stringId!!, it) }
                onResponse {
                    setNextUrl("")
                    addAll(it)
                }
                onComplete {
                    setNextUrl(null)
                    adapterToRecyclerViewCallback.setIsEmpty(size() == 0)
                    adapterToFragmentCallback.onRefreshFinished()
                }
            }
        } catch {
            it.cause?.printStackTrace()
            adapterToFragmentCallback.onRefreshFinished()
        }
    }

    override fun loadNextPage(nextURL: String) {
        recipientCall?.next()
    }

    override fun resetData() {
        recipientCall?.cancel()
        super.resetData()
    }

    fun getRecipients(): List<Recipient> = selectedRecipients.toList()

    override fun bindHolder(recipient: Recipient, holder: RecipientViewHolder, position: Int) {
        holder.bind(
            context,
            holder,
            recipient,
            adapterCallback,
            ThemePrefs.brandColor,
            selectedRecipients.contains(recipient),
            canSendMessagesAll
        )
    }

    override fun createViewHolder(v: View, viewType: Int): RecipientViewHolder {
        return RecipientViewHolder(v)
    }

    override fun itemLayoutResId(viewType: Int): Int {
        return RecipientViewHolder.HOLDER_RES_ID
    }

    override fun cancel() {
        recipientCall?.cancel()
    }

    companion object {

        // Decoupled for testability
        val inboxItemCallback = object : BaseListRecyclerAdapter.ItemComparableCallback<Recipient>() {

            val comparator = compareBy<Recipient>(
                { it.recipientType.ordinal }, // Compare types, should sort by group > metagroup > person
                { it.name!!.lowercase(Locale.getDefault()) }, // Compare by name
                { it.stringId } // Compare by id
            )

            override fun areContentsTheSame(oldItem: Recipient, newItem: Recipient): Boolean {
                return compare(newItem, oldItem) == 0
            }

            override fun getUniqueItemId(recipient: Recipient): Long {
                return recipient.stringId!!.hashCode().toLong()
            }

            override fun compare(o1: Recipient, o2: Recipient): Int {
                return comparator.compare(o1, o2)
            }

            override fun areItemsTheSame(item1: Recipient, item2: Recipient): Boolean {
                return item1.stringId == item2.stringId
            }
        }
    }

}
