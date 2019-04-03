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

import com.instructure.canvasapi.model.Attachment;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Enrollment;
import com.instructure.canvasapi.model.Favorite;
import com.instructure.canvasapi.model.FileUploadParams;
import com.instructure.canvasapi.model.GradingPeriodResponse;
import com.instructure.canvasapi.model.kaltura.FileUploadParamsWrapper;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.ExhaustiveBridgeCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.PartMap;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedFile;


public class CourseAPI extends BuildInterfaceAPI {

    interface CoursesInterface {

        @PUT("/courses/{courseid}")
        void updateCourse(@Path("courseid") long courseID,
                          @Query("course[name]") String name, @Query("course[course_code]") String courseCode,
                          @Query("course[start_at]") String startAt, @Query("course[end_at]") String endAt,
                          @Query("course[license]") String license, @Query("course[is_public]") Integer isPublic,
                          @Body String body,
                          CanvasCallback<Course> callback);

        @GET("/courses/{courseid}?include[]=term&include[]=permissions&include[]=license&include[]=is_public&include[]=needs_grading_count")
        void getCourse(@Path("courseid") long courseId, CanvasCallback<Course> callback);

        @GET("/courses/{courseid}?include[]=term&include[]=permissions&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=total_scores&include[]=current_grading_period_scores")
        void getCourseWithGrade(@Path("courseid") long courseId, CanvasCallback<Course> callback);

        @GET("/courses/{courseid}/enrollments")
        void getEnrollmentsForGradingPeriod(@Path("courseid") long courseId, @Query("grading_period_id") long gradingPeriodId, CanvasCallback<Enrollment[]> callback);

        @GET("/courses/{courseid}?include[]=syllabus_body&include[]=term&include[]=license&include[]=is_public&include[]=permissions")
        void getCourseWithSyllabus(@Path("courseid") long courseId, CanvasCallback<Course> callback);

        // I don't see why we wouldn't want to always get the grades
        @GET("/courses?include[]=term&include[]=total_scores&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=permissions&include[]=favorites&include[]=current_grading_period_scores&enrollment_state=active")
        void getFirstPageCourses(CanvasCallback<Course[]> callback);

        @GET("/{next}?&include[]=needs_grading_count&include[]=permissions&include[]=favorites")
        void getNextPageCourses(@Path(value = "next", encode = false) String nextURL, CanvasCallback<Course[]> callback);

        @GET("/users/self/favorites/courses?include[]=term&include[]=total_scores&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=permissions&include[]=current_grading_period_scores")
        void getFavoriteCourses(CanvasCallback<Course[]> callback);

        @GET("/courses/{courseId}/grading_periods")
        void getGradingPeriodsForCourse(@Path("courseId") long courseId, CanvasCallback<GradingPeriodResponse> callback);

        @POST("/users/self/favorites/courses/{courseId}")
        void addCourseToFavorites(@Path("courseId") long courseId, @Body String body, CanvasCallback<Favorite> callback);

        @DELETE("/users/self/favorites/courses/{courseId}")
        void removeCourseFromFavorites(@Path("courseId") long courseId, CanvasCallback<Favorite> callback);

        @GET("/users/{user_id}/courses?include[]=total_scores&include[]=syllabus_body")
        void getCoursesForUser(@Path("user_id") long userId, CanvasCallback<Course[]> callback);

        @GET("/{next}")
        void getNextPageCoursesForUser(@Path(value = "next", encode = false) String nextURL, CanvasCallback<Course[]> callback);

        @GET("/canvas/{parentId}/{studentId}/courses?include[]=total_scores&include[]=syllabus_body&include[]=current_grading_period_scores")
        void getCoursesForUserAirwolf(@Path("parentId") String parentId, @Path("studentId") String studentId, CanvasCallback<Course[]> callback);

        @GET("/canvas/{parentId}/{studentId}/courses/{courseId}?include[]=syllabus_body&include[]=term&include[]=license&include[]=is_public&include[]=permissions")
        void getCourseWithSyllabusAirwolf(@Path("parentId") String parentId, @Path("studentId") String studentId, @Path("courseId") long courseId, CanvasCallback<Course> callback);

        @GET("/canvas/{parentId}/{studentId}/courses/{courseId}?include[]=term&include[]=permissions&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=total_scores&include[]=current_grading_period_scores")
        void getCourseWithGradeAirwolf(@Path("parentId") String parentId, @Path("studentId") String studentId, @Path("courseId") long courseId, CanvasCallback<Course> callback);

        /////////////////////////////////////////////////////////////////////////////
        // Synchronous
        /////////////////////////////////////////////////////////////////////////////
        @GET("/courses?include[]=term&include[]=total_scores&include[]=license&include[]=is_public&include[]=permissions")
        Course[] getAllCoursesSynchronous(@Query("page") int page);

        @GET("/users/self/favorites/courses?include[]=term&include[]=total_scores&include[]=license&include[]=is_public&include[]=permissions")
        Course[] getFavCoursesSynchronous(@Query("page") int page);

        @POST("/courses/{courseId}/files")
        FileUploadParams getFileUploadParams(@Path("courseId") long courseId, @Query("parent_folder_id") Long parentFolderId, @Query("size") long size, @Query("name") String fileName, @Query("content_type") String content_type, @Body String body);

        @POST("/courses/{courseId}/quizzes/{quizId}/submissions/self/files")
        FileUploadParamsWrapper getQuizFileUploadParams(@Query("name") String name, @Query("duplicate_name") String duplicateName, @Path("courseId") long courseId, @Path("quizId") long quizId, @Body String body);

        @Multipart
        @POST("/")
        Attachment uploadCourseFile(@PartMap LinkedHashMap<String, String> params, @Part("file") TypedFile file);

        @Multipart
        @POST("/")
        Attachment uploadQuizFile(@PartMap LinkedHashMap<String, String> params, @Part("file") TypedFile file);
    }

    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////

    public static void getCourse(long courseId, CanvasCallback<Course> callback) {
        if (APIHelpers.paramIsNull(callback)) return;

        buildCacheInterface(CoursesInterface.class, callback, false).getCourse(courseId, callback);
        buildInterface(CoursesInterface.class, callback, false).getCourse(courseId, callback);
    }

    public static void getCourseWithGrade(long courseId, CanvasCallback<Course> callback) {
        if (APIHelpers.paramIsNull(callback)) return;

        buildCacheInterface(CoursesInterface.class, callback, false).getCourseWithGrade(courseId, callback);
        buildInterface(CoursesInterface.class, callback, false).getCourseWithGrade(courseId, callback);
    }

    public static void getCourseWithSyllabus(long courseId, CanvasCallback<Course> callback) {
        if (APIHelpers.paramIsNull(callback)) return;

        buildCacheInterface(CoursesInterface.class, callback, false).getCourseWithSyllabus(courseId, callback);
        buildInterface(CoursesInterface.class, callback, false).getCourseWithSyllabus(courseId, callback);
    }

    public static void getFirstPageCourses(CanvasCallback<Course[]> callback) {
        if (APIHelpers.paramIsNull(callback)) return;

        buildCacheInterface(CoursesInterface.class, callback).getFirstPageCourses(callback);
        buildInterface(CoursesInterface.class, callback).getFirstPageCourses(callback);
    }

    public static void getFirstPageFavoriteCourses(CanvasCallback<Course[]> callback) {
        if (APIHelpers.paramIsNull(callback)) return;

        buildCacheInterface(CoursesInterface.class, callback).getFavoriteCourses(callback);
        buildInterface(CoursesInterface.class, callback).getFavoriteCourses(callback);
    }

    public static void getGradingPeriodsForCourse(long courseId, CanvasCallback<GradingPeriodResponse> callback) {
        if (APIHelpers.paramIsNull(callback)) return;

        buildCacheInterface(CoursesInterface.class, callback).getGradingPeriodsForCourse(courseId, callback);
        buildInterface(CoursesInterface.class, callback).getGradingPeriodsForCourse(courseId, callback);
    }

    public static void getEnrollmentsForGradingPeriod(long courseId, long gradingPeriodId, CanvasCallback<Enrollment[]> callback) {
        if (APIHelpers.paramIsNull(callback)) return;

        buildCacheInterface(CoursesInterface.class, callback).getEnrollmentsForGradingPeriod(courseId, gradingPeriodId, callback);
        buildInterface(CoursesInterface.class, callback).getEnrollmentsForGradingPeriod(courseId, gradingPeriodId, callback);
    }

    public static void getNextPageCourses(CanvasCallback<Course[]> callback, String nextURL) {
        if (APIHelpers.paramIsNull(callback, nextURL)) return;

        callback.setIsNextPage(true);
        buildCacheInterface(CoursesInterface.class, callback).getNextPageCourses(nextURL, callback);
        buildInterface(CoursesInterface.class, callback).getNextPageCourses(nextURL, callback);
    }

    public static void getNextPageCoursesChained(CanvasCallback<Course[]> callback, String nextURL, boolean isCached) {
        if (APIHelpers.paramIsNull(callback, nextURL)) return;

        callback.setIsNextPage(true);
        if (isCached) {
            buildCacheInterface(CoursesInterface.class, callback).getNextPageCourses(nextURL, callback);
        } else {
            buildInterface(CoursesInterface.class, callback).getNextPageCourses(nextURL, callback);
        }
    }

    public static void addCourseToFavorites(final long courseId, final CanvasCallback<Favorite> callback) {
        if (APIHelpers.paramIsNull(callback)) return;

        buildInterface(CoursesInterface.class, callback).addCourseToFavorites(courseId, "", callback);
    }

    public static void removeCourseFromFavorites(final long courseId, final CanvasCallback<Favorite> callback) {
        if (APIHelpers.paramIsNull(callback)) return;

        buildInterface(CoursesInterface.class, callback).removeCourseFromFavorites(courseId, callback);
    }

    public static void getAllFavoriteCoursesChained(final CanvasCallback<Course[]> callback, boolean isCached) {
        if (APIHelpers.paramIsNull(callback)) return;

        CanvasCallback<Course[]> bridge = new ExhaustiveBridgeCallback<>(Course.class, callback, new ExhaustiveBridgeCallback.ExhaustiveBridgeEvents() {
            @Override
            public void performApiCallWithExhaustiveCallback(CanvasCallback bridgeCallback, String nextURL, boolean isCached) {
                if(callback.isCancelled()) { return; }

                CourseAPI.getNextPageCoursesChained(bridgeCallback, nextURL, isCached);
            }
        });

        if (isCached) {
            buildCacheInterface(CoursesInterface.class, callback).getFavoriteCourses(bridge);
        } else {
            buildInterface(CoursesInterface.class, callback).getFavoriteCourses(bridge);
        }
    }

    public static void getAllFavoriteCourses(final CanvasCallback<Course[]> callback) {
        if (APIHelpers.paramIsNull(callback)) return;

        CanvasCallback<Course[]> bridge = new ExhaustiveBridgeCallback<>(Course.class, callback, new ExhaustiveBridgeCallback.ExhaustiveBridgeEvents() {
            @Override
            public void performApiCallWithExhaustiveCallback(CanvasCallback bridgeCallback, String nextURL, boolean isCached) {
                if(callback.isCancelled()) { return; }

                CourseAPI.getNextPageCoursesChained(bridgeCallback, nextURL, isCached);
            }
        });

        buildCacheInterface(CoursesInterface.class, callback).getFavoriteCourses(bridge);
        buildInterface(CoursesInterface.class, callback).getFavoriteCourses(bridge);
    }

    public static void getAllCourses(final CanvasCallback<Course[]> callback) {
        if (APIHelpers.paramIsNull(callback)) return;

        CanvasCallback<Course[]> bridge = new ExhaustiveBridgeCallback<>(Course.class, callback, new ExhaustiveBridgeCallback.ExhaustiveBridgeEvents() {
            @Override
            public void performApiCallWithExhaustiveCallback(CanvasCallback bridgeCallback, String nextURL, boolean isCached) {
                if(callback.isCancelled()) { return; }

                CourseAPI.getNextPageCoursesChained(bridgeCallback, nextURL, isCached);
            }
        });

        buildCacheInterface(CoursesInterface.class, callback).getFirstPageCourses(bridge);
        buildInterface(CoursesInterface.class, callback).getFirstPageCourses(bridge);
    }

    /**
     * @param newCourseName (Optional)
     * @param newCourseCode (Optional)
     * @param newStartAt    (Optional)
     * @param newEndAt      (Optional)
     * @param license       (Optional)
     * @param newIsPublic   (Optional)
     * @param course        (Required)
     * @param callback      (Required)
     */
    public static void updateCourse(String newCourseName, String newCourseCode, Date newStartAt, Date newEndAt, Course.LICENSE license, Boolean newIsPublic, Course course, CanvasCallback<Course> callback) {
        if (APIHelpers.paramIsNull(callback, course)) return;

        String newStartAtString = APIHelpers.dateToString(newStartAt);
        String newEndAtString = APIHelpers.dateToString(newEndAt);
        Integer newIsPublicInteger = (newIsPublic == null) ? null : APIHelpers.booleanToInt(newIsPublic);

        buildInterface(CoursesInterface.class, callback).updateCourse(course.getId(), newCourseName, newCourseCode, newStartAtString, newEndAtString, Course.licenseToAPIString(license), newIsPublicInteger, "", callback);
    }

    public static void getCoursesForUser(long userId, CanvasCallback<Course[]> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        buildCacheInterface(CoursesInterface.class, callback).getCoursesForUser(userId, callback);
        buildInterface(CoursesInterface.class, callback).getCoursesForUser(userId, callback);

    }

    public static void getNextPageCoursesForUser(String nextURL, CanvasCallback<Course[]> callback) {
        if (APIHelpers.paramIsNull(nextURL, callback)) { return; }

        callback.setIsNextPage(true);

        buildCacheInterface(CoursesInterface.class, callback).getNextPageCoursesForUser(nextURL, callback);
        buildInterface(CoursesInterface.class, callback).getNextPageCoursesForUser(nextURL, callback);
    }

    public static void getCoursesForUserAirwolf(String parentId, String studentId, CanvasCallback<Course[]> callback) {
        if(APIHelpers.paramIsNull(parentId, studentId, callback)) { return; }

        buildCacheInterface(CoursesInterface.class, APIHelpers.getAirwolfDomain(callback.getContext()), callback).getCoursesForUserAirwolf(parentId, studentId, callback);
        buildInterface(CoursesInterface.class, APIHelpers.getAirwolfDomain(callback.getContext()), callback).getCoursesForUserAirwolf(parentId, studentId, callback);
    }

    public static void getNextPageCoursesForUserAirwolf(String nextURL, CanvasCallback<Course[]> callback) {
        if (APIHelpers.paramIsNull(nextURL, callback)) { return; }

        callback.setIsNextPage(true);

        buildCacheInterface(CoursesInterface.class, APIHelpers.getAirwolfDomain(callback.getContext()), callback).getNextPageCoursesForUser(nextURL, callback);
        buildInterface(CoursesInterface.class, APIHelpers.getAirwolfDomain(callback.getContext()), callback).getNextPageCoursesForUser(nextURL, callback);
    }

    public static void getCourseWithSyllabusAirwolf(String parentId, String studentId, long courseId, CanvasCallback<Course> callback) {
        if (APIHelpers.paramIsNull(parentId, studentId, callback)) return;

        buildCacheInterface(CoursesInterface.class, APIHelpers.getAirwolfDomain(callback.getContext()), callback, false).getCourseWithSyllabusAirwolf(parentId, studentId, courseId, callback);
        buildInterface(CoursesInterface.class, APIHelpers.getAirwolfDomain(callback.getContext()), callback, false).getCourseWithSyllabusAirwolf(parentId, studentId, courseId, callback);
    }

    public static void getCourseWithGradeAirwolf(String parentId, String studentId, long courseId, CanvasCallback<Course> callback) {
        if (APIHelpers.paramIsNull(parentId, studentId, callback)) return;

        buildCacheInterface(CoursesInterface.class, APIHelpers.getAirwolfDomain(callback.getContext()), callback, false).getCourseWithGradeAirwolf(parentId, studentId, courseId, callback);
        buildInterface(CoursesInterface.class, APIHelpers.getAirwolfDomain(callback.getContext()), callback, false).getCourseWithGradeAirwolf(parentId, studentId, courseId, callback);
    }
    /////////////////////////////////////////////////////////////////////////////
    // Helper Methods
    ////////////////////////////////////////////////////////////////////////////
    public static Map<Long, Course> createCourseMap(Course[] courses) {
        Map<Long, Course> courseMap = new HashMap<Long, Course>();
        if(courses == null) {
            return courseMap;
        }
        for (Course course : courses) {
            courseMap.put(course.getId(), course);
        }
        return courseMap;
    }

    /////////////////////////////////////////////////////////////////////////////
    // Synchronous
    //
    // If Retrofit is unable to parse (no network for example) Synchronous calls
    // will throw a nullPointer exception. All synchronous calls need to be in a
    // try catch block.
    /////////////////////////////////////////////////////////////////////////////

    public static Course[] getAllCoursesSynchronous(Context context) {
        RestAdapter restAdapter = CanvasRestAdapter.buildAdapter(context);

        //If not able to parse (no network for example), this will crash. Handle that case.
        try {
            ArrayList<Course> allCourses = new ArrayList<>();
            int page = 1;
            long firstItemId = -1;

            //for(ever) loop. break once we've run outta stuff;
            for (;;) {
                Course[] courses = restAdapter.create(CoursesInterface.class).getAllCoursesSynchronous(page);
                page++;

                //This is all or nothing. We don't want partial data.
                if(courses == null){
                    return null;
                } else if (courses.length == 0) {
                    break;
                } else if(courses[0].getId() == firstItemId){
                    break;
                } else {
                    firstItemId = courses[0].getId();

                    Collections.addAll(allCourses, courses);
                }
            }

            return allCourses.toArray(new Course[allCourses.size()]);

        } catch (Exception E) {
            return null;
        }
    }

    public static Course[] getFavCoursesSynchronous(Context context) {
        RestAdapter restAdapter = CanvasRestAdapter.buildAdapter(context);

        //If not able to parse (no network for example), this will crash. Handle that case.
        try {
            ArrayList<Course> allCourses = new ArrayList<>();
            int page = 1;
            long firstItemId = -1;

            //for(ever) loop. break once we've run outta stuff;
            for (;;) {
                Course[] courses = restAdapter.create(CoursesInterface.class).getFavCoursesSynchronous(page);
                page++;

                //This is all or nothing. We don't want partial data.
                if(courses == null){
                    return null;
                } else if (courses.length == 0) {
                    break;
                } else if(courses[0].getId() == firstItemId){
                    break;
                } else {
                    firstItemId = courses[0].getId();

                    Collections.addAll(allCourses, courses);
                }
            }

            return allCourses.toArray(new Course[allCourses.size()]);

        } catch (Exception E) {
            return null;
        }
    }

    public static FileUploadParams getFileUploadParams(Context context, long courseId, Long parentFolderId, String fileName, long size, String contentType){
        return buildInterface(CoursesInterface.class, context).getFileUploadParams(courseId, parentFolderId, size, fileName, contentType, "");
    }

    public static FileUploadParams getQuizFileUploadParams(Context context, long courseId, long quizId, String name, String duplicateName){
        return buildInterface(CoursesInterface.class, context, false).getQuizFileUploadParams(name, duplicateName, courseId, quizId, "").getUploadParams().get(0);
    }

    public static Attachment uploadCourseFile(Context context, String uploadUrl, LinkedHashMap<String,String> uploadParams, String mimeType, File file){
        return buildUploadInterface(CoursesInterface.class, uploadUrl).uploadCourseFile(uploadParams, new TypedFile(mimeType, file));
    }

    public static Attachment uploadQuizFile(String uploadUrl, LinkedHashMap<String,String> uploadParams, String mimeType, File file){
        return buildUploadInterface(CoursesInterface.class, uploadUrl).uploadQuizFile(uploadParams, new TypedFile(mimeType, file));
    }
}
