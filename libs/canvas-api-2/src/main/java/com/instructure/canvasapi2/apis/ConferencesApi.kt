/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ConferenceList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

object ConferencesApi {

    internal interface ConferencesInterface {
        @GET("{canvasContext}/conferences")
        fun getConferences(@Path("canvasContext", encoded = true) canvasContext: String): Call<ConferenceList>
    }

    fun getConferences(
        canvasContext: CanvasContext,
        adapter: RestBuilder,
        callback: StatusCallback<ConferenceList>,
        params: RestParams
    ) {
        callback.addCall(
            adapter
                .build(ConferencesInterface::class.java, params)
                .getConferences(canvasContext.toAPIString().drop(1))
        ).enqueue(callback)
    }

}
