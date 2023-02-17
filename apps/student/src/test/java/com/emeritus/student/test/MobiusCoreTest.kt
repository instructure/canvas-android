/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.emeritus.student.test

import com.emeritus.student.mobius.common.ui.EffectHandler
import com.emeritus.student.mobius.common.ui.UpdateInit
import com.emeritus.student.test.util.matchesEffects
import com.emeritus.student.test.util.matchesFirstEffects
import com.spotify.mobius.First
import com.spotify.mobius.Next
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class MobiusCoreTest {

    private data class TestModel(val initialized: Boolean = false, val updated: Boolean = false)

    private object TestEvent

    private sealed class TestEffect {
        object PerformInit : TestEffect()
        object PerformUpdate : TestEffect()
    }

    private class TestUpdate : UpdateInit<TestModel, TestEvent, TestEffect>() {

        override fun performInit(model: TestModel): First<TestModel, TestEffect> {
            return First.first(model.copy(initialized = true), setOf(TestEffect.PerformInit))
        }

        override fun update(model: TestModel, event: TestEvent): Next<TestModel, TestEffect> {
            return Next.next(model.copy(updated = true), setOf(TestEffect.PerformUpdate))
        }
    }

    private lateinit var update: TestUpdate
    private lateinit var initSpec: InitSpec<TestModel, TestEffect>
    private lateinit var updateSpec: UpdateSpec<TestModel, TestEvent, TestEffect>

    @Before
    fun setup() {
        update = TestUpdate()
        initSpec = InitSpec(update::init)
        updateSpec = UpdateSpec(update::update)
    }

    @Test
    fun `Initializes model with effect`() {
        val baseModel = TestModel()
        val expectedModel = TestModel(initialized = true)
        initSpec
            .whenInit(baseModel)
            .then(
                assertThatFirst(
                    FirstMatchers.hasModel(expectedModel),
                    _root_ide_package_.com.emeritus.student.test.util.matchesFirstEffects<TestModel, TestEffect>(
                        TestEffect.PerformInit
                    )
                )
            )
    }

    @Test
    fun `Initializes without changes when already initialized`() {
        val expectedModel = TestModel(initialized = true)

        // Perform first init
        `Initializes model with effect`()

        // Perform second init
        initSpec
            .whenInit(expectedModel)
            .then(
                assertThatFirst(
                    FirstMatchers.hasModel(expectedModel),
                    FirstMatchers.hasNoEffects()
                )
            )
    }

    @Test
    fun `Updates model with effect`() {
        val baseModel = TestModel(initialized = true)
        val expectedModel = baseModel.copy(updated = true)
        updateSpec
            .given(baseModel)
            .whenEvent(TestEvent)
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    _root_ide_package_.com.emeritus.student.test.util.matchesEffects<TestModel, TestEffect>(
                        TestEffect.PerformUpdate
                    )
                )
            )
    }

    @Test
    fun `Effect handler pushes events while consumer is connected`() {
        val testConsumer: Consumer<TestEvent> = mockk(relaxed = true)

        val effectHandler = object : EffectHandler<Any, TestEvent, TestEffect>() {
            override fun accept(value: TestEffect) = consumer.accept(TestEvent)
        }

        // Connect before pushing effect
        effectHandler.connect(testConsumer)
        effectHandler.accept(TestEffect.PerformInit)

        verify(exactly = 1) {
            testConsumer.accept(TestEvent)
        }

        confirmVerified(testConsumer)
    }

    @Test
    fun `Effect handler does not push events when canceled`() {
        val testConsumer: Consumer<TestEvent> = mockk(relaxed = true)

        val effectHandler = object : EffectHandler<Any, TestEvent, TestEffect>() {
            override fun accept(value: TestEffect) = consumer.accept(TestEvent)
        }

        // Connect and cancel before pushing effect
        effectHandler.connect(testConsumer)
        effectHandler.cancel()
        effectHandler.accept(TestEffect.PerformInit)

        verify(exactly = 0) {
            testConsumer.accept(TestEvent)
        }

        confirmVerified(testConsumer)
    }

    @Test
    fun `Effect handler defers events while consumer is disconnected`() {
        val eventCount = 3
        val testConsumer: Consumer<TestEvent> = mockk(relaxed = true)

        val effectHandler = object : EffectHandler<Any, TestEvent, TestEffect>() {
            override fun accept(value: TestEffect) = consumer.accept(TestEvent)
        }

        // Push events BEFORE connecting
        repeat(eventCount) { effectHandler.accept(TestEffect.PerformInit) }
        effectHandler.connect(testConsumer)

        verify(exactly = eventCount) {
            testConsumer.accept(TestEvent)
        }

        confirmVerified(testConsumer)
    }
}
