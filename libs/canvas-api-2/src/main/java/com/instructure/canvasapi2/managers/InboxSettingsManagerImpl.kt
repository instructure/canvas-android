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

import com.apollographql.apollo3.api.Optional
import com.instructure.canvasapi2.InboxSettingsQuery
import com.instructure.canvasapi2.QLClientConfig
import com.instructure.canvasapi2.UpdateInboxSettingsMutation
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.canvasapi2.utils.toApiString
import java.util.concurrent.TimeUnit

class InboxSettingsManagerImpl : InboxSettingsManager {

    override suspend fun getInboxSignatureSettings(forceNetwork: Boolean): DataResult<InboxSignatureSettings> {
        return try {
            val query = InboxSettingsQuery()
            val inboxSettingsData = QLClientConfig.enqueueQuery(query) {
                if (forceNetwork) {
//                    cachePolicy = HttpCachePolicy.NETWORK_FIRST.expireAfter(1, TimeUnit.HOURS)
                }
            }.dataAssertNoErrors
            val inboxSignatureSettings = InboxSignatureSettings(
                inboxSettingsData.myInboxSettings?.signature.orEmpty(),
                inboxSettingsData.myInboxSettings?.useSignature ?: false,
                inboxSettingsData.myInboxSettings?.useOutOfOffice ?: false,
                inboxSettingsData.myInboxSettings?.outOfOfficeMessage.orEmpty(),
                inboxSettingsData.myInboxSettings?.outOfOfficeSubject.orEmpty(),
                inboxSettingsData.myInboxSettings?.outOfOfficeFirstDate.toApiString(),
                inboxSettingsData.myInboxSettings?.outOfOfficeLastDate.toApiString()
            )
            return DataResult.Success(inboxSignatureSettings)
        } catch (e: Exception) {
            DataResult.Fail(Failure.Exception(e))
        }
    }

    override suspend fun updateInboxSignatureSettings(inboxSignatureSettings: InboxSignatureSettings): DataResult<InboxSignatureSettings> {
        try {
            val mutation = UpdateInboxSettingsMutation(
                useSignature = inboxSignatureSettings.useSignature,
                signature = Optional.present(inboxSignatureSettings.signature),
                useOutOfOffice = inboxSignatureSettings.useOutOfOffice,
                outOfOfficeMessage = Optional.present(inboxSignatureSettings.outOfOfficeMessage),
                outOfOfficeSubject = Optional.present(inboxSignatureSettings.outOfOfficeSubject),
                outOfOfficeFirstDate = Optional.present(inboxSignatureSettings.outOfOfficeFirstDate),
                outOfOfficeLastDate = Optional.present(inboxSignatureSettings.outOfOfficeLastDate)
            )

            val mutationResult = QLClientConfig.enqueueMutation(mutation).dataAssertNoErrors

//            QLClientConfig().buildClient().clearHttpCache()

            return DataResult.Success(
                InboxSignatureSettings(
                    mutationResult.updateMyInboxSettings?.myInboxSettings?.signature ?: "",
                    mutationResult.updateMyInboxSettings?.myInboxSettings?.useSignature ?: false
                )
            )
        } catch (e: Exception) {
            return DataResult.Fail(Failure.Exception(e))
        }
    }
}