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
 */

package com.instructure.canvasapi2.models.postmodels

import com.google.gson.annotations.SerializedName

data class PagePostBody(
        var body: String? = null,
        var title: String? = null,
        @SerializedName("front_page")
        var isFrontPage: Boolean = false,
        @SerializedName("editing_roles")
        var editingRoles: String? = null,
        @SerializedName("published")
        var isPublished: Boolean = false
)

class PagePostBodyWrapper {
    var wiki_page: PagePostBody? = null
}

