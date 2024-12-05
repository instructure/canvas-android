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

package com.instructure.teacher.presenters

import androidx.recyclerview.widget.SortedList
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.RecipientManager
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.pandautils.blueprint.SyncPresenter
import com.instructure.teacher.viewinterface.ChooseRecipientsView
import retrofit2.Response
import java.util.Stack


class ChooseRecipientsPresenter(rootContextId: String) : SyncPresenter<Recipient, ChooseRecipientsView>(Recipient::class.java) {

    private val selectedRecipients = HashSet<Recipient>()

    private val mBackStack = Stack<StackEntry>()

    private val mRecipientCallback = object : StatusCallback<List<Recipient>>() {
        override fun onResponse(response: Response<List<Recipient>>, linkHeaders: LinkHeaders, type: ApiType) {
            var idx: Int
            data.beginBatchedUpdates()
            for (recipient in response.body()!!) {
                idx = data.indexOf(recipient)
                if (idx == SortedList.INVALID_POSITION) {
                    data.add(recipient)
                }
            }
            data.endBatchedUpdates()
        }

        override fun onFinished(type: ApiType) {
            if (viewCallback != null) {
                viewCallback!!.onRefreshFinished()
                viewCallback!!.checkIfEmpty()
            }
        }
    }

    private val canvasContextId: String?
        get() = mBackStack.peek().recipientGroup.stringId

    val recipients: ArrayList<Recipient>
        get() = ArrayList(selectedRecipients)

    fun clearBackStack() {
        mBackStack.clear()
    }

    fun popBackStack(): Boolean {
        if (mBackStack.size > 1) {
            mBackStack.pop()
            refresh(false)
            return true
        }
        return false
    }

    private class StackEntry(var recipientGroup: Recipient)

    init {

        // Create root recipient group, add to back stack
        val rootContextRecipient = Recipient(
                stringId = rootContextId
        )
        mBackStack.add(StackEntry(rootContextRecipient))
    }

    override fun loadData(forceNetwork: Boolean) {
        if (data.size() == 0) {
            RecipientManager.searchAllRecipients(forceNetwork, null, canvasContextId!!, mRecipientCallback)
        }
    }

    override fun refresh(forceNetwork: Boolean) {
        onRefreshStarted()
        mRecipientCallback.reset()
        clearData()
        loadData(forceNetwork)
    }

    fun setContextRecipient(recipient: Recipient) {
        mBackStack.add(StackEntry(recipient))
        refresh(false)
    }

    fun isRecipientSelected(recipient: Recipient): Boolean = selectedRecipients.contains(recipient)

    fun addAlreadySelectedRecipients(recipients: ArrayList<Recipient>) {
        selectedRecipients.addAll(recipients)
    }

    fun addOrRemoveRecipient(recipient: Recipient) {
        if (!selectedRecipients.add(recipient)) {
            selectedRecipients.remove(recipient)
        }
    }

    override fun areItemsTheSame(item1: Recipient, item2: Recipient): Boolean {
        return item1.stringId == item2.stringId
    }
}
