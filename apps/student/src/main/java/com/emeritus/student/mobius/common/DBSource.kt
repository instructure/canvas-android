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
import com.squareup.sqldelight.Query

/**
 * An EventSource which aids in mapping database updates of one type to events a different type. To use this class,
 * either create a subclass and override [executeAndMap], or use a helper like [DBSource.ofList] and pass in a function
 * that performs event mapping.
 */
abstract class DBSource<T : Any, E : Any>(
    private val query: Query<T>,
    private val performInitialQuery: Boolean = true
) : EventSource<E> {

    override fun subscribe(eventConsumer: Consumer<E>): Disposable {
        val listener: Query.Listener = object : Query.Listener {
            override fun queryResultsChanged() {
                executeAndMap(query)?.let { eventConsumer.accept(it) }
            }
        }
        query.addListener(listener)
        if (performInitialQuery) executeAndMap(query)?.let { eventConsumer.accept(it) }
        return Disposable { query.removeListener(listener) }
    }

    /**
     * Takes in a query of type [T] and returns events of the target type [E]. Returns null if there is no valid mapping
     * for the input event or if an output event should not be produced.
     */
    abstract fun executeAndMap(query: Query<T>): E?

    companion object {

        /**
         * Creates a single-value [DBSource] object for query data type [T] and target event type [E]. A [mapper] function
         * must be provided to map input events of type [T] to events of type [E]. This function may return a null value
         * if there is no valid mapping for the input event or if an output event should not be produced. The input to
         * this function will be null if the query returns no results.
         */
        inline fun <reified T : Any, reified E : Any> ofSingle(
            query: Query<T>,
            performInitialQuery: Boolean = true,
            crossinline mapper: (T?) -> E?
        ): DBSource<T, E> {
            return object : DBSource<T, E>(query, performInitialQuery) {
                override fun executeAndMap(query: Query<T>): E? = mapper(query.executeAsOneOrNull())
            }
        }

        /**
         * Creates a list-value [DBSource] object with query data type [T] and target event type [E]. A [mapper] function
         * must be provided to map input events of type [List<T>] to events of type [E]. This function may return a null
         * value if there is no valid mapping for the input event or if an output event should not be produced.
         */
        inline fun <reified T : Any, reified E : Any> ofList(
            query: Query<T>,
            performInitialQuery: Boolean = true,
            crossinline mapper: (List<T>) -> E?
        ): DBSource<T, E> {
            return object : DBSource<T, E>(query, performInitialQuery) {
                override fun executeAndMap(query: Query<T>): E? = mapper(query.executeAsList())
            }
        }
    }

}
