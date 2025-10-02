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
package com.instructure.horizon.espresso

import com.instructure.canvas.espresso.CanvasComposeTest
import com.instructure.canvasapi2.models.User
import com.instructure.horizon.HorizonActivity
import com.instructure.horizon.pages.HorizonDashboardPage
import com.instructure.horizon.pages.HorizonNotificationPage

abstract class HorizonTest: CanvasComposeTest() {
    override val activityRule = HorizonActivityTestRule(HorizonActivity::class.java)
    override val isTesting = true

    override fun displaysPageObjects() = Unit

    val dashboardPage: HorizonDashboardPage = HorizonDashboardPage(composeTestRule)
    val notificationsPage: HorizonNotificationPage = HorizonNotificationPage(composeTestRule)

    fun tokenLogin(domain: String, token: String, user: User) {
        activityRule.runOnUiThread {
            (originalActivity as HorizonActivity).loginWithToken(
                token,
                domain,
                user
            )
        }
    }
}