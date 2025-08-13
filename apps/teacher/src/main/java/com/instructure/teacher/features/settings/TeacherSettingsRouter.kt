/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.teacher.features.settings

import androidx.fragment.app.FragmentActivity
import com.instructure.interactions.router.Route
import com.instructure.pandautils.dialogs.RatingDialog
import com.instructure.pandautils.features.notification.preferences.EmailNotificationPreferencesFragment
import com.instructure.pandautils.features.notification.preferences.PushNotificationPreferencesFragment
import com.instructure.pandautils.features.settings.SettingsRouter
import com.instructure.pandautils.features.settings.inboxsignature.InboxSignatureFragment
import com.instructure.pandautils.fragments.RemoteConfigParamsFragment
import com.instructure.pandautils.utils.AppType
import com.instructure.teacher.fragments.FeatureFlagsFragment
import com.instructure.teacher.fragments.ProfileFragment
import com.instructure.teacher.router.RouteMatcher

class TeacherSettingsRouter(private val activity: FragmentActivity) : SettingsRouter {

    override fun navigateToProfileSettings() {
        RouteMatcher.route(
            activity,
            Route(null, ProfileFragment::class.java)
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

    override fun navigateToRateApp() {
        RatingDialog.showRateDialog(activity, AppType.TEACHER, true)
    }

    override fun navigateToInboxSignature() {
        RouteMatcher.route(
            activity,
            Route(null, InboxSignatureFragment::class.java)
        )
    }
}