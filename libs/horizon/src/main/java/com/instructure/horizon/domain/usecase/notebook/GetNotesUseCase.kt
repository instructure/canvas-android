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
package com.instructure.horizon.domain.usecase.notebook

import com.instructure.horizon.data.datasource.NotebookNetworkDataSource
import com.instructure.horizon.data.repository.NotebookPage
import com.instructure.horizon.data.repository.NotebookRepository
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.redwood.type.OrderDirection
import javax.inject.Inject

class GetNotesUseCase @Inject constructor(
    private val repository: NotebookRepository,
) : BaseUseCase<GetNotesUseCase.Params, NotebookPage>() {

    data class Params(
        val after: String? = null,
        val before: String? = null,
        val itemCount: Int = NotebookNetworkDataSource.DEFAULT_PAGE_SIZE,
        val filterType: NotebookType? = null,
        val courseId: Long? = null,
        val objectTypeAndId: Pair<String, String>? = null,
        val orderDirection: OrderDirection? = null,
        val forceNetwork: Boolean = false,
    )

    override suspend fun execute(params: Params): NotebookPage = repository.getNotes(
        after = params.after,
        before = params.before,
        itemCount = params.itemCount,
        filterType = params.filterType,
        courseId = params.courseId,
        objectTypeAndId = params.objectTypeAndId,
        orderDirection = params.orderDirection,
        forceNetwork = params.forceNetwork,
    )
}
