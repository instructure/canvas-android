/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.loginapi.login.api;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.loginapi.login.model.CourseColorDataSync;
import com.instructure.loginapi.login.model.CourseColorDataSyncWrapper;
import com.instructure.loginapi.login.model.GlobalDataSync;
import com.instructure.loginapi.login.model.GlobalDataSyncWrapper;
import com.instructure.loginapi.login.util.Utils;

import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

public class GlobalDataSyncAPI {

    public enum NAMESPACE {
        MOBILE_CANVAS_DATA, MOBILE_POLLS_DATA, MOBILE_SPEEDGRADER_DATA,
        MOBILE_CANVAS_COLORS, MOBILE_POLLS_COLORS, MOBILE_SPEEDGRADER_COLORS,
        MOBILE_CANVAS_USER_BACKDROP_IMAGE, MOBILE_CANVAS_USER_NOTIFICATION_STATUS_SETUP}

    public interface DataSyncInterface {

        @GET("/users/{userId}/custom_data/data_sync")
        JsonElement getGlobalData(@Path("userId") long userId, @Query("ns") String nameSpace);

        @PUT("/users/{userId}/custom_data/data_sync")
        GlobalDataSync setGlobalData(@Path("userId") long userId, @Query("ns") String nameSpace, @Query("data") String json, @Body String body);
    }

    public static GlobalDataSync getGlobalData(Context context, NAMESPACE namespace){
        try {
            User user = APIHelpers.getCacheUser(context);
            RestAdapter restAdapter = CanvasRestAdapter.buildAdapter(context);
            JsonElement element = restAdapter.create(DataSyncInterface.class).getGlobalData(user.getId(), namespace.toString());

            if(element != null) {
                String json = cleanJson(element.toString());
                return new Gson().fromJson(json, GlobalDataSync.class);
            }
            return null;
        } catch (Exception e){
            Utils.e("===> GLOBAL DATA SYNC ERROR *GET*: " + e);
            return null;
        }
    }

    public static void setGlobalData(Context context, NAMESPACE namespace, GlobalDataSync data){
        try {
            User user = APIHelpers.getCacheUser(context);
            RestAdapter restAdapter = CanvasRestAdapter.buildAdapter(context);
            restAdapter.create(DataSyncInterface.class).setGlobalData(user.getId(), namespace.toString(), GlobalDataSync.toJsonString(data), "");
        } catch (Exception e){
            Utils.e("===> GLOBAL DATA SYNC ERROR *SET*: " + e);
        }
    }

    //Cleans our responses
    private static String cleanJson(String json) {

        if(TextUtils.isEmpty(json)) {
            return null;
        }

        Utils.d("JSON BEFORE: " + json);

        json = json.replaceAll("\\\\", "");

        if(json.startsWith("{\"data\":\"")) {
            String pattern = "\\{\"data\":\"";
            json = json.replaceFirst(pattern, "");
            if(json.endsWith("\"}")) {
                json = json.substring(0, json.length() -2)+json.substring((json.length()));
            }
        }

        Utils.d("JSON AFTER: " + json);
        return json;
    }
}
