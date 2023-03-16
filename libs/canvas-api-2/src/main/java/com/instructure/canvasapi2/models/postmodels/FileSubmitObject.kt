/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.canvasapi2.models.postmodels

import android.os.Parcelable
import com.instructure.canvasapi2.models.Attachment
import kotlinx.parcelize.Parcelize

@Parcelize
data class FileSubmitObject(
    var name: String,
    var size: Long,
    var contentType: String,
    var fullPath: String,
    var errorMessage: String? = null, // used when loading files in an asynctask
    var currentState: STATE = STATE.NORMAL
) : Parcelable {
    enum class STATE {
        NORMAL, UPLOADING, COMPLETE
    }

    /**
     * Used to get a basic attachment object for display.
     * @return A skin & bones attachment object
     */
    fun toAttachment(): Attachment {
        val attachment = Attachment()
        attachment.contentType = contentType
        attachment.displayName = name
        attachment.thumbnailUrl = fullPath
        return attachment
    }
}
