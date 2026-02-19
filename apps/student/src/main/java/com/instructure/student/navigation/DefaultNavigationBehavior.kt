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
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import com.instructure.interactions.router.Route
import com.instructure.pandautils.features.calendar.CalendarFragment
import com.instructure.pandautils.features.dashboard.widget.GlobalConfig
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.repository.WidgetConfigDataRepository
import com.instructure.pandautils.utils.CanvasFont
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.student.R
import com.instructure.student.features.dashboard.compose.DashboardFragment
import com.instructure.student.fragment.OldDashboardFragment
import com.instructure.student.fragment.NotificationListFragment
import com.instructure.student.fragment.ParentFragment
import kotlinx.coroutines.runBlocking

class DefaultNavigationBehavior(
    private val apiPrefs: ApiPrefs,
    private val featureFlagProvider: FeatureFlagProvider,
    private val widgetConfigDataRepository: WidgetConfigDataRepository
) : NavigationBehavior {

    private val widgetDashboardCanvasFlag by lazy {
        runBlocking { featureFlagProvider.checkWidgetDashboardFlag() }
    }

    private val newDashboardEnabled by lazy {
        runBlocking {
            val json = widgetConfigDataRepository.getConfigJson(WidgetMetadata.WIDGET_ID_GLOBAL)
            json?.let {
                try { GlobalConfig.fromJson(it) } catch (e: Exception) { GlobalConfig() }
            }?.newDashboardEnabled ?: true
        }
    }

    private fun shouldShowNewDashboard(): Boolean {
        val killSwitch = RemoteConfigUtils.getBoolean(RemoteConfigParam.DASHBOARD_REDESIGN)
        return killSwitch && widgetDashboardCanvasFlag && newDashboardEnabled
    }

    private val dashboardFragmentClass: Class<out Fragment>
        get() {
            return if (shouldShowNewDashboard()) {
                DashboardFragment::class.java
            } else {
                OldDashboardFragment::class.java
            }
        }

    override val bottomNavBarFragments: List<Class<out Fragment>>
        get() = listOf(
            dashboardFragmentClass,
            CalendarFragment::class.java,
            todoFragmentClass,
            NotificationListFragment::class.java,
            getInboxBottomBarFragment(apiPrefs)
        )

    override val homeFragmentClass: Class<out Fragment>
        get() = dashboardFragmentClass

    override val visibleNavigationMenuItems: Set<NavigationMenuItem> = setOf(NavigationMenuItem.FILES, NavigationMenuItem.BOOKMARKS, NavigationMenuItem.SETTINGS)

    override val visibleOptionsMenuItems: Set<OptionsMenuItem> = setOf(OptionsMenuItem.SHOW_GRADES, OptionsMenuItem.COLOR_OVERLAY)

    override val visibleAccountMenuItems: Set<AccountMenuItem> = setOf(AccountMenuItem.HELP, AccountMenuItem.CHANGE_USER, AccountMenuItem.LOGOUT)

    override val canvasFont: CanvasFont
        get() = CanvasFont.REGULAR

    override val bottomBarMenu: Int = R.menu.bottom_bar_menu

    override fun createHomeFragmentRoute(canvasContext: CanvasContext?): Route {
        return if (shouldShowNewDashboard()) {
            DashboardFragment.makeRoute(ApiPrefs.user)
        } else {
            OldDashboardFragment.makeRoute(ApiPrefs.user)
        }
    }

    override fun createHomeFragment(route: Route): ParentFragment {
        return if (shouldShowNewDashboard()) {
            DashboardFragment.newInstance(route)
        } else {
            OldDashboardFragment.newInstance(route)
        }
    }
}