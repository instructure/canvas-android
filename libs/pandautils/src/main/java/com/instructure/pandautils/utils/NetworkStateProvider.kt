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
 */

package com.instructure.pandautils.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


interface NetworkStateProvider {
    val isOnlineLiveData: LiveData<Boolean>
    fun isOnline(): Boolean
}

class NetworkStateProviderImpl(context: Context) : NetworkStateProvider {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    private val hasActiveNetwork = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).orDefault()

    private val _isOnlineLiveData = MutableLiveData<Boolean>()

    override val isOnlineLiveData: LiveData<Boolean>
        get() = _isOnlineLiveData

    init {
        _isOnlineLiveData.postValue(hasActiveNetwork)
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                _isOnlineLiveData.postValue(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                _isOnlineLiveData.postValue(false)
            }
        })
    }

    override fun isOnline(): Boolean {
        return _isOnlineLiveData.value ?: hasActiveNetwork
    }
}