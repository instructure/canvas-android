package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.models.journey.assist.JourneyAssistRequestBody
import com.instructure.canvasapi2.models.journey.assist.JourneyAssistResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface JourneyAssistAPI {
    @POST("assist")
    suspend fun answerPrompt(
        @Body body: JourneyAssistRequestBody,
    ): JourneyAssistResponse
}