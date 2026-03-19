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
 */package com.instructure.pandautils.domain.usecase.accountdomain

import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.pandautils.data.repository.accountdomain.AccountDomainRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

class SearchAccountDomainUseCase @Inject constructor(private val repository: AccountDomainRepository) : BaseUseCase<SearchAccountDomainUseCase.Params, List<AccountDomain>>() {

    override suspend fun execute(params: Params): List<AccountDomain> {
        if (params.query.length < 3) return emptyList()
        return repository.search(params.query)
    }

    data class Params(
        val query: String
    )
}