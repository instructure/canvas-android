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

package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Alert
import com.instructure.canvasapi2.models.ObserverAlert
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Url


object AlertAPI {
    const val ALERT_DISMISSED = "dismissed"
    const val ALERT_READ = "read"

    internal interface AlertInterface {
        @GET
        fun next(@Url nextUrl: String): Call<List<Alert>>

        @GET("users/self/observer_alerts/{studentId}")
        fun getObserverAlertsForStudent(@Path("studentId") studentId: Long): Call<List<ObserverAlert>>

        @GET
        fun nextObservers(@Url nextUrl: String): Call<List<ObserverAlert>>

        @PUT("users/self/observer_alerts/{alertId}/{workflowState}")
        fun updateObserverAlert(@Path("alertId") alertId: Long, @Path("workflowState") workflowState: String): Call<ObserverAlert>
    }

    fun getObserverAlerts(
            studentId: Long,
            adapter: RestBuilder,
            callback: StatusCallback<List<ObserverAlert>>,
            params: RestParams) {

        if (StatusCallback.isFirstPage(callback.linkHeaders)) {
            callback.addCall(adapter.build(AlertInterface::class.java, params)
                    .getObserverAlertsForStudent(studentId)).enqueue(callback)
        } else if (StatusCallback.moreCallsExist(callback.linkHeaders) && callback.linkHeaders != null) {
            callback.addCall(adapter.build(AlertInterface::class.java, params)
                    .nextObservers(callback.linkHeaders!!.nextUrl!!)).enqueue(callback)
        }
    }

    fun updateObserverAlert(
            alertId: Long,
            workflowState: String, // currently either 'read' or 'dismissed'
            adapter: RestBuilder,
            callback: StatusCallback<ObserverAlert>,
            params: RestParams) {

        callback.addCall(adapter.build(AlertInterface::class.java, params)
                .updateObserverAlert(alertId, workflowState)).enqueue(callback)
    }

}