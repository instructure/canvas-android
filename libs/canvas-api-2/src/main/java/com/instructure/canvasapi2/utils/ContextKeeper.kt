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
package com.instructure.canvasapi2.utils

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import androidx.core.os.ConfigurationCompat

class ContextKeeper : ContentProvider() {

    companion object {
        lateinit var appContext: Context

        fun updateLocale(config: Configuration) {
            val cachedLocale = ConfigurationCompat.getLocales(appContext.resources.configuration)[0]
            val expectedLocale = ConfigurationCompat.getLocales(config)[0]
            if (cachedLocale?.toLanguageTag() != expectedLocale?.toLanguageTag()) {
                ContextKeeper.appContext = ContextKeeper.appContext.createConfigurationContext(config)
            }
        }
    }

    override fun onCreate(): Boolean {
        appContext = context!!
        return false
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?) = 0
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?) = null
    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?) = null
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?) = 0

}
