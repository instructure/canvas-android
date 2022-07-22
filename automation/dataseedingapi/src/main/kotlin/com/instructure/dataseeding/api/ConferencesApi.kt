package com.instructure.dataseeding.api

import com.instructure.dataseeding.model.ConferencesRequestApiModel
import com.instructure.dataseeding.model.ConferencesResponseApiModel
import com.instructure.dataseeding.model.WebConferenceWrapper
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

object ConferencesApi {
    interface ConferencesService {

        @POST("courses/{courseId}/conferences")
        fun createCourseConference(@Path("courseId") courseId: Long, @Body createConference: WebConferenceWrapper): Call<ConferencesResponseApiModel>

        @GET("courses/{courseId}/conferences")
        fun getCourseConferences(@Path("courseId") userId: Long): Call<List<ConferencesResponseApiModel>>
    }

    private fun conferencesService(token: String): ConferencesService
            = CanvasNetworkAdapter.retrofitWithToken(token).create(ConferencesService::class.java)

    fun createCourseConference(token: String, title: String, description: String, conferenceType: String, longRunning: Boolean, duration: Int?, userIds: List<Long>?, courseId: Long): ConferencesResponseApiModel {
        val conference = WebConferenceWrapper(webConference = ConferencesRequestApiModel(
            title,
            description,
            conferenceType,
            longRunning,
            duration,
            userIds)
        )

        return conferencesService(token).createCourseConference(courseId, conference).execute().body()!!
    }

    fun getCourseConferences(token: String, courseId: Long): List<ConferencesResponseApiModel> {
        return conferencesService(token).getCourseConferences(courseId).execute().body()!!
    }

}
