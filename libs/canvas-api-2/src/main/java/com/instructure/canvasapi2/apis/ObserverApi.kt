/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 */    package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Alert
import com.instructure.canvasapi2.models.AlertThreshold
import com.instructure.canvasapi2.models.postmodels.CreateObserverThresholdWrapper
import com.instructure.canvasapi2.utils.DataResult
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag
import retrofit2.http.Url

interface ObserverApi {

    @GET("users/self/observer_alerts/{studentId}")
    suspend fun getObserverAlerts(@Path("studentId") studentId: Long, @Tag restParams: RestParams): DataResult<List<Alert>>

    @GET
    suspend fun getNextPageObserverAlerts(@Url nextUrl: String, @Tag restParams: RestParams): DataResult<List<Alert>>

    @PUT("users/self/observer_alerts/{alertId}/{workflowState}")
    suspend fun updateAlertWorkflow(@Path("alertId") alertId: Long, @Path("workflowState") workflowState: String, @Tag restParams: RestParams): DataResult<Alert>

    @GET("users/self/observer_alert_thresholds")
    suspend fun getObserverAlertThresholds(@Query("student_id") studentId: Long, @Tag restParams: RestParams): DataResult<List<AlertThreshold>>

    @POST("users/self/observees")
    suspend fun pairStudent(@Query("pairing_code") pairingCode: String, @Tag restParams: RestParams): DataResult<Unit>

    @POST("users/self/observer_alert_thresholds")
    suspend fun createObserverAlert(@Body data: CreateObserverThresholdWrapper, @Tag restParams: RestParams): DataResult<Unit>

    @DELETE("users/self/observer_alert_thresholds/{thresholdId}")
    suspend fun deleteObserverAlert(@Path("thresholdId") thresholdId: Long, @Tag restParams: RestParams): DataResult<Unit>
}