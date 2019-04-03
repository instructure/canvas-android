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


package api.bitrise.private

import api.bitrise.BitriseRestAdapter.noAuthAppRetrofit
import api.bitrise.private.State.updateState
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import util.getEnv

object Users {
    interface ApiService {
        // https://app.bitrise.io/users/sign_in.json
        @POST("users/sign_in.json")
        fun signIn(@Body body: User): Call<User>
    }

    private val apiService: ApiService by lazy {
        noAuthAppRetrofit.create(ApiService::class.java)
    }

    fun signIn(email: String = getEnv("BITRISE_USER"), password: String = getEnv("BITRISE_PASS")) {
        val user = User(Credential(email, password))
        val response = apiService.signIn(user).execute()

        updateState(response)
    }
}
