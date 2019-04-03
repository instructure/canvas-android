//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//


package api.bitrise

import api.RestAdapterUtils.noAuthClient
import api.bitrise.BitriseRestAdapter.retrofit
import okhttp3.Request
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import util.getEnv
import util.startDateWithinRange
import java.time.ZoneOffset
import java.time.ZonedDateTime

object BitriseApps {
    interface ApiService {
        @GET("apps")
        fun getApps(@Query("next") next: String = ""): Call<BitriseAppData>

        // TODO: There's no way to get a userSlug from a user name
        // https://discuss.bitrise.io/t/how-do-i-get-a-user-slug-from-a-user-name/4667
        // This endpoint will replace getAppsForOrg once slug translation is figured out
        // @GET("users/{userSlug}/apps")
        // fun getUsersApps(@Path("userSlug") appSlug: String,
        //                  @Query("next") next: String = ""): Call<BitriseAppData>

        @GET("apps/{appSlug}/builds")
        fun getBuilds(@Path("appSlug") appSlug: String,
                      @Query("next") next: String = ""): Call<BitriseBuildData>

        @GET("apps/{appSlug}/builds/{buildSlug}")
        fun getBuild(@Path("appSlug") appSlug: String,
                     @Path("buildSlug") buildSlug: String): Call<BitriseSingleBuildData>

        @GET("apps/{appSlug}/builds/{buildSlug}/log")
        fun getBuildLog(@Path("appSlug") appSlug: String,
                        @Path("buildSlug") buildSlug: String): Call<BitriseLogData>

        @POST("apps/{appSlug}/builds")
        fun triggerBuild(@Path("appSlug") appSlug: String,
                         @Body body: BitriseTriggerBuildRequest): Call<BitriseTriggerBuildResponse>

    }

    private val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    fun triggerBuild(appSlug: String, body: BitriseTriggerBuildRequest): BitriseTriggerBuildResponse {
        val response = apiService.triggerBuild(appSlug, body).execute()

        if (!response.isSuccessful) {
            throw RuntimeException(response.toString())
        }

        return response.body() ?: throw RuntimeException("triggerBuild failed")
    }

    fun getBuildLog(appSlug: String, build: BitriseBuildObject): ByteArray {
        var logUrl: String? = null
        val tryCount = 3
        var attempts = 0

        // expiring_raw_log_url only shows up on the second request.
        while (logUrl == null) {
            if (attempts > tryCount) break

            val response = apiService.getBuildLog(appSlug, build.slug).execute()
            logUrl = response.body()?.expiring_raw_log_url
            attempts += 1
        }

        if (logUrl == null) throw RuntimeException("expiring_raw_log_url is null after ${attempts}x attempts")

        // "Important: when you send your GET request to the expiring_raw_log_url URL please do not include the Authorization headers."
        // http://devcenter.bitrise.io/api/v0.1/
        val request = Request.Builder().get().url(logUrl).build()
        return noAuthClient.newCall(request).execute().body()?.bytes() ?: ByteArray(0)
    }

    fun getAppsForOrg(orgSlug: String = getEnv("BITRISE_ORG")): List<BitriseAppObject> {
        val appsList = ArrayList<BitriseAppObject>()
        var response: Response<BitriseAppData>
        var next: String? = ""

        while (next != null) {
            response = apiService.getApps(next).execute()
            val responseData = response.body()?.data
            if (responseData != null) appsList.addAll(responseData)
            next = response.body()?.paging?.next
        }

        val filteredApps = appsList.filter { it.owner.slug == orgSlug }
        return filteredApps
    }

    fun getBuild(appSlug: String, buildSlug: String): BitriseSingleBuildData {
        return apiService.getBuild(appSlug, buildSlug).execute().body() ?: throw RuntimeException("getBuild failed")
    }

    fun getBuilds(app: BitriseAppObject,
                  limitDateAfter: ZonedDateTime? = null,
                  limitDateBefore: ZonedDateTime? = null,
                  page: Boolean = true): List<BitriseBuildObject> {
        val buildsList = ArrayList<BitriseBuildObject>()
        var response: Response<BitriseBuildData>
        var next: String? = ""
        var noMoreData: Boolean

        while (next != null) {
            noMoreData = true
            response = apiService.getBuilds(app.slug, next).execute()

            val responseData = response.body()?.data
            if (responseData != null) {
                for (buildObject in responseData) {
                    if (startDateWithinRange(buildObject, limitDateAfter, limitDateBefore)) {
                        buildsList.add(buildObject)
                        noMoreData = false
                    }

                    // If the build started *after* the end date, then we must keep paging to get the old builds
                    val startTime = ZonedDateTime.parse(buildObject.triggered_at).withZoneSameInstant(ZoneOffset.UTC)
                    if (limitDateBefore != null && startTime.isAfter(limitDateBefore)) {
                        noMoreData = false
                    }
                }

                next = if (!page || noMoreData) null else response.body()?.paging?.next
            }
        }

        return buildsList
    }
}
