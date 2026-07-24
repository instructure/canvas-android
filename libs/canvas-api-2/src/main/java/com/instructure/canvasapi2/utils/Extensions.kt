/*
 * Copyright (C) 2022 - present Instructure, Inc.
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

import com.instructure.canvasapi2.builders.RestParams
import okhttp3.Request
import java.util.Locale

/**
 * Replacement for Kotlin's deprecated `capitalize()` function.
 */
fun String.capitalized(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) {
            it.titlecase(Locale.getDefault())
        } else {
            it.toString()
        }
    }
}

suspend fun <T>DataResult<List<T>>.depaginate(nextPageCall: suspend (nextUrl: String) -> DataResult<List<T>>): DataResult<List<T>> {
    if (this !is DataResult.Success) return this

    val depaginatedList = data.toMutableList()
    var nextUrl = linkHeaders.nextUrl
    while (nextUrl != null) {
        val newItemsResult = nextPageCall(nextUrl)
        if (newItemsResult is DataResult.Success) {
            depaginatedList.addAll(newItemsResult.data)
            nextUrl = newItemsResult.linkHeaders.nextUrl
        } else {
            nextUrl = null
        }
    }

    return DataResult.Success(depaginatedList, apiType = apiType)
}

/**
 * The HTTP status code behind a failed [DataResult], or null when the result is a success or the
 * failure carries no code (e.g. a parsing error or a network exception without a response).
 */
fun DataResult<*>?.httpStatusCode(): Int? {
    val fail = this as? DataResult.Fail ?: return null
    return when (val failure = fail.failure) {
        is Failure.Authorization -> HTTP_UNAUTHORIZED
        is Failure.Network -> failure.errorCode ?: fail.response?.code
        else -> fail.response?.code
    }
}

/** The failure message behind a failed [DataResult], or null when it is a success. */
fun DataResult<*>?.errorMessage(): String? {
    return (this as? DataResult.Fail)?.failure?.message
}

/**
 * Whether a failed [DataResult] is worth retrying: an unknown code, a 401 (which the token-refresh
 * path handles), or any 5xx. A null result (missing/invalid input) is treated as non-transient.
 */
fun DataResult<*>?.isTransientFailure(): Boolean {
    if (this == null) return false
    val code = httpStatusCode()
    return code == null || code == HTTP_UNAUTHORIZED || code >= HTTP_SERVER_ERROR
}

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_SERVER_ERROR = 500

fun Request.restParams(): RestParams? {
    return when {
        this.tag(RestParams::class.java) != null -> {
            this.tag(RestParams::class.java)
        }

        this.tag() != null && this.tag() is RestParams -> {
            this.tag() as RestParams
        }

        else -> null
    }
}
