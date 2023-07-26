/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 */

package com.instructure.student

import android.annotation.SuppressLint
import android.util.Log
import androidx.datastore.preferences.core.edit
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.instructure.canvasapi2.utils.ApiPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AuthListenerService : WearableListenerService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    @SuppressLint("VisibleForTests")
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItemPath = event.dataItem.uri.path ?: ""
                if (dataItemPath.startsWith("/auth")) {
                    val token = DataMapItem.fromDataItem(event.dataItem).dataMap.getString("accessToken")
                    val refreshToken = DataMapItem.fromDataItem(event.dataItem).dataMap.getString("refreshToken")
                    val domain = DataMapItem.fromDataItem(event.dataItem).dataMap.getString("domain")
                    ApiPrefs.accessToken = token ?: ""
                    ApiPrefs.refreshToken = refreshToken ?: ""
                    ApiPrefs.domain = domain ?: ""
                    Log.d("OnDataChanged", "onDataChanged: $token")

                    scope.launch {
                        dataStore.edit { preferences ->
                            preferences[TOKEN] = token ?: ""
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}