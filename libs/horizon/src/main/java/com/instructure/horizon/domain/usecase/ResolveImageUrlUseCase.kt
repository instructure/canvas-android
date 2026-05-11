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
 */
package com.instructure.horizon.domain.usecase

import com.instructure.horizon.database.dao.HorizonLocalImageDao
import java.io.File
import javax.inject.Inject

class ResolveImageUrlUseCase @Inject constructor(
    private val localImageDao: HorizonLocalImageDao,
) {
    suspend fun resolve(url: String?): String? {
        if (url.isNullOrBlank()) return null
        val local = localImageDao.findByUrl(url) ?: return url
        return if (File(local.localPath).exists()) "file://${local.localPath}" else url
    }

    suspend fun resolveBatch(urls: List<String?>): Map<String, String> {
        val nonNull = urls.filterNotNull().filter { it.isNotBlank() }
        if (nonNull.isEmpty()) return emptyMap()
        val locals = localImageDao.findByUrls(nonNull)
        return locals
            .filter { File(it.localPath).exists() }
            .associate { it.url to "file://${it.localPath}" }
    }
}
