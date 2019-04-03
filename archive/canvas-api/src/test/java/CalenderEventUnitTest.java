
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

import com.google.gson.Gson;
import com.instructure.canvasapi.model.ScheduleItem;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class CalenderEventUnitTest extends Assert {

    @Test
    public void testCalenderEvent(){
        Gson gson = CanvasRestAdapter.getGSONParser();
        ScheduleItem scheduleItem = gson.fromJson(calenderEventJSON, ScheduleItem.class);

        assertNotNull(scheduleItem);

        if(scheduleItem.isAllDay()){
            assertNotNull(scheduleItem.getAllDayDate());
        }else{
            assertFalse(scheduleItem.isAllDay());
            assertNull(scheduleItem.getAllDayDate());
        }

        assertTrue(scheduleItem.getId() > 0);
        assertNotNull(scheduleItem.getStartDate());
        assertNotNull(scheduleItem.getEndDate());
        assertNotNull(scheduleItem.getLocationName());
        assertNotNull(scheduleItem.getTitle());
        assertNotNull(scheduleItem.getDescription());
        assertNotNull(scheduleItem.getLocationAddress());
    }

    @Test
    public void testCalenderEventList(){
        Gson gson = CanvasRestAdapter.getGSONParser();
        ScheduleItem[] list = gson.fromJson(upcomingEventsJSON, ScheduleItem[].class);

        assertNotNull(list);

        ScheduleItem listScheduleItem = list[0];

        if(listScheduleItem.isAllDay()){
            assertNotNull(listScheduleItem.getAllDayDate());
        }else{
            assertFalse(listScheduleItem.isAllDay());
            assertNull(listScheduleItem.getAllDayDate());
        }

        assertTrue(listScheduleItem.getId() > 0);
        assertNotNull(listScheduleItem.getStartDate());
        assertNotNull(listScheduleItem.getEndDate());
        assertNotNull(listScheduleItem.getLocationName());
        assertNotNull(listScheduleItem.getTitle());
        assertNotNull(listScheduleItem.getDescription());
        assertNotNull(listScheduleItem.getLocationAddress ());
    }




    //vanilla event request
    //@GET("/calendar_events/{event_id}")
    //void getCalendarEvent(@Path("event_id") long event_id, Callback<ScheduleItem> callback);
    final String calenderEventJSON = "{\n" +
            "\"all_day\": false,\n" +
            "\"all_day_date\": null,\n" +
            "\"created_at\": \"2014-07-15T21:18:24Z\",\n" +
            "\"end_at\": \"2014-07-17T00:00:00Z\",\n" +
            "\"id\": 1935799,\n" +
            "\"location_address\": \"Hodor hodor hodor\",\n" +
            "\"location_name\": \"Here\",\n" +
            "\"start_at\": \"2014-07-16T23:00:00Z\",\n" +
            "\"title\": \"Hodor's Reckoning\",\n" +
            "\"updated_at\": \"2014-07-15T22:03:20Z\",\n" +
            "\"workflow_state\": \"active\",\n" +
            "\"description\": \"<p>Hodorrrrrr</p>\",\n" +
            "\"context_code\": \"user_5814789\",\n" +
            "\"child_events_count\": 0,\n" +
            "\"parent_event_id\": null,\n" +
            "\"hidden\": false,\n" +
            "\"child_events\": [],\n" +
            "\"url\": \"https://mobiledev.instructure.com/api/v1/calendar_events/1935799\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/calendar?event_id=1935799&include_contexts=user_5814789#7b2273686f77223a2267726f75705f757365725f35383134373839227d\"\n" +
            "}";

    //upcoming events request
    //@GET("/users/self/upcoming_events")
    //ScheduleItem[] getUpcomingEvents();
    final String upcomingEventsJSON = "[\n" +
            "{\n" +
            "\"all_day\": false,\n" +
            "\"all_day_date\": null,\n" +
            "\"created_at\": \"2014-07-15T21:18:24Z\",\n" +
            "\"end_at\": \"2014-07-17T00:00:00Z\",\n" +
            "\"id\": 1935799,\n" +
            "\"location_address\": \"Hodor hodor hodor\",\n" +
            "\"location_name\": \"Here\",\n" +
            "\"start_at\": \"2014-07-16T23:00:00Z\",\n" +
            "\"title\": \"Hodor's Reckoning\",\n" +
            "\"updated_at\": \"2014-07-15T22:03:20Z\",\n" +
            "\"workflow_state\": \"active\",\n" +
            "\"description\": \"<p>Hodorrrrrr</p>\",\n" +
            "\"context_code\": \"user_5814789\",\n" +
            "\"child_events_count\": 0,\n" +
            "\"parent_event_id\": null,\n" +
            "\"hidden\": false,\n" +
            "\"child_events\": [],\n" +
            "\"url\": \"https://mobiledev.instructure.com/api/v1/calendar_events/1935799\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/calendar?event_id=1935799&include_contexts=user_5814789#7b2273686f77223a2267726f75705f757365725f35383134373839227d\"\n" +
            "},\n" +
            "{\n" +
            "\"all_day\": false,\n" +
            "\"all_day_date\": null,\n" +
            "\"created_at\": \"2014-07-15T22:26:03Z\",\n" +
            "\"end_at\": \"2014-07-18T00:00:00Z\",\n" +
            "\"id\": 1935841,\n" +
            "\"location_address\": null,\n" +
            "\"location_name\": \"lol\",\n" +
            "\"start_at\": \"2014-07-17T22:00:00Z\",\n" +
            "\"title\": \"Woop\",\n" +
            "\"updated_at\": \"2014-07-15T22:26:03Z\",\n" +
            "\"workflow_state\": \"active\",\n" +
            "\"description\": null,\n" +
            "\"context_code\": \"user_5814789\",\n" +
            "\"child_events_count\": 0,\n" +
            "\"parent_event_id\": null,\n" +
            "\"hidden\": false,\n" +
            "\"child_events\": [],\n" +
            "\"url\": \"https://mobiledev.instructure.com/api/v1/calendar_events/1935841\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/calendar?event_id=1935841&include_contexts=user_5814789#7b2273686f77223a2267726f75705f757365725f35383134373839227d\"\n" +
            "},\n" +
            "{\n" +
            "\"all_day\": false,\n" +
            "\"all_day_date\": null,\n" +
            "\"created_at\": \"2014-07-15T22:26:25Z\",\n" +
            "\"end_at\": \"2014-07-18T21:00:00Z\",\n" +
            "\"id\": 1935842,\n" +
            "\"location_address\": null,\n" +
            "\"location_name\": \"lololol\",\n" +
            "\"start_at\": \"2014-07-18T19:00:00Z\",\n" +
            "\"title\": \"Poow\",\n" +
            "\"updated_at\": \"2014-07-15T22:26:25Z\",\n" +
            "\"workflow_state\": \"active\",\n" +
            "\"description\": null,\n" +
            "\"context_code\": \"user_5814789\",\n" +
            "\"child_events_count\": 0,\n" +
            "\"parent_event_id\": null,\n" +
            "\"hidden\": false,\n" +
            "\"child_events\": [],\n" +
            "\"url\": \"https://mobiledev.instructure.com/api/v1/calendar_events/1935842\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/calendar?event_id=1935842&include_contexts=user_5814789#7b2273686f77223a2267726f75705f757365725f35383134373839227d\"\n" +
            "}\n" +
            "]";
}
