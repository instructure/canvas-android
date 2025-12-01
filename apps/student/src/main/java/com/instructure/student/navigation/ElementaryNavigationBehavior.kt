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

import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.interactions.router.Route
import com.instructure.pandautils.features.calendar.CalendarFragment
import com.instructure.pandautils.utils.CanvasFont
import com.instructure.student.R
import com.instructure.student.fragment.NotificationListFragment
import com.instructure.student.fragment.ParentFragment
import com.instructure.student.mobius.elementary.ElementaryDashboardFragment

class ElementaryNavigationBehavior(private val apiPrefs: ApiPrefs) : NavigationBehavior {

    override val bottomNavBarFragments: List<Class<out Fragment>> = listOf(
        ElementaryDashboardFragment::class.java,
        CalendarFragment::class.java,
        todoFragmentClass,
        NotificationListFragment::class.java,
        getInboxBottomBarFragment(apiPrefs)
    )

    override val homeFragmentClass: Class<out ParentFragment> = ElementaryDashboardFragment::class.java

    override val visibleNavigationMenuItems: Set<NavigationMenuItem> = setOf(NavigationMenuItem.FILES, NavigationMenuItem.SETTINGS)

    override val visibleOptionsMenuItems: Set<OptionsMenuItem> = emptySet()

    override val visibleAccountMenuItems: Set<AccountMenuItem> = setOf(AccountMenuItem.HELP, AccountMenuItem.CHANGE_USER, AccountMenuItem.LOGOUT)

    override val canvasFont: CanvasFont
        get() = CanvasFont.K5

    override val bottomBarMenu: Int = R.menu.bottom_bar_menu_elementary

    override fun createHomeFragmentRoute(canvasContext: CanvasContext?): Route {
        return ElementaryDashboardFragment.makeRoute(ApiPrefs.user)
    }

    override fun createHomeFragment(route: Route): ParentFragment {
        return ElementaryDashboardFragment.newInstance(route)
    }
}