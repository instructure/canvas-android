/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.pandautils.features.offline.sync

import android.content.Context
import com.instructure.canvasapi2.utils.ApiPrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudioOfflineVideoHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiPrefs: ApiPrefs
) {
    private val studioMediaIdRegex = Regex("[?&]custom_arc_media_id=([^&]+)")

    fun getStudioMediaId(externalUrl: String?): String? =
        externalUrl?.let { studioMediaIdRegex.find(it)?.groupValues?.get(1) }

    fun isStudioVideoAvailableOffline(mediaId: String): Boolean =
        getStudioVideoFile(mediaId).exists()

    fun getStudioVideoUri(mediaId: String): String =
        "file://${getStudioVideoFile(mediaId).absolutePath}"

    fun getStudioPosterUri(mediaId: String): String? {
        val poster = File(getStudioVideoDir(mediaId), "poster.jpg")
        return if (poster.exists()) "file://${poster.absolutePath}" else null
    }

    private fun getStudioVideoDir(ltiLaunchId: String): File {
        val userDir = File(context.filesDir, apiPrefs.user?.id.toString())
        return File(File(userDir, "studio"), ltiLaunchId)
    }

    private fun getStudioVideoFile(ltiLaunchId: String): File =
        File(getStudioVideoDir(ltiLaunchId), "$ltiLaunchId.mp4")
}