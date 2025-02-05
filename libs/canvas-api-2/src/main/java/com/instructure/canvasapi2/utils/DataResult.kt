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

import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Call

sealed class DataResult<out A> {

    // Default api type is unknown because previusly we haven't stored this and would need to change evereywhere in the codebase.
    data class Success<A>(val data: A, val linkHeaders: LinkHeaders = LinkHeaders(), val apiType: ApiType = ApiType.UNKNOWN) : DataResult<A>()

    data class Fail(
        val failure: Failure? = null,
        val response: Response? = null,
        val errorBody: ResponseBody? = null,
    ) : DataResult<Nothing>()

    val isSuccess get() = this is Success<A>

    val isFail get() = this is Fail

    val dataOrNull: A? get() = when (this) {
        is Success -> data
        is Fail -> null
    }

    val dataOrThrow get() = dataOrNull ?: throw IllegalStateException("Cannot get data from DataResult because it is Failed")

    inline fun onSuccess(block: (A) -> Unit): DataResult<A> {
        (this as? Success<A>)?.let { block(it.data) }
        return this
    }

    inline fun <reified FAILURE : Failure> onFail(block: (failure: FAILURE) -> Unit) : DataResult<A> {
        (this as? Fail)?.let { result -> (result.failure as? FAILURE)?.let { block(it) } }
        return this
    }

    inline fun onFailure(block: (failure: Failure?) -> Unit) : DataResult<A> {
        (this as? Fail)?.let { block(it.failure) }
        return this
    }

    fun <B> map(block: (A) -> B): DataResult<B> {
        return when (this) {
            is Success -> Success(block(data), linkHeaders, apiType)
            is Fail -> this
        }
    }

    fun <B> then(block: (A) -> DataResult<B>): DataResult<B> {
        return when (this) {
            is Success -> block(data)
            is Fail -> this
        }
    }

}

// Simple abstraction for repository layer errors, add to as needed
sealed class Failure(open val message: String?) {
    data class Network(override val message: String? = null, val errorCode: Int? = null) : Failure(message) // Covers 404/500, no internet, etc. Generic case for failed request
    data class Authorization(override val message: String? = null) : Failure(message) // Covers 401, or permission errors.
    data class Exception(val exception: Throwable, override val message: String? = null) : Failure(message)
    object ParsingError : Failure("Response was successful, but couldn't be converted to the expected type")
}

internal fun <T> Call<T>.dataResult(): DataResult<T> {
    val response = execute()
    return when {
        response.isSuccessful && response.body() != null -> DataResult.Success(response.body()!!)
        response.code() == 401 -> DataResult.Fail(Failure.Authorization(response.message()))
        else -> DataResult.Fail(Failure.Network(response.message()))
    }
}