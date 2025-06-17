/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.canvasapi2.utils

import sdk.pendo.io.PendoPhasesCallbackInterface
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedQueue


object PendoInitCallbackHandler: PendoPhasesCallbackInterface {

    private val listeners = mutableListOf<WeakReference<PendoInitListener>>()
    private val eventQueue: ConcurrentLinkedQueue<String> = ConcurrentLinkedQueue()

    fun addListener(listener: PendoInitListener) {
        listeners.add(WeakReference(listener))
    }

    fun removeListener(listener: PendoInitListener) {
        listeners.removeAll { it.get() == listener }
    }

    fun addEvent(event: String) {
        if (Analytics.isSessionActive()) {
            Analytics.logEvent(event)
        } else {
            eventQueue.add(event)
        }
    }

    override fun onInitComplete() {
        // Process any events that were queued before initialization
        while (eventQueue.isNotEmpty()) {
            val event = eventQueue.poll()
            if (event != null) {
                Analytics.logEvent(event)
            }
        }

        for (listenerRef in listeners) {
            listenerRef.get()?.onInitComplete()
        }
    }

    override fun onInitFailed() {
        for (listenerRef in listeners) {
            listenerRef.get()?.onInitFailed()
        }
    }
}

interface PendoInitListener {
    fun onInitComplete()
    fun onInitFailed()
}