/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.pandautils.features.about

import android.content.Context
import com.instructure.canvasapi2.utils.ApiPrefs

abstract class AboutRepository(
    private val context: Context,
    private val apiPrefs: ApiPrefs
) {

    val appName = context.packageManager.getApplicationLabel(context.applicationInfo).toString()

    val domain = apiPrefs.domain

    val loginId = apiPrefs.user?.loginId.orEmpty()

    val email = apiPrefs.user?.email ?: apiPrefs.user?.primaryEmail.orEmpty()

    abstract val appVersion: String
}