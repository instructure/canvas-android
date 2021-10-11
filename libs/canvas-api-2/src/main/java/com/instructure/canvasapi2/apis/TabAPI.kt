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
package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Tab

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

object TabAPI {

    internal interface TabsInterface {
        @GET("{contextId}/tabs")
        fun getTabs(@Path("contextId") contextId: Long): Call<List<Tab>>

        @GET("{contextId}/tabs?include[]=course_subject_tabs")
        fun getTabsForElementary(@Path("contextId") contextId: Long): Call<List<Tab>>
    }

    fun getTabs(
            contextId: Long,
            adapter: RestBuilder,
            callback: StatusCallback<List<Tab>>,
            params: RestParams) {
        callback.addCall(adapter.build(TabsInterface::class.java, params).getTabs(contextId)).enqueue(callback)
    }

    fun getTabsForElementary(
        contextId: Long,
        adapter: RestBuilder,
        callback: StatusCallback<List<Tab>>,
        params: RestParams) {
        callback.addCall(adapter.build(TabsInterface::class.java, params).getTabsForElementary(contextId)).enqueue(callback)
    }
}