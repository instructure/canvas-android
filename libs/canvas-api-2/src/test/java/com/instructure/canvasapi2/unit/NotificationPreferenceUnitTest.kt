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

package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.models.NotificationPreference
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test


class NotificationPreferenceUnitTest : Assert() {

    @Test
    fun testNotificationPreference() {
        val notificationPreferences: Array<NotificationPreference> = notificationPreferenceJSON.parse()

        Assert.assertNotNull(notificationPreferences)

        for (preference in notificationPreferences) {
            Assert.assertNotNull(preference)
            Assert.assertNotNull(preference.notification)
            Assert.assertNotNull(preference.category)
            Assert.assertNotNull(preference.frequency)
        }
    }

    @Language("JSON")
    private var notificationPreferenceJSON = """
      [
        {
          "frequency": "never",
          "notification": "new_announcement",
          "category": "announcement"
        },
        {
          "frequency": "never",
          "notification": "assignment_due_date_changed",
          "category": "due_date"
        },
        {
          "frequency": "never",
          "notification": "assignment_changed",
          "category": "course_content"
        },
        {
          "frequency": "never",
          "notification": "assignment_created",
          "category": "due_date"
        },
        {
          "frequency": "never",
          "notification": "grade_weight_changed",
          "category": "grading_policies"
        },
        {
          "frequency": "never",
          "notification": "assignment_graded",
          "category": "grading"
        },
        {
          "frequency": "never",
          "notification": "new_event_created",
          "category": "calendar"
        },
        {
          "frequency": "never",
          "notification": "event_date_changed",
          "category": "calendar"
        },
        {
          "frequency": "never",
          "notification": "collaboration_invitation",
          "category": "invitation"
        },
        {
          "frequency": "never",
          "notification": "web_conference_invitation",
          "category": "invitation"
        },
        {
          "frequency": "immediately",
          "notification": "confirm_email_communication_channel",
          "category": "registration"
        },
        {
          "frequency": "immediately",
          "notification": "confirm_sms_communication_channel",
          "category": "registration"
        },
        {
          "frequency": "immediately",
          "notification": "confirm_registration",
          "category": "registration"
        },
        {
          "frequency": "immediately",
          "notification": "forgot_password",
          "category": "registration"
        },
        {
          "frequency": "never",
          "notification": "new_discussion_topic",
          "category": "discussion"
        },
        {
          "frequency": "immediately",
          "notification": "enrollment_invitation",
          "category": "registration"
        },
        {
          "frequency": "immediately",
          "notification": "enrollment_notification",
          "category": "registration"
        },
        {
          "frequency": "never",
          "notification": "assignment_submitted_late",
          "category": "late_grading"
        },
        {
          "frequency": "never",
          "notification": "group_assignment_submitted_late",
          "category": "late_grading"
        },
        {
          "frequency": "never",
          "notification": "submission_graded",
          "category": "grading"
        },
        {
          "frequency": "never",
          "notification": "submission_comment",
          "category": "submission_comment"
        },
        {
          "frequency": "never",
          "notification": "submission_grade_changed",
          "category": "grading"
        },
        {
          "frequency": "never",
          "notification": "new_wiki_page",
          "category": "course_content"
        },
        {
          "frequency": "never",
          "notification": "updated_wiki_page",
          "category": "course_content"
        },
        {
          "frequency": "daily",
          "notification": "summaries",
          "category": "summaries"
        },
        {
          "frequency": "immediately",
          "notification": "enrollment_registration",
          "category": "registration"
        },
        {
          "frequency": "never",
          "notification": "rubric_assessment_submission_reminder",
          "category": "invitation"
        },
        {
          "frequency": "never",
          "notification": "rubric_assessment_invitation",
          "category": "invitation"
        },
        {
          "frequency": "never",
          "notification": "rubric_association_created",
          "category": "invitation"
        },
        {
          "frequency": "never",
          "notification": "new_account_user",
          "category": "other"
        },
        {
          "frequency": "daily",
          "notification": "assignment_publishing_reminder",
          "category": "reminder"
        },
        {
          "frequency": "daily",
          "notification": "assignment_grading_reminder",
          "category": "reminder"
        },
        {
          "frequency": "daily",
          "notification": "assignment_due_date_reminder",
          "category": "reminder"
        },
        {
          "frequency": "never",
          "notification": "teacher_context_message",
          "category": "other"
        },
        {
          "frequency": "never",
          "notification": "new_context_group_membership",
          "category": "membership_update"
        },
        {
          "frequency": "never",
          "notification": "submission_comment_for_teacher",
          "category": "submission_comment"
        },
        {
          "frequency": "never",
          "notification": "enrollment_accepted",
          "category": "membership_update"
        },
        {
          "frequency": "never",
          "notification": "new_context_group_membership_invitation",
          "category": "invitation"
        },
        {
          "frequency": "never",
          "notification": "group_membership_accepted",
          "category": "membership_update"
        },
        {
          "frequency": "never",
          "notification": "group_membership_rejected",
          "category": "membership_update"
        },
        {
          "frequency": "never",
          "notification": "new_student_organized_group",
          "category": "other"
        },
        {
          "frequency": "never",
          "notification": "new_course",
          "category": "other"
        },
        {
          "frequency": "never",
          "notification": "new_user",
          "category": "other"
        },
        {
          "frequency": "never",
          "notification": "new_teacher_registration",
          "category": "other"
        },
        {
          "frequency": "never",
          "notification": "new_discussion_entry",
          "category": "discussion_entry"
        },
        {
          "frequency": "immediately",
          "notification": "migration_export_ready",
          "category": "migration"
        },
        {
          "frequency": "immediately",
          "notification": "migration_import_finished",
          "category": "migration"
        },
        {
          "frequency": "immediately",
          "notification": "merge_email_communication_channel",
          "category": "registration"
        },
        {
          "frequency": "immediately",
          "notification": "migration_import_failed",
          "category": "migration"
        },
        {
          "frequency": "never",
          "notification": "assignment_submitted",
          "category": "all_submissions"
        },
        {
          "frequency": "never",
          "notification": "assignment_resubmitted",
          "category": "all_submissions"
        },
        {
          "frequency": "never",
          "notification": "new_teacher_registration_immediate",
          "category": "other"
        },
        {
          "frequency": "never",
          "notification": "report_generated",
          "category": "other"
        },
        {
          "frequency": "never",
          "notification": "report_generation_failed",
          "category": "other"
        },
        {
          "frequency": "immediately",
          "notification": "account_user_registration",
          "category": "registration"
        },
        {
          "frequency": "immediately",
          "notification": "account_user_notification",
          "category": "registration"
        },
        {
          "frequency": "immediately",
          "notification": "pseudonym_registration",
          "category": "registration"
        },
        {
          "frequency": "immediately",
          "notification": "content_export_finished",
          "category": "migration"
        },
        {
          "frequency": "immediately",
          "notification": "content_export_failed",
          "category": "migration"
        },
        {
          "frequency": "never",
          "notification": "conversation_message",
          "category": "conversation_message"
        },
        {
          "frequency": "never",
          "notification": "added_to_conversation",
          "category": "added_to_conversation"
        },
        {
          "frequency": "daily",
          "notification": "alert",
          "category": "alert"
        },
        {
          "frequency": "never",
          "notification": "assignment_unmuted",
          "category": "grading"
        },
        {
          "frequency": "never",
          "notification": "appointment_canceled_by_user",
          "category": "student_appointment_signups"
        },
        {
          "frequency": "never",
          "notification": "appointment_deleted_for_user",
          "category": "appointment_cancelations"
        },
        {
          "frequency": "never",
          "notification": "appointment_group_deleted",
          "category": "appointment_cancelations"
        },
        {
          "frequency": "never",
          "notification": "appointment_group_published",
          "category": "appointment_availability"
        },
        {
          "frequency": "never",
          "notification": "appointment_group_updated",
          "category": "appointment_availability"
        },
        {
          "frequency": "never",
          "notification": "appointment_reserved_by_user",
          "category": "student_appointment_signups"
        },
        {
          "frequency": "never",
          "notification": "appointment_reserved_for_user",
          "category": "appointment_signups"
        },
        {
          "frequency": "never",
          "notification": "new_file_added",
          "category": "files"
        },
        {
          "frequency": "never",
          "notification": "new_files_added",
          "category": "files"
        },
        {
          "frequency": "never",
          "notification": "assignment_due_date_override_changed",
          "category": "due_date"
        },
        {
          "frequency": "immediately",
          "notification": "canvasnet_migration",
          "category": "registration"
        },
        {
          "frequency": "immediately",
          "notification": "course_started",
          "category": "registration"
        },
        {
          "frequency": "immediately",
          "notification": "course_starts_in_week",
          "category": "registration"
        },
        {
          "frequency": "immediately",
          "notification": "course_required_materials",
          "category": "registration"
        },
        {
          "frequency": "immediately",
          "notification": "course_already_started",
          "category": "registration"
        },
        {
          "frequency": "never",
          "notification": "submission_needs_grading",
          "category": "all_submissions"
        },
        {
          "frequency": "immediately",
          "notification": "quiz_regrade_finished",
          "category": "grading"
        },
        {
          "frequency": "immediately",
          "notification": "self_enrollment_registration",
          "category": "registration"
        },
        {
          "frequency": "immediately",
          "notification": "twd_migration_new",
          "category": "registration"
        },
        {
          "frequency": "immediately",
          "notification": "twd_migration_existing",
          "category": "registration"
        },
        {
          "frequency": "immediately",
          "notification": "twd_migration_new_late",
          "category": "registration"
        },
        {
          "frequency": "immediately",
          "notification": "twd_migration_existing_late",
          "category": "registration"
        },
        {
          "frequency": "immediately",
          "notification": "peer_review_invitation",
          "category": "invitation"
        },
        {
          "frequency": "daily",
          "notification": "announcement_created_by_you",
          "category": "announcement_created_by_you"
        },
        {
          "frequency": "daily",
          "notification": "announcement_reply",
          "category": "announcement_created_by_you"
        },
        {
          "frequency": "never",
          "notification": "conversation_created",
          "category": "conversation_created"
        }
      ]"""
}
