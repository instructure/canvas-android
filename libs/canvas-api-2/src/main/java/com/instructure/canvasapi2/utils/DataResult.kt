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
package com.instructure.canvasapi2.utils

sealed class DataResult<out A> {

    data class Success<A>(val data: A) : DataResult<A>()

    data class Fail(
            val failure: Failure? = null
    ) : DataResult<Nothing>()

    val isSuccess get() = this is Success<A>

    val isFail get() = this is Fail

    val dataOrNull: A? get() = when (this) {
        is Success -> data
        is Fail -> null
    }

    inline fun onSuccess(block: (A) -> Unit): DataResult<A> {
        (this as? Success<A>)?.let { block(it.data) }
        return this
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <FAILURE : Failure> onFail(block: (failure: FAILURE) -> Unit) : DataResult<A> {
        (this as? Fail)?.let { result -> (result.failure as? FAILURE)?.let { block(it) } }
        return this
    }

    inline fun onFailure(block: (failure: Failure?) -> Unit) : DataResult<A> {
        (this as? Fail)?.let { block(it.failure) }
        return this
    }

    fun <B> map(block: (A) -> B): DataResult<B> {
        return when (this) {
            is Success -> Success(
                block(data)
            )
            is Fail -> this
        }
    }
}

// Simple abstraction for repository layer errors, add to as needed
sealed class Failure {
    data class Network(val message: String? = null) : Failure() // Covers 404/500, no internet, etc. Generic case for failed request
    data class Authorization(val message: String? = null) : Failure() // Covers 401, or permission errors.
    data class Exception(val exception: Throwable, val message: String? = null) : Failure()
}
