package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.weave.apiAsync
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

object PlannerAPI {

    internal interface PlannerInterface {

        @GET("users/self/planner/items")
        fun getPlannerItems(@Query("start_date") startDate: String?,
                            @Query("end_date") endDate: String?): Call<List<PlannerItem>>
    }

    fun getPlannerItems(adapter: RestBuilder, callback: StatusCallback<List<PlannerItem>>, params: RestParams, startDate: String? = null, endDate: String? = null) {
        callback.addCall(adapter.build(PlannerInterface::class.java, params).getPlannerItems(startDate, endDate)).enqueue(callback)
    }
}