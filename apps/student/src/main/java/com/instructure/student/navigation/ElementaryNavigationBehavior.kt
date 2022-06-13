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

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.FontFamily
import com.instructure.student.R
import com.instructure.student.fragment.*
import com.instructure.student.mobius.elementary.ElementaryDashboardFragment

class ElementaryNavigationBehavior() : NavigationBehavior {

    override val bottomNavBarFragments: List<Class<out ParentFragment>> = listOf(
        ElementaryDashboardFragment::class.java,
        CalendarFragment::class.java,
        ToDoListFragment::class.java,
        NotificationListFragment::class.java,
        InboxFragment::class.java
    )

    override val homeFragmentClass: Class<out ParentFragment> = ElementaryDashboardFragment::class.java

    override val visibleNavigationMenuItems: Set<NavigationMenuItem> = setOf(NavigationMenuItem.FILES, NavigationMenuItem.SETTINGS)

    override val visibleOptionsMenuItems: Set<OptionsMenuItem> = emptySet()

    override val visibleAccountMenuItems: Set<AccountMenuItem> = setOf(AccountMenuItem.HELP, AccountMenuItem.CHANGE_USER, AccountMenuItem.LOGOUT)

    override val fontFamily: FontFamily
        get() = FontFamily.K5

    override val bottomBarMenu: Int = R.menu.bottom_bar_menu_elementary

    override fun createHomeFragmentRoute(canvasContext: CanvasContext?): Route {
        return ElementaryDashboardFragment.makeRoute(ApiPrefs.user)
    }

    override fun createHomeFragment(route: Route): ParentFragment {
        return ElementaryDashboardFragment.newInstance(route)
    }
}