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
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Favorite
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ExhaustiveListCallback
import com.instructure.canvasapi2.utils.weave.apiAsync
import retrofit2.Response
import java.io.IOException
import java.util.*

object GroupManager {

    @JvmStatic
    fun getFavoriteGroups(callback: StatusCallback<List<Group>>, forceNetwork: Boolean) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        val adapter = RestBuilder(callback)
        val depaginatedCallback = object : ExhaustiveListCallback<Group>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Group>>, nextUrl: String, isCached: Boolean) {
                GroupAPI.getNextPageGroups(nextUrl, adapter, callback, params)
            }
        }
        adapter.statusCallback = depaginatedCallback
        GroupAPI.getFavoriteGroups(adapter, depaginatedCallback, params)
    }

    @JvmStatic
    fun getAllGroups(callback: StatusCallback<List<Group>>, forceNetwork: Boolean) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        val adapter = RestBuilder(callback)
        val depaginatedCallback = object : ExhaustiveListCallback<Group>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Group>>, nextUrl: String, isCached: Boolean) {
                GroupAPI.getNextPageGroups(nextUrl, adapter, callback, params)
            }
        }
        adapter.statusCallback = depaginatedCallback
        GroupAPI.getFirstPageGroups(adapter, depaginatedCallback, params)
    }

    @JvmStatic
    fun getDetailedGroup(groupId: Long, callback: StatusCallback<Group>, forceNetwork: Boolean) {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        val adapter = RestBuilder(callback)
        GroupAPI.getDetailedGroup(adapter, callback, params, groupId)
    }

    @JvmStatic
    fun addGroupToFavorites(groupId: Long, callback: StatusCallback<Favorite>) {
        val params = RestParams()
        val adapter = RestBuilder(callback)
        GroupAPI.addGroupToFavorites(adapter, callback, params, groupId)
    }

    @JvmStatic
    fun removeGroupFromFavorites(groupId: Long, callback: StatusCallback<Favorite>) {
        val params = RestParams()
        val adapter = RestBuilder(callback)
        GroupAPI.removeGroupFromFavorites(adapter, callback, params, groupId)
    }

    /**
     * So we are only going to fetch the first 200 groups. If you are reading this and are an instructor with more than 200 groups... sorry.
     * @param forceNetwork
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    @JvmStatic
    fun getGroupsSynchronous(forceNetwork: Boolean): List<Group> {
        val adapter = RestBuilder()
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        val items = ArrayList<Group>()
        val response = GroupAPI.getGroupsSynchronously(adapter, params)
        if (response.isSuccessful) response.body()?.let { items.addAll(it) }

        val nextUrl = nextUrl(response)
        if (nextUrl != null) {
            val nextResponse = GroupAPI.getNextPageGroupsSynchronously(nextUrl, adapter, params)
            if (nextResponse.isSuccessful) nextResponse.body()?.let { items.addAll(it) }
        }

        return items
    }

    private fun <T> nextUrl(response: Response<T>): String? =
        APIHelper.parseLinkHeaderResponse(response.headers()).nextUrl

    @JvmStatic
    fun getAllGroupsForCourse(courseId: Long, callback: StatusCallback<List<Group>>, forceNetwork: Boolean) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        val adapter = RestBuilder(callback)
        val depaginatedCallback = object : ExhaustiveListCallback<Group>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Group>>, nextUrl: String, isCached: Boolean) {
                GroupAPI.getNextPageGroups(nextUrl, adapter, callback, params)
            }
        }
        adapter.statusCallback = depaginatedCallback
        GroupAPI.getGroupsForCourse(adapter, depaginatedCallback, params, courseId)
    }

    @Throws(IOException::class)
    @JvmStatic
    fun getFavoriteGroupsSynchronous(forceNetwork: Boolean): List<Group> {
        val adapter = RestBuilder()
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        val response = GroupAPI.getFavoriteGroupsSynchronously(adapter, params)
        return if (response.isSuccessful) response.body() ?: arrayListOf()
        else arrayListOf()
    }

    @JvmStatic
    fun createGroupMap(groups: List<Group>): Map<Long, Group> = groups.associateBy { it.id }

    fun getPermissionsAsync(
        groupId: Long,
        forceNetwork: Boolean = false,
        requestedPermissions: List<String> = emptyList()
    ) = apiAsync<CanvasContextPermission> {
        val adapter = RestBuilder()
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        GroupAPI.getGroupPermissions(groupId, requestedPermissions, adapter, it, params)
    }

}
