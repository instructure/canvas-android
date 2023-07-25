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

package com.instructure.student.wear

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import com.instructure.canvasapi2.utils.ApiPrefs

class WearManager(private val context: Context) {

    @SuppressLint("VisibleForTests")
    fun refreshToken() {
        val domain = ApiPrefs.fullDomain
        val token = ApiPrefs.accessToken
        val refreshToken = ApiPrefs.refreshToken
        val dataClient = Wearable.getDataClient(context)

        val putDataReq = PutDataMapRequest.create("/auth").apply {
            dataMap.putString("accessToken", token)
            dataMap.putString("refreshToken", refreshToken)
            dataMap.putString("domain", domain)
        }.asPutDataRequest()
            .setUrgent()
        val result = dataClient.putDataItem(putDataReq)
    }
}