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

import com.instructure.canvasapi2.utils.DataResult
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

class DataResultCallAdapter<T : Any>(
    private val successType: Type
) : CallAdapter<T, Call<DataResult<T>>> {

    override fun responseType(): Type = successType

    override fun adapt(call: Call<T>): Call<DataResult<T>> {
        return DataResultCall(call, successType)
    }
}