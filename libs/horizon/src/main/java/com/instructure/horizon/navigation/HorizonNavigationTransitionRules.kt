/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.navigation

import com.instructure.horizon.features.account.navigation.AccountRoute
import com.instructure.horizon.features.home.HomeNavigationRoute
import com.instructure.horizon.features.inbox.navigation.HorizonInboxRoute
import com.instructure.horizon.features.learn.navigation.LearnRoute
import com.instructure.horizon.features.notebook.navigation.NotebookRoute
import com.instructure.horizon.horizonui.animation.NavigationTransitionAnimation

data class NavigationTransitionAnimationRule(
    val from: String? = null,
    val to: String? = null,
    val style: NavigationTransitionAnimation,
)

val animationRules = listOf(
    //Notebook
    NavigationTransitionAnimationRule(
        from = NotebookRoute.Notebook.route,
        to = NotebookRoute.EditNotebook.serializableRoute,
        style = NavigationTransitionAnimation.SCALE,
    ),
    NavigationTransitionAnimationRule(
        from = NotebookRoute.Notebook.route,
        to = NotebookRoute.AddNotebook.serializableRoute,
        style = NavigationTransitionAnimation.SCALE
    ),
    NavigationTransitionAnimationRule(
        from = MainNavigationRoute.ModuleItemSequence.serializableRoute,
        to = NotebookRoute.Notebook.route,
        style = NavigationTransitionAnimation.SCALE
    ),
    NavigationTransitionAnimationRule(
        from = MainNavigationRoute.ModuleItemSequence.serializableRoute,
        to = NotebookRoute.AddNotebook.serializableRoute,
        style = NavigationTransitionAnimation.SCALE
    ),
    NavigationTransitionAnimationRule(
        from = MainNavigationRoute.ModuleItemSequence.serializableRoute,
        to = NotebookRoute.EditNotebook.serializableRoute,
        style = NavigationTransitionAnimation.SCALE
    ),

    //Account
    NavigationTransitionAnimationRule(
        from = HomeNavigationRoute.Account.route,
        to = AccountRoute.Advanced.route,
        style = NavigationTransitionAnimation.SLIDE
    ),
    NavigationTransitionAnimationRule(
        from = HomeNavigationRoute.Account.route,
        to = AccountRoute.CalendarFeed.route,
        style = NavigationTransitionAnimation.SLIDE
    ),
    NavigationTransitionAnimationRule(
        from = HomeNavigationRoute.Account.route,
        to = AccountRoute.Notifications.route,
        style = NavigationTransitionAnimation.SLIDE
    ),
    NavigationTransitionAnimationRule(
        from = HomeNavigationRoute.Account.route,
        to = AccountRoute.Password.route,
        style = NavigationTransitionAnimation.SLIDE
    ),
    NavigationTransitionAnimationRule(
        from = HomeNavigationRoute.Account.route,
        to = AccountRoute.Profile.route,
        style = NavigationTransitionAnimation.SLIDE
    ),

    //Inbox
    NavigationTransitionAnimationRule(
        from = null,
        to = HorizonInboxRoute.InboxCompose.route,
        style = NavigationTransitionAnimation.SCALE
    ),

    //Learn
    NavigationTransitionAnimationRule(
        from = LearnRoute.LearnScreen.route,
        to = LearnRoute.LearnCourseDetailsScreen.route,
        style = NavigationTransitionAnimation.SLIDE
    ),
    NavigationTransitionAnimationRule(
        from = LearnRoute.LearnScreen.route,
        to = LearnRoute.LearnProgramDetailsScreen.route,
        style = NavigationTransitionAnimation.SLIDE
    ),
    NavigationTransitionAnimationRule(
        from = LearnRoute.LearnProgramDetailsScreen.route,
        to = LearnRoute.LearnCourseDetailsScreen.route,
        style = NavigationTransitionAnimation.SLIDE
    ),
    NavigationTransitionAnimationRule(
        from = LearnRoute.LearnScreen.route,
        to = LearnRoute.LearnLearningLibraryDetailsScreen.route,
        style = NavigationTransitionAnimation.SLIDE
    ),
    NavigationTransitionAnimationRule(
        from = LearnRoute.LearnScreen.route,
        to = LearnRoute.LearnLearningLibraryCompletedScreen.route,
        style = NavigationTransitionAnimation.SLIDE
    ),
    NavigationTransitionAnimationRule(
        from = LearnRoute.LearnScreen.route,
        to = LearnRoute.LearnLearningLibraryBookmarkScreen.route,
        style = NavigationTransitionAnimation.SLIDE
    ),
)

private val Any.serializableRoute: String
    get() = this::class.java.name.replace("$", ".").replace(".Companion", "")