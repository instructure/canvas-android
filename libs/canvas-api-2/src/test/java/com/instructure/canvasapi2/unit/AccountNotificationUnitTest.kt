/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 *
 */

package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test


class AccountNotificationUnitTest : Assert() {

    @Test
    fun testAccountNotifications() {
        val accountNotifications: Array<AccountNotification> = accountNotificationsJSON.parse()
        Assert.assertNotNull(accountNotifications)
        Assert.assertEquals(2, accountNotifications.size)
        for (accountNotification in accountNotifications) {
            Assert.assertNotNull(accountNotification.id)
            Assert.assertNotNull(accountNotification.subject)
            Assert.assertNotNull(accountNotification.message)
            Assert.assertNotNull(accountNotification.startDate)
            Assert.assertNotNull(accountNotification.endDate)
            Assert.assertNotNull(accountNotification.icon)
        }
    }

    @Language("JSON")
    private val accountNotificationsJSON: String = """
      [
        {
          "end_at": "2015-03-18T06:00:00Z",
          "icon": "warning",
          "id": 3038,
          "message": "\u003Cp\u003EGood weather warning.\u003C/p\u003E",
          "start_at": "2015-03-16T06:00:00Z",
          "subject": "Good Weather Warning",
          "role_ids": [
            2441,
            1642,
            2442,
            2443
          ],
          "roles": [
            "StudentEnrollment",
            "Nerd",
            "TeacherEnrollment",
            "TaEnrollment"
          ]
        },
        {
          "end_at": "2015-03-18T06:00:00Z",
          "icon": "warning",
          "id": 3038,
          "message": "\u003Cp\u003EGood weather warning.\u003C/p\u003E",
          "start_at": "2015-03-16T06:00:00Z",
          "subject": "Good Weather Warning",
          "role_ids": [
            2441,
            1642,
            2442,
            2443
          ],
          "roles": [
            "StudentEnrollment",
            "Nerd",
            "TeacherEnrollment",
            "TaEnrollment"
          ]
        }
      ]"""
}
