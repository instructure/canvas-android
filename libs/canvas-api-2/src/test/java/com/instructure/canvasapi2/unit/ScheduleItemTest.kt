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

import com.google.gson.GsonBuilder
import com.instructure.canvasapi2.models.AssignmentOverride
import com.instructure.canvasapi2.models.ScheduleItem
import org.intellij.lang.annotations.Language
import org.junit.Assert.assertEquals
import org.junit.Test

class ScheduleItemTest {
    @Test
    fun getIdTest_Number() {
        val scheduleItem = ScheduleItem(itemId = 43243.toString())

        assertEquals(43243, scheduleItem.id)
    }

    @Test
    fun getIdTest_Assignment() {
        // Can't set a schedule item's id to be a string by any exposed method, so use JSON parsing
        // to test it
        val builder = GsonBuilder()

        val gson = builder.create()
        val scheduleItem = gson.fromJson(scheduleItemJSON, ScheduleItem::class.java)

        assertEquals(673956, scheduleItem.id)
    }

    @Test
    fun getIdTest_AssignmentOverrides() {
        // Can't set a schedule item's id to be a string by any exposed method, so use JSON parsing
        // to test it
        val builder = GsonBuilder()
        val gson = builder.create()
        var scheduleItem = gson.fromJson(scheduleItemJSON, ScheduleItem::class.java)

        val assignmentOverride = AssignmentOverride(id = 1234567)
        val assignmentOverrides = arrayListOf(assignmentOverride)

        scheduleItem = scheduleItem.copy(assignmentOverrides = assignmentOverrides)

        assertEquals(1234567, scheduleItem.id)
    }


    @Test
    fun getContextIdTest_User() {
        val scheduleItem = ScheduleItem(contextCode = "user_12345")

        assertEquals(12345, scheduleItem.contextId)
    }

    @Test
    fun getContextIdTest_Course() {
        val scheduleItem = ScheduleItem(contextCode = "course_12345")

        assertEquals(12345, scheduleItem.contextId)
    }

    @Test
    fun getContextIdTest_Group() {
        val scheduleItem = ScheduleItem(contextCode = "group_12345")

        assertEquals(12345, scheduleItem.contextId)
    }

    @Test
    fun getUserIdTest() {
        val scheduleItem = ScheduleItem(contextCode = "user_12345")

        assertEquals(12345, scheduleItem.userId)
    }

    @Test
    fun getCourseIdTest() {
        val scheduleItem = ScheduleItem(contextCode = "course_12345")

        assertEquals(12345, scheduleItem.courseId)
    }

    companion object {
        @Language("JSON")
        val scheduleItemJSON =
                """
                    {
                      "all_day": true,
                      "all_day_date": "2012-10-17",
                      "created_at": "2012-10-06T01:09:52Z",
                      "end_at": "2012-10-17T06:00:00Z",
                      "id": "assignment_673956",
                      "location_address": null,
                      "location_name": null,
                      "start_at": "2012-10-17T06:00:00Z",
                      "title": "No Class",
                      "updated_at": "2012-10-06T01:09:52Z",
                      "workflow_state": "active",
                      "description": null,
                      "context_code": "course_833052",
                      "child_events_count": 0,
                      "parent_event_id": null,
                      "hidden": false,
                      "child_events": [],
                      "url": "https://mobiledev.instructure.com/api/v1/calendar_events/673956",
                      "html_url": "https://mobiledev.instructure.com/calendar?event_id=673956&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d"
                      }
                """
    }
}
