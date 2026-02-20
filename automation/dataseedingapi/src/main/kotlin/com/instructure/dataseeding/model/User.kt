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
 *
 */
package com.instructure.dataseeding.model

import com.google.gson.annotations.SerializedName
import com.instructure.dataseeding.util.CanvasNetworkAdapter

/**
 * Used as part of the initial creation of a user
 */
data class User(
        val name: String,
        @SerializedName("short_name")
        val shortName: String,
        @SerializedName("sortable_name")
        val sortableName: String,
        @SerializedName("terms_of_use")
        val termsOfUse: Boolean = true
)

/**
 * Type used when making the api call to create a user
 */
data class CreateUser(
        val user: User,
        val pseudonym: Pseudonym,
        @SerializedName("communication_channel")
        val communicationChannel: CommunicationChannel
)

data class OAuthToken(
        @SerializedName("access_token")
        val accessToken: String
)

data class ForwardURL(
        @SerializedName("forward_url")
        val forwardURL: String
)

/**
 * Type used when user creation api call returns; Use this
 * for everything thereafter
 */
data class CanvasUserApiModel(
        val id: Long,
        val name: String,
        @SerializedName("short_name")
        val shortName: String,
        @SerializedName("sortable_name")
        val sortableName: String,
        @SerializedName("terms_of_use")
        var loginId: String = "",
        var password: String = "",
        @SerializedName("avatar_url")
        val avatarUrl: String? = "",
        var token: String = "",
        var domain: String = CanvasNetworkAdapter.canvasDomain
)
