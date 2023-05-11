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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emeritus.student.mobius.settings.pairobserver.PairObserverModel
import com.emeritus.student.mobius.settings.pairobserver.PairObserverPresenter
import com.emeritus.student.mobius.settings.pairobserver.ui.PairObserverViewState
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PairObserverPresenterTest : Assert() {

    private val domain = "domain"
    private val accountId = 123L

    private lateinit var context: Context
    private lateinit var baseModel: PairObserverModel

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        baseModel = PairObserverModel(domain = domain)
    }

    @Test
    fun `Returns Loading state when model is loading`() {
        val expectedState = PairObserverViewState.Loading
        val model = baseModel.copy(isLoading = true)
        val actualState = PairObserverPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns failed state when model has a null pairing code`() {
        val expectedState = PairObserverViewState.Failed
        val actualState = PairObserverPresenter.present(baseModel, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns failed state when model has an empty pairing code`() {
        val expectedState = PairObserverViewState.Failed
        val model = baseModel.copy(pairingCode = "");
        val actualState = PairObserverPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns Loaded state when model has a pairing code`() {
        val code = "code"
        val expectedState = PairObserverViewState.Loaded(code, domain, accountId)
        val model = baseModel.copy(pairingCode = code, accountId = accountId)
        val actualState = PairObserverPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }
}
