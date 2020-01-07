package com.instructure.canvasapi2.pact.canvas.apis

import au.com.dius.pact.consumer.PactProviderRuleMk2
import au.com.dius.pact.model.PactSpecVersion
import com.instructure.canvasapi2.apis.CourseAPI
import org.junit.Rule
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

open class ApiPactTestBase {
    @Rule
    @JvmField
    val provider = PactProviderRuleMk2("Canvas LMS API", PactSpecVersion.V2, this)

    fun getClient() : Retrofit {
        val client = Retrofit.Builder()
                .baseUrl(provider.url + "/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        return client
    }
}