/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.canvasapi2.pact.docviewer.teacher

import android.content.Context
import au.com.dius.pact.consumer.junit.ConsumerPactTest
import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.core.model.PactSpecVersion
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.Logger
import org.junit.Before
import org.mockito.Mockito

abstract class DocViewerPact : ConsumerPactTest() {

    companion object {
        const val providerName = "DocViewer"
        const val consumerName = "Android Teacher"
        const val versionApi = "2018-04-06" // DocViewer Api version, note not all endpoints use this version
    }

    override fun providerName(): String {
        return providerName
    }

    override fun consumerName(): String {
        return consumerName
    }

    fun getParams(mockServer: MockServer): RestParams {
        return RestParams(domain = mockServer.getUrl(), apiVersion = "")
    }

    @Before
    fun setUp() {
        RestBuilder.isPact = true
        ContextKeeper.appContext = Mockito.mock(Context::class.java)
        Logger.IS_LOGGING = false
    }

    override fun getSpecificationVersion(): PactSpecVersion {
        return PactSpecVersion.V2
    }
}
