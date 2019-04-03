/*
 * Copyright (C) 2016 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.teacheraid.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class Prefs {

    // SAVE PREFERENCE
    public static void save(Context context, String key, int value) {
        SharedPreferences sp = context.getSharedPreferences(ApplicationManager.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void save(Context context, String key, float value) {
        SharedPreferences sp = context.getSharedPreferences(ApplicationManager.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public static void save(Context context, String key, long value) {
        SharedPreferences sp = context.getSharedPreferences(ApplicationManager.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static void save(Context context, String key, boolean value) {
        SharedPreferences sp = context.getSharedPreferences(ApplicationManager.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void save(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(ApplicationManager.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    //LOAD PREFERENCE
    public static int load(Context context, String key, int defaultInt) {
        SharedPreferences sp = context.getSharedPreferences(ApplicationManager.PREF_NAME, Context.MODE_PRIVATE);
        return sp.getInt(key, defaultInt);
    }

    public static float load(Context context, String key, float defaultFloat) {
        SharedPreferences sp = context.getSharedPreferences(ApplicationManager.PREF_NAME, Context.MODE_PRIVATE);
        return sp.getFloat(key, defaultFloat);
    }

    public static long load(Context context, String key, long defaultLong) {
        SharedPreferences sp = context.getSharedPreferences(ApplicationManager.PREF_NAME, Context.MODE_PRIVATE);
        return sp.getLong(key, defaultLong);
    }

    public static boolean load(Context context, String key, boolean defaultBool) {
        SharedPreferences sp = context.getSharedPreferences(ApplicationManager.PREF_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(key, defaultBool);
    }

    public static String load(Context context, String key, String defaultString) {
        SharedPreferences sp = context.getSharedPreferences(ApplicationManager.PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(key, defaultString);
    }

    // DOES SHARED PREFERENCE EXIST
    public static boolean doesPrefExist(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(ApplicationManager.PREF_NAME, Context.MODE_PRIVATE);
        return sp.contains(key);
    }

    //Removes a shared preference
    public static void removePreferenceIfExists(Context context, String key) {
        if(context == null || TextUtils.isEmpty(key)) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(ApplicationManager.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        editor.apply();
    }
}
