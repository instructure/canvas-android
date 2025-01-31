/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.features.login.createaccount

import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.models.PairingCode
import com.instructure.canvasapi2.models.TermsOfService
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.models.postmodels.CreateObserverPostBody
import com.instructure.canvasapi2.models.postmodels.Observer
import com.instructure.canvasapi2.models.postmodels.Pseudonym
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class CreateAccountRepositoryTest {

    private val userApi: UserAPI.UsersInterface = mockk(relaxed = true)

    private lateinit var createAccountRepository: CreateAccountRepository

    private val termsTestResponse = TermsOfService(content = "Content")

    private val createObserverUserTestResponse = DataResult.Success(User(name = "Test User"))

    private fun createRepository() {
        createAccountRepository = CreateAccountRepository(userApi)
    }

    @Before
    fun setup() {
        createRepository()
    }

    @Test
    fun `createObserverAccount should return proper data`() = runTest {
        coEvery {
            userApi.createObserverAccount(
                any(),
                any()
            )
        } returns createObserverUserTestResponse
        val result = createAccountRepository.createObserverUser(
            domain = "domain",
            accountId = "accountId",
            pairingCode = "pairingCode",
            fullName = "name",
            email = "test@email.com",
            password = "password"
        )
        assertEquals(createObserverUserTestResponse, result)
    }

    @Test
    fun `createObserverAccount should create proper data`() = runTest {
        createAccountRepository.createObserverUser(
            domain = "domain",
            accountId = "accountId",
            pairingCode = "pairingCode",
            fullName = "name",
            email = "test@email.com",
            password = "password"
        )
        val body = CreateObserverPostBody(
            Observer("name"),
            Pseudonym("test@email.com", "password"),
            PairingCode("pairingCode")
        )
        val url = "https://domain/api/v1/accounts/accountId/users"
        coVerify { userApi.createObserverAccount(url, body) }
    }

    @Test
    fun `getTermsOfService should return proper data`() = runTest {
        coEvery { userApi.getTermsOfService(any()) } returns DataResult.Success(termsTestResponse)
        val result = createAccountRepository.getTermsOfService(
            domain = "domain",
            accountId = "accountId"
        )
        assertEquals(termsTestResponse, result)
    }

    @Test
    fun `createObserverAccount should return fail object if call fails`() = runTest {
        val fail = DataResult.Fail()
        coEvery { userApi.createObserverAccount(any(), any()) } returns fail

        val result = createAccountRepository.createObserverUser(
            domain = "testDomain",
            accountId = "accountId",
            pairingCode = "pairingCode",
            fullName = "Test Name",
            email = "test@email.com",
            password = "password"
        )
        assertEquals(fail, result)
    }

    @Test
    fun `getTermsOfService returns null if call fails`() = runTest {
        coEvery { userApi.getTermsOfService(any()) } returns DataResult.Fail()

        val result = createAccountRepository.getTermsOfService(
            domain = "domain",
            accountId = "accountId"
        )
        assertNull(result)
    }
}
