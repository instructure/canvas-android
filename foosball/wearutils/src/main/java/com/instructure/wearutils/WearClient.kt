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

package com.instructure.wearutils

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.data.FreezableUtils
import com.google.android.gms.wearable.*
import com.instructure.wearutils.interfaces.WearableCallbacks
import java.util.*


class WearClient(context: Context, private val mConnectionListenerCallback: WearClient.OnConnectedListener?, private val mWearCallbacks: WearableCallbacks?) : DataApi.DataListener, MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    interface OnConnectedListener {
        fun onClientConnected()
        fun onConnectionFailed()
    }

    private val mGoogleApiClient: GoogleApiClient

    init {

        mGoogleApiClient = GoogleApiClient.Builder(context).addApi(Wearable.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build()
    }

    fun connect() {
        mGoogleApiClient.connect()
    }

    override fun onConnected(bundle: Bundle?) {
        Wearable.DataApi.addListener(mGoogleApiClient, this)
        Wearable.MessageApi.addListener(mGoogleApiClient, this)

        mConnectionListenerCallback?.onClientConnected()
    }

    fun disconnect() {
        Wearable.DataApi.removeListener(mGoogleApiClient, this)
        Wearable.MessageApi.removeListener(mGoogleApiClient, this)
        mGoogleApiClient.disconnect()
    }

    override fun onConnectionSuspended(i: Int) {

    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {

        val events = FreezableUtils.freezeIterable(dataEvents)

        for (event in events) {
            if (event.type == DataEvent.TYPE_CHANGED) {

                val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                val dataMap = dataMapItem.dataMap

                mWearCallbacks?.OnSyncDataItemTask(dataMap)

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

        if (!mGoogleApiClient.isConnected) {
            return
        }

        //let's send the dataItem to the DataLayer API
        Wearable.DataApi.putDataItem(mGoogleApiClient, request).setResultCallback { dataItemResult ->
            if (!dataItemResult.status.isSuccess) {

            }
        }
    }

    private val nodes: Collection<String>
        get() {
            val results = HashSet<String>()
            val nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await()

            for (node in nodes.nodes) {
                results.add(node.id)
            }

            return results
        }


    //Task to send messages to nodes
    private inner class StartTeleportMessageTask : AsyncTask<Any, Void, Any>() {

        override fun doInBackground(vararg args: Any): Void? {
            val nodes = nodes
            for (node in nodes) {
                Log.d("wear", "sending to nodes...")
                propagateMessageToNodes(node, args[0] as String, args[1] as ByteArray?)
            }
            return null
        }
    }

    //propagate message to nodes
    private fun propagateMessageToNodes(node: String, path: String, payload: ByteArray?) {
        Wearable.MessageApi.sendMessage(mGoogleApiClient, node, path, payload).setResultCallback { sendMessageResult -> Log.d("wear", "Send Message Result: " + sendMessageResult.status.statusMessage!!) }
    }

    fun sendMessage(path: String, payload: ByteArray?) {
        StartTeleportMessageTask().execute(path, payload)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d("wear", "onMessageReceived()")
        if (mWearCallbacks != null) {
            val messagePath = messageEvent.path
            mWearCallbacks.OnGetMessageTask(messagePath)
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d("wear", "onConnectionFailed(" + getErrorCodeString(connectionResult.errorCode) + ")")
        mConnectionListenerCallback?.onConnectionFailed()
    }

    private fun getErrorCodeString(errorCode: Int): String {
        when (errorCode) {
            0 -> return "SUCCESS"
            1 -> return "SERVICE_MISSING"
            2 -> return "SERVICE_VERSION_UPDATE_REQUIRED"
            3 -> return "SERVICE_DISABLED"
            4 -> return "SIGN_IN_REQUIRED"
            5 -> return "INVALID_ACCOUNT"
            6 -> return "RESOLUTION_REQUIRED"
            7 -> return "NETWORK_ERROR"
            8 -> return "INTERNAL_ERROR"
            9 -> return "SERVICE_INVALID"
            10 -> return "DEVELOPER_ERROR"
            11 -> return "LICENSE_CHECK_FAILED"
            12 -> return "DATE_INVALID"
            13 -> return "CANCELED"
            14 -> return "TIMEOUT"
            15 -> return "INTERRUPTED"
            16 -> return "API_UNAVAILABLE"
            1500 -> return "DRIVE_EXTERNAL_STORAGE_REQUIRED"
        }
        return "UNKNOWN"
    }
}
