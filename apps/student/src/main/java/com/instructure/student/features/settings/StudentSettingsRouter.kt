/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.student.features.settings

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.interactions.router.Route
import com.instructure.pandautils.features.notification.preferences.EmailNotificationPreferencesFragment
import com.instructure.pandautils.features.notification.preferences.PushNotificationPreferencesFragment
import com.instructure.pandautils.features.offline.sync.settings.SyncSettingsFragment
import com.instructure.pandautils.features.settings.SettingsRouter
import com.instructure.pandautils.fragments.RemoteConfigParamsFragment
import com.instructure.student.activity.NothingToSeeHereFragment
import com.instructure.student.fragment.AccountPreferencesFragment
import com.instructure.student.fragment.FeatureFlagsFragment
import com.instructure.student.fragment.ProfileSettingsFragment
import com.instructure.student.mobius.settings.pairobserver.ui.PairObserverFragment
import com.instructure.student.router.RouteMatcher

class StudentSettingsRouter(
    private val activity: FragmentActivity
) : SettingsRouter {
    override fun navigateToProfileSettings() {
        val fragment = if (ApiPrefs.isStudentView) {
            NothingToSeeHereFragment::class.java
        } else {
            ProfileSettingsFragment::class.java
        }
        RouteMatcher.route(
            activity,
            Route(null, fragment)
        )
    }

    override fun navigateToPushNotificationsSettings() {
        RouteMatcher.route(
            activity,
            Route(null, PushNotificationPreferencesFragment::class.java)
        )

    }

    override fun navigateToEmailNotificationsSettings() {
        RouteMatcher.route(
            activity,
            Route(null, EmailNotificationPreferencesFragment::class.java)
        )
    }

    override fun navigateToPairWithObserver() {
        RouteMatcher.route(
            activity,
            Route(null, PairObserverFragment::class.java)
        )
    }

    override fun navigateToSyncSettings() {
        RouteMatcher.route(
            activity,
            Route(null, SyncSettingsFragment::class.java)
        )
    }

    override fun navigateToAccountPreferences() {
        RouteMatcher.route(
            activity,
            Route(null, AccountPreferencesFragment::class.java)
        )
    }

    override fun navigateToRemoteConfig() {
        RouteMatcher.route(
            activity,
            Route(null, RemoteConfigParamsFragment::class.java)
        )
    }

    override fun navigateToFeatureFlags() {
        RouteMatcher.route(
            activity,
            Route(null, FeatureFlagsFragment::class.java)
        )
    }
}