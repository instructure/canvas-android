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
package com.instructure.loginapi.login.unit;

import com.instructure.loginapi.login.model.DomainVerificationResult;

import org.junit.Assert;

import org.junit.Test;

public class DomainVerificationResultTest extends org.junit.Assert {
    @Test
    public void getResult_success() throws Exception {
        DomainVerificationResult.DomainVerificationCode code = DomainVerificationResult.DomainVerificationCode.Success;

        DomainVerificationResult result = new DomainVerificationResult();
        result.setResult(code);

        Assert.assertEquals(DomainVerificationResult.DomainVerificationCode.Success, result.getResult());
    }

    @Test
    public void getResult_unknownError() throws Exception {
        DomainVerificationResult.DomainVerificationCode code = DomainVerificationResult.DomainVerificationCode.UnknownError;

        DomainVerificationResult result = new DomainVerificationResult();
        result.setResult(code);

        Assert.assertEquals(DomainVerificationResult.DomainVerificationCode.UnknownError, result.getResult());
    }

    @Test
    public void getResult_unknownUserAgent() throws Exception {
        DomainVerificationResult.DomainVerificationCode code = DomainVerificationResult.DomainVerificationCode.UnknownUserAgent;

        DomainVerificationResult result = new DomainVerificationResult();
        result.setResult(code);

        Assert.assertEquals(DomainVerificationResult.DomainVerificationCode.UnknownUserAgent, result.getResult());
    }

    @Test
    public void getResult_generalError() throws Exception {
        DomainVerificationResult.DomainVerificationCode code = DomainVerificationResult.DomainVerificationCode.GeneralError;

        DomainVerificationResult result = new DomainVerificationResult();
        result.setResult(code);

        Assert.assertEquals(DomainVerificationResult.DomainVerificationCode.GeneralError, result.getResult());
    }

    @Test
    public void getResult_domainNotAuthorized() throws Exception {
        DomainVerificationResult.DomainVerificationCode code = DomainVerificationResult.DomainVerificationCode.DomainNotAuthorized;

        DomainVerificationResult result = new DomainVerificationResult();
        result.setResult(code);

        Assert.assertEquals(DomainVerificationResult.DomainVerificationCode.DomainNotAuthorized, result.getResult());
    }
}
