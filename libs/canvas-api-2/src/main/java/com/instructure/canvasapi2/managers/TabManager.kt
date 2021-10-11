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
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.TabAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.weave.apiAsync

object TabManager {

    fun getTabs(canvasContext: CanvasContext, callback: StatusCallback<List<Tab>>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(canvasContext = canvasContext, isForceReadFromNetwork = forceNetwork)

        TabAPI.getTabs(canvasContext.id, adapter, callback, params)
    }

    fun getTabsForElementary(canvasContext: CanvasContext, callback: StatusCallback<List<Tab>>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(canvasContext = canvasContext, isForceReadFromNetwork = forceNetwork)

        TabAPI.getTabsForElementary(canvasContext.id, adapter, callback, params)
    }

    fun getTabsForElementaryAsync(canvasContext: CanvasContext, forceNetwork: Boolean) = apiAsync<List<Tab>> { getTabsForElementary(canvasContext, it, forceNetwork) }

}
