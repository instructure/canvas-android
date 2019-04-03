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

import com.instructure.canvasapi.model.ColumnDatum;
import com.instructure.canvasapi.model.CustomColumn;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;



public class CustomGradebookColumnAPI extends BuildInterfaceAPI {

    public interface CustomGradebookInterface {
        @GET("/courses/{course_id}/custom_gradebook_columns")
        void getGradebookColumns(@Path("course_id") long courseId, CanvasCallback<CustomColumn[]> callback);

        @POST("/courses/{course_id}/custom_gradebook_columns")
        void createGradebookColumn(@Path("course_id") long courseId, @Query("column[title]") String title, @Query("column[position]") int position, @Query("column[hidden]") boolean isHidden, @Query("column[teacher_notes]") boolean isTeacherNotes, @Body String body, CanvasCallback<CustomColumn> callback);

        @GET("/courses/{course_id}/custom_gradebook_columns/{column_id}/data")
        void getColumnData(@Path("course_id") long courseId, @Path("column_id") long columnId, CanvasCallback<ColumnDatum[]> callback);

        @PUT("/courses/{course_id}/custom_gradebook_columns/{column_id}/data/{user_id}")
        void updateColumnData(@Path("course_id") long courseId, @Path("column_id") long columnId, @Path("user_id") long user_Id, @Query("column_data[content]") String content, @Body String body, CanvasCallback<ColumnDatum> callback);
    }

    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////

    public static void getGradebookColumns(long courseID, CanvasCallback<CustomColumn[]> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        buildCacheInterface(CustomGradebookInterface.class, callback).getGradebookColumns(courseID, callback);
        buildInterface(CustomGradebookInterface.class, callback).getGradebookColumns(courseID, callback);
    }

    public static void createGradebookColumns(long courseId, String title, int position, boolean isHidden, boolean isTeacherNotes, CanvasCallback<CustomColumn> callback) {
        if (APIHelpers.paramIsNull(callback, title)) { return; }

        buildInterface(CustomGradebookInterface.class, callback).createGradebookColumn(courseId, title, position, isHidden, isTeacherNotes, "", callback);
    }

    public static void getColumnData(long courseID, long columnId, CanvasCallback<ColumnDatum[]> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        buildCacheInterface(CustomGradebookInterface.class, callback).getColumnData(courseID, columnId, callback);
        buildInterface(CustomGradebookInterface.class, callback).getColumnData(courseID, columnId, callback);
    }

    public static void updateColumnData(long courseID, long columnId, long userId, String content, CanvasCallback<ColumnDatum> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        buildInterface(CustomGradebookInterface.class, callback).updateColumnData(courseID, columnId, userId, content, "", callback);
    }
}
