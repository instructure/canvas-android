package com.instructure.dataseeding.api

import com.instructure.dataseeding.model.ConferencesRequestApiModel
import com.instructure.dataseeding.model.ConferencesResponseApiModel
import com.instructure.dataseeding.model.WebConferenceWrapper
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import com.instructure.dataseeding.util.Randomizer
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

object ConferencesApi {
    interface ConferencesService {

        @POST("courses/{courseId}/conferences")
        fun createCourseConference(@Path("courseId") courseId: Long, @Body createConference: WebConferenceWrapper): Call<ConferencesResponseApiModel>
    }

    private fun conferencesService(token: String): ConferencesService
            = CanvasNetworkAdapter.retrofitWithToken(token).create(ConferencesService::class.java)

    fun createCourseConference(courseId: Long, token: String, title: String = Randomizer.randomConferenceTitle(), description: String = Randomizer.randomConferenceDescription(), conferenceType: String = "BigBlueButton", longRunning: Boolean = false, duration: Int = 70, recipientUserIds: List<Long>): ConferencesResponseApiModel {
        val conference = WebConferenceWrapper(webConference = ConferencesRequestApiModel(
            title,
            description,
            conferenceType,
            longRunning,
            duration,
            recipientUserIds)
        )

        return conferencesService(token).createCourseConference(courseId, conference).execute().body()!!
    }

}
