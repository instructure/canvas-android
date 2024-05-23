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

package com.instructure.teacher.features.modules.progression

import androidx.fragment.app.Fragment
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.teacher.features.assignment.details.AssignmentDetailsFragment
import com.instructure.teacher.fragments.PageDetailsFragment
import com.instructure.teacher.fragments.QuizDetailsFragment

enum class ModuleItemAsset(
    val assetType: String,
    val assetIdParamName: String,
    val routeClass: Class<out Fragment>? = null
) {
    MODULE_ITEM("ModuleItem", RouterParams.MODULE_ITEM_ID),
    PAGE("Page", RouterParams.PAGE_ID, PageDetailsFragment::class.java),
    QUIZ("Quiz", RouterParams.QUIZ_ID, QuizDetailsFragment::class.java),
    DISCUSSION("Discussion", RouterParams.MESSAGE_ID, DiscussionRouterFragment::class.java),
    ASSIGNMENT("Assignment", RouterParams.ASSIGNMENT_ID, AssignmentDetailsFragment::class.java)
}
