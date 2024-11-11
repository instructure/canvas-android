/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
package com.instructure.student.features.modules.util

import android.content.Context
import android.net.Uri
import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.APIHelper.expandTildeId
import com.instructure.canvasapi2.utils.findWithPrevious
import com.instructure.canvasapi2.utils.isLocked
import com.instructure.interactions.router.Route
import com.instructure.pandautils.features.discussion.details.DiscussionDetailsWebViewFragment
import com.instructure.student.R
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsFragment
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsFragment.Companion.makeRoute
import com.instructure.student.features.discussion.details.DiscussionDetailsFragment
import com.instructure.student.features.discussion.details.DiscussionDetailsFragment.Companion.makeRoute
import com.instructure.student.features.files.details.FileDetailsFragment
import com.instructure.student.features.modules.progression.LockedModuleItemFragment
import com.instructure.student.features.modules.progression.ModuleQuizDecider
import com.instructure.student.features.modules.progression.NotAvailableOfflineFragment
import com.instructure.student.features.pages.details.PageDetailsFragment
import com.instructure.student.fragment.InternalWebviewFragment
import com.instructure.student.fragment.InternalWebviewFragment.Companion.makeRoute
import com.instructure.student.fragment.MasteryPathSelectionFragment
import com.instructure.student.fragment.MasteryPathSelectionFragment.Companion.makeRoute
import java.util.Date

object ModuleUtility {
    fun getFragment(
        item: ModuleItem,
        course: Course,
        moduleObject: ModuleObject?,
        navigatedFromModules: Boolean,
        isOnline: Boolean,
        syncedTabs: Set<String>,
        syncedFileIds: List<Long>,
        context: Context
    ): Fragment? = when (item.type) {
        "Page" -> PageDetailsFragment.newInstance(PageDetailsFragment.makeRoute(course, item.title, item.pageUrl, navigatedFromModules))
        "Assignment" -> {
            createFragmentWithOfflineCheck(isOnline, course, item, syncedTabs, context, setOf(Tab.ASSIGNMENTS_ID, Tab.GRADES_ID, Tab.SYLLABUS_ID)) {
                AssignmentDetailsFragment.newInstance(makeRoute(course, getAssignmentId(item)))
            }
        }
        "Discussion" -> {
            createFragmentWithOfflineCheck(isOnline, course, item, syncedTabs, context, setOf(Tab.DISCUSSIONS_ID)) {
                if (isOnline) {
                    DiscussionDetailsWebViewFragment.newInstance(getDiscussionRedesignRoute(item, course))
                } else {
                    DiscussionDetailsFragment.newInstance(getDiscussionRoute(item, course))
                }
            }
        }
        "Locked" -> LockedModuleItemFragment.newInstance(LockedModuleItemFragment.makeRoute(course, item.title!!, item.moduleDetails?.lockExplanation ?: ""))
        "SubHeader" -> null // Don't do anything with headers, they're just dividers so we don't show them here.
        "Quiz" -> {
            val apiURL = removeDomain(item.url)
            ModuleQuizDecider.newInstance(ModuleQuizDecider.makeRoute(course, item.htmlUrl!!, apiURL!!, item.contentId))
        }
        "ChooseAssignmentGroup" -> {
            createFragmentWithOfflineCheck(isOnline, course, item, syncedTabs, context) {
                val route = makeRoute(course, item.masteryPaths!!, moduleObject!!.id, item.masteryPathsItemId)
                MasteryPathSelectionFragment.newInstance(route)
            }
        }
        "ExternalUrl", "ExternalTool" -> {
            if (item.isLocked()) {
                LockedModuleItemFragment.newInstance(LockedModuleItemFragment.makeRoute(course, item.title!!, item.moduleDetails?.lockExplanation ?: ""))
            } else {
                createFragmentWithOfflineCheck(isOnline, course, item, syncedTabs, context) {
                    val uri = Uri.parse(item.htmlUrl).buildUpon().appendQueryParameter("display", "borderless").build()
                    val route = makeRoute(course, uri.toString(), item.title!!, true, true, true)
                    InternalWebviewFragment.newInstance(route)
                }
            }
        }
        "File" -> { // TODO Handle offline availability after files sync
            createFileDetailsFragmentWithOfflineCheck(isOnline, course, item, moduleObject, syncedFileIds, context)
        }
        else -> null
    }

    private fun createFragmentWithOfflineCheck(
        isOnline: Boolean,
        course: Course,
        item: ModuleItem,
        syncedTabs: Set<String>,
        context: Context,
        tabs: Set<String> = emptySet(),
        creationBlock: () -> Fragment?
    ): Fragment? {
        return if (isOnline || tabs.any { syncedTabs.contains(it) }) {
            creationBlock()
        } else {
            val descriptionResource = if (tabs.isEmpty()) R.string.notAvailableOfflineDescription else R.string.notAvailableOfflineDescriptionForTabs
            NotAvailableOfflineFragment.newInstance(NotAvailableOfflineFragment.makeRoute(course, item.title, context.getString(descriptionResource)))
        }
    }

    private fun createFileDetailsFragmentWithOfflineCheck(
        isOnline: Boolean,
        course: Course,
        item: ModuleItem,
        moduleObject: ModuleObject?,
        syncedFiles: List<Long>,
        context: Context,
    ): Fragment? {
        return if (isOnline || syncedFiles.contains(item.contentId)) {
            val url = removeDomain(item.url)
            if (moduleObject == null) {
                FileDetailsFragment.newInstance(
                    FileDetailsFragment.makeRoute(
                        course,
                        url!!,
                        item.contentId
                    )
                )
            } else {
                FileDetailsFragment.newInstance(
                    FileDetailsFragment.makeRoute(
                        course,
                        moduleObject,
                        item.id,
                        url!!,
                        item.contentId
                    )
                )
            }
        } else {
            NotAvailableOfflineFragment.newInstance(NotAvailableOfflineFragment.makeRoute(course, item.title, context.getString(R.string.notAvailableOfflineDescriptionForTabs)))
        }
    }

    fun isGroupLocked(module: ModuleObject?): Boolean {
        // NOTE: The state of the group is is "Locked" until the user visits the modules online
        // Check if the unlock date has passed
        if (module?.unlockDate?.after(Date()) == true) return true

        // Check if the state is Locked AND there are prerequisites
        return module?.prerequisiteIds != null && module.state == ModuleObject.State.Locked.apiString
    }

    private fun getAssignmentId(moduleItem: ModuleItem): Long {
        // Get the assignment id from the url
        return Uri.parse(moduleItem.url).pathSegments
            .findWithPrevious { previous, _ -> previous == "assignments" }
            ?.let { expandTildeId(it) }
            ?.toLongOrNull() ?: 0
    }

    private fun getDiscussionRoute(moduleItem: ModuleItem, course: Course): Route {
        // Get the topic id from the url
        val topicId = Uri.parse(moduleItem.url).pathSegments
            .findWithPrevious { previous, _ -> previous == "discussion_topics" }
            ?.let { expandTildeId(it) }
            ?.toLongOrNull() ?: 0
        return makeRoute(course, topicId, null)
    }

    private fun getDiscussionRedesignRoute(moduleItem: ModuleItem, course: Course): Route {
        // Get the topic id from the url
        val topicId = Uri.parse(moduleItem.url).pathSegments
            .findWithPrevious { previous, _ -> previous == "discussion_topics" }
            ?.let { expandTildeId(it) }
            ?.toLongOrNull() ?: 0
        return DiscussionDetailsWebViewFragment.makeRoute(course, topicId)
    }

    /** Strips off the domain and protocol */
    private fun removeDomain(url: String?): String? = url?.substringAfter("/api/v1/")
}
