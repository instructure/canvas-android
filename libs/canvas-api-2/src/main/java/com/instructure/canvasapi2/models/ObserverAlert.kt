/*
 * Copyright (C) 2018 - present Instructure, Inc.
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

package com.instructure.canvasapi2.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ObserverAlert(
        override val id: Long = 0,
        val title: String? = null,
        @SerializedName("user_id")
        val userId: Long = 0,
        @SerializedName("observer_id")
        val observerId: Long = 0,
        @SerializedName("observer_alert_threshold_id")
        val observerAlertThresholdId: Long = 0,
        @SerializedName("alert_type")
        val alertType: String? = null,
        @SerializedName("context_type")
        val contextType: String? = null,
        @SerializedName("context_id")
        val contextId: String? = null,
        @SerializedName("workflow_state")
        var workflowState: String? = null,
        @SerializedName("html_url")
        val htmlUrl: String? = null,
        @SerializedName("action_date")
        var date: String? = null
) : CanvasModel<ObserverAlert>(), Parcelable {
    override val comparisonString get() = title

    fun isMarkedRead() : Boolean = workflowState == "read"
}
