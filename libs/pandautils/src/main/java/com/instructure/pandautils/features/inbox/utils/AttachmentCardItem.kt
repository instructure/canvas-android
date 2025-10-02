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
 */
package com.instructure.pandautils.features.inbox.utils

import androidx.work.WorkInfo
import com.instructure.canvasapi2.models.Attachment

data class AttachmentCardItem (
    val attachment: Attachment,
    val status: AttachmentStatus, // TODO: Currently this is not used for proper state handling, but if the upload process will be refactored it can be useful
    val readOnly: Boolean,
    val uploadProgress: Float? = null // Upload progress from 0.0 to 1.0, null if not uploading
)

enum class AttachmentStatus {
    UPLOADING,
    UPLOADED,
    FAILED

    ;

    companion object {
        fun fromWorkInfoState(state: WorkInfo.State): AttachmentStatus {
            return when (state) {
                WorkInfo.State.SUCCEEDED -> AttachmentStatus.UPLOADED
                WorkInfo.State.FAILED -> AttachmentStatus.FAILED
                WorkInfo.State.ENQUEUED -> AttachmentStatus.UPLOADING
                WorkInfo.State.RUNNING -> AttachmentStatus.UPLOADING
                WorkInfo.State.BLOCKED -> AttachmentStatus.FAILED
                WorkInfo.State.CANCELLED -> AttachmentStatus.FAILED
            }
        }
    }
}