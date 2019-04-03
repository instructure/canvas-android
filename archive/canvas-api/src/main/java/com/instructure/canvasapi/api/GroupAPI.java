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

import com.instructure.canvasapi.model.Favorite;
import com.instructure.canvasapi.model.Group;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.ExhaustiveBridgeCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;


public class GroupAPI extends BuildInterfaceAPI {

    interface GroupsInterface {
        @GET("/users/self/groups?include[]=favorites")
        void getFirstPageGroups(CanvasCallback<Group[]> callback);

        @GET("/courses/{courseid}/groups?include[]=favorites&include=users")
        void getFirstPageGroupsInCourse(@Path("courseid") long courseId, CanvasCallback<Group[]> callback);

        @GET("/{next}?include[]=favorites")
        void getNextPageGroups(@Path(value = "next", encode = false)String nextURL, CanvasCallback<Group[]> callback);

        @GET("/groups/{groupid}?include[]=permissions&include[]=favorites")
        void getDetailedGroup(@Path("groupid") long groupId, CanvasCallback<Group> callback);

        @GET("/groups/{groupid}/users")
        void getGroupUsers(@Path("groupid") long groupId, CanvasCallback<User[]> callback);

        @GET("/groups/{groupid}/users?include[]=avatar_url")
        void getGroupUsersWithAvatars(@Path("groupid") long groupId, CanvasCallback<User[]> callback);

        @GET("/{next}?[]=favorites")
        void getNextPageGroupUsers(@Path(value = "next", encode = false) String nextURL, CanvasCallback<User[]> callback);

        @POST("/groups?[]=favorites")
        void createGroup(@Query("name") String name, @Query("is_public") boolean isPublic, @Body String body, CanvasCallback<Group> callback);

        @DELETE("/groups/{groupid}")
        void deleteGroup(@Path("groupid") long groupId, CanvasCallback<Response> callback);

        @POST("/groups/{groupid}/memberships")
        void createMembership(@Path("groupid") long groupId, @Query("user_id") String userId, @Body String body, CanvasCallback<Response> callback);

        @POST("/group_categories/{group_category_id}/groups")
        void createGroupWithCategory(@Path("group_category_id") long groupCategoryId, @Query("name") String name, @Query("is_public") boolean isPublic, @Body String body, CanvasCallback<Group> callback);

        @GET("/users/self/favorites/groups?[]=favorites")
        void getFavoriteGroups(CanvasCallback<Group[]> callback);

        @POST("/users/self/favorites/groups/{groupId}")
        void addGroupToFavorites(@Path("groupId") long groupId, @Body String body, CanvasCallback<Favorite> callback);

        @DELETE("/users/self/favorites/groups/{groupId}")
        void removeGroupFromFavorites(@Path("groupId") long groupId, CanvasCallback<Favorite> callback);

        /////////////////////////////////////////////////////////////////////////////
        // Synchronous
        /////////////////////////////////////////////////////////////////////////////

        @GET("/users/self/groups")
        Group[] getGroupsSynchronous(@Query("page") int page);
    }

    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////

    public static void getAllGroups(final CanvasCallback<Group[]> callback){
        if(APIHelpers.paramIsNull(callback)) return;

        CanvasCallback<Group[]> bridge = new ExhaustiveBridgeCallback<>(Group.class, callback, new ExhaustiveBridgeCallback.ExhaustiveBridgeEvents() {
            @Override
            public void performApiCallWithExhaustiveCallback(CanvasCallback bridgeCallback, String nextURL, boolean isCached) {
                if(callback.isCancelled()) { return; }

                getNextPageGroupsChained(nextURL, bridgeCallback, isCached);
            }
        });

        buildCacheInterface(GroupsInterface.class, callback).getFirstPageGroups(bridge);
        buildInterface(GroupsInterface.class, callback).getFirstPageGroups(bridge);
    }

    public static void getAllGroupsInCourse(long courseID, final CanvasCallback<Group[]> callback) {
        if (APIHelpers.paramIsNull(callback)) return;

        CanvasCallback<Group[]> bridge = new ExhaustiveBridgeCallback<>(Group.class, callback, new ExhaustiveBridgeCallback.ExhaustiveBridgeEvents() {
            @Override
            public void performApiCallWithExhaustiveCallback(CanvasCallback bridgeCallback, String nextURL, boolean isCached) {
                if(callback.isCancelled()) { return; }

                getNextPageGroupsChained(nextURL, bridgeCallback, isCached);
            }
        });

        buildCacheInterface(GroupsInterface.class, callback).getFirstPageGroupsInCourse(courseID, bridge);
        buildInterface(GroupsInterface.class, callback).getFirstPageGroupsInCourse(courseID, bridge);
    }

    public static void getGroupsForUserChained(final CanvasCallback<Group[]> callback, boolean isCached) {
        if (APIHelpers.paramIsNull(callback)) return;

        CanvasCallback<Group[]> bridge = new ExhaustiveBridgeCallback<>(Group.class, callback, new ExhaustiveBridgeCallback.ExhaustiveBridgeEvents() {
            @Override
            public void performApiCallWithExhaustiveCallback(CanvasCallback bridgeCallback, String nextURL, boolean isCached) {
                if(callback.isCancelled()) { return; }

                getNextPageGroupsChained(nextURL, bridgeCallback, isCached);
            }
        });

        if (isCached) {
            buildCacheInterface(GroupsInterface.class, callback).getFirstPageGroups(bridge);
        } else {
            buildInterface(GroupsInterface.class, callback).getFirstPageGroups(bridge);
        }
    }

    public static void getGroupsForUser(final CanvasCallback<Group[]> callback) {
        if (APIHelpers.paramIsNull(callback)) return;

        CanvasCallback<Group[]> bridge = new ExhaustiveBridgeCallback<>(Group.class, callback, new ExhaustiveBridgeCallback.ExhaustiveBridgeEvents() {
            @Override
            public void performApiCallWithExhaustiveCallback(CanvasCallback bridgeCallback, String nextURL, boolean isCached) {
                if(callback.isCancelled()) { return; }

                getNextPageGroupsChained(nextURL, bridgeCallback, isCached);
            }
        });

        buildCacheInterface(GroupsInterface.class, callback).getFirstPageGroups(bridge);
        buildInterface(GroupsInterface.class, callback).getFirstPageGroups(bridge);
    }

    public static void getNextPageGroups(String nextURL, CanvasCallback<Group[]> callback) {
        if (APIHelpers.paramIsNull(callback, nextURL)) return;

        callback.setIsNextPage(true);
        buildCacheInterface(GroupsInterface.class, callback, false).getNextPageGroups(nextURL, callback);
        buildInterface(GroupsInterface.class, callback, false).getNextPageGroups(nextURL, callback);
    }

    public static void getNextPageGroupsChained(String nextURL, CanvasCallback<Group[]> callback, boolean isCached) {
        if (APIHelpers.paramIsNull(callback, nextURL)) return;

        callback.setIsNextPage(true);
        if (isCached) {
            buildCacheInterface(GroupsInterface.class, callback, false).getNextPageGroups(nextURL, callback);
        } else {
            buildInterface(GroupsInterface.class, callback, false).getNextPageGroups(nextURL, callback);
        }
    }

    public static void getDetailedGroup(long groupId, CanvasCallback<Group> callback) {
        if (APIHelpers.paramIsNull(callback)) return;

        buildCacheInterface(GroupsInterface.class, callback).getDetailedGroup(groupId, callback);
        buildInterface(GroupsInterface.class, callback).getDetailedGroup(groupId, callback);
    }

    public static void getGroupUsers(long groupId, CanvasCallback<User[]> callback) {
        if (APIHelpers.paramIsNull(groupId, callback)) return;

        buildCacheInterface(GroupsInterface.class, callback).getGroupUsers(groupId, callback);
        buildInterface(GroupsInterface.class, callback).getGroupUsers(groupId, callback);
    }

    public static void getGroupUsersWithAvatars(long groupId, CanvasCallback<User[]> callback) {
        if (APIHelpers.paramIsNull(groupId, callback)) return;

        buildCacheInterface(GroupsInterface.class, callback).getGroupUsersWithAvatars(groupId, callback);
        buildInterface(GroupsInterface.class, callback).getGroupUsersWithAvatars(groupId, callback);
    }

    public static void getNextPageGroupUsers(String nextURL, CanvasCallback<User[]> callback) {
        if (APIHelpers.paramIsNull(callback, nextURL)) return;

        callback.setIsNextPage(true);
        buildCacheInterface(GroupsInterface.class, callback, false).getNextPageGroupUsers(nextURL, callback);
        buildInterface(GroupsInterface.class, callback, false).getNextPageGroupUsers(nextURL, callback);
    }

    public static void createGroup(String name, boolean isPublic, CanvasCallback<Group> callback) {
        if (APIHelpers.paramIsNull(name, callback)) return;

        buildInterface(GroupsInterface.class, callback).createGroup(name, isPublic, "", callback);
    }

    public static void createGroupWithCategory(long categoryId, String name, boolean isPublic, CanvasCallback<Group> callback) {
        if (APIHelpers.paramIsNull(name, callback)) return;

        buildInterface(GroupsInterface.class, callback).createGroupWithCategory(categoryId, name, isPublic, "", callback);
    }

    public static void deleteGroup(long groupId, CanvasCallback<Response>responseCanvasCallback){
        if(APIHelpers.paramIsNull(responseCanvasCallback)){return;}

        buildInterface(GroupsInterface.class, responseCanvasCallback).deleteGroup(groupId, responseCanvasCallback);
    }

    public static void createMembership(long groupId, String userId, CanvasCallback<Response> callback) {
        if (APIHelpers.paramIsNull(userId, callback)) return;

        buildInterface(GroupsInterface.class, callback).createMembership(groupId, userId, "", callback);
    }

    public static void addGroupToFavorites(final long groupId, final CanvasCallback<Favorite> callback) {
        if (APIHelpers.paramIsNull(callback)) return;

        buildInterface(GroupsInterface.class, callback).addGroupToFavorites(groupId, "", callback);
    }

    public static void removeGroupFromFavorites(final long groupId, final CanvasCallback<Favorite> callback) {
        if (APIHelpers.paramIsNull(callback)) return;

        buildInterface(GroupsInterface.class, callback).removeGroupFromFavorites(groupId, callback);
    }

    public static void getNextPageGroupsChained(CanvasCallback<Group[]> callback, String nextURL, boolean isCached) {
        if (APIHelpers.paramIsNull(callback, nextURL)) return;

        callback.setIsNextPage(true);
        if (isCached) {
            buildCacheInterface(GroupsInterface.class, callback).getNextPageGroups(nextURL, callback);
        } else {
            buildInterface(GroupsInterface.class, callback).getNextPageGroups(nextURL, callback);
        }
    }

    public static void getAllFavoriteGroupsChained(final CanvasCallback<Group[]> callback, boolean isCached) {
        if (APIHelpers.paramIsNull(callback)) return;

        CanvasCallback<Group[]> bridge = new ExhaustiveBridgeCallback<>(Group.class, callback, new ExhaustiveBridgeCallback.ExhaustiveBridgeEvents() {
            @Override
            public void performApiCallWithExhaustiveCallback(CanvasCallback bridgeCallback, String nextURL, boolean isCached) {
                if(callback.isCancelled()) { return; }

                GroupAPI.getNextPageGroupsChained(bridgeCallback, nextURL, isCached);
            }
        });

        if (isCached) {
            buildCacheInterface(GroupsInterface.class, callback).getFavoriteGroups(bridge);
        } else {
            buildInterface(GroupsInterface.class, callback).getFavoriteGroups(bridge);
        }
    }

    public static void getAllFavoriteGroups(final CanvasCallback<Group[]> callback) {
        if (APIHelpers.paramIsNull(callback)) return;

        CanvasCallback<Group[]> bridge = new ExhaustiveBridgeCallback<>(Group.class, callback, new ExhaustiveBridgeCallback.ExhaustiveBridgeEvents() {
            @Override
            public void performApiCallWithExhaustiveCallback(CanvasCallback bridgeCallback, String nextURL, boolean isCached) {
                if(callback.isCancelled()) { return; }

                GroupAPI.getNextPageGroupsChained(bridgeCallback, nextURL, isCached);
            }
        });

        buildCacheInterface(GroupsInterface.class, callback).getFavoriteGroups(bridge);
        buildInterface(GroupsInterface.class, callback).getFavoriteGroups(bridge);
    }

    /////////////////////////////////////////////////////////////////////////////
    // Helper Methods
    ////////////////////////////////////////////////////////////////////////////

    public static Map<Long, Group> createGroupMap(Group[] groups) {
        Map<Long, Group> groupMap = new HashMap<Long, Group>();
        for (Group group : groups) {
            groupMap.put(group.getId(), group);
        }
        return groupMap;
    }

    /////////////////////////////////////////////////////////////////////////////
    // Synchronous
    //
    // If Retrofit is unable to parse (no network for example) Synchronous calls
    // will throw a nullPointer exception. All synchronous calls need to be in a
    // try catch block.
    /////////////////////////////////////////////////////////////////////////////

    public static Group[] getAllGroupsSynchronous(Context context) {
        try {
            ArrayList<Group> allGroups = new ArrayList<Group>();
            int page = 1;
            long firstItemId = -1;

            //for(ever) loop. break once we've run outta stuff;
            for(;;){
                Group[] groups = buildInterface(GroupsInterface.class, context).getGroupsSynchronous(page);
                page++;

                //This is all or nothing. We don't want partial data.
                if(groups == null){
                    return null;
                } else if(groups.length == 0){
                    break;
                } else if(groups[0].getId() == firstItemId){
                    break;
                } else{
                    firstItemId = groups[0].getId();
                    Collections.addAll(allGroups, groups);
                }
            }

            return allGroups.toArray(new Group[allGroups.size()]);

        } catch (Exception E) {
            return null;
        }
    }

}
