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

package com.instructure.canvasapi.api;

import android.content.Context;
import android.text.TextUtils;

import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.ScheduleItem;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.ExhaustiveBridgeCallback;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.EncodedQuery;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public class CalendarEventAPI extends BuildInterfaceAPI {

    public enum EVENT_TYPE {CALENDAR_EVENT, ASSIGNMENT_EVENT;

        public static String getEventTypeName(EVENT_TYPE eventType) {
            if(eventType == CALENDAR_EVENT) {
                return "event";
            } else {
                return "assignment";
            }
        }
    }

    private static String getCalendarEventCacheFilename(long eventID) {
        return "/calendar_events/" + eventID;
    }

    private static String getCalendarEventsCacheFilename(CanvasContext canvasContext) {
        return "/calendar_events?start_date=1990-01-01&end_date=2099-12-31&context_codes[]=" + canvasContext.getId();
    }

    private static String getUpcomingEventsCacheFilename(){
        return "/users/self/upcoming_events";
    }

    private static String getAllEventsCacheFilename(String startDate, EVENT_TYPE eventType){
        String resultString = startDate.replaceAll("[^\\p{L}\\p{Nd}]+", "");
        return "/users/self/all" + eventType.name() + resultString.substring(0, 7);
    }

    private static String getAllCalendarEventsCacheFilename(String contextIds, EVENT_TYPE eventType){
        int lengthLimit = contextIds.length();
        if (lengthLimit > 30) {
            lengthLimit = 30;
        }
        return "/calendar_events?all_events=1&type=" + eventType.name() + "&" + contextIds.substring(0, lengthLimit); // limit the filename length
    }

    public interface CalendarEventsInterface {
        @GET("/calendar_events/{event_id}")
        void getCalendarEvent(@Path("event_id") long event_id, Callback<ScheduleItem> callback);

        @GET("/canvas/{parentId}/{studentId}/calendar_events/{eventId}")
        void getCalendarEventAirwolf(@Path("parentId") String parentId, @Path("studentId") String studentId, @Path("eventId") String eventId, Callback<ScheduleItem> callback);

        @GET("/calendar_events?start_date=1990-01-01&end_date=2099-12-31")
        void getCalendarEvents(@Query("context_codes[]") String context_id, Callback<ScheduleItem[]> callback);

        @GET("/users/self/upcoming_events")
        void getUpcomingEvents(Callback<ScheduleItem[]> callback);

        @GET("/{next}")
        void getNextPageCalendarEvents(@Path(value = "next", encode = false) String nextURL, Callback<ScheduleItem[]> callback);

        @GET("/calendar_events/")
        void getCalendarEvents(
                @Query("all_events") boolean allEvents,
                @Query("type") String type,
                @EncodedQuery("context_codes[]") String contextCodes,
                Callback<ScheduleItem[]> callback);

        @GET("/calendar_events/")
        void getCalendarEvents(
                @Query("all_events") boolean allEvents,
                @Query("type") String type,
                @Query("start_date") String startDate,
                @Query("end_date") String endDate,
                @EncodedQuery("context_codes[]") String contextCodes,
                Callback<ScheduleItem[]> callback);

        @POST("/calendar_events/")
        void createCalendarEvent(@Query("calendar_event[context_code]") String contextCode,
                                 @Query("calendar_event[title]") String title,
                                 @Query("calendar_event[description]") String description,
                                 @Query("calendar_event[start_at]") String startDate,
                                 @Query("calendar_event[end_at]") String endDate,
                                 @Query("calendar_event[location_name]") String locationName,
                                 @Body String body,
                                 CanvasCallback<ScheduleItem> callback);

        @DELETE("/calendar_events/{event_id}")
        void deleteCalendarEvent(@Path("event_id") long event_id, @Query("cancel_reason") String cancelReason,
                                 CanvasCallback<ScheduleItem> callback);

        @GET("/users/{user_id}/calendar_events/")
        void getCalendarEventsForUser(
                @Path("user_id") long user_id,
                @Query("type") String type,
                @Query("start_date") String startDate,
                @Query("end_date") String endDate,
                @EncodedQuery("context_codes[]") String contextCodes,
                Callback<ScheduleItem[]> callback);

        @GET("/users/{user_id}/calendar_events?include[]=submission")
        void getCalendarEventsForUserWithSubmissions(
                @Path("user_id") long user_id,
                @Query("type") String type,
                @Query("start_date") String startDate,
                @Query("end_date") String endDate,
                @EncodedQuery("context_codes[]") String contextCodes,
                Callback<ScheduleItem[]> callback);

        @GET("/canvas/{parent_id}/{student_id}/calendar_events?include[]=submission")
        void getCalendarEventsWithSubmissionsAirwolf(
                @Path("parent_id") String parentId,
                @Path("student_id") String studentId,
                @Query("start_date") String startDate,
                @Query("end_date") String endDate,
                @EncodedQuery("context_codes[]") String contextCodes,
                Callback<ScheduleItem[]> callback);
        /////////////////////////////////////////////////////////////////////////////
        // Synchronous
        /////////////////////////////////////////////////////////////////////////////

        @GET("/users/self/upcoming_events")
        ScheduleItem[] getUpcomingEvents();
    }

    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////

    public static void getCalendarEvent(long calendarEventId, final CanvasCallback<ScheduleItem> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        buildCacheInterface(CalendarEventsInterface.class, callback).getCalendarEvent(calendarEventId, callback);
        buildInterface(CalendarEventsInterface.class, callback).getCalendarEvent(calendarEventId, callback);
    }

    public static void getCalendarEventAirwolf(String parentId, String studentId, String eventId, final CanvasCallback<ScheduleItem> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        buildCacheInterface(CalendarEventsInterface.class, callback).getCalendarEventAirwolf(parentId, studentId, eventId, callback);
        buildInterface(CalendarEventsInterface.class, callback).getCalendarEventAirwolf(parentId, studentId, eventId, callback);
    }

    public static void getCalendarEvents(CanvasContext canvasContext, final CanvasCallback<ScheduleItem[]> callback) {
        if (APIHelpers.paramIsNull(callback, canvasContext)) { return; }

        buildCacheInterface(CalendarEventsInterface.class, callback).getCalendarEvents(canvasContext.getContextId(), callback);
        buildInterface(CalendarEventsInterface.class, callback).getCalendarEvents(canvasContext.getContextId(), callback);
    }

    public static void getUpcomingEvents(final CanvasCallback<ScheduleItem[]> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        buildCacheInterface(CalendarEventsInterface.class, callback).getUpcomingEvents(callback);
        buildInterface(CalendarEventsInterface.class, callback).getUpcomingEvents(callback);
    }

    public static void getNextPageCalendarEventsChained(String nextURL, CanvasCallback<ScheduleItem[]> callback, boolean isCached){
        if(APIHelpers.paramIsNull(callback, nextURL)){ return;}

        callback.setIsNextPage(true);
        if (isCached) {
            buildCacheInterface(CalendarEventsInterface.class, callback, false).getNextPageCalendarEvents(nextURL, callback);
        } else {
            buildInterface(CalendarEventsInterface.class, callback, false).getNextPageCalendarEvents(nextURL, callback);
        }
    }

    public static void getAllCalendarEventsExhaustive(EVENT_TYPE eventType, String startDate, String endDate, ArrayList<String> canvasContextIds, final CanvasCallback<ScheduleItem[]> callback) {
        String contextIds = buildContextArray(canvasContextIds);
        CanvasCallback<ScheduleItem[]> bridge = new ExhaustiveBridgeCallback<>(ScheduleItem.class, callback, new ExhaustiveBridgeCallback.ExhaustiveBridgeEvents() {
            @Override
            public void performApiCallWithExhaustiveCallback(CanvasCallback bridgeCallback, String nextURL, boolean isCached) {
                if(callback.isCancelled()) { return; }

                CalendarEventAPI.getNextPageCalendarEventsChained(nextURL, bridgeCallback, isCached);
            }
        });

        buildCacheInterface(CalendarEventsInterface.class, callback).getCalendarEvents(false, EVENT_TYPE.getEventTypeName(eventType), startDate, endDate, contextIds, bridge);
        buildInterface(CalendarEventsInterface.class, callback).getCalendarEvents(false, EVENT_TYPE.getEventTypeName(eventType), startDate, endDate, contextIds, bridge);
    }

    public static void getAllCalendarEventsExhaustive(EVENT_TYPE eventType, ArrayList<String> canvasContextIds, final CanvasCallback<ScheduleItem[]> callback) {
        String contextIds = buildContextArray(canvasContextIds);

        CanvasCallback<ScheduleItem[]> bridge = new ExhaustiveBridgeCallback<>(ScheduleItem.class, callback, new ExhaustiveBridgeCallback.ExhaustiveBridgeEvents() {
            @Override
            public void performApiCallWithExhaustiveCallback(CanvasCallback bridgeCallback, String nextURL, boolean isCached) {
                if(callback.isCancelled()) { return; }
                CalendarEventAPI.getNextPageCalendarEventsChained(nextURL, bridgeCallback, isCached);
            }
        });

        buildCacheInterface(CalendarEventsInterface.class, callback).getCalendarEvents(true, EVENT_TYPE.getEventTypeName(eventType), contextIds, bridge);
        buildInterface(CalendarEventsInterface.class, callback).getCalendarEvents(true, EVENT_TYPE.getEventTypeName(eventType), contextIds, bridge);
    }

    public static void createCalendarEvent(String contextCode, String title, String description, String startDate, String endDate, String location, final CanvasCallback<ScheduleItem> callback){
        if(APIHelpers.paramIsNull(callback, contextCode) || TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate)){return;}

        buildInterface(CalendarEventsInterface.class, callback).createCalendarEvent(contextCode, title, description, startDate, endDate, location, "", callback);
    }

    public static void deleteCalendarEvent(long calendarEventId, String cancelReason, CanvasCallback<ScheduleItem> callback){
        if(APIHelpers.paramIsNull(callback)){return;}

        buildInterface(CalendarEventsInterface.class, callback).deleteCalendarEvent(calendarEventId, cancelReason, callback);
    }

    public static void getAllCalendarEventsForUserExhaustive(long userId, EVENT_TYPE eventType, String startDate, String endDate, ArrayList<String> canvasContextIds, final CanvasCallback<ScheduleItem[]> callback) {
        CanvasCallback<ScheduleItem[]> bridge = new ExhaustiveBridgeCallback<>(ScheduleItem.class, callback, new ExhaustiveBridgeCallback.ExhaustiveBridgeEvents() {
            @Override
            public void performApiCallWithExhaustiveCallback(CanvasCallback bridgeCallback, String nextURL, boolean isCached) {
                if(callback.isCancelled()) { return; }

                CalendarEventAPI.getNextPageCalendarEventsChained(nextURL, bridgeCallback, isCached);
            }
        });

        buildCacheInterface(CalendarEventsInterface.class, callback).getCalendarEventsForUser(userId, EVENT_TYPE.getEventTypeName(eventType), startDate, endDate, buildContextArray(canvasContextIds), bridge);
        buildInterface(CalendarEventsInterface.class, callback).getCalendarEventsForUser(userId, EVENT_TYPE.getEventTypeName(eventType), startDate, endDate, buildContextArray(canvasContextIds), bridge);
    }

    public static void getAllCalendarEventsForUserWithSubmissionsExhaustive(long userId, EVENT_TYPE eventType, String startDate, String endDate, ArrayList<String> canvasContextIds, final CanvasCallback<ScheduleItem[]> callback) {
        CanvasCallback<ScheduleItem[]> bridge = new ExhaustiveBridgeCallback<>(ScheduleItem.class, callback, new ExhaustiveBridgeCallback.ExhaustiveBridgeEvents() {
            @Override
            public void performApiCallWithExhaustiveCallback(CanvasCallback bridgeCallback, String nextURL, boolean isCached) {
                if(callback.isCancelled()) { return; }

                CalendarEventAPI.getNextPageCalendarEventsChained(nextURL, bridgeCallback, isCached);
            }
        });

        buildCacheInterface(CalendarEventsInterface.class, callback).getCalendarEventsForUserWithSubmissions(userId, EVENT_TYPE.getEventTypeName(eventType), startDate, endDate, buildContextArray(canvasContextIds), bridge);
        buildInterface(CalendarEventsInterface.class, callback).getCalendarEventsForUserWithSubmissions(userId, EVENT_TYPE.getEventTypeName(eventType), startDate, endDate, buildContextArray(canvasContextIds), bridge);
    }

    public static void getAllCalendarEventsWithSubmissionsExhaustiveAirwolf(String parentId, String studentId, String startDate, String endDate, ArrayList<String> canvasContextIds, final CanvasCallback<ScheduleItem[]> callback) {
        CanvasCallback<ScheduleItem[]> bridge = new ExhaustiveBridgeCallback<>(ScheduleItem.class, callback, new ExhaustiveBridgeCallback.ExhaustiveBridgeEvents() {
            @Override
            public void performApiCallWithExhaustiveCallback(CanvasCallback bridgeCallback, String nextURL, boolean isCached) {
                if(callback.isCancelled()) { return; }

                CalendarEventAPI.getNextPageCalendarEventsChained(nextURL, bridgeCallback, isCached);
            }
        });

        buildCacheInterface(CalendarEventsInterface.class, APIHelpers.getAirwolfDomain(callback.getContext()), callback).getCalendarEventsWithSubmissionsAirwolf(parentId, studentId, startDate, endDate, buildContextArray(canvasContextIds), bridge);
        buildInterface(CalendarEventsInterface.class, APIHelpers.getAirwolfDomain(callback.getContext()), callback).getCalendarEventsWithSubmissionsAirwolf(parentId, studentId, startDate, endDate, buildContextArray(canvasContextIds), bridge);
    }

    private static String buildContextArray(ArrayList<String> canvasContextIds){
        //Builds an array of context_codes, the way we have to build and send the array is funky.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < canvasContextIds.size(); i++) {
            sb.append(canvasContextIds.get(i));

            if(i == canvasContextIds.size() - 1) {
                break;
            }
            sb.append("&context_codes[]=");
        }

        return sb.toString();
    }

    /////////////////////////////////////////////////////////////////////////////
    // Synchronous
    //
    // If Retrofit is unable to parse (no network for example) Synchronous calls
    // will throw a nullPointer exception. All synchronous calls need to be in a
    // try catch block.
    /////////////////////////////////////////////////////////////////////////////


    public static ScheduleItem[] getUpcomingEventsSynchronous(Context context) {
        try {
            return buildInterface(CalendarEventsInterface.class, context).getUpcomingEvents();
        } catch (Exception E){
            return null;
        }
    }
}
