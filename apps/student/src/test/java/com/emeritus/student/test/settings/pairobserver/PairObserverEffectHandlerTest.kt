/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.emeritus.student.test.settings.pairobserver

import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.PairingCode
import com.instructure.canvasapi2.models.TermsOfService
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.DataResult
import com.emeritus.student.mobius.settings.pairobserver.PairObserverEffect
import com.emeritus.student.mobius.settings.pairobserver.PairObserverEffectHandler
import com.emeritus.student.mobius.settings.pairobserver.PairObserverEvent
import com.emeritus.student.mobius.settings.pairobserver.ui.PairObserverView
import com.spotify.mobius.functions.Consumer
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executors

class PairObserverEffectHandlerTest : Assert() {
    private val view: PairObserverView = mockk(relaxed = true)
    private val effectHandler =
        PairObserverEffectHandler()
            .apply { view = this@PairObserverEffectHandlerTest.view }
    private val eventConsumer: Consumer<PairObserverEvent> = mockk(relaxed = true)
    private val connection = effectHandler.connect(eventConsumer)

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    }

    @Test
    fun `LoadData with failed pairing code results in failed DataLoaded`() {
        val expectedEvent = PairObserverEvent.DataLoaded(
            DataResult.Fail(),
            DataResult.Fail()
        )

        mockkObject(UserManager)
        every { UserManager.generatePairingCodeAsync(true) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        every { UserManager.getTermsOfServiceAsync(true) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        connection.accept(PairObserverEffect.LoadData(true))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `LoadData results in DataLoaded`() {
        val pairingCode = PairingCode(code = "code")
        val termsOfService = TermsOfService(accountId = 123L)
        val expectedEvent = PairObserverEvent.DataLoaded(
            DataResult.Success(pairingCode),
            DataResult.Success(termsOfService)
        )

        mockkObject(UserManager)
        every { UserManager.generatePairingCodeAsync(true) } returns mockk {
            coEvery { await() } returns DataResult.Success(pairingCode)
        }

        every { UserManager.getTermsOfServiceAsync(true) } returns mockk {
            coEvery { await() } returns DataResult.Success(termsOfService)
        }

        connection.accept(PairObserverEffect.LoadData(true))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }
}