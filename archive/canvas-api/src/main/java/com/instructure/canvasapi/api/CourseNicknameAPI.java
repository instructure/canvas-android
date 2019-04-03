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

import com.instructure.canvasapi.model.CourseNickname;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;


public class CourseNicknameAPI extends BuildInterfaceAPI {

    interface NicknameInterface {

        @GET("/users/self/course_nicknames/")
        void getAllNicknames(Callback<List<CourseNickname>> callback);

        @GET("/users/self/course_nicknames/{course_id}")
        void getNickname(@Path("course_id") long courseId, Callback<CourseNickname> callback);

        @PUT("/users/self/course_nicknames/{course_id}")
        void setNickname(@Path("course_id") long courseId, @Query("nickname") String nickname, @Body String body, Callback<CourseNickname> callback);

        @DELETE("/users/self/course_nicknames/{course_id}")
        void deleteNickname(@Path("course_id") long courseId, Callback<CourseNickname> callback);

        @DELETE("/users/self/course_nicknames/")
        void deleteAllNicknames(Callback<CourseNickname> callback);
    }

    public static void getAllNicknames(CanvasCallback<List<CourseNickname>> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        buildInterface(NicknameInterface.class, callback, false).getAllNicknames(callback);
    }

    public static void getNickname(long courseId, CanvasCallback<CourseNickname> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        buildInterface(NicknameInterface.class, callback, false).getNickname(courseId, callback);
    }

    public static void setNickname(long courseId, String nickname, CanvasCallback<CourseNickname> callback) {
        if (APIHelpers.paramIsNull(callback, nickname) || nickname.length() == 0) { return; }

        //Reduces the nickname to only 60 max chars per the api docs.
        nickname = nickname.substring(0, Math.min(nickname.length(), 60));

        buildInterface(NicknameInterface.class, callback, false).setNickname(courseId, nickname, "", callback);
    }

    public static void deleteNickname(long courseId, CanvasCallback<CourseNickname> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        buildInterface(NicknameInterface.class, callback, false).deleteNickname(courseId, callback);
    }

    public static void deleteAllNicknames(CanvasCallback<CourseNickname> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        buildInterface(NicknameInterface.class, callback, false).deleteAllNicknames(callback);
    }
}
