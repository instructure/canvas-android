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

import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class CalenderEventUnitTest : Assert() {

    @Test
    fun testCalenderEvent() {
        val scheduleItem: ScheduleItem = calenderEventJSON.parse()

        Assert.assertNotNull(scheduleItem)

        if (scheduleItem.isAllDay) {
            Assert.assertNotNull(scheduleItem.allDayAt)
        } else {
            Assert.assertFalse(scheduleItem.isAllDay)
            Assert.assertNull(scheduleItem.allDayAt)
        }

        Assert.assertTrue(scheduleItem.id > 0)
        Assert.assertNotNull(scheduleItem.startAt)
        Assert.assertNotNull(scheduleItem.endAt)
        Assert.assertNotNull(scheduleItem.locationName)
        Assert.assertNotNull(scheduleItem.title)
        Assert.assertNotNull(scheduleItem.description)
        Assert.assertNotNull(scheduleItem.locationAddress)
    }

    @Test
    fun testCalenderEventList() {
        val list: Array<ScheduleItem> = upcomingEventsJSON.parse()

        Assert.assertNotNull(list)

        val listScheduleItem = list[0]

        if (listScheduleItem.isAllDay) {
            Assert.assertNotNull(listScheduleItem.allDayAt)
        } else {
            Assert.assertFalse(listScheduleItem.isAllDay)
            Assert.assertNull(listScheduleItem.allDayAt)
        }

        Assert.assertTrue(listScheduleItem.id > 0)
        Assert.assertNotNull(listScheduleItem.startAt)
        Assert.assertNotNull(listScheduleItem.endAt)
        Assert.assertNotNull(listScheduleItem.locationName)
        Assert.assertNotNull(listScheduleItem.title)
        Assert.assertNotNull(listScheduleItem.description)
        Assert.assertNotNull(listScheduleItem.locationAddress)
    }

    /**
     * vanilla event request
     * @GET("/calendar_events/{event_id}")
     * void getCalendarEvent(@Path("event_id") long event_id, Callback<ScheduleItem> callback);
     */
    @Language("JSON")
    private val calenderEventJSON = """
      {
        "all_day": false,
        "all_day_date": null,
        "created_at": "2014-07-15T21:18:24Z",
        "end_at": "2014-07-17T00:00:00Z",
        "id": 1935799,
        "location_address": "Hodor hodor hodor",
        "location_name": "Here",
        "start_at": "2014-07-16T23:00:00Z",
        "title": "Hodor's Reckoning",
        "updated_at": "2014-07-15T22:03:20Z",
        "workflow_state": "active",
        "description": "<p>Hodorrrrrr</p>",
        "context_code": "user_5814789",
        "child_events_count": 0,
        "parent_event_id": null,
        "hidden": false,
        "child_events": [],
        "url": "https://mobiledev.instructure.com/api/v1/calendar_events/1935799",
        "html_url": "https://mobiledev.instructure.com/calendar?event_id=1935799&include_contexts=user_5814789#7b2273686f77223a2267726f75705f757365725f35383134373839227d"
      }"""

    /**
     * upcoming events request
     * @GET("/users/self/upcoming_events")
     * ScheduleItem[] getUpcomingEvents();
     */
    @Language("JSON")
    private val upcomingEventsJSON = """
      [
        {
          "all_day": false,
          "all_day_date": null,
          "created_at": "2014-07-15T21:18:24Z",
          "end_at": "2014-07-17T00:00:00Z",
          "id": 1935799,
          "location_address": "Hodor hodor hodor",
          "location_name": "Here",
          "start_at": "2014-07-16T23:00:00Z",
          "title": "Hodor's Reckoning",
          "updated_at": "2014-07-15T22:03:20Z",
          "workflow_state": "active",
          "description": "<p>Hodorrrrrr</p>",
          "context_code": "user_5814789",
          "child_events_count": 0,
          "parent_event_id": null,
          "hidden": false,
          "child_events": [],
          "url": "https://mobiledev.instructure.com/api/v1/calendar_events/1935799",
          "html_url": "https://mobiledev.instructure.com/calendar?event_id=1935799&include_contexts=user_5814789#7b2273686f77223a2267726f75705f757365725f35383134373839227d"
        },
        {
          "all_day": false,
          "all_day_date": null,
          "created_at": "2014-07-15T22:26:03Z",
          "end_at": "2014-07-18T00:00:00Z",
          "id": 1935841,
          "location_address": null,
          "location_name": "lol",
          "start_at": "2014-07-17T22:00:00Z",
          "title": "Woop",
          "updated_at": "2014-07-15T22:26:03Z",
          "workflow_state": "active",
          "description": null,
          "context_code": "user_5814789",
          "child_events_count": 0,
          "parent_event_id": null,
          "hidden": false,
          "child_events": [],
          "url": "https://mobiledev.instructure.com/api/v1/calendar_events/1935841",
          "html_url": "https://mobiledev.instructure.com/calendar?event_id=1935841&include_contexts=user_5814789#7b2273686f77223a2267726f75705f757365725f35383134373839227d"
        },
        {
          "all_day": false,
          "all_day_date": null,
          "created_at": "2014-07-15T22:26:25Z",
          "end_at": "2014-07-18T21:00:00Z",
          "id": 1935842,
          "location_address": null,
          "location_name": "lololol",
          "start_at": "2014-07-18T19:00:00Z",
          "title": "Poow",
          "updated_at": "2014-07-15T22:26:25Z",
          "workflow_state": "active",
          "description": null,
          "context_code": "user_5814789",
          "child_events_count": 0,
          "parent_event_id": null,
          "hidden": false,
          "child_events": [],
          "url": "https://mobiledev.instructure.com/api/v1/calendar_events/1935842",
          "html_url": "https://mobiledev.instructure.com/calendar?event_id=1935842&include_contexts=user_5814789#7b2273686f77223a2267726f75705f757365725f35383134373839227d"
        }
      ]"""
}
