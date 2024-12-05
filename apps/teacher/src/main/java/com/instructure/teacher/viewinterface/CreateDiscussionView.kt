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
package com.instructure.teacher.viewinterface

import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.User
import com.instructure.teacher.interfaces.RceMediaUploadView
import com.instructure.pandautils.blueprint.FragmentViewInterface

interface CreateDiscussionView : FragmentViewInterface, RceMediaUploadView {
    fun startSavingDiscussion()
    fun errorSavingDiscussion()
    fun discussionSavedSuccessfully(discussionTopic: DiscussionTopicHeader?)
    fun updateDueDateGroups(groups: HashMap<Long, Group>, sections: HashMap<Long, Section>, students: HashMap<Long, User>)
    fun errorOccurred()
    fun updatedAssignment()
    fun discussionDeletedSuccessfully(discussionTopicHeaderId: Long)
}