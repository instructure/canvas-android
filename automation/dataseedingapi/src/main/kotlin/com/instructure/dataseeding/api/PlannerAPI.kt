package com.instructure.dataseeding.api

import com.instructure.dataseeding.model.PlannerNoteApiModel
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

object PlannerAPI {

    interface PlannerService {

        @POST("planner_notes")
        fun createPlannerNote(@Body plannerNote: PlannerNoteApiModel): Call<PlannerNoteApiModel>
    }

    private fun plannerService(token: String): PlannerService {
        return CanvasNetworkAdapter.retrofitWithToken(token).create(PlannerService::class.java)
    }

    fun createPlannerNote(token: String, title: String, details: String, todoDate: String): PlannerNoteApiModel {
        val plannerNote = PlannerNoteApiModel(
            title = title,
            details = details,
            todoDate = todoDate
        )
        return plannerService(token).createPlannerNote(plannerNote)
            .execute()
            .body()!!
    }


}