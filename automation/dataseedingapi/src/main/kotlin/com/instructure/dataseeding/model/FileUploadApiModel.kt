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

data class StartFileUpload(
        val name: String,
        val size: Long,
        @SerializedName("on_duplicate")
        val onDuplicate: String = "rename"
)

data class FileUploadParams(
        @SerializedName("upload_url")
        val uploadUrl: String,

        @SerializedName("upload_params")
        var uploadParams: Map<String, String> = emptyMap()
)

data class AttachmentApiModel(
        val id: Long,

        @SerializedName("display_name")
        val displayName: String,

        @SerializedName("filename")
        val fileName: String
)
