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
