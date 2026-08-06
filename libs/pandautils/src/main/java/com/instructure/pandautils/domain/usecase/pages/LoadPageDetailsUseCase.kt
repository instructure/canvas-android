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
package com.instructure.pandautils.domain.usecase.pages

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.page.PageRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

class LoadPageDetailsUseCase @Inject constructor(
    private val pageRepository: PageRepository
) : BaseUseCase<LoadPageDetailsUseCase.Params, DataResult<Page>>() {

    override suspend fun execute(params: Params): DataResult<Page> {
        return pageRepository.getPageDetails(params.canvasContext, params.pageId, params.forceNetwork)
    }

    data class Params(
        val canvasContext: CanvasContext,
        val pageId: String,
        val forceNetwork: Boolean = false
    )
}
