/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 */package com.instructure.student.mobius.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.spotify.mobius.EventSource
import com.spotify.mobius.disposables.Disposable
import com.spotify.mobius.functions.Consumer

abstract class LiveDataSource<T : Any, E : Any>(
    private val liveData: LiveData<T?>,
) : EventSource<E> {

    override fun subscribe(consumer: Consumer<E>): Disposable {
        val observer = Observer<T?> { map(it)?.let { consumer.accept(it) } }
        liveData.observeForever(observer)
        return Disposable { liveData.removeObserver(observer) }
    }

    abstract fun map(data: T?): E?

    companion object {

        inline fun <reified T : Any, E : Any> of(
            liveData: LiveData<T?>,
            crossinline mapper: (T?) -> E?
        ): EventSource<E> {
            return object : LiveDataSource<T, E>(liveData) {
                override fun map(data: T?): E? = mapper(data)
            }
        }
    }
}