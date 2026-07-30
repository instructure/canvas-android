/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.model

data class AccountNotificationPreference(
    val channel: HorizonAccountNotificationChannel,
    val category: AccountNotificationCategory,
    val frequency: AccountNotificationFrequency,
)

enum class AccountNotificationCategory(val apiString: String) {
    ACCOUNT_NOTIFICATION("account_notification"),
    ANNOUNCEMENT("announcement"),
    CONVERSATION_MESSAGE("conversation_message"),
    DUE_DATE("due_date"),
    GRADING("grading");

    companion object {
        fun fromApiString(apiString: String): AccountNotificationCategory? {
            return entries.firstOrNull { it.apiString == apiString }
        }
    }
}

enum class AccountNotificationFrequency(val apiString: String) {
    IMMEDIATELY("immediately"),
    NEVER("never");

    companion object {
        fun fromApiString(apiString: String): AccountNotificationFrequency {
            return entries.firstOrNull { it.apiString == apiString } ?: NEVER
        }
    }
}

enum class AccountNotificationType(val apiString: String) {
    EMAIL("email"),
    PUSH("push"),
    SMS("sms");

    companion object {
        fun fromApiString(apiString: String): AccountNotificationType {
            return entries.firstOrNull { it.apiString == apiString } ?: EMAIL
        }
    }
}

enum class HorizonAccountNotificationCategory(val categories: List<AccountNotificationCategory>) {
    ANNOUNCEMENTS_AND_MESSAGES(
        listOf(
            AccountNotificationCategory.ACCOUNT_NOTIFICATION,
            AccountNotificationCategory.ANNOUNCEMENT,
            AccountNotificationCategory.CONVERSATION_MESSAGE,
        )
    ),
    ASSIGNMENT_DUE_DATE(listOf(AccountNotificationCategory.DUE_DATE)),
    SCORES(listOf(AccountNotificationCategory.GRADING)),
}

data class HorizonAccountNotificationPreference(
    val channel: HorizonAccountNotificationChannel,
    val category: HorizonAccountNotificationCategory,
    val frequency: AccountNotificationFrequency,
)

data class HorizonAccountNotificationChannel(
    val id: Long,
    val type: AccountNotificationType,
)
