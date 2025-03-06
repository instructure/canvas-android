/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.parentapp.features.calendarevent.details

import android.content.res.Resources
import android.net.Uri
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.pandautils.R
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsDefaultValues
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsDisabledFields
import com.instructure.parentapp.features.calendarevent.ParentEventViewModelBehavior
import com.instructure.parentapp.util.ParentPrefs
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test


class ParentEventViewModelBehaviorTest {

    private val resources: Resources = mockk(relaxed = true)
    private val parentPrefs: ParentPrefs = mockk(relaxed = true)

    private val behavior = ParentEventViewModelBehavior(resources, parentPrefs)

    @Test
    fun `Should show message fab`() = runTest {
        assertEquals(true, behavior.shouldShowMessageFab)
    }

    @Test
    fun `Get correct compose options`() = runTest {
        every { parentPrefs.currentStudent } returns User(name = "Student Name")
        every { resources.getString(R.string.regardingHiddenMessageWithEventPrefix, any(), any()) } answers {
            val args = secondArg<Array<Any>>()
            "Regarding: ${args[0]}, Event - ${args[1]}"
        }
        every { resources.getString(R.string.regardingHiddenMessage, any(), any()) } answers {
            val args = secondArg<Array<Any>>()
            "Regarding: ${args[0]}, Event - ${args[1]}"
        }
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } answers {
            mockk<Uri> {
                every { scheme } returns "https"
                every { host } returns "event.com"
                every { getQueryParameter("event_id") } returns "420"
            }
        }

        val course = Course(name = "Course Name", id = 1)
        val scheduleItem = ScheduleItem(title = "Event Title", htmlUrl = "https://event.com/event_id=420", contextCode = "course_1")

        val expected = InboxComposeOptions(
            disabledFields = InboxComposeOptionsDisabledFields(isContextDisabled = true),
            defaultValues = InboxComposeOptionsDefaultValues(
                contextCode = "course_1",
                contextName = "Course Name",
                subject = "Regarding: Student Name, Event - Event Title",
            ),
            autoSelectRecipientsFromRoles = listOf(EnrollmentType.TEACHERENROLLMENT),
            hiddenBodyMessage = "Regarding: Student Name, Event - https://event.com/courses/1/calendar_events/420"
        )

        val result = behavior.getInboxComposeOptions(course, scheduleItem)

        assertEquals(expected, result)

        unmockkAll()
    }
}
