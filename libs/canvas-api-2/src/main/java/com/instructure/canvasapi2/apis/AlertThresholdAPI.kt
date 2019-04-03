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
import com.instructure.canvasapi2.models.ObserverAlertThreshold
import com.instructure.canvasapi2.models.postmodels.ObserverAlertThresholdPostBodyWrapper

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


object AlertThresholdAPI {

    internal interface AlertThresholdInterface {

        @GET("users/self/observer_alert_thresholds")
        fun getObserverAlertThresholds(
                @Query("student_id") studentId: Long): Call<List<ObserverAlertThreshold>>

        @POST("users/self/observer_alert_thresholds")
        fun createObserverAlertThreshold(
                @Body body: ObserverAlertThresholdPostBodyWrapper): Call<ObserverAlertThreshold>

        @FormUrlEncoded
        @PUT("users/self/observer_alert_thresholds/{observerAlertThresholdId}")
        fun updateObserverAlertThreshold(
                @Path("observerAlertThresholdId") thresholdIdPath: String,
                @Field("threshold") threshold: String): Call<ObserverAlertThreshold>

        //threshold field is optional

        @DELETE("users/self/observer_alert_thresholds/{observerAlertThresholdId}")
        fun deleteObserverAlertThreshold(
                @Path("observerAlertThresholdId") thresholdId: String): Call<ResponseBody>

    }

    fun getObserverAlertThresholds(
            adapter: RestBuilder,
            params: RestParams,
            studentId: Long,
            callback: StatusCallback<List<ObserverAlertThreshold>>) {

        callback.addCall(adapter.build(AlertThresholdInterface::class.java, params).getObserverAlertThresholds(studentId)).enqueue(callback)
    }

    fun createObserverAlertThreshold(
            adapter: RestBuilder,
            params: RestParams,
            body: ObserverAlertThresholdPostBodyWrapper,
            callback: StatusCallback<ObserverAlertThreshold>) {

        callback.addCall(adapter.build(AlertThresholdInterface::class.java, params).createObserverAlertThreshold(body)).enqueue(callback)
    }

    fun updateObserverAlertThreshold(
            adapter: RestBuilder,
            params: RestParams,
            thresholdId: String,
            threshold: String,
            callback: StatusCallback<ObserverAlertThreshold>) {

        callback.addCall(adapter.build(AlertThresholdInterface::class.java, params).updateObserverAlertThreshold(thresholdId, threshold)).enqueue(callback)
    }

    fun deleteObserverAlertThreshold(
            adapter: RestBuilder,
            params: RestParams,
            thresholdId: String,
            callback: StatusCallback<ResponseBody>) {

        callback.addCall(adapter.build(AlertThresholdInterface::class.java, params).deleteObserverAlertThreshold(thresholdId)).enqueue(callback)
    }
}
