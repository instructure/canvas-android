/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.features.modules.list

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.modules.list.datasource.ModuleListDataSource
import com.instructure.student.features.modules.list.datasource.ModuleListLocalDataSource
import com.instructure.student.features.modules.list.datasource.ModuleListNetworkDataSource
import kotlinx.coroutines.CoroutineScope
import retrofit2.Response

class ModuleListRepository(
    private val lifecycleScope: CoroutineScope,
    localDataSource: ModuleListLocalDataSource,
    private val networkDataSource: ModuleListNetworkDataSource,
    networkStateProvider: NetworkStateProvider) : Repository<ModuleListDataSource>(localDataSource, networkDataSource, networkStateProvider) {

    fun getAllModuleObjects(canvasContext: CanvasContext, forceNetwork: Boolean, callback: StatusCallback<List<ModuleObject>>) {
        convertDataResultCallToStatusCallback(callback) {
            dataSource.getAllModuleObjects(canvasContext, forceNetwork)
        }
    }

    fun getFirstPageModuleObjects(canvasContext: CanvasContext, forceNetwork: Boolean, callback: StatusCallback<List<ModuleObject>>) {
        convertDataResultCallToStatusCallback(callback) {
            dataSource.getFirstPageModuleObjects(canvasContext, forceNetwork)
        }
    }

    fun getNextPageModuleObjects(nextUrl: String, forceNetwork: Boolean, callback: StatusCallback<List<ModuleObject>>) {
        convertDataResultCallToStatusCallback(callback) {
            networkDataSource.getNextPageModuleObjects(nextUrl, forceNetwork)
        }
    }

    suspend fun getTabs(canvasContext: CanvasContext, forceNetwork: Boolean): List<Tab> {
        val tabs = dataSource.getTabs(canvasContext, forceNetwork).dataOrNull ?: emptyList()
        return tabs.filter { !(it.isExternal && it.isHidden) }
    }

    fun getFirstPageModuleItems(canvasContext: CanvasContext, moduleId: Long, forceNetwork: Boolean, callback: StatusCallback<List<ModuleItem>>) {
        convertDataResultCallToStatusCallback(callback) {
            dataSource.getFirstPageModuleItems(canvasContext, moduleId, forceNetwork)
        }
    }

    fun getNextPageModuleItems(nextUrl: String, forceNetwork: Boolean, callback: StatusCallback<List<ModuleItem>>) {
        convertDataResultCallToStatusCallback(callback) {
            networkDataSource.getNextPageModuleItems(nextUrl, forceNetwork)
        }
    }

    private fun <T> convertDataResultCallToStatusCallback(callback: StatusCallback<T>, call: suspend () -> DataResult<T>) {
        lifecycleScope.tryLaunch {
            val result = call.invoke()
            if (result is DataResult.Success) {
                callback.onResponse(Response.success(result.dataOrThrow), result.linkHeaders, result.apiType)
                callback.onFinished(result.apiType)
            }
        } catch {
            callback.onFail(null, it, null)
            callback.onFinished(ApiType.API)
        }
    }
}