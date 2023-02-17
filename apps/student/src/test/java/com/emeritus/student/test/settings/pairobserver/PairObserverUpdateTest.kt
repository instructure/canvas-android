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

import com.instructure.canvasapi2.models.PairingCode
import com.instructure.canvasapi2.models.TermsOfService
import com.instructure.canvasapi2.utils.DataResult
import com.emeritus.student.mobius.settings.pairobserver.PairObserverEffect
import com.emeritus.student.mobius.settings.pairobserver.PairObserverEvent
import com.emeritus.student.mobius.settings.pairobserver.PairObserverModel
import com.emeritus.student.mobius.settings.pairobserver.PairObserverUpdate
import com.instructure.student.test.util.matchesEffects
import com.instructure.student.test.util.matchesFirstEffects
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PairObserverUpdateTest : Assert() {
    private val initSpec = InitSpec(PairObserverUpdate()::init)
    private val updateSpec = UpdateSpec(PairObserverUpdate()::update)

    private lateinit var initModel: PairObserverModel

    @Before
    fun setup() {
        initModel = PairObserverModel(domain = "domain")
    }

    @Test
    fun `Initializes into a loading state`() {
        val expectedModel = initModel.copy(isLoading = true)
        initSpec
            .whenInit(initModel)
            .then(
                assertThatFirst(
                    FirstMatchers.hasModel(expectedModel),
                    matchesFirstEffects<PairObserverModel, PairObserverEffect>(
                        PairObserverEffect.LoadData(true))
                )
            )
    }

    @Test
    fun `Refresh event forces network reload`() {
        val expectedModel = initModel.copy(isLoading = true)
        updateSpec
            .given(initModel)
            .whenEvent(PairObserverEvent.RefreshCode)
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    matchesEffects<PairObserverModel, PairObserverEffect>(
                        PairObserverEffect.LoadData(true),
                        PairObserverEffect.LogRefresh
                    )
                )
            )
    }

    @Test
    fun `DataLoaded event updates the model`() {
        val code = "code"
        val pairingCode = DataResult.Success(PairingCode(code = code))
        val termsOfService = DataResult.Success(TermsOfService(accountId = 123L))

        val expectedModel = initModel.copy(isLoading = false, pairingCode = code, accountId = 123L)

        updateSpec
            .given(initModel.copy(isLoading = true))
            .whenEvent(PairObserverEvent.DataLoaded(pairingCode, termsOfService))
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel)
                )
            )
    }

    @Test
    fun `DataLoaded event updates the model when failed to get pairing code`() {
        val initModel = initModel.copy(isLoading = true, pairingCode = "Old")

        val expectedModel = initModel.copy(isLoading = false, pairingCode = null, accountId = null)

        updateSpec
            .given(initModel)
            .whenEvent(PairObserverEvent.DataLoaded(DataResult.Fail(), DataResult.Fail()))
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel)
                )
            )
    }
}