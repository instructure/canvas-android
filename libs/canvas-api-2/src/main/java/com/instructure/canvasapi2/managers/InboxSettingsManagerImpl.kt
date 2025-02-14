/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.instructure.canvasapi2.InboxSettingsQuery
import com.instructure.canvasapi2.QLCallback
import com.instructure.canvasapi2.UpdateInboxSettingsMutation
import com.instructure.canvasapi2.enqueueMutation
import com.instructure.canvasapi2.enqueueQuery
import com.instructure.canvasapi2.utils.weave.awaitQL

class InboxSettingsManagerImpl : InboxSettingsManager {

    override suspend fun getInboxSignatureSettings(forceNetwork: Boolean): InboxSignatureSettings {
        return try {
            val inboxSettingsData = awaitQL { getInboxSignature(it, forceNetwork) }
            InboxSignatureSettings(
                inboxSettingsData.myInboxSettings?.signature ?: "",
                inboxSettingsData.myInboxSettings?.isUseSignature ?: false
            )
        } catch (e: Exception) {
            InboxSignatureSettings("", false)
        }
    }

    private fun getInboxSignature(
        callback: QLCallback<InboxSettingsQuery.Data>,
        forceNetwork: Boolean
    ) {
        val query = InboxSettingsQuery.builder().build()

        callback.enqueueQuery(query) {
            if (forceNetwork) {
                cachePolicy = HttpCachePolicy.NETWORK_FIRST
            }
        }
    }

    override suspend fun updateInboxSignatureSettings(signature: String, useSignature: Boolean): Boolean {
        try {
            val mutationResult = awaitQL<UpdateInboxSettingsMutation.Data> {
                val mutation = UpdateInboxSettingsMutation.builder()
                    .signature(signature)
                    .useSignature(useSignature)
                    .build()

                it.enqueueMutation(mutation)
            }

            return true
        } catch (e: Exception) {
            return false
        }
    }
}