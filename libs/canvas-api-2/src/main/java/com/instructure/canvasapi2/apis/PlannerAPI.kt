package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.PlannerOverride
import com.instructure.canvasapi2.utils.weave.apiAsync
import retrofit2.Call
import retrofit2.http.*

object PlannerAPI {

    internal interface PlannerInterface {

        @GET("users/self/planner/items")
        fun getPlannerItems(@Query("start_date") startDate: String?,
                            @Query("end_date") endDate: String?): Call<List<PlannerItem>>

        @POST("planner/overrides")
        fun createPlannerOverride(@Body plannerOverride: PlannerOverride): Call<PlannerOverride>

        @FormUrlEncoded
        @PUT("planner/overrides/{overrideId}")
        fun updatePlannerOverride(@Path("overrideId") plannerOverrideId: Long, @Field("marked_complete") complete: Boolean): Call<PlannerOverride>
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