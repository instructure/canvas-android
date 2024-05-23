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

import kotlinx.parcelize.Parcelize

/**
 * A bookmark object used for storing Canvas URLs and meta data about said url.
 */
@Parcelize
data class Bookmark(
        override val id: Long = 0,
        val name: String? = null,
        val url: String? = null,
        val position: Int = 0

) : CanvasModel<Bookmark>() {
    // A helper for storing a course id, not part of the API
    var courseId: Long = 0
}
