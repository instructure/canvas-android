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
package com.instructure.loginapi.login.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.instructure.canvasapi2.managers.OAuthManager;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.loginapi.login.model.SignedInUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import androidx.annotation.Nullable;

public class PreviousUsersUtils {

    private final static String SIGNED_IN_USERS_PREF_NAME = "signedInUsersList";

    //Does the CURRENT user support Multiple Users.
    public static ArrayList<SignedInUser> get(Context context) {

        ArrayList<SignedInUser> signedInUsers = new ArrayList<>();

        SharedPreferences sharedPreferences = context.getSharedPreferences(SIGNED_IN_USERS_PREF_NAME, Context.MODE_PRIVATE);
        Map<String, ?> keys = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            SignedInUser signedInUser = null;

            try {
                signedInUser = new Gson().fromJson(entry.getValue().toString(), SignedInUser.class);
            } catch (Exception ignore) {
                //Do Nothing
            }

            if (signedInUser != null) {
                signedInUsers.add(signedInUser);
            }
        }

        //Sort by last signed in date.
        Collections.sort(signedInUsers);
        return signedInUsers;
    }

    public static boolean removeByToken(Context context, String token) {
        ArrayList<SignedInUser> signedInUsers = get(context);
        boolean removedUser = false;
        for(SignedInUser user : signedInUsers) {
            if(user.getToken().equals(token)) {
                remove(context, user);
                removedUser = true;
            }
        }
        return removedUser;
    }

    public static boolean remove(Context context, SignedInUser signedInUser) {

        // Delete Access Token. We don't care about the result.
        OAuthManager.deleteToken();

        //Save Signed In User to sharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences(SIGNED_IN_USERS_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(getGlobalUserId(signedInUser.getDomain(), signedInUser.getUser()));
        return editor.commit();
    }

    public static boolean add(Context context, SignedInUser signedInUser) {

        String signedInUserJSON = new Gson().toJson(signedInUser);

        //Save Signed In User to sharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences(SIGNED_IN_USERS_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getGlobalUserId(ApiPrefs.getDomain(), ApiPrefs.getUser()), signedInUserJSON);
        return editor.commit();
    }

    @Nullable
    public static SignedInUser getSignedInUser(Context context, String domain, long userId) {
        SharedPreferences prefs = context.getSharedPreferences(SIGNED_IN_USERS_PREF_NAME, Context.MODE_PRIVATE);
        String userJson = prefs.getString(getGlobalUserId(domain, userId), null);
        try {
            return new Gson().fromJson(userJson, SignedInUser.class);
        } catch (Exception e) {
            return null;
        }
    }

    private static String getGlobalUserId(String domain, long userId) {
        return domain + "-" + userId;
    }

    private static String getGlobalUserId(String domain, User user) {
        if (user == null) return "";
        return getGlobalUserId(domain, user.getId());
    }

    public static void clear(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SIGNED_IN_USERS_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
