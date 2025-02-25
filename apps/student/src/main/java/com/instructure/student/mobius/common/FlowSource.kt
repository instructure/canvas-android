/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.instructure.student.mobius.common

import com.spotify.mobius.EventSource
import com.spotify.mobius.disposables.Disposable
import com.spotify.mobius.functions.Consumer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * An EventSource which aids in mapping Flow data of one type to events a target type. To use this class, either
 * create a subclass and override [mapEvent], or call [FlowSource.getSource] and pass in a function that performs
 * event mapping.
 */
abstract class FlowSource<T : Any, E : Any> (private val sharedFlow: SharedFlow<T>) : EventSource<E> {

    override fun subscribe(eventConsumer: Consumer<E>): Disposable {
        val job = GlobalScope.launch {
            sharedFlow.collect {
                val event = mapEvent(it)
                event?.let { nunNullEvent -> eventConsumer.accept(nunNullEvent) }
            }
        }
        return Disposable { job.cancel() }
    }

    /**
     * Takes in events of type [T] and returns events of the target type [E]. Returns null if there is no valid mapping
     * for the input event or if an output event should not be produced.
     */
    abstract fun mapEvent(event: T): E?

    companion object {
        val sharedFlowStore = hashMapOf<String, MutableSharedFlow<*>>()

        /**
         * Produces a [MutableSharedFlow] of the specified type [T], returning an existing channel if it exists or creating
         * a new channel if either it does not exist, or it does exist but has been closed. Only one channel per unique
         * type [T] will exist at a time.
         */
        @Suppress("UNCHECKED_CAST")
        inline fun <reified T : Any> getFlow(): MutableSharedFlow<T> {
            val className = T::class.java.canonicalName!!
            return sharedFlowStore.computeIfAbsent(className) {
                MutableSharedFlow<T>(replay = 0, extraBufferCapacity = 100)
            } as MutableSharedFlow<T>
        }

        /**
         * Creates a ChannelSource object with channel data type [T] and target event type [E]. A [mapper] function
         * must be provided to map input events of type [T] to events of type [E]. This function may return a null value
         * if there is no valid mapping for the input event or if an output event should not be produced.
         */
        inline fun <reified T : Any, reified E : Any> getSource(crossinline mapper: (T) -> E?): FlowSource<T, E> {
            val flow = getFlow<T>()
            return object : FlowSource<T, E>(flow) {
                override fun mapEvent(event: T): E? = mapper(event)
            }
        }

    }
}

/**
 * Extension function for MutableSharedFlow that replicates the trySend behavior.
 * It attempts to emit the event into the flow and returns a result similar to BroadcastChannel's trySend.
 */
fun <T> MutableSharedFlow<T>.trySend(value: T): Boolean {
    return try {
        // Emit value, with an immediate return of false if buffer is full (replay == 0 and no extraBufferCapacity)
        this.tryEmit(value)
    } catch (e: Exception) {
        // In case of any exception (which is rare in SharedFlow), we return false
        false
    }
}
