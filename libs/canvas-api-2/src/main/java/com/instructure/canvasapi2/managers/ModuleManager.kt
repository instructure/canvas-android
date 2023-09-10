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
import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.MasteryPathSelectResponse
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleItemSequence
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.ExhaustiveListCallback
import com.instructure.canvasapi2.utils.weave.apiAsync
import okhttp3.ResponseBody

object ModuleManager {

    const val MODULE_ASSET_MODULE_ITEM = "ModuleItem"

    fun getFirstPageModuleObjects(
        canvasContext: CanvasContext,
        callback: StatusCallback<List<ModuleObject>>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(
            canvasContext = canvasContext,
            usePerPageQueryParam = true,
            isForceReadFromNetwork = forceNetwork
        )
        ModuleAPI.getFirstPageModuleObjects(adapter, params, canvasContext.id, callback)
    }

    fun getAllModuleObjets(
        canvasContext: CanvasContext,
        callback: StatusCallback<List<ModuleObject>>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(
                canvasContext = canvasContext,
                usePerPageQueryParam = true,
                isForceReadFromNetwork = forceNetwork
        )
        val depaginatedCallback = object : ExhaustiveListCallback<ModuleObject>(callback) {
            override fun getNextPage(callback: StatusCallback<List<ModuleObject>>, nextUrl: String, isCached: Boolean) {
                val nextParams = params.copy(canvasContext = null, usePerPageQueryParam = false)
                ModuleAPI.getAllModuleObjects(adapter, nextParams, canvasContext.id, callback)
            }
        }
        adapter.statusCallback = depaginatedCallback
        ModuleAPI.getAllModuleObjects(adapter, params, canvasContext.id, depaginatedCallback)
    }

    fun getFirstPageModulesWithItems(
        canvasContext: CanvasContext,
        callback: StatusCallback<List<ModuleObject>>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(
            canvasContext = canvasContext,
            usePerPageQueryParam = false, // Use API default
            isForceReadFromNetwork = forceNetwork
        )
        ModuleAPI.getFirstPageModulesWithItems(adapter, params, canvasContext.id, callback)
    }

    fun getNextPageModuleObjects(nextUrl: String, callback: StatusCallback<List<ModuleObject>>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = false, isForceReadFromNetwork = forceNetwork)
        ModuleAPI.getNextPageModuleObjects(adapter, params, nextUrl, callback)
    }

    fun getFirstPageModuleItems(
        canvasContext: CanvasContext,
        moduleId: Long,
        callback: StatusCallback<List<ModuleItem>>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(
            canvasContext = canvasContext,
            usePerPageQueryParam = true,
            isForceReadFromNetwork = forceNetwork
        )
        ModuleAPI.getFirstPageModuleItems(adapter, params, canvasContext.id, moduleId, callback)
    }

    fun getNextPageModuleItems(nextUrl: String, callback: StatusCallback<List<ModuleItem>>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        ModuleAPI.getNextPageModuleItems(adapter, params, nextUrl, callback)
    }

    fun getAllModuleItems(
        canvasContext: CanvasContext,
        moduleId: Long,
        callback: StatusCallback<List<ModuleItem>>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(
            canvasContext = canvasContext,
            usePerPageQueryParam = true,
            isForceReadFromNetwork = forceNetwork
        )
        val depaginatedCallback = object : ExhaustiveListCallback<ModuleItem>(callback) {
            override fun getNextPage(callback: StatusCallback<List<ModuleItem>>, nextUrl: String, isCached: Boolean) {
                val nextParams = params.copy(canvasContext = null, usePerPageQueryParam = false)
                ModuleAPI.getAllModuleItems(adapter, nextParams, canvasContext.id, moduleId, callback)
            }
        }
        adapter.statusCallback = depaginatedCallback
        ModuleAPI.getAllModuleItems(adapter, params, canvasContext.id, moduleId, depaginatedCallback)
    }

    fun markModuleItemAsRead(
        canvasContext: CanvasContext,
        moduleId: Long,
        itemId: Long,
        callback: StatusCallback<ResponseBody>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(canvasContext = canvasContext)
        ModuleAPI.markModuleItemAsRead(adapter, params, canvasContext, moduleId, itemId, callback)
    }

    fun selectMasteryPath(
        canvasContext: CanvasContext,
        moduleId: Long,
        itemId: Long,
        assignmentSetId: Long,
        callback: StatusCallback<MasteryPathSelectResponse>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(canvasContext = canvasContext)

        ModuleAPI.selectMasteryPath(adapter, params, canvasContext, moduleId, itemId, assignmentSetId, callback)
    }

    fun getModuleItemSequence(
        canvasContext: CanvasContext,
        assetType: String,
        assetId: String,
        callback: StatusCallback<ModuleItemSequence>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(
            canvasContext = canvasContext,
            usePerPageQueryParam = true,
            isForceReadFromNetwork = forceNetwork
        )
        ModuleAPI.getModuleItemSequence(adapter, params, canvasContext, assetType, assetId, callback)
    }

    fun getModuleItem(
        canvasContext: CanvasContext,
        moduleId: Long,
        moduleItemId: Long,
        forceNetwork: Boolean,
        callback: StatusCallback<ModuleItem>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(
            canvasContext = canvasContext,
            isForceReadFromNetwork = forceNetwork
        )
        ModuleAPI.getModuleItem(adapter, params, canvasContext.id, moduleId, moduleItemId, callback)
    }

    fun getModuleItemAsync(
        canvasContext: CanvasContext,
        moduleId: Long,
        moduleItemId: Long,
        forceNetwork: Boolean
    ) = apiAsync<ModuleItem> { getModuleItem(canvasContext, moduleId, moduleItemId, forceNetwork, it) }

}
