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

import com.emeritus.student.mobius.common.GlobalEvent
import com.emeritus.student.mobius.common.GlobalEventMapper
import com.emeritus.student.mobius.common.GlobalEventSource
import com.emeritus.student.mobius.common.GlobalEvents
import com.spotify.mobius.functions.Consumer
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class GlobalEventsTest : Assert() {

    @Before
    fun setup() {
        GlobalEvents.subscribers.clear()
    }

    @Suppress("UNUSED_VALUE")
    @Test
    fun `Weakly references subscribers`() {
        var subscriber: GlobalEventSource<Any>? = GlobalEventSource(object : GlobalEventMapper<Any> {})
        GlobalEvents.subscribe(subscriber!!)
        assertEquals(1, GlobalEvents.subscribers.size)
        subscriber = null
        System.gc()
        Thread.sleep(50) // Wait for GC
        assertEquals(0, GlobalEvents.subscribers.size)
    }

    @Test
    fun `Propagates events to subscribers`() {
        val expectedCount = 3
        val mapper = mockk<GlobalEventMapper<Any>>(relaxed = true)
        val subscribers = List(expectedCount) { GlobalEventSource(mapper) }
        subscribers.onEach { GlobalEvents.subscribe(it) }
        assertEquals(expectedCount, GlobalEvents.subscribers.size)
        val event = GlobalEvent.TestEvent(0)
        GlobalEvents.post(event)
        verify(exactly = expectedCount) {
            mapper.mapGlobalEvent(event)
        }
        confirmVerified(mapper)
    }

    @Test
    fun `Disposes subscribers`() {
        val mapper = mockk<GlobalEventMapper<Any>>(relaxed = true)
        val subscriber = GlobalEventSource(mapper)
        GlobalEvents.subscribe(subscriber)
        subscriber.dispose()
        val event = GlobalEvent.TestEvent(0)
        GlobalEvents.post(event)
        verify(exactly = 0) {
            mapper.mapGlobalEvent(event)
        }
        confirmVerified(mapper)
    }

    @Test
    fun `Propagates events to active consumer`() {
        val eventValue = "Event!"
        val expectedEventCount = 3
        val consumer: Consumer<String> = mockk(relaxed = true)
        val subscriber = GlobalEventSource(object : GlobalEventMapper<String> {
            override fun mapGlobalEvent(event: GlobalEvent): String? = eventValue
        })
        subscriber.subscribe(consumer)
        GlobalEvents.subscribe(subscriber)
        val event = GlobalEvent.TestEvent(0)
        repeat(expectedEventCount) { GlobalEvents.post(event) }
        verify(exactly = expectedEventCount) {
            consumer.accept(eventValue)
        }
        confirmVerified(consumer)
    }

    @Test
    fun `Propagates events to resumed consumer`() {
        val eventValue = "Event!"
        val expectedEventCount = 3
        val consumer: Consumer<String> = mockk(relaxed = true)
        val subscriber = GlobalEventSource(object : GlobalEventMapper<String> {
            override fun mapGlobalEvent(event: GlobalEvent): String? = eventValue
        })
        GlobalEvents.subscribe(subscriber)
        val event = GlobalEvent.TestEvent(0)
        repeat(expectedEventCount) { GlobalEvents.post(event) }
        subscriber.subscribe(consumer)
        verify(exactly = expectedEventCount) {
            consumer.accept(eventValue)
        }
        confirmVerified(consumer)
    }
}
