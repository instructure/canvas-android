/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
package com.instructure.teacher.features.inbox.list

import androidx.appcompat.widget.Toolbar
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.Conversation
import com.instructure.pandautils.features.inbox.list.InboxRouter

class TeacherInboxRouter : InboxRouter {

    override fun openConversation(conversation: Conversation, scope: InboxApi.Scope) {

    }

    override fun attachNavigationIcon(toolbar: Toolbar) {

    }

    override fun routeToNewMessage() {

    }
}