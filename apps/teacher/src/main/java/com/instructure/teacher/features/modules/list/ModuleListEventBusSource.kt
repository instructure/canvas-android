/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.teacher.features.modules.list

import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.pandautils.utils.FileFolderDeletedEvent
import com.instructure.pandautils.utils.FileFolderUpdatedEvent
import com.instructure.teacher.events.AssignmentDeletedEvent
import com.instructure.teacher.events.AssignmentUpdatedEvent
import com.instructure.teacher.events.DiscussionTopicHeaderDeletedEvent
import com.instructure.teacher.events.DiscussionUpdatedEvent
import com.instructure.teacher.events.PageDeletedEvent
import com.instructure.teacher.events.PageUpdatedEvent
import com.instructure.teacher.events.QuizUpdatedEvent
import com.instructure.teacher.mobius.common.EventBusSource
import org.greenrobot.eventbus.Subscribe

@Suppress("unused")
class ModuleListEventBusSource : EventBusSource<ModuleListEvent>() {

    private val subId = ModuleListEventBusSource::class.java.name

    @Subscribe(sticky = true)
    fun onAssignmentEdited(event: AssignmentUpdatedEvent) {
        event.once(subId) { assignmentId -> updateModuleItem("Assignment") { it.contentId == assignmentId } }
    }

    @Subscribe(sticky = true)
    fun onAssignmentDeleted(event: AssignmentDeletedEvent) {
        event.once(subId) { assignmentId -> deleteModuleItem("Assignment") { it.contentId == assignmentId } }
    }

    @Subscribe(sticky = true)
    fun onDiscussionUpdate(event: DiscussionUpdatedEvent) {
        event.once(subId) { discussion -> updateModuleItem("Discussion") { it.contentId == discussion.id } }
    }

    @Subscribe(sticky = true)
    fun onDiscussionDeleted(event: DiscussionTopicHeaderDeletedEvent) {
        event.once(subId) { discussionId -> deleteModuleItem("Discussion") { it.contentId == discussionId } }
    }

    @Subscribe(sticky = true)
    fun onFileUpdated(event: FileFolderUpdatedEvent) {
        event.once(subId) { file -> updateModuleItem("File") { it.contentId == file.id } }
    }

    @Subscribe(sticky = true)
    fun onFileDeleted(event: FileFolderDeletedEvent) {
        event.once(subId) { file -> deleteModuleItem("File") { it.contentId == file.id } }
    }

    @Subscribe(sticky = true)
    fun onPageUpdated(event : PageUpdatedEvent) {
        event.once(subId) { page ->
            /* The module API does not expose the page ID in module items, so we must use the page URL to identify
            which page was updated. Unfortunately that URL is based on the page name, so if the page name was
            updated then we have no way to uniquely identify and update that page. */
            updateModuleItem("Page") { it.pageUrl == page.url }
        }
    }

    @Subscribe(sticky = true)
    fun onPageDeleted(event : PageDeletedEvent) {
        event.once(subId) { page -> deleteModuleItem("Page") { it.pageUrl == page.url } }
    }

    @Subscribe(sticky = true)
    fun onQuizUpdated(event: QuizUpdatedEvent) {
        event.once(subId) { quizId -> updateModuleItem("Quiz") { it.contentId == quizId } }
    }

    private fun updateModuleItem(type: String, predicate: (item: ModuleItem) -> Boolean) {
        sendEvent(ModuleListEvent.ItemRefreshRequested(type, predicate))
    }

    private fun deleteModuleItem(type: String, predicate: (item: ModuleItem) -> Boolean) {
        sendEvent(ModuleListEvent.RemoveModuleItems(type, predicate))
    }

}
