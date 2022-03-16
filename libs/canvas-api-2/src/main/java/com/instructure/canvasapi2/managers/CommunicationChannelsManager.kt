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
 */
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.CommunicationChannelsAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CommunicationChannel
import com.instructure.canvasapi2.utils.weave.apiAsync
import okhttp3.ResponseBody
import retrofit2.Response

object CommunicationChannelsManager {

    fun getCommunicationChannels(
        userId: Long,
        callback: StatusCallback<List<CommunicationChannel>>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        CommunicationChannelsAPI.getCommunicationChannels(userId, adapter, params, callback)
    }

    fun getCommunicationChannelsAsync(
            userId: Long,
            forceNetwork: Boolean
    ) = apiAsync<List<CommunicationChannel>> { getCommunicationChannels(userId, it, forceNetwork) }

    fun addNewPushCommunicationChannelSynchronous(
        registrationId: String,
        callback: StatusCallback<ResponseBody>
    ): Response<ResponseBody>? {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = true)
        return CommunicationChannelsAPI.addNewPushCommunicationChannelSynchronous(registrationId, adapter, params)
    }

    fun deletePushCommunicationChannelSynchronous(registrationId: String) {
        return CommunicationChannelsAPI.deletePushCommunicationChannelSynchronous(registrationId)
    }
}
