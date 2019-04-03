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

import com.instructure.canvasapi.model.Group;
import com.instructure.canvasapi.model.GroupCategory;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;


public class GroupCategoriesAPI extends BuildInterfaceAPI {

    interface GroupCategoriesInterface {

        @GET("/group_categories/{group_categories_id}/users")
        void getUsersForGroupCategory(@Path("group_categories_id") long groupCategoryId, CanvasCallback<User[]> callback);

        @GET("/courses/{course_id}/group_categories")
        void getFirstPageGroupCategories(@Path("course_id") long courseId, CanvasCallback<GroupCategory[]> callback);

        @GET("/{next}")
        void getNextPageGroupCategories(@Path(value = "next", encode = false) String nextURL, Callback<GroupCategory[]> callback);

        @POST("/courses/{course_id}/group_categories")
        void createGroupCategoryForCourse(@Path("course_id") long courseId, @Query("name") String name, @Body String body, CanvasCallback<GroupCategory> callback);

        @GET("/group_categories/{group_category_id}/groups")
        void getFirstPageGroupsFromCategory(@Path("group_category_id") long groupCategoryId, CanvasCallback<Group[]> callback);

        @GET("/group_categories/{group_category_id}/users")
        void getFirstPageUsersInCategory(@Path("group_category_id") long groupCategoryId, @Query("unassigned") boolean onlyIncludeUnassigned, CanvasCallback<User[]> callback);

        @GET("/{next}")
        void getNextPageUsersInCategory(@Path(value = "next", encode = false) String nextURL, Callback<User[]> callback);

    }

    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////
    public static void getFirstPageGroupsInCourse(long groupCategoryId, CanvasCallback<User[]> callback) {
        if (APIHelpers.paramIsNull(callback)) return;

        buildCacheInterface(GroupCategoriesInterface.class, callback).getUsersForGroupCategory(groupCategoryId, callback);
        buildInterface(GroupCategoriesInterface.class, callback).getUsersForGroupCategory(groupCategoryId, callback);
    }

    public static void createGroupCategoryForCourse(long courseId, String name, CanvasCallback<GroupCategory> callback) {
        if(APIHelpers.paramIsNull(name, callback)) return;

        buildInterface(GroupCategoriesInterface.class, callback).createGroupCategoryForCourse(courseId, name, "", callback);
    }

    public static void getFirstPageGroupCategoriesInCourse(long courseId, CanvasCallback<GroupCategory[]> callback) {
        if(APIHelpers.paramIsNull(callback)) return;

        buildCacheInterface(GroupCategoriesInterface.class, callback).getFirstPageGroupCategories(courseId, callback);
        buildInterface(GroupCategoriesInterface.class, callback).getFirstPageGroupCategories(courseId, callback);
    }

    public static void getNextPageGroupCategoriesInCourse(String nextURL, CanvasCallback<GroupCategory[]> callback) {
        if (APIHelpers.paramIsNull(callback, nextURL)) return;

        callback.setIsNextPage(true);
        buildCacheInterface(GroupCategoriesInterface.class, callback, false).getNextPageGroupCategories(nextURL, callback);
        buildInterface(GroupCategoriesInterface.class, callback, false).getNextPageGroupCategories(nextURL, callback);
    }

    public static void getFirstPageGroupsFromCategory(long groupCategoryId, CanvasCallback<Group[]> callback) {
        if(APIHelpers.paramIsNull(callback)) return;

        buildCacheInterface(GroupCategoriesInterface.class, callback).getFirstPageGroupsFromCategory(groupCategoryId, callback);
        buildInterface(GroupCategoriesInterface.class, callback).getFirstPageGroupsFromCategory(groupCategoryId, callback);
    }

    public static void getFirstPageUsersInCategory(long groupCategoryId, boolean onlyIncludeUnassigned, CanvasCallback<User[]> callback) {
        if(APIHelpers.paramIsNull(callback)) return;

        buildCacheInterface(GroupCategoriesInterface.class, callback).getFirstPageUsersInCategory(groupCategoryId, onlyIncludeUnassigned, callback);
        buildInterface(GroupCategoriesInterface.class, callback).getFirstPageUsersInCategory(groupCategoryId, onlyIncludeUnassigned, callback);
    }

    public static void getNextPageUsersInCategory(String nextURL, CanvasCallback<User[]> callback) {
        if (APIHelpers.paramIsNull(callback, nextURL)) return;

        callback.setIsNextPage(true);
        buildCacheInterface(GroupCategoriesInterface.class, callback, false).getNextPageUsersInCategory(nextURL, callback);
        buildInterface(GroupCategoriesInterface.class, callback, false).getNextPageUsersInCategory(nextURL, callback);
    }
}
