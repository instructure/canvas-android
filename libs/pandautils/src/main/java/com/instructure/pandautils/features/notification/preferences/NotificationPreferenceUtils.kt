/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.instructure.pandautils.features.notification.preferences

import android.content.res.Resources
import androidx.annotation.StringRes
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R

class NotificationPreferenceUtils(private val resources: Resources) {

    enum class CategoryGroup {
        COURSE_ACTIVITIES, DISCUSSIONS, CONVERSATIONS, SCHEDULING, GROUPS, ALERTS, CONFERENCES
    }
    
    private fun getString(@StringRes id: Int) = resources.getString(id)

    val categoryTitleMap = hashMapOf(
            // COURSE ACTIVITIES
            "due_date" to getString(R.string.notification_pref_due_date),
            "grading_policies" to getString(R.string.notification_pref_grading_policies),
            "course_content" to getString(R.string.notification_pref_course_content),
            "files" to getString(R.string.notification_pref_files),
            "announcement" to getString(R.string.notification_pref_announcement),
            "announcement_created_by_you" to getString(R.string.notification_pref_announcement_created_by_you),
            "grading" to getString(R.string.notification_pref_grading),
            "invitation" to getString(R.string.notification_pref_invitation),
            "all_submissions" to getString(R.string.notification_pref_all_submissions),
            "late_grading" to getString(R.string.notification_pref_late_grading),
            "submission_comment" to getString(R.string.notification_pref_submission_comment),

            // DISCUSSIONS
            "discussion" to getString(R.string.notification_pref_discussion),
            "discussion_entry" to getString(R.string.notification_pref_discussion_post),

            // CONVERSATIONS
            "added_to_conversation" to getString(R.string.notification_pref_add_to_conversation),
            "conversation_message" to getString(R.string.notification_pref_conversation_message),
            "conversation_created" to getString(R.string.notification_pref_conversations_created_by_you),

            // SCHEDULING
            "student_appointment_signups" to getString(R.string.notification_pref_student_appointment_signups),
            "appointment_signups" to getString(R.string.notification_pref_appointment_signups),
            "appointment_cancelations" to getString(R.string.notification_pref_appointment_cancelations),
            "appointment_availability" to getString(R.string.notification_pref_appointment_availability),
            "calendar" to getString(R.string.notification_pref_calendar),

            // GROUPS
            "membership_update" to getString(R.string.notification_pref_membership_update),

            // ALERTS
            "other" to getString(R.string.notification_pref_admin),

            // CONFERENCES
            "recording_ready" to getString(R.string.notification_pref_recording_ready)
    )

    val categoryDescriptionMap = hashMapOf(
            // COURSE ACTIVITIES
            "due_date" to getString(R.string.notification_desc_due_date),
            "grading_policies" to getString(R.string.notification_desc_grading_policies),
            "course_content" to getString(R.string.notification_desc_course_content),
            "files" to getString(R.string.notification_desc_files),
            "announcement" to getString(R.string.notification_desc_announcement),
            "announcement_created_by_you" to getString(R.string.notification_desc_announcement_created_by_you),
            "grading" to getString(R.string.notification_desc_grading),
            "invitation" to getString(R.string.notification_desc_invitation),
            "all_submissions" to getString(R.string.notification_desc_all_submissions),
            "late_grading" to getString(R.string.notification_desc_late_grading),
            "submission_comment" to getString(R.string.notification_desc_submission_comment),

            // DISCUSSIONS
            "discussion" to getString(R.string.notification_desc_discussion),
            "discussion_entry" to getString(R.string.notification_desc_discussion_post),

            // CONVERSATIONS
            "added_to_conversation" to getString(R.string.notification_desc_add_to_conversation),
            "conversation_message" to getString(R.string.notification_desc_conversation_message),
            "conversation_created" to getString(R.string.notification_desc_conversations_created_by_you),

            // SCHEDULING
            "student_appointment_signups" to getString(R.string.notification_desc_student_appointment_signups),
            "appointment_signups" to getString(R.string.notification_desc_appointment_signups),
            "appointment_cancelations" to getString(R.string.notification_desc_appointment_cancelations),
            "appointment_availability" to getString(R.string.notification_desc_appointment_availability),
            "calendar" to getString(R.string.notification_desc_calendar),

            // GROUPS
            "membership_update" to getString(R.string.notification_desc_membership_update),

            // ALERTS
            "other" to getString(R.string.notification_desc_admin),

            // CONFERENCES
            "recording_ready" to getString(R.string.notification_desc_recording_ready)
    )

    val categoryHelperMap = mapOf(
            // COURSE ACTIVITIES
            "due_date" to CategoryHelper(CategoryGroup.COURSE_ACTIVITIES, 1),
            "grading_policies" to CategoryHelper(CategoryGroup.COURSE_ACTIVITIES, 2),
            "course_content" to CategoryHelper(CategoryGroup.COURSE_ACTIVITIES, 3),
            "files" to CategoryHelper(CategoryGroup.COURSE_ACTIVITIES, 4),
            "announcement" to CategoryHelper(CategoryGroup.COURSE_ACTIVITIES, 5),
            "announcement_created_by_you" to CategoryHelper(CategoryGroup.COURSE_ACTIVITIES, 6),
            "grading" to CategoryHelper(CategoryGroup.COURSE_ACTIVITIES, 7),
            "invitation" to CategoryHelper(CategoryGroup.COURSE_ACTIVITIES, 8),
            "all_submissions" to CategoryHelper(CategoryGroup.COURSE_ACTIVITIES, 9),
            "late_grading" to CategoryHelper(CategoryGroup.COURSE_ACTIVITIES, 10),
            "submission_comment" to CategoryHelper(CategoryGroup.COURSE_ACTIVITIES, 11),

            // DISCUSSIONS
            "discussion" to CategoryHelper(CategoryGroup.DISCUSSIONS, 1),
            "discussion_entry" to CategoryHelper(CategoryGroup.DISCUSSIONS, 2),

            // CONVERSATIONS
            "added_to_conversation" to CategoryHelper(CategoryGroup.CONVERSATIONS, 1),
            "conversation_message" to CategoryHelper(CategoryGroup.CONVERSATIONS, 2),
            "conversation_created" to CategoryHelper(CategoryGroup.CONVERSATIONS, 3),

            // SCHEDULING
            "student_appointment_signups" to CategoryHelper(CategoryGroup.SCHEDULING, 1),
            "appointment_signups" to CategoryHelper(CategoryGroup.SCHEDULING, 2),
            "appointment_cancelations" to CategoryHelper(CategoryGroup.SCHEDULING, 3),
            "appointment_availability" to CategoryHelper(CategoryGroup.SCHEDULING, 4),
            "calendar" to CategoryHelper(CategoryGroup.SCHEDULING, 5),

            // GROUPS
            "membership_update" to CategoryHelper(CategoryGroup.GROUPS, 1),

            // ALERTS
            "other" to CategoryHelper(CategoryGroup.ALERTS, 1),

            // CONFERENCES
            "recording_ready" to CategoryHelper(CategoryGroup.CONFERENCES, 1)
    )

    val categoryGroupHeaderMap = mapOf(
            CategoryGroup.COURSE_ACTIVITIES to NotificationCategoryHeaderViewData(getString(R.string.notification_cat_course_activities), 0),
            CategoryGroup.DISCUSSIONS to NotificationCategoryHeaderViewData(getString(R.string.notification_cat_discussions), 1),
            CategoryGroup.CONVERSATIONS to NotificationCategoryHeaderViewData(getString(R.string.notification_cat_conversations), 2),
            CategoryGroup.SCHEDULING to NotificationCategoryHeaderViewData(getString(R.string.notification_cat_scheduling), 3),
            CategoryGroup.GROUPS to NotificationCategoryHeaderViewData(getString(R.string.notification_cat_groups), 4),
            CategoryGroup.ALERTS to NotificationCategoryHeaderViewData(getString(R.string.notification_cat_alerts), 5),
            CategoryGroup.CONFERENCES to NotificationCategoryHeaderViewData(getString(R.string.notification_cat_conferences), 6)
    )

    // Used to match web sorting
    class CategoryHelper(var categoryGroup: CategoryGroup, var position: Int)
}
