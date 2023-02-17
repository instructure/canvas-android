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
package com.emeritus.student.mobius.common

import com.spotify.mobius.EventSource
import com.spotify.mobius.disposables.Disposable
import com.spotify.mobius.functions.Consumer
import java.util.*

sealed class GlobalEvent {
    data class TestEvent(val testValue: Int) : GlobalEvent()
}

object GlobalEvents {

    val subscribers: MutableSet<GlobalEventSource<*>> = Collections.newSetFromMap(WeakHashMap<GlobalEventSource<*>, Boolean>())

    fun post(event: GlobalEvent) {
        publish(event)
    }

    private fun publish(event: GlobalEvent) {
        synchronized(subscribers) {
            subscribers.forEach { it.postEvent(event) }
        }
    }

    fun subscribe(eventSource: GlobalEventSource<*>) {
        synchronized(subscribers) { subscribers += eventSource }
    }

    fun unsubscribe(eventSource: GlobalEventSource<*>) {
        synchronized(subscribers) { subscribers -= eventSource }
    }
}

interface GlobalEventMapper<E> {
    fun mapGlobalEvent(event: GlobalEvent): E? = null
}

class GlobalEventSource<E>(private val mapper: GlobalEventMapper<E>) : EventSource<E> {

    private val queuedEvents = LinkedList<E>()

    private var consumer: Consumer<E>? = null

    override fun subscribe(eventConsumer: Consumer<E>): Disposable {
        consumer = eventConsumer
        GlobalEvents.subscribe(this)
        while (queuedEvents.isNotEmpty()) {
            consumer?.accept(queuedEvents.poll())
        }
        return Disposable { consumer = null }
    }

    fun dispose() {
        consumer = null
        GlobalEvents.unsubscribe(this)
    }

    fun postEvent(event: GlobalEvent) {
        mapper.mapGlobalEvent(event)?.let { localEvent ->
            if (consumer == null) {
                queuedEvents.add(localEvent)
            } else {
                consumer?.accept(localEvent)
            }
        }
    }
}
