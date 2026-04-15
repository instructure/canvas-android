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

import com.instructure.horizon.data.repository.FileContentRepository
import com.instructure.horizon.data.repository.FileDetails
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

class GetFileDetailsUseCase @Inject constructor(
    private val repository: FileContentRepository,
) : BaseUseCase<GetFileDetailsUseCase.Params, FileDetails>() {

    data class Params(
        val url: String,
        val courseId: Long,
    )

    override suspend fun execute(params: Params): FileDetails {
        return repository.getFileDetails(params.url, params.courseId)
    }
}
