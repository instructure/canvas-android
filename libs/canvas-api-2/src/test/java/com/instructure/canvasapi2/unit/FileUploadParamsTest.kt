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

package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.models.FileUploadParams
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.*

class FileUploadParamsTest {
    @Test
    fun getPlainTextUploadParams() {
        val uploadParams = LinkedHashMap<String, String>()
        uploadParams["param1"] = "value1"
        uploadParams["param2"] = "value2"
        uploadParams["param3"] = "value3"

        val fileUploadParams = FileUploadParams()
        fileUploadParams.uploadParams = uploadParams

        val plainUploadParams = fileUploadParams.getPlainTextUploadParams()

        for ((key, _) in uploadParams) {
            val requestBody = plainUploadParams[key]
            assertNotNull(requestBody)
            assertEquals("text", requestBody?.contentType()!!.type)
            assertEquals("plain", requestBody.contentType()!!.subtype)
        }
    }
}