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

package com.instructure.pandautils.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class Prefs {

    private SharedPreferences mSp;

    public static String PREF_NAME;

    public Prefs(Context context, String preferenceName ) {
        PREF_NAME = preferenceName;
        mSp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void save(String key, int value) {
        SharedPreferences.Editor editor = mSp.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void save(String key, float value) {
        SharedPreferences.Editor editor = mSp.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public void save(String key, long value) {
        SharedPreferences.Editor editor = mSp.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public void save(String key, boolean value) {
        SharedPreferences.Editor editor = mSp.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void save(String key, String value) {
        SharedPreferences.Editor editor = mSp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void save(String key, Set<String> value){
        SharedPreferences.Editor editor = mSp.edit();
        editor.putStringSet(key, value);
        editor.apply();
    }

    public int load(String key, int defaultInt) {
        return mSp.getInt(key, defaultInt);
    }

    public float load(String key, float defaultFloat) {
        return mSp.getFloat(key, defaultFloat);
    }

    public long load(String key, long defaultLong) {
        return mSp.getLong(key, defaultLong);
    }

    public boolean load(String key, boolean defaultBool) {
        return mSp.getBoolean(key, defaultBool);
    }

    public String load(String key, String defaultString) {
        return mSp.getString(key, defaultString);
    }

    public Set<String> load(String key, Set<String> defaultSet){
        return mSp.getStringSet(key, defaultSet);
    }

    public boolean exists(String key) {
        return mSp.contains(key);
    }

    public void remove(String key) {
        if(mSp.contains(key)) {
            SharedPreferences.Editor editor = mSp.edit();
            editor.remove(key);
            editor.apply();
        }
    }
}
