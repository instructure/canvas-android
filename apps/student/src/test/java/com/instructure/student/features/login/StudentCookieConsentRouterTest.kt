/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.student.features.login

import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.services.PushNotificationRegistrationWorker
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudentCookieConsentRouterTest {

    private val activity: FragmentActivity = mockk(relaxed = true)

    private lateinit var router: StudentCookieConsentRouter

    @Before
    fun setup() {
        mockkObject(PushNotificationRegistrationWorker.Companion)
        every { PushNotificationRegistrationWorker.scheduleJob(any(), any()) } just Runs
        mockkObject(ApiPrefs)
        every { ApiPrefs.isMasquerading } returns false
        every { activity.intent } returns null
        router = StudentCookieConsentRouter(activity)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `startApp schedules push notification registration`() {
        router.startApp()

        verify(exactly = 1) { PushNotificationRegistrationWorker.scheduleJob(activity, false) }
    }

    @Test
    fun `startApp forwards masquerading flag to registration`() {
        every { ApiPrefs.isMasquerading } returns true

        router.startApp()

        verify(exactly = 1) { PushNotificationRegistrationWorker.scheduleJob(activity, true) }
    }
}
