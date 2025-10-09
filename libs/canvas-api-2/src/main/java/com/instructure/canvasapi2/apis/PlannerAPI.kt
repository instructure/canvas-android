package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.PlannerOverride
import com.instructure.canvasapi2.models.postmodels.PlannerNoteBody
import com.instructure.canvasapi2.utils.DataResult
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag
import retrofit2.http.Url

object PlannerAPI {

    interface PlannerInterface {

        @GET("users/self/planner/items")
        fun getPlannerItems(@Query("start_date") startDate: String?, @Query("end_date") endDate: String?): Call<List<PlannerItem>>

        @GET("users/self/planner/items")
        suspend fun getPlannerItems(
            @Query("start_date") startDate: String?,
            @Query("end_date") endDate: String?,
            @Query(value = "context_codes[]", encoded = true) contextCodes: List<String>,
            @Query("filter") filter: String? = null,
            @Tag restParams: RestParams
        ): DataResult<List<PlannerItem>>

        @GET
        suspend fun nextPagePlannerItems(@Url nextUrl: String, @Tag params: RestParams): DataResult<List<PlannerItem>>

        @POST("planner/overrides")
        fun createPlannerOverride(@Body plannerOverride: PlannerOverride): Call<PlannerOverride>

        @POST("planner/overrides")
        suspend fun createPlannerOverride(@Body plannerOverride: PlannerOverride, @Tag params: RestParams): DataResult<PlannerOverride>

        @FormUrlEncoded
        @PUT("planner/overrides/{overrideId}")
        fun updatePlannerOverride(@Path("overrideId") plannerOverrideId: Long, @Field("marked_complete") complete: Boolean): Call<PlannerOverride>

        @FormUrlEncoded
        @PUT("planner/overrides/{overrideId}")
        suspend fun updatePlannerOverride(@Path("overrideId") plannerOverrideId: Long, @Field("marked_complete") complete: Boolean, @Tag params: RestParams): DataResult<PlannerOverride>

        @DELETE("planner_notes/{noteId}")
        suspend fun deletePlannerNote(@Path("noteId") noteId: Long, @Tag params: RestParams): DataResult<Unit>

        @POST("planner_notes")
        suspend fun createPlannerNote(@Body noteBody: PlannerNoteBody, @Tag params: RestParams): DataResult<Plannable>

        @PUT("planner_notes/{noteId}")
        suspend fun updatePlannerNote(@Path("noteId") noteId: Long, @Body noteBody: PlannerNoteBody, @Tag params: RestParams): DataResult<Plannable>

        @GET("planner_notes/{noteId}")
        suspend fun getPlannerNote(@Path("noteId") noteId: Long, @Tag params: RestParams): DataResult<Plannable>

        @GET("planner_notes")
        suspend fun getPlannerNotes(
            @Query("start_date") startDate: String?,
            @Query("end_date") endDate: String?,
            @Query(value = "context_codes[]", encoded = true) contextCodes: List<String>,
            @Tag restParams: RestParams
        ): DataResult<List<Plannable>>

        @GET
        suspend fun nextPagePlannerNotes(@Url nextUrl: String, @Tag params: RestParams): DataResult<List<Plannable>>
    }

    fun getPlannerItems(adapter: RestBuilder, callback: StatusCallback<List<PlannerItem>>, params: RestParams, startDate: String? = null, endDate: String? = null) {
        callback.addCall(adapter.build(PlannerInterface::class.java, params).getPlannerItems(startDate, endDate)).enqueue(callback)
    }

    fun createPlannerOverride(adapter: RestBuilder, callback: StatusCallback<PlannerOverride>, params: RestParams, plannerOverride: PlannerOverride) {
        callback.addCall(adapter.build(PlannerInterface::class.java, params).createPlannerOverride(plannerOverride)).enqueue(callback)
    }

    fun updatePlannerOverride(adapter: RestBuilder, callback: StatusCallback<PlannerOverride>, params: RestParams, id: Long, complete: Boolean) {
        callback.addCall(adapter.build(PlannerInterface::class.java, params).updatePlannerOverride(id, complete)).enqueue(callback)
    }
}