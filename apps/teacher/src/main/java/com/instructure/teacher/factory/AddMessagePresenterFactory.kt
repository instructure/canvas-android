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
package com.instructure.teacher.factory

import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.teacher.presenters.AddMessagePresenter
import instructure.androidblueprint.PresenterFactory
import java.util.*

class AddMessagePresenterFactory(
    private val mConversation: Conversation?,
    private val mParticipants: ArrayList<BasicUser>?,
    private val mMessages: ArrayList<Message>?,
    private val mIsReply: Boolean
) : PresenterFactory<AddMessagePresenter?> {
    override fun create() = AddMessagePresenter(mConversation, mParticipants, mMessages, mIsReply)
}
