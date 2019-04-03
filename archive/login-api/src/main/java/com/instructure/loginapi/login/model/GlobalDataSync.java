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

package com.instructure.loginapi.login.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.instructure.loginapi.login.api.GlobalDataSyncAPI;
import com.instructure.loginapi.login.util.Utils;

/**
 * A helper object for sending/receiving global data.
 */
public class GlobalDataSync {

    private static final String GLOBAL_DATA_PREFS_FILE = "globalDataPrefs";
    public String data = "";

    public GlobalDataSync(){}

    public GlobalDataSync(String data){
        this.data = data;
    }

    public static GlobalDataSync getCachedGlobalData(Context context, GlobalDataSyncAPI.NAMESPACE namespace) {
        SharedPreferences sp = context.getSharedPreferences(namespace + GLOBAL_DATA_PREFS_FILE, Context.MODE_PRIVATE);
        GlobalDataSync data = fromJsonString(sp.getString(namespace.toString(), ""));
        if(data == null) {
            data = new GlobalDataSync();
        }
        return data;
    }

    public static void setCachedGlobalData(Context context, GlobalDataSync data,  GlobalDataSyncAPI.NAMESPACE namespace) {
        SharedPreferences sp = context.getSharedPreferences(namespace + GLOBAL_DATA_PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(namespace.toString(), toJsonString(data));
        editor.apply();
    }

    public static void clearCachedGlobalData(Context context,  GlobalDataSyncAPI.NAMESPACE namespace) {
        SharedPreferences sp = context.getSharedPreferences(namespace + GLOBAL_DATA_PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }

    public static String toJsonString(GlobalDataSync data) {
        return new Gson().toJson(data, GlobalDataSync.class);
    }

    public static GlobalDataSync fromJsonString(String json) {
        return new Gson().fromJson(json, GlobalDataSync.class);
    }
}
