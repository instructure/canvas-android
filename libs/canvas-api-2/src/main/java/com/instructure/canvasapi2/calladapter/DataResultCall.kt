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
package com.instructure.canvasapi2.calladapter

import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

class DataResultCall<T : Any>(private val delegate: Call<T>, private val successType: Type): Call<DataResult<T>> {

    override fun clone(): Call<DataResult<T>> = DataResultCall(delegate.clone(), successType)

    override fun execute(): Response<DataResult<T>> {
        throw UnsupportedOperationException("DataResultCall doesn't support execute")
    }

    override fun enqueue(callback: Callback<DataResult<T>>) {
        return delegate.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                val code = response.code()
                val error = response.errorBody()

                if (response.isSuccessful) {
                    callback.onResponse(this@DataResultCall, Response.success(createSuccessResult(response)))
                } else {
                    if (error != null) {
                        val failure = if (code == 401 || code == 403) {
                            Failure.Authorization(response.message())
                        } else {
                            Failure.Network(response.message(), code)
                        }
                        callback.onResponse(this@DataResultCall, Response.success(DataResult.Fail(failure, response.raw(), error)))
                    } else {
                        callback.onResponse(this@DataResultCall, Response.success(DataResult.Fail()))
                    }
                }
            }

            override fun onFailure(call: Call<T>, throwable: Throwable) {
                callback.onResponse(this@DataResultCall, Response.success(DataResult.Fail(Failure.Exception(throwable, throwable.message))))
            }
        })
    }

    private fun createSuccessResult(response: Response<T>): DataResult<T> {
        val body = response.body()
        val unitResponse = successType.typeName == Unit.javaClass.name

        return if (body != null || unitResponse) {
            val linkHeaders = APIHelper.parseLinkHeaderResponse(response.headers())
            val isCachedResponse = APIHelper.isCachedResponse(response.raw())
            val apiType = if (isCachedResponse) ApiType.CACHE else ApiType.API

            if (body == null) {
                try {
                    // This should always be Unit, but we can catch the exception and return a Failure if it's not. (In cases where the API interface is misconfigured)
                    DataResult.Success(Unit as T, linkHeaders, apiType)
                } catch (e: ClassCastException) {
                    return DataResult.Fail(Failure.ParsingError)
                }
            } else {
                DataResult.Success(body, linkHeaders, apiType)
            }
        } else {
            DataResult.Fail(Failure.ParsingError)
        }
    }

    override fun isExecuted(): Boolean = delegate.isExecuted

    override fun cancel() = delegate.cancel()

    override fun isCanceled(): Boolean = delegate.isCanceled

    override fun request(): Request = delegate.request()

    override fun timeout(): Timeout = delegate.timeout()

}