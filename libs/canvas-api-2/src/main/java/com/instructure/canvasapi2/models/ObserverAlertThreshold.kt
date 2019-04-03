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

import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ObserverAlertThreshold(
        override val id: Long = 0,
        @SerializedName("user_id")
        val userId: Long = 0,
        @SerializedName("observer_id")
        val observerId: Long = 0,
        val threshold: String? = null,
        @SerializedName("alert_type")
        val alertType: String? = null,
        @SerializedName("workflow_state")
        val workflowState: String? = null
) : CanvasModel<ObserverAlertThreshold>()