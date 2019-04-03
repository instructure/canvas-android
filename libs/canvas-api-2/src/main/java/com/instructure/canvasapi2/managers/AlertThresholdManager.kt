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
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.AlertThresholdAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.ObserverAlertThreshold
import com.instructure.canvasapi2.models.postmodels.ObserverAlertThresholdPostBody
import com.instructure.canvasapi2.models.postmodels.ObserverAlertThresholdPostBodyWrapper
import okhttp3.ResponseBody

object AlertThresholdManager {

    @JvmStatic
    fun getObserverAlertThresholds(studentId: Long, callback: StatusCallback<List<ObserverAlertThreshold>>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = true)
        AlertThresholdAPI.getObserverAlertThresholds(adapter, params, studentId, callback)
    }

    @JvmStatic
    fun updateObserverAlertThreshold(
        thresholdId: String,
        threshold: String,
        callback: StatusCallback<ObserverAlertThreshold>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        AlertThresholdAPI.updateObserverAlertThreshold(adapter, params, thresholdId, threshold, callback)
    }

    @JvmStatic
    fun deleteObserverAlertThreshold(thresholdId: String, callback: StatusCallback<ResponseBody>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        AlertThresholdAPI.deleteObserverAlertThreshold(adapter, params, thresholdId, callback)
    }

    @JvmStatic
    fun createObserverAlertThreshold(
        studentId: Long,
        alertType: String,
        callback: StatusCallback<ObserverAlertThreshold>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()

        val body = ObserverAlertThresholdPostBody(studentId, alertType, null)
        val wrapper = ObserverAlertThresholdPostBodyWrapper()
        wrapper.observer_alert_threshold = body

        AlertThresholdAPI.createObserverAlertThreshold(adapter, params, wrapper, callback)
    }

    @JvmStatic
    fun createObserverAlertThreshold(
        studentId: Long,
        alertType: String,
        threshold: String,
        callback: StatusCallback<ObserverAlertThreshold>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()

        val body = ObserverAlertThresholdPostBody(studentId, alertType, Integer.parseInt(threshold))
        val wrapper = ObserverAlertThresholdPostBodyWrapper()
        wrapper.observer_alert_threshold = body
        AlertThresholdAPI.createObserverAlertThreshold(adapter, params, wrapper, callback)
    }

}
