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
package com.emeritus.student.interfaces

import com.emeritus.student.R
import com.emeritus.student.view.AttachmentView
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.models.Message

interface MessageAdapterCallback {

    enum class MessageClickAction(val labelResId: Int) {
        REPLY(R.string.reply),
        REPLY_ALL(R.string.replyAll),
        FORWARD(R.string.forward),
        DELETE(R.string.delete)
    }

    fun onAvatarClicked(user: BasicUser)
    fun onAttachmentClicked(action: AttachmentView.AttachmentAction, attachment: Attachment)
    fun onMessageAction(action: MessageClickAction, message: Message)
    fun getParticipantById(id: Long): BasicUser?
    fun onRefreshFinished()
}
