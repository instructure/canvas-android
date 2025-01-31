/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */package com.instructure.parentapp.features.login.createaccount

import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.PairingCode
import com.instructure.canvasapi2.models.TermsOfService
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.models.postmodels.CreateObserverPostBody
import com.instructure.canvasapi2.models.postmodels.Observer
import com.instructure.canvasapi2.models.postmodels.Pseudonym
import com.instructure.canvasapi2.utils.DataResult
import javax.inject.Inject

class CreateAccountRepository @Inject constructor(
    private val userApi: UserAPI.UsersInterface
) {

    suspend fun createObserverUser(
        domain: String,
        accountId: String,
        pairingCode: String,
        fullName: String,
        email: String,
        password: String
    ): DataResult<User> {
        val domainUrl = if (!domain.startsWith("https://")) {
            "https://$domain"
        } else {
            domain
        }
        val url = "${domainUrl}${RestParams().apiVersion}accounts/${accountId}/users"
        val createObserver = CreateObserverPostBody(
            Observer(fullName),
            Pseudonym(email, password),
            PairingCode(pairingCode)
        )
        return userApi.createObserverAccount(url, createObserver)
    }

    suspend fun getTermsOfService(
        domain: String,
        accountId: String,
    ): TermsOfService? {
        val domainUrl = if (!domain.startsWith("https://")) {
            "https://$domain"
        } else {
            domain
        }
        val url = "${domainUrl}${RestParams().apiVersion}accounts/${accountId}/terms_of_service"
        return userApi.getTermsOfService(url).dataOrNull
    }
}
