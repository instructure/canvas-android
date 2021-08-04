/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.pandautils.features.elementary.resources

import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.mvvm.ItemViewModel

data class ResourcesViewData(val importantLinksItems: List<ItemViewModel>, val actionItems: List<ItemViewModel>)

data class ResourcesHeaderViewData(val title: String, val hasDivider: Boolean = false)

data class LtiApplicationViewData(val title: String, val imageUrl: String, val ltiUrl: String)

data class ContactInfoViewData(val name: String, val description: String, val imageUrl: String)

sealed class ResourcesAction {
    data class OpenLtiApp(val ltiTools: List<LTITool>) : ResourcesAction()
    data class OpenComposeMessage(val recipient: User) : ResourcesAction()
}

enum class ResourcesItemViewType(val viewType: Int) {
    RESOURCES_HEADER(0),
    LTI_APPLICATION(1),
    CONTACT_INFO(2)
}