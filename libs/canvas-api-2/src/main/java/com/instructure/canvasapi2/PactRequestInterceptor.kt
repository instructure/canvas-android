package com.instructure.canvasapi2

import okhttp3.Interceptor
import okhttp3.Response

class PactRequestInterceptor(private val authUser: String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
        builder.addHeader("Authorization", "Bearer some_token")
        builder.addHeader("Auth-user", authUser ?: "")
        return chain.proceed(builder.build())
    }
}
