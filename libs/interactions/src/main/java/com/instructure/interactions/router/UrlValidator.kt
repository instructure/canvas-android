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
package com.instructure.interactions.router

import android.net.Uri

class UrlValidator(var url: String, userDomain: String) {

    private val replacements = listOf(
        "//canvas-student//" to "canvas-student//",
        "//canvas-teacher//" to "canvas-teacher//",
        "//canvas-parent//" to "canvas-parent//"
    )

    val uri: Uri?

    val isHostForLoggedInUser: Boolean

    val isValid: Boolean

    init {
        replacements.forEach { url = url.replace(it.first, it.second) }
        uri = Uri.parse(url)
        isValid = uri != null
        // Assumes user is already signed in (InterwebsToApplication does a signin check)
        isHostForLoggedInUser = userDomain == uri?.host
    }

}
