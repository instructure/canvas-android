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

import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Section;
import com.instructure.canvasapi.model.Submission;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;

import java.util.Date;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;


public class SectionAPI extends BuildInterfaceAPI {

    interface SectionsInterface {

        @PUT("{courseid}/sections/{sectionid}")
        void updateSection(@Path("courseid") long courseID,
                           @Path("sectionid") long sectionID,
                           @Query("course_section[name]") String name,
                           @Query("course_section[start_at]") String startAt,
                           @Query("course_section[end_at]") String endAt,
                           @Body String body,
                           CanvasCallback<Section> callback
        );

        @GET("/{courseid}/sections")
        void getFirstPageSectionsList(@Path("courseid") long courseID, Callback<Section[]> callback);

        @GET("/{courseid}/sections?include[]=students&include[]=avatar_url")
        void getCourseSectionsWithStudents(@Path("courseid") long courseID, Callback<Section[]> callback);

        @GET("/{next}")
        void getNextPageSectionsList(@Path(value = "next", encode = false) String nextURL, Callback<Section[]> callback);

        @GET("/courses/{courseid}/sections/{sectionid}")
        void getSingleSection(@Path("courseid") long courseID, @Path("sectionid") long sectionID, Callback<Section> callback);

        @GET("/{section_id}/assignments/{assignment_id}/submissions")
        void getAssignmentSubmissionsForSection(@Path("section_id") long section_id, @Path("assignment_id") long assignment_id, Callback<Submission[]> callback);
    }


    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////
    public static void getFirstPageSectionsList(Course course, CanvasCallback<Section[]> callback) {
        if (APIHelpers.paramIsNull(callback, course)) { return; }

        buildCacheInterface(SectionsInterface.class, callback, course).getFirstPageSectionsList(course.getId(), callback);
        buildInterface(SectionsInterface.class, callback, course).getFirstPageSectionsList(course.getId(), callback);
    }

    public static void getCourseSectionsWithStudents(Course course, CanvasCallback<Section[]> callback){
        if (APIHelpers.paramIsNull(callback, course)) { return; }

        buildCacheInterface(SectionsInterface.class, callback, course).getCourseSectionsWithStudents(course.getId(), callback);
        buildInterface(SectionsInterface.class, callback, course).getCourseSectionsWithStudents(course.getId(), callback);
    }

    public static void getNextPageSectionsList(String nextURL, CanvasCallback<Section[]> callback){
        if (APIHelpers.paramIsNull(callback, nextURL)) { return; }

        callback.setIsNextPage(true);

        buildCacheInterface(SectionsInterface.class, callback, false).getNextPageSectionsList(nextURL, callback);
        buildInterface(SectionsInterface.class, callback, false).getNextPageSectionsList(nextURL, callback);
    }

    public static void getAssignmentSubmissionsForSection(CanvasContext canvasContext, long assignment_id, final CanvasCallback<Submission[]> callback){
        if(APIHelpers.paramIsNull(callback, canvasContext)){return;}

        buildCacheInterface(SectionsInterface.class, callback, canvasContext).getAssignmentSubmissionsForSection(canvasContext.getId(), assignment_id, callback);
        buildInterface(SectionsInterface.class, callback, canvasContext).getAssignmentSubmissionsForSection(canvasContext.getId(), assignment_id, callback);
    }

    /**
     *
     * @param newSectionName (Optional)
     * @param newStartAt (Optional)
     * @param newEndAt (Optional)
     * @param course (Required)
     * @param section (Required)
     * @param callback (Required)
     */
    public static void updateSection(String newSectionName, Date newStartAt, Date newEndAt, Course course, Section section, CanvasCallback<Section> callback){
        if(APIHelpers.paramIsNull(callback, course, section)){return;}


        String startAtString = APIHelpers.dateToString(newStartAt);
        String endAtString = APIHelpers.dateToString(newEndAt);

        buildInterface(SectionsInterface.class, callback,course).updateSection(course.getId(), section.getId(), newSectionName, startAtString, endAtString, "", callback);

    }

    public static void getSingleSection(long courseID, long sectionID, CanvasCallback<Section> callback){
        if (APIHelpers.paramIsNull(callback)) { return; }

        buildCacheInterface(SectionsInterface.class, callback, null).getSingleSection(courseID, sectionID, callback);
        buildInterface(SectionsInterface.class, callback, null).getSingleSection(courseID, sectionID, callback);
    }

}
