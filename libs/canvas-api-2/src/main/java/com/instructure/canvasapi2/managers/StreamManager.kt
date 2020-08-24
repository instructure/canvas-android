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
import com.instructure.canvasapi2.apis.StreamAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.HiddenStreamItem
import com.instructure.canvasapi2.models.StreamItem
import java.util.*

object StreamManager {

    fun getUserStream(callback: StatusCallback<List<StreamItem>>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        StreamAPI.getUserStream(adapter, params, callback)
    }

    fun getCourseStream(
        canvasContext: CanvasContext,
        callback: StatusCallback<List<StreamItem>>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(
            usePerPageQueryParam = true,
            canvasContext = canvasContext,
            isForceReadFromNetwork = forceNetwork
        )

        StreamAPI.getCourseStream(canvasContext, adapter, params, callback)
    }

    fun hideStreamItem(streamId: Long, callback: StatusCallback<HiddenStreamItem>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()

        StreamAPI.hideStreamItem(streamId, adapter, params, callback)
    }

    fun getUserStreamSynchronous(numberToReturn: Int, forceNetwork: Boolean): List<StreamItem> {
        val adapter = RestBuilder()
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        return StreamAPI.getUserStreamSynchronous(numberToReturn, adapter, params) ?: ArrayList()
    }

}
