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

import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.AvatarAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ExhaustiveListCallback
import com.instructure.canvasapi2.utils.weave.apiAsync
import com.instructure.canvasapi2.utils.weave.awaitApi

object UserManager {

    @JvmStatic
    fun getColors(callback: StatusCallback<CanvasColor>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromCache = !forceNetwork, isForceReadFromNetwork = forceNetwork)
        UserAPI.getColors(adapter, callback, params)
    }

    @JvmStatic
    fun setColors(callback: StatusCallback<CanvasColor>, contextId: String, color: Int) {
        val adapter = RestBuilder(callback)
        UserAPI.setColor(adapter, callback, contextId, color)
    }

    @JvmStatic
    fun getSelf(callback: StatusCallback<User>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(
            acceptLanguageOverride = getSystemAcceptLanguage()
        )

        UserAPI.getSelf(adapter, params, callback)
    }

    @JvmStatic
    fun getSelf(forceNetwork: Boolean, callback: StatusCallback<User>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(
            isForceReadFromNetwork = forceNetwork,
            acceptLanguageOverride = getSystemAcceptLanguage()
        )

        UserAPI.getSelf(adapter, params, callback)
    }

    fun getSelfSettings(forceNetwork: Boolean) = apiAsync<UserSettings> { callback ->
        UserAPI.getSelfSettings(
            adapter = RestBuilder(callback),
            params = RestParams(isForceReadFromNetwork = forceNetwork),
            callback = callback
        )
    }

    fun getSelfFeatures(forceNetwork: Boolean = false) = apiAsync<List<CanvasFeatureFlag>> { callback ->
        UserAPI.getSelfFeatures(
            adapter = RestBuilder(callback),
            params = RestParams(isForceReadFromNetwork = forceNetwork),
            callback = callback
        )
    }

    fun setHideColorOverlay(hide: Boolean) = apiAsync<UserSettings> { callback ->
        UserAPI.setHideColorOverlaySetting(
            hide = hide,
            adapter = RestBuilder(callback),
            params = RestParams(),
            callback = callback
        )
    }

    @JvmStatic
    fun getSelfEnrollments(forceNetwork: Boolean, callback: StatusCallback<List<Enrollment>>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        UserAPI.getSelfEnrollments(adapter, params, callback)
    }

    @JvmStatic
    fun getSelfWithPermissions(forceNetwork: Boolean, callback: StatusCallback<User>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(
            isForceReadFromNetwork = forceNetwork,
            acceptLanguageOverride = getSystemAcceptLanguage()
        )
        UserAPI.getSelfWithPermissions(adapter, params, callback)
    }

    @JvmStatic
    fun getUser(userId: Long?, callback: StatusCallback<User>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        UserAPI.getUser(adapter, params, userId, callback)
    }

    @JvmStatic
    fun getTestUser(courseId: Long?, callback: StatusCallback<User>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        UserAPI.getTestUser(adapter, params, courseId, callback)
    }

    @JvmStatic
    fun getUserForContextId(
        canvasContext: CanvasContext,
        userId: Long,
        callback: StatusCallback<User>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        UserAPI.getUserForContextId(adapter, params, canvasContext, userId, callback)
    }

    @JvmStatic
    fun getAllPeopleList(canvasContext: CanvasContext, callback: StatusCallback<List<User>>, forceNetwork: Boolean) {
        val params = RestParams(
            usePerPageQueryParam = true,
            canvasContext = canvasContext,
            isForceReadFromNetwork = forceNetwork
        )

        // We don't want the canvas context on the paginated params
        val paginatedParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        val adapter = RestBuilder(callback)
        val depaginatedCallback = object : ExhaustiveListCallback<User>(callback) {
            override fun getNextPage(callback: StatusCallback<List<User>>, nextUrl: String, isCached: Boolean) {
                UserAPI.getPeopleList(adapter, paginatedParams, canvasContext.id, callback)
            }
        }
        adapter.statusCallback = depaginatedCallback
        UserAPI.getPeopleList(adapter, params, canvasContext.id, depaginatedCallback)
    }

    @JvmStatic
    fun getAllEnrollmentsPeopleList(
        canvasContext: CanvasContext,
        callback: StatusCallback<List<User>>,
        forceNetwork: Boolean
    ) {
        val params = RestParams(
            usePerPageQueryParam = true,
            canvasContext = canvasContext,
            isForceReadFromNetwork = forceNetwork
        )
        val paginatedParams = RestParams(
            usePerPageQueryParam = true,
            isForceReadFromNetwork = forceNetwork
        ) // We don't want the canvas context on the paginated params
        val adapter = RestBuilder(callback)
        val depaginatedCallback = object : ExhaustiveListCallback<User>(callback) {
            override fun getNextPage(callback: StatusCallback<List<User>>, nextUrl: String, isCached: Boolean) {
                UserAPI.getAllPeopleList(adapter, paginatedParams, canvasContext.id, callback)
            }
        }
        adapter.statusCallback = depaginatedCallback
        UserAPI.getAllPeopleList(adapter, params, canvasContext.id, depaginatedCallback)
    }

    @JvmStatic
    fun getFirstPagePeopleList(
            canvasContext: CanvasContext,
            enrollmentType: UserAPI.EnrollmentType,
            forceNetwork: Boolean,
            callback: StatusCallback<List<User>>
    ) {
        val params = RestParams(
            usePerPageQueryParam = true,
            canvasContext = canvasContext,
            isForceReadFromNetwork = forceNetwork
        )
        val adapter = RestBuilder(callback)
        UserAPI.getFirstPagePeopleList(adapter, params, canvasContext.id, enrollmentType, callback)
    }

    @JvmStatic
    fun getFirstPagePeopleList(
        canvasContext: CanvasContext,
        forceNetwork: Boolean,
        callback: StatusCallback<List<User>>
    ) {
        val params = RestParams(
            usePerPageQueryParam = true,
            canvasContext = canvasContext,
            isForceReadFromNetwork = forceNetwork
        )
        val adapter = RestBuilder(callback)
        UserAPI.getFirstPagePeopleList(adapter, params, canvasContext.id, callback)
    }

    @JvmStatic
    fun getNextPagePeopleList(forceNetwork: Boolean, nextUrl: String, callback: StatusCallback<List<User>>) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        val adapter = RestBuilder(callback)
        UserAPI.getNextPagePeopleList(adapter, params, nextUrl, callback)
    }

    @JvmStatic
    fun updateUserShortName(shortName: String, callback: StatusCallback<User>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        UserAPI.updateUserShortName(adapter, params, shortName, callback)
    }

    @JvmStatic
    fun updateUsersAvatar(urlPath: String, callback: StatusCallback<User>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        AvatarAPI.updateAvatar(adapter, params, urlPath, callback)
    }

    @JvmStatic
    fun updateUsersAvatarWithToken(avatarToken: String, callback: StatusCallback<User>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        AvatarAPI.updateAvatarWithToken(adapter, params, avatarToken, callback)
    }

    @JvmStatic
    fun getTermsOfService(callback: StatusCallback<TermsOfService>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        UserAPI.getTermsOfService(adapter, params, callback)
    }

    @JvmStatic
    fun getSelfAccount(forceNetwork: Boolean, callback: StatusCallback<Account>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        UserAPI.getSelfAccount(adapter, params, callback)
    }

    @JvmStatic
    fun getBecomeUserPermission(
        forceNetwork: Boolean,
        accountId: Long,
        callback: StatusCallback<BecomeUserPermission>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        UserAPI.getBecomeUserPermission(adapter, params, accountId, callback)
    }

    fun getGeneratePairingCodePermission(
        forceNetwork: Boolean,
        callback: StatusCallback<GeneratePairingCodePermission>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        UserAPI.getGeneratePairingCodePermission(adapter, params, callback)
    }

    fun generatePairingCode(forceNetwork: Boolean, callback: StatusCallback<PairingCode>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        UserAPI.generatePairingCode(adapter, params, callback)
    }

    fun generatePairingCodeAsync(forceNetwork: Boolean) =
        apiAsync<PairingCode> { generatePairingCode(forceNetwork, it) }

    @JvmStatic
    fun addObserveeWithPairingCode(userId: Long, pairingCode: String, callback: StatusCallback<User>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        UserAPI.addObserveeWithPairingCode(adapter, params, userId, pairingCode, callback)
    }

    private fun getSystemAcceptLanguage(): String {
        val systemLocale = ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]
        return "${systemLocale.toLanguageTag()},${systemLocale.language}"
    }

}
