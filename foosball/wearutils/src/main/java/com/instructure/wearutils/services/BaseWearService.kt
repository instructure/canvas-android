/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.wearutils.services

import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.data.FreezableUtils
import com.google.android.gms.wearable.DataApi
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.instructure.wearutils.interfaces.WearableCallbacks

import java.util.Date


open class BaseWearService : WearableListenerService() {

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mWearCallbacks: WearableCallbacks? = null

    override fun onCreate() {
        super.onCreate()
        mGoogleApiClient = GoogleApiClient.Builder(this).addApi(Wearable.API).build()
        mGoogleApiClient!!.connect()
    }

    fun setCallbacks(callbacks: WearableCallbacks) {
        mWearCallbacks = callbacks
    }

    override fun onDataChanged(dataEvents: DataEventBuffer?) {

        val events = FreezableUtils.freezeIterable(dataEvents!!)

        for (event in events) {
            if (event.type == DataEvent.TYPE_CHANGED) {

                val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                val dataMap = dataMapItem.dataMap
                if (mWearCallbacks != null) {
                    mWearCallbacks!!.OnSyncDataItemTask(dataMap)
                }

            } else if (event.type == DataEvent.TYPE_DELETED) {
                if (event.dataItem != null) {

                }
            }
        }
    }

    //sync String

    fun syncString(key: String, item: String) {
        val putDataMapRequest = PutDataMapRequest.create("/" + key)
        putDataMapRequest.dataMap.putLong("time", Date().time)
        putDataMapRequest.dataMap.putString(key, item)
        syncDataItem(putDataMapRequest)
    }

    //General method to sync data in the Data Layer
    fun syncDataItem(putDataMapRequest: PutDataMapRequest) {

        val request = putDataMapRequest.asPutDataRequest()

        if (!mGoogleApiClient!!.isConnected) {
            return
        }

        //let's send the dataItem to the DataLayer API
        Wearable.DataApi.putDataItem(mGoogleApiClient, request).setResultCallback { }
    }

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        if (mWearCallbacks != null) {
            val messagePath = messageEvent!!.path
            mWearCallbacks!!.OnGetMessageTask(messagePath)
        }
    }
}
