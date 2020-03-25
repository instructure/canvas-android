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

import com.instructure.canvasapi2.models.Author
import io.pactfoundation.consumer.dsl.LambdaDslObject
import org.junit.Assert

fun LambdaDslObject.populateAuthorFields(): LambdaDslObject {
    this
            .id("id")
            .stringType("display_name")
            .stringType("avatar_image_url")
            .stringType("html_url")
            .stringType("pronouns")

    return this
}

fun assertAuthorPopulated(description: String, author: Author) {
    Assert.assertNotNull("$description + id", author.id)
    Assert.assertNotNull("$description + displayName", author.displayName)
    Assert.assertNotNull("$description + avatarImageUrl", author.avatarImageUrl)
    Assert.assertNotNull("$description + htmlUrl", author.htmlUrl)
    Assert.assertNotNull("$description + pronouns", author.pronouns)
}