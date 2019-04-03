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

import com.instructure.canvasapi.model.Avatar;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Query;


public class AvatarAPI extends BuildInterfaceAPI {

    interface AvatarsInterface{
        @GET("/users/self/avatars")
        void getFirstPageOfAvatarList( Callback<Avatar[]> callback);

        @PUT("/users/self")
        void updateAvatar(@Query("user[avatar][url]") String avatarURL, @Body String body, Callback<User> callback);
    }

    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////

    public static void getFirstPageOfAvatarList(CanvasCallback<Avatar[]> callback){
        if(APIHelpers.paramIsNull(callback)) { return; }

        buildInterface(AvatarsInterface.class, callback).getFirstPageOfAvatarList( callback);
        buildInterface(AvatarsInterface.class, callback).getFirstPageOfAvatarList( callback);
    }

    public static void updateAvatar(String avatarURL, CanvasCallback<User> callback){
        if(APIHelpers.paramIsNull(callback,avatarURL)){ return; }

        buildInterface(AvatarsInterface.class, callback).updateAvatar(avatarURL, "", callback);
    }

}
