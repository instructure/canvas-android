//
// Copyright (C) 2018-present Instructure, Inc.
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

data class GroupMembershipApiModel(
        val id: Long,
        @SerializedName("group_id")
        val groupId: Long,
        @SerializedName("user_id")
        val userId: Long,
        @SerializedName("workflow_state")
        val workflowState: String
)

data class CreateGroupMembership(
        @SerializedName("user_id")
        val userId: Long
)
