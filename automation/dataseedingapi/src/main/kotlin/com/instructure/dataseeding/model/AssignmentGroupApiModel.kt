//
// Copyright (C) 2022-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.instructure.dataseeding.model

import com.google.gson.annotations.SerializedName

data class CreateAssignmentGroupWrapper(
        val assignmentGroup: CreateAssignmentGroup
)

data class CreateAssignmentGroup(
        @SerializedName("name")
        val name: String,
        @SerializedName("position")
        var position: Int? = null,
        @SerializedName("group_weight")
        var groupWeight: Int? = null,
        @SerializedName("sis_source_id")
        var sisSourceId: Long? = null

)

data class AssignmentGroupApiModel (
        val id: Long,
        @SerializedName("name")
        val name: String,
        @SerializedName("position")
        var position: Int,
        @SerializedName("group_weight")
        var groupWeight: Int
)
