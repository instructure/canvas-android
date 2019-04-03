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
 *
 */

package com.instructure.canvasapi.api;

import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.MasteryPathSelectResponse;
import com.instructure.canvasapi.model.ModuleItem;
import com.instructure.canvasapi.model.ModuleItemSequence;
import com.instructure.canvasapi.model.ModuleObject;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.ExhaustiveBridgeCallback;
import com.squareup.okhttp.Response;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;


public class ModuleAPI extends BuildInterfaceAPI {

    public static final String MODULE_ASSET_ASSIGNMENT = "Assignment";
    public static final String MODULE_ASSET_PAGE = "Page";
    public static final String MODULE_ASSET_MODULE_ITEM = "ModuleItem";
    public static final String MODULE_ASSET_QUIZ = "Quiz";
    public static final String MODULE_ASSET_FILE = "File";
    public static final String MODULE_ASSET_DISCUSSION = "Discussion";
    public static final String MODULE_ASSET_EXTERNAL_TOOL = "ExternalTool";

    interface ModulesInterface {
        @GET("/{context_id}/modules")
        void getFirstPageModuleObjects(@Path("context_id") long context_id, Callback<ModuleObject[]> callback);

        @GET("/{next}")
        void getNextPageModuleObjectList(@Path(value = "next", encode = false) String nextURL, Callback<ModuleObject[]> callback);

        @GET("/{context_id}/modules/{module_id}/items?include[]=content_details&include[]=mastery_paths")
        void getFirstPageModuleItems(@Path("context_id") long context_id, @Path("module_id") long moduleID, Callback<ModuleItem[]> callback);

        @GET("/{next}?include[]=content_details&include[]=mastery_paths")
        void getNextPageModuleItemList(@Path(value = "next", encode = false) String nextURL, Callback<ModuleItem[]> callback);

        @POST("/{context_id}/modules/{module_id}/items/{item_id}/mark_read")
        void markModuleItemRead(@Path("context_id") long context_id, @Path("module_id") long module_id, @Path("item_id") long item_id, @Body String body, Callback<Response> callback);

        @PUT("/{context_id}/modules/{module_id}/items/{item_id}/done")
        void markModuleAsDone(@Path("context_id") long context_id, @Path("module_id") long module_id, @Path("item_id") long item_id, @Body String body, Callback<Response> callback);

        @DELETE("/{context_id}/modules/{module_id}/items/{item_id}/done")
        void markModuleAsNotDone(@Path("context_id") long context_id, @Path("module_id") long module_id, @Path("item_id") long item_id, Callback<Response> callback);

        @POST("/{context_id}/modules/{module_id}/items/{item_id}/select_mastery_path")
        void selectMasteryPath(@Path("context_id") long contextId, @Path("module_id") long moduleId, @Path("item_id") long itemId, @Query("assignment_set_id") long assignmentSetId, @Body String body, Callback<MasteryPathSelectResponse> callback);

        @GET("/{context_id}/module_item_sequence")
        void getModuleItemSequence(@Path("context_id") long context_id, @Query("asset_type") String assetType, @Query("asset_id") String assetId, Callback<ModuleItemSequence> callback);
    }

    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////

    public static void getModuleItemsExhaustive(CanvasContext canvasContext, long moduleId, final CanvasCallback<ModuleItem[]> callback) {
        if (APIHelpers.paramIsNull(canvasContext, callback)) { return; }

        CanvasCallback<ModuleItem[]> bridge = new ExhaustiveBridgeCallback<>(ModuleItem.class, callback, new ExhaustiveBridgeCallback.ExhaustiveBridgeEvents() {
            @Override
            public void performApiCallWithExhaustiveCallback(CanvasCallback bridgeCallback, String nextURL, boolean isCached) {
                if(callback.isCancelled()) { return; }

                ModuleAPI.getNextPageModuleItemsChained(nextURL, bridgeCallback, isCached);
            }
        });

        buildCacheInterface(ModulesInterface.class, callback, canvasContext).getFirstPageModuleItems(canvasContext.getId(), moduleId, bridge);
        buildInterface(ModulesInterface.class, callback, canvasContext).getFirstPageModuleItems(canvasContext.getId(), moduleId, bridge);
    }

    public static void getFirstPageModuleObjects(CanvasContext canvasContext, CanvasCallback<ModuleObject[]> callback) {
        if (APIHelpers.paramIsNull(callback, canvasContext)) { return; }

        buildCacheInterface(ModulesInterface.class, callback, canvasContext).getFirstPageModuleObjects(canvasContext.getId(), callback);
        buildInterface(ModulesInterface.class, callback, canvasContext).getFirstPageModuleObjects(canvasContext.getId(), callback);
    }

    public static void getNextPageModuleObjects(String nextURL, CanvasCallback<ModuleObject[]> callback){
        if (APIHelpers.paramIsNull(callback, nextURL)) { return; }

        callback.setIsNextPage(true);
        buildCacheInterface(ModulesInterface.class, callback, false).getNextPageModuleObjectList(nextURL, callback);
        buildInterface(ModulesInterface.class, callback, false).getNextPageModuleObjectList(nextURL, callback);
    }

    public static void getFirstPageModuleItems(CanvasContext canvasContext, long moduleId, CanvasCallback<ModuleItem[]> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        buildCacheInterface(ModulesInterface.class, callback, canvasContext).getFirstPageModuleItems(canvasContext.getId(), moduleId, callback);
        buildInterface(ModulesInterface.class, callback, canvasContext).getFirstPageModuleItems(canvasContext.getId(), moduleId, callback);
    }

    public static void getNextPageModuleItems(String nextURL, CanvasCallback<ModuleItem[]> callback){
        if (APIHelpers.paramIsNull(callback, nextURL)) { return; }

        callback.setIsNextPage(true);
        buildCacheInterface(ModulesInterface.class, callback, false).getNextPageModuleItemList(nextURL, callback);
        buildInterface(ModulesInterface.class, callback, false).getNextPageModuleItemList(nextURL, callback);
    }

    public static void getNextPageModuleItemsChained(String nextURL, CanvasCallback<ModuleItem[]> callback, boolean isCached){
        if (APIHelpers.paramIsNull(callback, nextURL)) { return; }

        callback.setIsNextPage(true);
        if (isCached) {
            buildCacheInterface(ModulesInterface.class, callback, false).getNextPageModuleItemList(nextURL, callback);
        } else {
            buildInterface(ModulesInterface.class, callback, false).getNextPageModuleItemList(nextURL, callback);
        }
    }

    public static void markModuleItemRead(CanvasContext canvasContext, long moduleId, long itemId, CanvasCallback<Response> callback){
        if(APIHelpers.paramIsNull(callback, canvasContext)){
            return;
        }

        buildInterface(ModulesInterface.class, callback, canvasContext).markModuleItemRead(canvasContext.getId(), moduleId, itemId, "", callback);
    }

    public static void markModuleAsDone(CanvasContext canvasContext, long moduleId, long itemId, CanvasCallback<Response> callback){
        if(APIHelpers.paramIsNull(callback, canvasContext)){
            return;
        }

        buildInterface(ModulesInterface.class, callback, canvasContext, false).markModuleAsDone(canvasContext.getId(), moduleId, itemId, "", callback);
    }

    public static void markModuleAsNotDone(CanvasContext canvasContext, long moduleId, long itemId, CanvasCallback<Response> callback){
        if(APIHelpers.paramIsNull(callback, canvasContext)){
            return;
        }

        buildInterface(ModulesInterface.class, callback, canvasContext, false).markModuleAsNotDone(canvasContext.getId(), moduleId, itemId, callback);
    }

    public static void selectMasteryPath(CanvasContext canvasContext, long moduleId, long itemId, long assignmentSetId, CanvasCallback<MasteryPathSelectResponse> callback){
        if(APIHelpers.paramIsNull(callback, canvasContext)){
            return;
        }

        buildInterface(ModulesInterface.class, callback, canvasContext, false).selectMasteryPath(canvasContext.getId(), moduleId, itemId, assignmentSetId, "", callback);
    }

    public static void getModuleItemSequence(CanvasContext canvasContext, String assetType, String assetId, CanvasCallback<ModuleItemSequence> callback) {
        if(APIHelpers.paramIsNull(canvasContext, callback, assetType)) {
            return;
        }

        buildCacheInterface(ModulesInterface.class, callback, canvasContext).getModuleItemSequence(canvasContext.getId(), assetType, assetId, callback);
        buildInterface(ModulesInterface.class, callback, canvasContext).getModuleItemSequence(canvasContext.getId(), assetType, assetId, callback);
    }
}
