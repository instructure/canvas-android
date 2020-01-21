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

package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.APIHelper
import retrofit2.Call
import retrofit2.http.*


object UserAPI {

    enum class EnrollmentType {
        STUDENT, TEACHER, TA, OBSERVER, DESIGNER
    }

    internal interface UsersInterface {

        @GET("users/self/colors")
        fun getColors(): Call<CanvasColor>

        @GET("users/self/profile")
        fun getSelf(): Call<User>

        @GET("users/self/settings")
        fun getSelfSettings(): Call<UserSettings>

        @PUT("users/self/settings")
        fun setHideColorOverlaySetting(@Query("hide_dashcard_color_overlays") hideOverlay: Boolean): Call<UserSettings>

        @GET("users/self/enrollments?state[]=active&state[]=invited&state[]=completed")
        fun getSelfEnrollments(): Call<List<Enrollment>>

        @GET("users/self")
        fun getSelfWithPermissions(): Call<User>

        @GET("accounts/self/terms_of_service")
        fun getTermsOfService(): Call<TermsOfService>

        @GET("accounts/self")
        fun getAccount(): Call<Account>

        @PUT("users/self/colors/{context_id}")
        fun setColor(@Path("context_id") contextId: String, @Query(value = "hexcode") color: String): Call<CanvasColor>

        @PUT("users/self")
        fun updateUserShortName(@Query("user[short_name]") shortName: String, @Body body: String): Call<User>

        @GET("users/{userId}/profile")
        fun getUser(@Path("userId") userId: Long?): Call<User>

        @GET("{contextType}/{contextId}/users/{userId}?include[]=avatar_url&include[]=user_id&include[]=email&include[]=bio&include[]=enrollments")
        fun getUserForContextId(@Path("contextType") contextType: String, @Path("contextId") contextId: Long, @Path("userId") userId: Long): Call<User>

        @GET("{context_id}/users?include[]=enrollments&include[]=avatar_url&include[]=user_id&include[]=email&include[]=bio&exclude_inactive=true")
        fun getFirstPagePeopleList(@Path("context_id") context_id: Long, @Query("enrollment_type") enrollmentType: String): Call<List<User>>

        @GET("{context_id}/users?include[]=enrollments&include[]=avatar_url&include[]=user_id&include[]=email&include[]=bio&exclude_inactive=true")
        fun getFirstPageAllPeopleList(@Path("context_id") context_id: Long): Call<List<User>>

        @GET
        fun next(@Url nextURL: String): Call<List<User>>

        @GET("accounts/{accountId}/permissions?permissions[]=become_user")
        fun getBecomeUserPermission(@Path("accountId") accountId: Long): Call<BecomeUserPermission>

        //region Airwolf
        @POST("users/{userId}/observees")
        fun addObserveeWithPairingCode(@Path("userId") userId: Long?, @Query(value = "pairing_code") pairingCode: String): Call<User>
        //endregion
    }

    fun getColors(adapter: RestBuilder, callback: StatusCallback<CanvasColor>, params: RestParams) {
        callback.addCall(adapter.build(UsersInterface::class.java, params).getColors()).enqueue(callback)
    }

    fun setColor(adapter: RestBuilder, callback: StatusCallback<CanvasColor>, contextId: String, color: Int) {
        if (APIHelper.paramIsNull(adapter, callback, contextId)) {
            return
        }

        // Modifies a color into a RRGGBB color string with no #.
        var hexColor = Integer.toHexString(color)
        hexColor = hexColor.substring(hexColor.length - 6)

        if (hexColor.contains("#")) {
            hexColor = hexColor.replace("#".toRegex(), "")
        }

        adapter.build(UsersInterface::class.java, RestParams()).setColor(contextId, hexColor).enqueue(callback)
    }

    fun getSelf(adapter: RestBuilder, params: RestParams, callback: StatusCallback<User>) {
        callback.addCall(adapter.build(UsersInterface::class.java, params).getSelf()).enqueue(callback)
    }

    fun getSelfSettings(adapter: RestBuilder, params: RestParams, callback: StatusCallback<UserSettings>) {
        callback.addCall(adapter.build(UsersInterface::class.java, params).getSelfSettings()).enqueue(callback)
    }

    fun setHideColorOverlaySetting(
        hide: Boolean,
        adapter: RestBuilder,
        params: RestParams,
        callback: StatusCallback<UserSettings>
    ) {
        callback.addCall(adapter.build(UsersInterface::class.java, params).setHideColorOverlaySetting(hide))
            .enqueue(callback)
    }

    fun getSelfEnrollments(adapter: RestBuilder, params: RestParams, callback: StatusCallback<List<Enrollment>>) {
        callback.addCall(adapter.build(UsersInterface::class.java, params).getSelfEnrollments()).enqueue(callback)
    }

    fun getSelfWithPermissions(adapter: RestBuilder, params: RestParams, callback: StatusCallback<User>) {
        callback.addCall(adapter.build(UsersInterface::class.java, params).getSelfWithPermissions()).enqueue(callback)
    }

    fun getUser(adapter: RestBuilder, params: RestParams, userId: Long?, callback: StatusCallback<User>) {
        callback.addCall(adapter.build(UsersInterface::class.java, params).getUser(userId)).enqueue(callback)
    }

    fun getUserForContextId(adapter: RestBuilder, params: RestParams, canvasContext: CanvasContext, userId: Long, callback: StatusCallback<User>) {
        callback.addCall(adapter.build(UsersInterface::class.java, params).getUserForContextId(canvasContext.apiContext(), canvasContext.id, userId)).enqueue(callback)
    }

    fun updateUserShortName(adapter: RestBuilder, params: RestParams, shortName: String, callback: StatusCallback<User>) {
        callback.addCall(adapter.build(UsersInterface::class.java, params).updateUserShortName(shortName, "")).enqueue(callback)
    }

    fun getPeopleList(adapter: RestBuilder, params: RestParams, contextId: Long, callback: StatusCallback<List<User>>) {
        if (StatusCallback.isFirstPage(callback.linkHeaders)) {
            callback.addCall(adapter.build(UsersInterface::class.java, params).getFirstPagePeopleList(contextId, getEnrollmentTypeString(EnrollmentType.STUDENT))).enqueue(callback)
        } else if (callback.linkHeaders != null && StatusCallback.moreCallsExist(callback.linkHeaders)) {
            callback.addCall(adapter.build(UsersInterface::class.java, params).next(callback.linkHeaders!!.nextUrl!!)).enqueue(callback)
        }
    }

    fun getFirstPagePeopleList(adapter: RestBuilder, params: RestParams, contextId: Long, enrollmentType: EnrollmentType, callback: StatusCallback<List<User>>) {
        callback.addCall(adapter.build(UsersInterface::class.java, params).getFirstPagePeopleList(contextId, getEnrollmentTypeString(enrollmentType))).enqueue(callback)
    }

    fun getNextPagePeopleList(adapter: RestBuilder, params: RestParams, nextUrl: String, callback: StatusCallback<List<User>>) {
        callback.addCall(adapter.build(UsersInterface::class.java, params).next(nextUrl)).enqueue(callback)
    }

    fun getFirstPagePeopleList(adapter: RestBuilder, params: RestParams, contextId: Long, callback: StatusCallback<List<User>>) {
        callback.addCall(adapter.build(UsersInterface::class.java, params).getFirstPageAllPeopleList(contextId)).enqueue(callback)
    }

    fun getAllPeopleList(adapter: RestBuilder, params: RestParams, contextId: Long, callback: StatusCallback<List<User>>) {
        if (StatusCallback.isFirstPage(callback.linkHeaders)) {
            callback.addCall(adapter.build(UsersInterface::class.java, params).getFirstPageAllPeopleList(contextId)).enqueue(callback)
        } else if (callback.linkHeaders != null && StatusCallback.moreCallsExist(callback.linkHeaders)) {
            callback.addCall(adapter.build(UsersInterface::class.java, params).next(callback.linkHeaders!!.nextUrl!!)).enqueue(callback)
        }
    }

    fun getTermsOfService(adapter: RestBuilder, params: RestParams, callback: StatusCallback<TermsOfService>) {
        callback.addCall(adapter.build(UsersInterface::class.java, params).getTermsOfService()).enqueue(callback)
    }

    fun getSelfAccount(adapter: RestBuilder, params: RestParams, callback: StatusCallback<Account>) {
        callback.addCall(adapter.build(UsersInterface::class.java, params).getAccount()).enqueue(callback)
    }

    fun getBecomeUserPermission(adapter: RestBuilder, params: RestParams, accountId: Long, callback: StatusCallback<BecomeUserPermission>) {
        callback.addCall(adapter.build(UsersInterface::class.java, params).getBecomeUserPermission(accountId)).enqueue(callback)
    }

    private fun getEnrollmentTypeString(enrollmentType: EnrollmentType): String =
            when (enrollmentType) {
                EnrollmentType.DESIGNER -> "designer"
                EnrollmentType.OBSERVER -> "observer"
                EnrollmentType.STUDENT -> "student"
                EnrollmentType.TA -> "ta"
                EnrollmentType.TEACHER -> "teacher"
            }


    //region Airwolf
    fun addObserveeWithPairingCode(
            adapter: RestBuilder,
            params: RestParams,
            userId: Long?,
            pairingCode: String,
            callback: StatusCallback<User>) {

        callback.addCall(adapter.build(UsersInterface::class.java, params).addObserveeWithPairingCode(userId, pairingCode)).enqueue(callback)
    }

    //endregion
}
