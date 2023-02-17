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

package com.emeritus.student.mobius.common

import com.spotify.mobius.EventSource
import com.spotify.mobius.disposables.Disposable
import com.spotify.mobius.functions.Consumer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

/**
 * An EventSource which aids in mapping Channel data of one type to events a target type. To use this class, either
 * create a subclass and override [mapEvent], or call [ChannelSource.getSource] and pass in a function that performs
 * event mapping.
 */
abstract class ChannelSource<T : Any, E : Any> (private val channel: BroadcastChannel<T>) : EventSource<E> {

    override fun subscribe(eventConsumer: Consumer<E>): Disposable {
        val receiveChannel = channel.openSubscription()
        GlobalScope.launch {
            receiveChannel.consumeEach {
                val event = mapEvent(it)
                event?.let { nunNullEvent -> eventConsumer.accept(nunNullEvent) }
            }
        }
        return Disposable { receiveChannel.cancel() }
    }

    /**
     * Takes in events of type [T] and returns events of the target type [E]. Returns null if there is no valid mapping
     * for the input event or if an output event should not be produced.
     */
    abstract fun mapEvent(event: T): E?

    companion object {
        val channelStore = hashMapOf<String, BroadcastChannel<*>>()

        /**
         * Produces a [BroadcastChannel] of the specified type [T], returning an existing channel if it exists or creating
         * a new channel if either it does not exist, or it does exist but has been closed. Only one channel per unique
         * type [T] will exist at a time.
         */
        @Suppress("UNCHECKED_CAST")
        inline fun <reified T : Any> getChannel(): BroadcastChannel<T> {
            val className = T::class.java.canonicalName!!
            var channel = channelStore[className]
            if ((channel as? BroadcastChannel<T>)?.isClosedForSend != false) {
                channel?.close()
                channel = BroadcastChannel<T>(100)
                channelStore[className] = channel
            }
            return channel
        }

        /**
         * Creates a ChannelSource object with channel data type [T] and target event type [E]. A [mapper] function
         * must be provided to map input events of type [T] to events of type [E]. This function may return a null value
         * if there is no valid mapping for the input event or if an output event should not be produced.
         */
        inline fun <reified T : Any, reified E : Any> getSource(crossinline mapper: (T) -> E?): ChannelSource<T, E> {
            val channel = getChannel<T>()
            return object : ChannelSource<T, E>(channel) {
                override fun mapEvent(event: T): E? = mapper(event)
            }
        }

    }

}
