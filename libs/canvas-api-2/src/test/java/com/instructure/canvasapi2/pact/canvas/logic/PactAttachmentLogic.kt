/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.canvasapi2.pact.canvas.logic

import com.instructure.canvasapi2.models.Attachment
import io.pactfoundation.consumer.dsl.LambdaDslObject
import org.junit.Assert

fun LambdaDslObject.populateAttachmentFields(): LambdaDslObject {
    this
            .id("id")
            .stringType("content-type")
            .stringType("filename")
            .stringType("display_name")
            .stringType("url")
            // .stringType("thumbnail_url") // TODO: Punt
            // .stringType("preview_url") // TODO: Punt
            .stringMatcher("created_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .id("size") // long whole number

    return this
}

fun assertAttachmentPopulated(description: String, attachment: Attachment) {
    Assert.assertNotNull("$description + id", attachment.id)
    Assert.assertNotNull("$description + contentType", attachment.contentType)
    Assert.assertNotNull("$description + filename", attachment.filename)
    Assert.assertNotNull("$description + displayName", attachment.displayName)
    Assert.assertNotNull("$description + url", attachment.url)
    Assert.assertNotNull("$description + createdAt", attachment.createdAt)
    Assert.assertNotNull("$description + size", attachment.size)
}