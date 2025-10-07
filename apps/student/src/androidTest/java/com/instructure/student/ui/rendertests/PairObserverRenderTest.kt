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
 *
 */
package com.instructure.student.ui.rendertests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.student.mobius.settings.pairobserver.PairObserverModel
import com.instructure.student.mobius.settings.pairobserver.ui.PairObserverFragment
import com.instructure.student.ui.utils.StudentRenderTest
import com.spotify.mobius.runners.WorkRunner
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PairObserverRenderTest : StudentRenderTest() {

    private lateinit var baseModel: PairObserverModel

    @Before
    fun setup() {
        baseModel = PairObserverModel(domain = "domain")
    }

    @Test
    fun displaysToolbarTitles() {
        val model = baseModel.copy()
        loadPageWithModel(model)

        pairObserverRenderPage.assertDisplaysToolbarTitle("Pair with Observer")
    }

    @Test
    fun displaysPairingCode() {
        val code = "code"
        val accountId = 123L
        val model = baseModel.copy(pairingCode = code, accountId = accountId)
        loadPageWithModel(model)

        pairObserverRenderPage.assertCodeDisplayed(code)
    }

    @Test
    fun displaysError() {
        val model = baseModel.copy()
        loadPageWithModel(model)

        pairObserverRenderPage.assertDisplaysError()
    }

    @Test
    fun displaysLoading() {
        val model = baseModel.copy(isLoading = true)
        loadPageWithModel(model)

        pairObserverRenderPage.assertDisplaysLoading()
    }

    private fun loadPageWithModel(model: PairObserverModel) {
        val emptyEffectRunner = object : WorkRunner {
            override fun dispose() = Unit
            override fun post(runnable: Runnable) = Unit
        }
        val fragment = PairObserverFragment.newInstance().apply {
            overrideInitModel = model
            loopMod = { it.effectRunner { emptyEffectRunner } }
        }
        activityRule.activity.loadFragment(fragment)
    }
}
