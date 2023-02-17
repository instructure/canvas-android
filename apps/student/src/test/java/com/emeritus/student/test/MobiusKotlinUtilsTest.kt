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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emeritus.student.mobius.common.contraMap
import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private typealias MappedType = String
private typealias ConsumerType = Any
private typealias InputType = Int

@RunWith(AndroidJUnit4::class)
class MobiusKotlinUtilsTest : Assert() {

    private lateinit var context: Context

    private lateinit var connection: VerifiableConnection

    private lateinit var connectable: VerifiableConnectable

    private lateinit var mapper: VerifiableMapper

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        connection = VerifiableConnection()
        connectable = VerifiableConnectable(connection)
        mapper = VerifiableMapper()
    }

    @Test
    fun `Invokes mapping function`() {
        val contraConnection = connectable.contraMap(mapper::map, context).connect {}
        contraConnection.accept(0)
        assertTrue(mapper.mapCalled)
    }

    @Test
    fun `Passes input value to mapper`() {
        val expectedInputValue = 0
        val contraConnection = connectable.contraMap(mapper::map, context).connect {}
        contraConnection.accept(expectedInputValue)
        assertEquals(expectedInputValue, mapper.inputValue)
    }

    @Test
    fun `Passes context to mapper`() {
        val contraConnection = connectable.contraMap(mapper::map, context).connect {}
        contraConnection.accept(0)
        assertEquals(context, mapper.contextValue)
    }

    @Test
    fun `Connects wrapped connectable`() {
        val contraConnection = connectable.contraMap(mapper::map, context).connect {}
        contraConnection.accept(0)
        assertTrue(connectable.connectCalled)
    }

    @Test
    fun `Passes consumer to wrapped connectable`() {
        val expectedConsumer = Consumer<ConsumerType> {}
        val contraConnection = connectable.contraMap(mapper::map, context).connect(expectedConsumer)
        contraConnection.accept(0)
        assertEquals(expectedConsumer, connectable.consumerValue)
    }

    @Test
    fun `Invokes accept on wrapped connection`() {
        val contraConnection = connectable.contraMap(mapper::map, context).connect {}
        contraConnection.accept(0)
        assertTrue(connection.acceptCalled)
    }

    @Test
    fun `Invokes dispose on wrapped connection`() {
        val contraConnection = connectable.contraMap(mapper::map, context).connect {}
        contraConnection.dispose()
        assertTrue(connection.disposeCalled)
    }

    @Test
    fun `Passes mapped value to wrapped connection`() {
        val expectedValue = "test"
        mapper.returnValue = expectedValue
        val contraConnection = connectable.contraMap(mapper::map, context).connect {}
        contraConnection.accept(0)
        assertEquals(expectedValue, connection.acceptValue)
    }

    /*
     * Mockk struggled with various aspects of this test, so for now we're using manual
     * implementations to verify invocations and parameters
     */
    private class VerifiableConnection : Connection<MappedType> {

        var acceptValue: MappedType? = null
        var acceptCalled = false
        var disposeCalled = false

        override fun accept(value: MappedType) {
            acceptCalled = true
            acceptValue = value
        }

        override fun dispose() {
            disposeCalled = true
        }
    }

    private class VerifiableConnectable(val connection: VerifiableConnection) : Connectable<MappedType, ConsumerType> {

        var connectCalled = false
        var consumerValue: Consumer<ConsumerType>? = null

        override fun connect(output: Consumer<ConsumerType>): Connection<MappedType> {
            connectCalled = true
            consumerValue = output
            return connection
        }
    }

    private class VerifiableMapper {

        var mapCalled = false
        var inputValue: InputType? = null
        var contextValue: Context? = null
        var returnValue = ""

        fun map(input: InputType, context: Context): MappedType {
            mapCalled = true
            inputValue = input
            contextValue = context
            return returnValue
        }
    }
}
