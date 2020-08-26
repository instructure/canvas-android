/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
@file:Suppress("PackageDirectoryMismatch")

package okreplay

import java.util.*

/**
 * An implementation of [Tape] for use with Ditto.
 *
 * This is essentially a wrapper around [YamlTape][okreplay.YamlTape] which alters its behavior of 'sequential'
 * reads/writes to only be sequential for matching requests. In other words, all requests that match 'A' will be
 * sequential and and all requests that match 'B' will be sequential, but 'B' requests do no need to be sequential
 * to 'A' requests or vice versa. This is useful for testing code that executes network requests in a nondeterministic
 * manner (e.g. parallel/out-of-order requests) but also requires sequential behavior.
 */
class DittoTape(val wrappedTape: Tape) : Tape by wrappedTape {

    var responseMods: MutableList<DittoResponseMod> = mutableListOf()

    init {
        if (wrappedTape !is YamlTape) throw IllegalArgumentException("DittoTape can only wrap instances of YamlTape")
    }

    @Suppress("UNCHECKED_CAST")
    private val interactions: List<YamlRecordedInteraction> by lazy {
        val field = MemoryTape::class.java.getDeclaredField("interactions")
        field.isAccessible = true
        field.get(wrappedTape) as List<YamlRecordedInteraction>
    }

    private val interactionList: List<Queue<RecordedInteraction>> by lazy {
        val list = mutableListOf<Queue<RecordedInteraction>>()
        interactions.map { it.toImmutable() }.forEach { interaction ->
            val queue = list.firstOrNull { matchRule.isMatch(it.peek().request(), interaction.request()) }
                    ?: LinkedList<RecordedInteraction>().also { list.add(it) }
            queue.add(interaction)
        }
        list
    }

    override fun seek(request: Request): Boolean {
        return if (isSequential) {
            return interactionList.any { matchRule.isMatch(request, it.peek().request()) }
        } else {
            wrappedTape.seek(request)
        }
    }

    override fun play(request: Request): Response {
        if (!isReadable) throw IllegalStateException("The tape is not readable")
        return if (isSequential) {
            synchronized(interactionList) {
                val queue = interactionList.firstOrNull { matchRule.isMatch(request, it.peek().request()) }
                        ?: throw IllegalStateException("No recording found that matches request: ${request.url()}")
                if (queue.size == 1) return queue.peek().response() else queue.poll().response()
            }
        } else {
            wrappedTape.play(request)
        }
    }

}
