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

import com.google.gson.Gson;
import com.instructure.canvasapi.model.NotificationPreference;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;


@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class NotificationPreferenceUnitTest extends Assert {

    @Test
    public void testNotificationPreference() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        NotificationPreference[] notificationPreferences = gson.fromJson(notificationPreferenceJSON, NotificationPreference[].class);

        assertNotNull(notificationPreferences);

        for(NotificationPreference preference : notificationPreferences) {
            assertNotNull(preference);

            assertNotNull(preference.notification);
            assertNotNull(preference.category);
            assertNotNull(preference.frequency);

        }
    }

    String notificationPreferenceJSON = "[\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"new_announcement\",\n" +
            "\"category\": \"announcement\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"assignment_due_date_changed\",\n" +
            "\"category\": \"due_date\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"assignment_changed\",\n" +
            "\"category\": \"course_content\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"assignment_created\",\n" +
            "\"category\": \"due_date\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"grade_weight_changed\",\n" +
            "\"category\": \"grading_policies\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"assignment_graded\",\n" +
            "\"category\": \"grading\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"new_event_created\",\n" +
            "\"category\": \"calendar\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"event_date_changed\",\n" +
            "\"category\": \"calendar\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"collaboration_invitation\",\n" +
            "\"category\": \"invitation\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"web_conference_invitation\",\n" +
            "\"category\": \"invitation\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"confirm_email_communication_channel\",\n" +
            "\"category\": \"registration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"confirm_sms_communication_channel\",\n" +
            "\"category\": \"registration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"confirm_registration\",\n" +
            "\"category\": \"registration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"forgot_password\",\n" +
            "\"category\": \"registration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"new_discussion_topic\",\n" +
            "\"category\": \"discussion\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"enrollment_invitation\",\n" +
            "\"category\": \"registration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"enrollment_notification\",\n" +
            "\"category\": \"registration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"assignment_submitted_late\",\n" +
            "\"category\": \"late_grading\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"group_assignment_submitted_late\",\n" +
            "\"category\": \"late_grading\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"submission_graded\",\n" +
            "\"category\": \"grading\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"submission_comment\",\n" +
            "\"category\": \"submission_comment\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"submission_grade_changed\",\n" +
            "\"category\": \"grading\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"new_wiki_page\",\n" +
            "\"category\": \"course_content\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"updated_wiki_page\",\n" +
            "\"category\": \"course_content\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"daily\",\n" +
            "\"notification\": \"summaries\",\n" +
            "\"category\": \"summaries\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"enrollment_registration\",\n" +
            "\"category\": \"registration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"rubric_assessment_submission_reminder\",\n" +
            "\"category\": \"invitation\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"rubric_assessment_invitation\",\n" +
            "\"category\": \"invitation\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"rubric_association_created\",\n" +
            "\"category\": \"invitation\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"new_account_user\",\n" +
            "\"category\": \"other\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"daily\",\n" +
            "\"notification\": \"assignment_publishing_reminder\",\n" +
            "\"category\": \"reminder\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"daily\",\n" +
            "\"notification\": \"assignment_grading_reminder\",\n" +
            "\"category\": \"reminder\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"daily\",\n" +
            "\"notification\": \"assignment_due_date_reminder\",\n" +
            "\"category\": \"reminder\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"teacher_context_message\",\n" +
            "\"category\": \"other\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"new_context_group_membership\",\n" +
            "\"category\": \"membership_update\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"submission_comment_for_teacher\",\n" +
            "\"category\": \"submission_comment\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"enrollment_accepted\",\n" +
            "\"category\": \"membership_update\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"new_context_group_membership_invitation\",\n" +
            "\"category\": \"invitation\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"group_membership_accepted\",\n" +
            "\"category\": \"membership_update\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"group_membership_rejected\",\n" +
            "\"category\": \"membership_update\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"new_student_organized_group\",\n" +
            "\"category\": \"other\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"new_course\",\n" +
            "\"category\": \"other\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"new_user\",\n" +
            "\"category\": \"other\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"new_teacher_registration\",\n" +
            "\"category\": \"other\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"new_discussion_entry\",\n" +
            "\"category\": \"discussion_entry\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"migration_export_ready\",\n" +
            "\"category\": \"migration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"migration_import_finished\",\n" +
            "\"category\": \"migration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"merge_email_communication_channel\",\n" +
            "\"category\": \"registration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"migration_import_failed\",\n" +
            "\"category\": \"migration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"assignment_submitted\",\n" +
            "\"category\": \"all_submissions\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"assignment_resubmitted\",\n" +
            "\"category\": \"all_submissions\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"new_teacher_registration_immediate\",\n" +
            "\"category\": \"other\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"report_generated\",\n" +
            "\"category\": \"other\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"report_generation_failed\",\n" +
            "\"category\": \"other\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"account_user_registration\",\n" +
            "\"category\": \"registration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"account_user_notification\",\n" +
            "\"category\": \"registration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"pseudonym_registration\",\n" +
            "\"category\": \"registration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"content_export_finished\",\n" +
            "\"category\": \"migration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"content_export_failed\",\n" +
            "\"category\": \"migration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"conversation_message\",\n" +
            "\"category\": \"conversation_message\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"added_to_conversation\",\n" +
            "\"category\": \"added_to_conversation\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"daily\",\n" +
            "\"notification\": \"alert\",\n" +
            "\"category\": \"alert\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"assignment_unmuted\",\n" +
            "\"category\": \"grading\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"appointment_canceled_by_user\",\n" +
            "\"category\": \"student_appointment_signups\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"appointment_deleted_for_user\",\n" +
            "\"category\": \"appointment_cancelations\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"appointment_group_deleted\",\n" +
            "\"category\": \"appointment_cancelations\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"appointment_group_published\",\n" +
            "\"category\": \"appointment_availability\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"appointment_group_updated\",\n" +
            "\"category\": \"appointment_availability\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"appointment_reserved_by_user\",\n" +
            "\"category\": \"student_appointment_signups\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"appointment_reserved_for_user\",\n" +
            "\"category\": \"appointment_signups\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"new_file_added\",\n" +
            "\"category\": \"files\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"new_files_added\",\n" +
            "\"category\": \"files\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"assignment_due_date_override_changed\",\n" +
            "\"category\": \"due_date\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"canvasnet_migration\",\n" +
            "\"category\": \"registration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"course_started\",\n" +
            "\"category\": \"registration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"course_starts_in_week\",\n" +
            "\"category\": \"registration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"course_required_materials\",\n" +
            "\"category\": \"registration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"course_already_started\",\n" +
            "\"category\": \"registration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"submission_needs_grading\",\n" +
            "\"category\": \"all_submissions\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"quiz_regrade_finished\",\n" +
            "\"category\": \"grading\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"self_enrollment_registration\",\n" +
            "\"category\": \"registration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"twd_migration_new\",\n" +
            "\"category\": \"registration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"twd_migration_existing\",\n" +
            "\"category\": \"registration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"twd_migration_new_late\",\n" +
            "\"category\": \"registration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"twd_migration_existing_late\",\n" +
            "\"category\": \"registration\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"immediately\",\n" +
            "\"notification\": \"peer_review_invitation\",\n" +
            "\"category\": \"invitation\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"daily\",\n" +
            "\"notification\": \"announcement_created_by_you\",\n" +
            "\"category\": \"announcement_created_by_you\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"daily\",\n" +
            "\"notification\": \"announcement_reply\",\n" +
            "\"category\": \"announcement_created_by_you\"\n" +
            "},\n" +
            "{\n" +
            "\"frequency\": \"never\",\n" +
            "\"notification\": \"conversation_created\",\n" +
            "\"category\": \"conversation_created\"\n" +
            "}" +
            "]";
}
