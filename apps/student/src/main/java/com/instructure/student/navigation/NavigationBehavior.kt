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
package com.instructure.student.navigation

import androidx.annotation.MenuRes
import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import com.instructure.interactions.router.Route
import com.instructure.pandautils.features.inbox.list.InboxFragment
import com.instructure.pandautils.utils.CanvasFont
import com.instructure.student.activity.NothingToSeeHereFragment
import com.instructure.student.features.todolist.ToDoListFragment
import com.instructure.student.fragment.OldToDoListFragment
import com.instructure.student.fragment.ParentFragment

interface NavigationBehavior {

    /** 'Root' fragments that should include the bottom nav bar */
    val bottomNavBarFragments: List<Class<out Fragment>>

    val homeFragmentClass: Class<out ParentFragment>

    val visibleNavigationMenuItems: Set<NavigationMenuItem>

    val visibleOptionsMenuItems: Set<OptionsMenuItem>

    val visibleAccountMenuItems: Set<AccountMenuItem>

    val canvasFont: CanvasFont

    val todoFragmentClass: Class<out Fragment>
        get() {
            return if (RemoteConfigUtils.getBoolean(RemoteConfigParam.TODO_REDESIGN)) {
                ToDoListFragment::class.java
            } else {
                OldToDoListFragment::class.java
            }
        }

    @get:MenuRes
    val bottomBarMenu: Int

    fun createHomeFragmentRoute(canvasContext: CanvasContext?): Route

    fun createHomeFragment(route: Route): ParentFragment

    fun getInboxBottomBarFragment(apiPrefs: ApiPrefs): Class<out Fragment> {
        return if (apiPrefs.isStudentView) {
            NothingToSeeHereFragment::class.java
        } else {
            InboxFragment::class.java
        }
    }
}

enum class NavigationMenuItem {
    FILES, BOOKMARKS, SETTINGS
}

enum class OptionsMenuItem {
    SHOW_GRADES, COLOR_OVERLAY
}

enum class AccountMenuItem {
    HELP, CHANGE_USER, LOGOUT
}