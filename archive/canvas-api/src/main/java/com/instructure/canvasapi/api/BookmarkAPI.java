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

import com.instructure.canvasapi.model.Bookmark;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

public class BookmarkAPI extends BuildInterfaceAPI {

    public static String getBookmarksCacheFilename(){
        return "/users/self/bookmarks";
    }

    interface BookmarkInterface {

        @GET("/users/self/bookmarks")
        void getBookmarks(Callback<Bookmark[]> callback);

        @GET("/users/self/bookmarks/{id}")
        void getBookmark(@Path("id") long bookmarkId, Callback<Bookmark> callback);

        @POST("/users/self/bookmarks")
        void createBookmark(
                @Query("name") String name,
                @Query(value = "url", encodeValue = true) String url,
                @Query("position") int position,
                @Body String body,
                Callback<Bookmark> callback);

        @PUT("/users/self/bookmarks/{id}")
        void updateBookmark(@Path("id") long bookmarkId,
                            @Query("name") String name,
                            @Query(value = "url", encodeValue = false) String url,
                            @Query("position") int position,
                            @Body String body,
                            Callback<Bookmark> callback);

        @DELETE("/users/self/bookmarks/{id}")
        void deleteBookmark(@Path("id") long bookmarkId, Callback<Bookmark> callback);
    }

    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////

    public static void getBookmarks(CanvasCallback<Bookmark[]> callback) {
        buildCacheInterface(BookmarkInterface.class, callback).getBookmarks(callback);
        buildInterface(BookmarkInterface.class, callback).getBookmarks(callback);
    }

    public static void getBookmark(long bookmarkId, CanvasCallback<Bookmark> callback) {
        buildCacheInterface(BookmarkInterface.class, callback).getBookmark(bookmarkId, callback);
        buildInterface(BookmarkInterface.class, callback).getBookmark(bookmarkId, callback);
    }

    public static void createBookmark(Bookmark bookmark, CanvasCallback<Bookmark> callback) {
        buildInterface(BookmarkInterface.class, callback, false).createBookmark(bookmark.getName(), bookmark.getUrl(), bookmark.getPosition(), "", callback);
    }

    public static void deleteBookmark(Bookmark bookmark,  CanvasCallback<Bookmark> callback) {
        deleteBookmark(bookmark.getId(), callback);
    }

    public static void deleteBookmark(long bookmarkId, CanvasCallback<Bookmark> callback) {
        buildInterface(BookmarkInterface.class, callback, false).deleteBookmark(bookmarkId, callback);
    }

    public static void update(Bookmark bookmark, CanvasCallback<Bookmark> callback) {
        buildInterface(BookmarkInterface.class, callback, false).updateBookmark(bookmark.getId(), bookmark.getName(), bookmark.getUrl(), bookmark.getPosition(), "", callback);
    }
}
