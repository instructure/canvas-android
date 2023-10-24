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

package com.instructure.canvasapi2.models

import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.utils.toDate
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Message(
        override var id: Long = 0,
        @SerializedName("created_at")
        var createdAt: String? = null,
        var body: String? = null,
        @SerializedName("author_id")
        var authorId: Long = 0,
        @SerializedName("generated")
        var isGenerated: Boolean = false,
        var attachments: List<Attachment> = ArrayList(),
        @SerializedName("media_comment")
        var mediaComment: MediaComment? = null,
        // The submission field is not supported by the API, and not used in our code
        //var submission: Submission? = null,
        @SerializedName("forwarded_messages")
        var forwardedMessages: List<Message> = ArrayList(),
        @SerializedName("participating_user_ids")
        var participatingUserIds: List<Long> = ArrayList()
) : CanvasModel<Message>() {
    override val comparisonDate get() = createdAt.toDate()
}
