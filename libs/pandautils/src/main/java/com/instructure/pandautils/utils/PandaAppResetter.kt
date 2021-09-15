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

package com.instructure.pandautils.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.FileUtils
import java.io.File
import java.io.IOException

object PandaAppResetter {

    fun reset(context: Context) {
        clearPrefs()
        clearFiles(context)
        clearHttpClientCache()
    }

    private fun clearPrefs() {
        // Clear all Shared Preferences.
        PandaPrefs.clearPrefs()
        ColorKeeper.clearPrefs()
        FilePrefs.clearPrefs()
        ThemePrefs.clearPrefs()
        ApiPrefs.clearAllData()
    }

    private fun clearFiles(context: Context) {
        val cacheDir = File(context.filesDir, "cache")
        val exCacheDir = Utils.getAttachmentsDirectory(context)
        // Remove the cached stuff for masqueraded user
        val masqueradeCacheDir = File(context.filesDir, "cache_masquerade")
        // Need to delete the contents of the external cache folder so previous user's results don't show up on incorrect user
        FileUtils.deleteAllFilesInDirectory(masqueradeCacheDir)
        FileUtils.deleteAllFilesInDirectory(cacheDir)
        FileUtils.deleteAllFilesInDirectory(exCacheDir)
    }

    private fun clearHttpClientCache() {
        try {
            CanvasRestAdapter.client?.cache?.evictAll()
        } catch (e: IOException) {
            //Do Nothing
        }

        RestBuilder.clearCacheDirectory()
    }

    fun getEditor(context: Context, prefName: String): SharedPreferences.Editor {
        val preferences = getSharedPreferences(context, prefName)
        return preferences.edit()
    }

    private fun getSharedPreferences(context: Context, prefName: String): SharedPreferences {
        return context.getSharedPreferences(prefName, MODE_PRIVATE)
    }

}
