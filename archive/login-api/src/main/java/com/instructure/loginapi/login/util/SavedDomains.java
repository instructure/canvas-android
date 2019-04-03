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

package com.instructure.loginapi.login.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.instructure.loginapi.login.OAuthWebLogin;
import com.instructure.loginapi.login.URLSignIn;

import org.json.JSONArray;
import org.json.JSONException;

public class SavedDomains {
    /**
     * Retrieves the previously successful domains from saved preferences.
     *
     * @param context
     * @return
     */
    public static JSONArray getSavedDomains(Context context, String preferenceName) {
        SharedPreferences prefs = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        JSONArray domains = null;
        try {
            domains = new JSONArray(prefs.getString(URLSignIn.URL_ENTRIES, "[]"));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return domains;
    }

    /**
     * Saves the domain array to saved preferences using json array syntax.
     *
     * @param ctx
     * @param values
     * @return True if the values were successfully committed to storage.
     */
    public static boolean setSavedDomains(Context ctx, JSONArray values, String preferenceName) {
        SharedPreferences prefs = ctx.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(URLSignIn.URL_ENTRIES, values.toString());
        return editor.commit();
    }

}
