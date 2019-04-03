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


package api.bitrise.yaml

import api.bitrise.BitriseRestAdapter.retrofit
import api.bitrise.YamlConfig
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

object BitriseYaml {
    interface ApiService {
        @GET("apps/{appSlug}/bitrise.yml")
        fun getYaml(@Path("appSlug") appSlug: String): Call<ResponseBody>

        @POST("apps/{appSlug}/bitrise.yml")
        fun postYaml(@Path("appSlug") appSlug: String, @Body yaml: YamlConfig): Call<ResponseBody>
    }

    private val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    fun getYaml(appSlug: String): String {
        return apiService.getYaml(appSlug).execute().body()?.string() ?: ""
    }

    fun postYaml(appSlug: String, yaml: String): String? {
        val yamlRequest = YamlConfig(app_config_datastore_yaml = yaml)
        val response = apiService.postYaml(appSlug, yamlRequest).execute()
        if (response.code() != 200) throw RuntimeException("Failed to upload yaml update!")
        return response.body()?.string() ?: throw RuntimeException("Bitrise failed to respond with updated JSON!")
    }
}
