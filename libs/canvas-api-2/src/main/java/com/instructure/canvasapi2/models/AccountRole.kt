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
 */    package com.instructure.canvasapi2.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AccountRole(
    override val id: Long = 0,
    val role: String = "",
    val label: String? = null,
    @SerializedName("base_role_type")
    val baseRoleType: String? = null,
    @SerializedName("workflow_state")
    val workflowState: String? = null,
    val permissions: Map<String, AccountPermission> = emptyMap()
) : CanvasModel<AccountRole>()

@Parcelize
data class AccountPermission(
    var enabled: Boolean = false,
    var locked: Boolean = false,
    var readonly: Boolean = false,
    var explicit: Boolean = false,
    @SerializedName("applies_to_descendants")
    var appliesToDescendants: Boolean = false,
    @SerializedName("applies_to_self")
    var appliesToSelf: Boolean = false
) : Parcelable
