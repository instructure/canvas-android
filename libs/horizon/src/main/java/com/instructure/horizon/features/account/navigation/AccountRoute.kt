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
package com.instructure.horizon.features.account.navigation

sealed class AccountRoute(val route: String) {
    data object Account : AccountRoute("account_home")
    data object Profile : AccountRoute("profile")
    data object Password : AccountRoute("password")
    data object Notifications : AccountRoute("notifications")
    data object CalendarFeed : AccountRoute("calendar_feed")
    data object Advanced : AccountRoute("advanced")
    data object ReportABug : AccountRoute("report_a_bug")
}