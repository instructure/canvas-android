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
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.ExhaustiveListCallback
import com.instructure.canvasapi2.utils.weave.apiAsync

object EnrollmentManager {

    fun getAllEnrollmentsForCourse(
        courseId: Long,
        enrollmentType: String?,
        forceNetwork: Boolean,
        callback: StatusCallback<List<Enrollment>>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        val depaginatedCallback = object : ExhaustiveListCallback<Enrollment>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Enrollment>>, nextUrl: String, isCached: Boolean) {
                EnrollmentAPI.getNextPageEnrollments(forceNetwork, nextUrl, adapter, callback)
            }
        }
        adapter.statusCallback = depaginatedCallback
        EnrollmentAPI.getFirstPageEnrollmentsForCourse(adapter, params, courseId, enrollmentType, depaginatedCallback)
    }

    fun getAllEnrollmentsForUserInCourse(
        courseId: Long,
        userId: Long,
        forceNetwork: Boolean,
        callback: StatusCallback<List<Enrollment>>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        val depaginatedCallback = object : ExhaustiveListCallback<Enrollment>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Enrollment>>, nextUrl: String, isCached: Boolean) {
                EnrollmentAPI.getNextPageEnrollments(forceNetwork, nextUrl, adapter, callback)
            }
        }
        adapter.statusCallback = depaginatedCallback
        EnrollmentAPI.getFirstPageEnrollmentsForUserInCourse(adapter, params, courseId, userId, depaginatedCallback)
    }

    fun getSelfEnrollments(
        types: List<String>?,
        states: List<String>?,
        forceNetwork: Boolean,
        callback: StatusCallback<List<Enrollment>>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        val depaginatedCallback = object : ExhaustiveListCallback<Enrollment>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Enrollment>>, nextUrl: String, isCached: Boolean) {
                EnrollmentAPI.getSelfEnrollments(types, states, adapter, params, callback)
            }
        }
        adapter.statusCallback = depaginatedCallback
        EnrollmentAPI.getSelfEnrollments(types, states, adapter, params, depaginatedCallback)
    }

    fun getObserveeEnrollments(forceNetwork: Boolean, callback: StatusCallback<List<Enrollment>>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        val depaginatedCallback = object : ExhaustiveListCallback<Enrollment>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Enrollment>>, nextUrl: String, isCached: Boolean) {
                EnrollmentAPI.getObserveeEnrollments(adapter, params, callback)
            }
        }
        adapter.statusCallback = depaginatedCallback
        EnrollmentAPI.getObserveeEnrollments(adapter, params, depaginatedCallback)
    }

    fun getObserveeEnrollmentsAsync(forceNetwork: Boolean)
            = apiAsync<List<Enrollment>> { getObserveeEnrollments(forceNetwork, it) }


    fun handleInvite(courseId: Long, enrollmentId: Long, acceptInvite: Boolean, callback: StatusCallback<Void>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        EnrollmentAPI.handleInvite(courseId, enrollmentId, acceptInvite, adapter, params, callback)
    }

}
