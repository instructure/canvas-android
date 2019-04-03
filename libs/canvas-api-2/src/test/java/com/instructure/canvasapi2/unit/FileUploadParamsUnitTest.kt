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
import com.instructure.canvasapi2.utils.parse
import org.intellij.lang.annotations.Language
import org.junit.Assert
import org.junit.Test

class FileUploadParamsUnitTest : Assert() {

    @Test
    fun testDiscussionTopic() {
        val uploadParams: FileUploadParams = json.parse()

        Assert.assertNotNull(uploadParams)
        Assert.assertNotNull(uploadParams.uploadParams)
        Assert.assertNotNull(uploadParams.uploadUrl)

        val paramsHash = uploadParams.uploadParams

        Assert.assertNotNull("upload_url", "https://instructure-uploads.s3.amazonaws.com/")

        Assert.assertEquals(8, paramsHash.size)
        Assert.assertEquals(paramsHash["AWSAccessKeyId"], "ASDFDDFKJDSF")
        Assert.assertEquals(paramsHash["key"], "account_99298/attachments/67193665/IMG_FILENAME.jpg")
        Assert.assertEquals(paramsHash["acl"], "private")
        Assert.assertEquals(paramsHash["Policy"], "eyJleHBpcmF0aW9uIjoiMjAxT2xyNlYifSx7ImNvbnRlbnQtdHlwZSI6ImltYWdlXC9qcGV")
        Assert.assertEquals(paramsHash["Signature"], "BNudkjo4pLUIUHYYnbT0D8phmBw")
        Assert.assertEquals(paramsHash["success_action_redirect"], "https://mobiledev.instructure.com/api/v1/files/67193665/create_success?uuid=dssdfsfdsfs")
        Assert.assertEquals(paramsHash["content-type"], "image/jpeg")
    }

    @Language("JSON")
    private val json = """
      {
        "upload_url": "https://instructure-uploads.s3.amazonaws.com/",
        "upload_params": {
          "AWSAccessKeyId": "ASDFDDFKJDSF",
          "Filename": "",
          "key": "account_99298/attachments/67193665/IMG_FILENAME.jpg",
          "acl": "private",
          "Policy": "eyJleHBpcmF0aW9uIjoiMjAxT2xyNlYifSx7ImNvbnRlbnQtdHlwZSI6ImltYWdlXC9qcGV",
          "Signature": "BNudkjo4pLUIUHYYnbT0D8phmBw",
          "success_action_redirect": "https://mobiledev.instructure.com/api/v1/files/67193665/create_success?uuid=dssdfsfdsfs",
          "content-type": "image/jpeg"
        },
        "file_param": "file"
      }"""
}
