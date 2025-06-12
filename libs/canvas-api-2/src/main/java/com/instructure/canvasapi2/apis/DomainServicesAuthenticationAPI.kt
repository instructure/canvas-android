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
package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.JWTTokenResponse
import com.instructure.canvasapi2.utils.DataResult
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Tag

interface DomainServicesAuthenticationAPI {
    @POST("jwts")
    suspend fun getDomainServiceAuthentication(
        @Query("audience") audience: String,
        @Query("workflows[]") workflows: String,
        @Tag params: RestParams
    ): DataResult<JWTTokenResponse>
}