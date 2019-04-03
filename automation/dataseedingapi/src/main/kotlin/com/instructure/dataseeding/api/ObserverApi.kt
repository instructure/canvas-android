package com.instructure.dataseeding.api

import com.instructure.dataseeding.model.*
import com.instructure.dataseeding.util.CanvasRestAdapter
import com.instructure.dataseeding.util.Randomizer
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

object ObserverApi {
    interface ObserverService {

        @POST("users/self/observees")
        fun addObserveeWithCredentials(@Body createObservee: CreateObserveeWithCredentialsWrapper): Call<ObserveeApiModel>

        @GET("users/self/observer_alert_thresholds")
        fun getObserverAlertThresholds(): Call<List<ObserverAlertThresholdApiModel>>

        @POST("users/self/observer_alert_thresholds")
        fun addObserverAlertThreshold(@Body addThreshold: AddObserverAlertThresholdWrapper): Call<ObserverAlertThresholdApiModel>

        @GET("users/self/observer_alerts/{userId}")
        fun getObserverAlerts(@Path("userId") userId: Long): Call<List<ObserverAlertApiModel>>
    }

    private fun observerUserService(token: String): ObserverService
            = CanvasRestAdapter.retrofitWithToken(token).create(ObserverService::class.java)

    fun addObserverWithCredentials(loginId: String, password: String, token: String): ObserveeApiModel {
        val observee = CreateObserveeWithCredentialsWrapper(
                CreateObserveeWithCredentials(loginId, password)
        )

        return observerUserService(token).addObserveeWithCredentials(observee).execute().body()!!
    }

    fun getObserverAlertThresholds(token: String): List<ObserverAlertThresholdApiModel> {
        return observerUserService(token).getObserverAlertThresholds().execute().body()!!
    }

    fun addObserverAlertThreshold(alertType: String, userId: Long, observerId: Long, token: String): ObserverAlertThresholdApiModel {
        val threshold = AddObserverAlertThresholdWrapper(AddObserverAlertThreshold(
                alertType,
                Randomizer.randomThreshold(alertType),
                userId,
                observerId
        ))
        return observerUserService(token).addObserverAlertThreshold(threshold).execute().body()!!
    }

    fun getObserverAlerts(userId: Long, token: String): List<ObserverAlertApiModel> {
        return observerUserService(token).getObserverAlerts(userId).execute().body()!!
    }
}
