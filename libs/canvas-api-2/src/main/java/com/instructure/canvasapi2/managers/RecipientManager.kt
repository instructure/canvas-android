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
import com.instructure.canvasapi2.apis.RecipientAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.ExhaustiveListCallback

object RecipientManager {

    fun searchRecipients(searchQuery: String?, context: String, callback: StatusCallback<List<Recipient>>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true)
        RecipientAPI.getRecipients(searchQuery, context, callback, adapter, params)
    }

    fun searchAllRecipients(
        forceNetwork: Boolean,
        searchQuery: String?,
        context: String,
        callback: StatusCallback<List<Recipient>>
    ) {
        val adapter = RestBuilder(callback)
        val depaginatedCallback = object : ExhaustiveListCallback<Recipient>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Recipient>>, nextUrl: String, isCached: Boolean) {
                RecipientAPI.getNextPageRecipients(forceNetwork, nextUrl, adapter, callback)
            }
        }
        adapter.statusCallback = depaginatedCallback
        RecipientAPI.getFirstPageRecipients(forceNetwork, searchQuery, context, adapter, depaginatedCallback)
    }

    /**
     * Synthetic contexts == sections and groups, so this will return only actual users, not groups or sections
     *
     * @param forceNetwork
     * @param searchQuery
     * @param context
     * @param callback
     */
    fun searchAllRecipientsNoSyntheticContexts(
        forceNetwork: Boolean,
        searchQuery: String?,
        context: String,
        callback: StatusCallback<List<Recipient>>
    ) {
        val adapter = RestBuilder(callback)
        val depaginatedCallback = object : ExhaustiveListCallback<Recipient>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Recipient>>, nextUrl: String, isCached: Boolean) {
                RecipientAPI.getNextPageRecipients(forceNetwork, nextUrl, adapter, callback)
            }
        }
        adapter.statusCallback = depaginatedCallback
        RecipientAPI.getFirstPageRecipientsNoSyntheticContexts(
            forceNetwork,
            searchQuery,
            context,
            adapter,
            depaginatedCallback
        )
    }

}
