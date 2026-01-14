/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.canvasapi2.builders

import com.instructure.canvasapi2.BuildConfig
import com.instructure.canvasapi2.JourneyAdapter
import com.instructure.canvasapi2.calladapter.DataResultCallAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class JourneyRestBuilder @Inject constructor(
    private val journeyAdapter: JourneyAdapter
) {
    fun <T> build(clazz: Class<T>): T {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.JOURNEY_BASE_URL + "/api/v1/")
            .client(journeyAdapter.buildOHttpClient())
            .addCallAdapterFactory(DataResultCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(clazz)
    }
}