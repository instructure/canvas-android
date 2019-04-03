/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

internal object ModuleAPI {

    internal interface ModuleInterface {
        @GET("{contextId}/modules")
        fun getFirstPageModuleObjects(@Path("contextId") contextId: Long) : Call<List<ModuleObject>>

        @GET
        fun getNextPageModuleObjectList(@Url nextURL: String) : Call<List<ModuleObject>>

        @GET("{contextId}/modules/{moduleId}/items?include[]=content_details&include[]=mastery_paths")
        fun getFirstPageModuleItems(@Path("contextId") contextId: Long, @Path("moduleId") moduleId: Long) : Call<List<ModuleItem>>

        @GET
        fun getNextPageModuleItemList(@Url nextURL: String) : Call<List<ModuleItem>>

        @POST("{contextId}/modules/{moduleId}/items/{itemId}/mark_read")
        fun markModuleItemRead(@Path("contextId") contextId: Long, @Path("moduleId") moduleId: Long, @Path("itemId") itemId: Long) : Call<ResponseBody>

        @PUT("{contextId}/modules/{moduleId}/items/{itemId}/done")
        fun markModuleAsDone(@Path("contextId") contextId: Long, @Path("moduleId") moduleId: Long, @Path("itemId") itemId: Long) : Call<ResponseBody>

        @DELETE("{contextId}/modules/{moduleId}/items/{itemId}/done")
        fun markModuleAsNotDone(@Path("contextId") contextId: Long, @Path("moduleId") moduleId: Long, @Path("itemId") itemId: Long): Call<ResponseBody>

        @POST("{contextId}/modules/{moduleId}/items/{itemId}/select_mastery_path")
        fun selectMasteryPath(@Path("contextId") contextId: Long, @Path("moduleId") moduleId: Long, @Path("itemId") itemId: Long, @Query("assignment_set_id") assignmentSetId: Long) : Call<MasteryPathSelectResponse>

        @GET("{contextId}/module_item_sequence")
        fun getModuleItemSequence(@Path("contextId") contextId: Long, @Query("asset_type") assetType: String, @Query("asset_id") assetId: String) : Call<ModuleItemSequence>
    }


    fun getAllModuleItems(adapter: RestBuilder, params: RestParams, contextId: Long, moduleId: Long, callback: StatusCallback<List<ModuleItem>>) {
        if (StatusCallback.isFirstPage(callback.linkHeaders)) {
            callback.addCall(adapter.build(ModuleInterface::class.java, params).getFirstPageModuleItems(contextId, moduleId)).enqueue(callback)
        } else {
            callback.addCall(adapter.build(ModuleInterface::class.java, params).getNextPageModuleItemList(callback.linkHeaders?.nextUrl ?: "")).enqueue(callback)
        }
    }

    fun getFirstPageModuleItems(adapter: RestBuilder, params: RestParams, contextId: Long, moduleId: Long, callback: StatusCallback<List<ModuleItem>>) {
        callback.addCall(adapter.build(ModuleInterface::class.java, params).getFirstPageModuleItems(contextId, moduleId)).enqueue(callback)
    }

    fun getNextPageModuleItems(adapter: RestBuilder, params: RestParams, nextUrl: String, callback: StatusCallback<List<ModuleItem>>) {
        callback.addCall(adapter.build(ModuleInterface::class.java, params).getNextPageModuleItemList(nextUrl)).enqueue(callback)
    }

    fun getFirstPageModuleObjects(adapter: RestBuilder, params: RestParams, contextId: Long, callback: StatusCallback<List<ModuleObject>>) {
        callback.addCall(adapter.build(ModuleInterface::class.java, params).getFirstPageModuleObjects(contextId)).enqueue(callback)
    }

    fun getNextPageModuleObjects(adapter: RestBuilder, params: RestParams, nextUrl: String, callback: StatusCallback<List<ModuleObject>>) {
        callback.addCall(adapter.build(ModuleInterface::class.java, params).getNextPageModuleObjectList(nextUrl)).enqueue(callback)
    }

    fun markModuleItemAsRead(adapter: RestBuilder, params: RestParams,canvasContext: CanvasContext, moduleId: Long, itemId: Long, callback: StatusCallback<ResponseBody>) {
        callback.addCall(adapter.build(ModuleInterface::class.java, params).markModuleItemRead(canvasContext.id, moduleId, itemId)).enqueue(callback)
    }

    fun markModuleAsDone(adapter: RestBuilder, params: RestParams,canvasContext: CanvasContext, moduleId: Long, itemId: Long, callback: StatusCallback<ResponseBody>) {
        callback.addCall(adapter.build(ModuleInterface::class.java, params).markModuleAsDone(canvasContext.id, moduleId, itemId)).enqueue(callback)
    }

    fun markModuleAsNotDone(adapter: RestBuilder, params: RestParams,canvasContext: CanvasContext, moduleId: Long, itemId: Long, callback: StatusCallback<ResponseBody>) {
        callback.addCall(adapter.build(ModuleInterface::class.java, params).markModuleAsNotDone(canvasContext.id, moduleId, itemId)).enqueue(callback)
    }

    fun selectMasteryPath(adapter: RestBuilder, params: RestParams,canvasContext: CanvasContext, moduleId: Long, itemId: Long, assignmentSetId: Long, callback: StatusCallback<MasteryPathSelectResponse>) {
        callback.addCall(adapter.build(ModuleInterface::class.java, params).selectMasteryPath(canvasContext.id, moduleId, itemId, assignmentSetId)).enqueue(callback)
    }

    fun getModuleItemSequence(adapter: RestBuilder, params: RestParams,canvasContext: CanvasContext, assetType: String, assetId: String, callback: StatusCallback<ModuleItemSequence>) {
        callback.addCall(adapter.build(ModuleInterface::class.java, params).getModuleItemSequence(canvasContext.id, assetType, assetId)).enqueue(callback)
    }
}