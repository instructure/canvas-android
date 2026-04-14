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
package com.instructure.pandautils.data.repository.accountdomain

import com.instructure.canvasapi2.apis.AccountDomainInterface
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.depaginate

class AccountDomainRepositoryImpl(private val api: AccountDomainInterface) :
    AccountDomainRepository {
    override suspend fun search(query: String): List<AccountDomain> {
        val restParams = RestParams(
            usePerPageQueryParam = true,
            isForceReadFromNetwork = true,
        )
        val result = api.campusSearch(query, restParams).depaginate {
            api.next(it, restParams)
        }
        return result.dataOrNull.orEmpty()
    }
}