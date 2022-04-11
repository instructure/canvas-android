package com.instructure.dataseeding.api

import com.instructure.dataseeding.model.ColorApiModel
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Path

object ColorsApi {
    interface ColorsService {

        @PUT("users/self/colors/{contextId}")
        fun setColor(@Path("contextId") contextId: String, @Body colorApiModel: ColorApiModel): Call<ColorApiModel>

    }

    private fun colorsService(token: String): ColorsService
            = CanvasNetworkAdapter.retrofitWithToken(token).create(ColorsService::class.java)

    fun setColor(studentToken: String, contextId: String, hexcode: String): ColorApiModel
            = colorsService(studentToken)
            .setColor(contextId, ColorApiModel(hexcode = hexcode))
            .execute()
            .body()!!
}