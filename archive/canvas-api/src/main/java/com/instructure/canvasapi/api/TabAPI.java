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
import com.instructure.canvasapi.model.Tab;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;


public class TabAPI extends BuildInterfaceAPI {

    interface TabsInterface {

        @PUT("{/context_id}/tabs{tab_id}")
        void updateTab(@Query("hidden") Integer hidden, @Query("position") Integer oneBasedIndexPosition, @Body String body, CanvasCallback<Tab> callback);

        @GET("/{context_id}/tabs?include[]=external")
        void getTabs(@Path("context_id") long context_id, CanvasCallback<Tab[]> callback);
    }


    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////

    public static void getTabs(CanvasContext canvasContext, CanvasCallback<Tab[]> callback) {
        if (APIHelpers.paramIsNull(callback, canvasContext)) return;

        buildCacheInterface(TabsInterface.class, callback, canvasContext).getTabs(canvasContext.getId(), callback);
        buildInterface(TabsInterface.class, callback, canvasContext).getTabs(canvasContext.getId(), callback);
    }


    /**
     *
     * @param newIsHidden               (Optional)
     * @param newOneBasedIndexPosition  (Optional)
     * @param canvasContext             (Required)
     * @param tab                       (Required)
     * @param callback                  (Required)
     */
    public static void updateTab(Boolean newIsHidden, Integer newOneBasedIndexPosition, CanvasContext canvasContext, Tab tab, CanvasCallback<Tab>callback){
        if(APIHelpers.paramIsNull(callback,canvasContext,tab) || (newOneBasedIndexPosition != null && newOneBasedIndexPosition < 1)) {return;}

        Integer hiddenInteger = newIsHidden == null ? null : APIHelpers.booleanToInt(newIsHidden);

        buildInterface(TabsInterface.class, callback,canvasContext).updateTab(hiddenInteger, newOneBasedIndexPosition, "", callback);
    }

}
