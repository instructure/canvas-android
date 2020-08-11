/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.loginapi.login.unit

import com.instructure.loginapi.login.model.DomainVerificationResult
import com.instructure.loginapi.login.model.DomainVerificationResult.DomainVerificationCode
import org.junit.Assert
import org.junit.Test

class DomainVerificationResultTest : Assert() {
    @Test
    fun getResult_success() {
        val code = DomainVerificationCode.Success
        val result = DomainVerificationResult()
        result.result = code
        assertEquals(DomainVerificationCode.Success, result.result)
    }

    @Test
    fun getResult_unknownError() {
        val code = DomainVerificationCode.UnknownError
        val result = DomainVerificationResult()
        result.result = code
        assertEquals(DomainVerificationCode.UnknownError, result.result)
    }

    @Test
    fun getResult_unknownUserAgent() {
        val code = DomainVerificationCode.UnknownUserAgent
        val result = DomainVerificationResult()
        result.result = code
        assertEquals(DomainVerificationCode.UnknownUserAgent, result.result)
    }

    @Test
    fun getResult_generalError() {
        val code = DomainVerificationCode.GeneralError
        val result = DomainVerificationResult()
        result.result = code
        assertEquals(DomainVerificationCode.GeneralError, result.result)
    }

    @Test
    fun getResult_domainNotAuthorized() {
        val code = DomainVerificationCode.DomainNotAuthorized
        val result = DomainVerificationResult()
        result.result = code
        assertEquals(DomainVerificationCode.DomainNotAuthorized, result.result)
    }
}
