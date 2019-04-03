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
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.AssignmentDueDate;
import com.instructure.canvasapi.model.LockInfo;
import com.instructure.canvasapi.model.LockedModule;
import com.instructure.canvasapi.model.ModuleCompletionRequirement;
import com.instructure.canvasapi.model.RubricCriterion;
import com.instructure.canvasapi.model.RubricCriterionRating;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.List;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class AssignmentUnitTest extends Assert {

    @Test
    public void testAssignment() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        Assignment assignment = gson.fromJson(assignmentJSON, Assignment.class);

        assertTrue(assignment.getId() > 0);

        assertEquals(assignment.getPointsPossible(), 30.0);

        assertEquals(assignment.getSubmissionTypes().size(), 3);
        assertTrue(assignment.getSubmissionTypes().get(0).toString().equalsIgnoreCase("online_upload"));
        assertTrue(assignment.getSubmissionTypes().get(1).toString().equalsIgnoreCase("online_text_entry"));
        assertTrue(assignment.getSubmissionTypes().get(2).toString().equalsIgnoreCase("media_recording"));

        assertEquals(assignment.getAllowedExtensions().size(), 3);
        assertTrue(assignment.getAllowedExtensions().get(0).equalsIgnoreCase("doc"));
        assertTrue(assignment.getAllowedExtensions().get(1).equalsIgnoreCase("pdf"));
        assertTrue(assignment.getAllowedExtensions().get(2).equalsIgnoreCase("txt"));

        assertEquals(assignment.getCourseId(), 833052);

        assertNotNull(assignment.getDescription());

        assertNotNull(assignment.getDueDate());

        assertNotNull(assignment.getName());

        assertNotNull(assignment.getLastSubmission());


        assertEquals(assignment.getAssignmentGroupId(), 534100);
    }

    @Test
    public void testLockedAssignment() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        Assignment lockInfoAssignment = gson.fromJson(lockInfoJSON, Assignment.class);


        // If the assignment is locked for the user, make sure the lock_info & explanation aren't empty/null
        if(lockInfoAssignment.isLockedForUser()){
            assertTrue(!lockInfoAssignment.getLockInfo().isEmpty());
            assertNotNull(lockInfoAssignment.getLock_explanation());
        }

        LockInfo lockInfo = lockInfoAssignment.getLockInfo();
        assertNotNull(lockInfo);

        // The lock_info should have a context_module
        LockedModule lockedModule = lockInfo.getContext_module();
        assertNotNull(lockedModule);
        assertNotNull(lockedModule.getId());
        assertNotNull(lockedModule.getContext_id());
        assertNotNull(lockedModule.getContextType());
        assertNotNull(lockedModule.getName());
        assertNotNull(lockedModule.getUnlock_at());
        assertNotNull(lockedModule.isRequireSequentialProgress());

        List<ModuleCompletionRequirement> completionRequirements = lockedModule.getCompletionRequirements();
        assertNotNull(completionRequirements);
        assertEquals(9, completionRequirements.size());
        for(ModuleCompletionRequirement requirement : completionRequirements){
            assertNotNull(requirement.getId());
            assertNotNull(requirement.getType());
        }
    }

    @Test
    public void testAssignmentDueDate() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        Assignment assignment = gson.fromJson(assignmentDueDates, Assignment.class);

        List<AssignmentDueDate> allDates = assignment.getDueDates();

        assertEquals(allDates.size(), 2);

        for(AssignmentDueDate dueDate : allDates){
            assertNotNull(dueDate.getDueDate());
        }
    }

    @Test
    public void testRubricAssignment() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        Assignment rubricAssignment = gson.fromJson(rubricAssignmentJSON, Assignment.class);

        assertNotNull(rubricAssignment.getRubric());

        List<RubricCriterion> rubricCriterions = rubricAssignment.getRubric();
        assertEquals(rubricCriterions.size(), 3);
        for(RubricCriterion rubricCriterion : rubricCriterions){
            testRubricCriterion(rubricCriterion);
        }
    }

    public static void testRubricCriterion (RubricCriterion rubricCriterion) {

        assertNotNull(rubricCriterion);

        assertNotNull(rubricCriterion.getId());

        assertNotNull(rubricCriterion.getCriterionDescription());

        assertNotNull(rubricCriterion.getLongDescription());

        assertTrue(rubricCriterion.getPoints() >= 0);

        if(rubricCriterion.getRatings() != null) {
            for(RubricCriterionRating rubricCriterionRating : rubricCriterion.getRatings()){
                testRubricCriterionRating(rubricCriterionRating);
            }
        }
    }

    public static void testRubricCriterionRating(RubricCriterionRating rubricCriterionRating) {

        assertNotNull(rubricCriterionRating);

        assertNotNull(rubricCriterionRating.getId());

        assertNotNull(rubricCriterionRating.getRatingDescription());

        assertTrue(rubricCriterionRating.getPoints() >= 0);

    }

    String assignmentJSON = "{"
            +"\"assignment_group_id\": 534100,"
            +"\"automatic_peer_reviews\": false,"
            +"\"description\": \"<p>List all the different types of layouts that are used in xml.</p>\","
            +"\"due_at\": \"2012-10-25T05:59:00Z\","
            +"\"grade_group_students_individually\": false,"
            +"\"grading_standard_id\": null,"
            +"\"grading_type\": \"points\","
            +"\"group_category_id\": null,"
            +"\"id\": 2241839,"
            +"\"lock_at\": null,"
            +"\"peer_reviews\": false,"
            +"\"points_possible\": 30,"
            +"\"position\": 1,"
            +"\"unlock_at\": null,"
            +"\"course_id\": 833052,"
            +"\"name\": \"Android 101\","
            +"\"submission_types\": ["
                +"\"online_upload\","
                +"\"online_text_entry\","
                +"\"media_recording\"],"
            +"\"muted\": false,"
            +"\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/2241839\","
            +"\"allowed_extensions\": ["
                +"\"doc\","
                +"\"pdf\","
                +"\"txt\"],"
            +"\"submission\": {"
                +"\"assignment_id\": 2241839,"
                +"\"attempt\": 15,"
                +"\"body\": \"Hey Hey Hey \","
                +"\"grade\": \"28\","
                +"\"grade_matches_current_submission\": false,"
                +"\"graded_at\": \"2012-10-09T02:01:58Z\","
                +"\"grader_id\": 3356518,"
                +"\"id\": 10186303,"
                +"\"score\": 28,"
                +"\"submission_type\": \"online_text_entry\","
                +"\"submitted_at\": \"2013-09-12T19:44:55Z\","
                +"\"url\": null,"
                +"\"user_id\": 3360251,"
                +"\"workflow_state\": \"submitted\","
                +"\"late\": true,"
                +"\"preview_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/2241839/submissions/3360251?preview=1\"},"
            +"\"locked_for_user\": false"
            +"}";

    String rubricAssignmentJSON = "{"
            +"\"assignment_group_id\": 534100,"
            +"\"automatic_peer_reviews\": false,"
            +"\"description\": \"Replacement description\","
            +"\"due_at\": \"2013-06-01T05:59:00Z\","
            +"\"grade_group_students_individually\": false,"
            +"\"grading_standard_id\": null,"
            +"\"grading_type\": \"points\","
            +"\"group_category_id\": null,"
            +"\"id\": 3119886,"
            +"\"lock_at\": null,"
            +"\"peer_reviews\": false,"
            +"\"points_possible\": 15,"
            +"\"position\": 20,"
            +"\"unlock_at\": null,"
            +"\"course_id\": 833052,"
            +"\"name\": \"Education\","
            +"\"submission_types\": ["
                +"\"online_text_entry\","
                +"\"online_url\","
                +"\"media_recording\","
                +"\"online_upload\"],"
            +"\"muted\": false,"
            +"\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/3119886\","
            +"\"use_rubric_for_grading\": true,"
            +"\"free_form_criterion_comments\": false,"
            +"\"rubric\": "
            +"[" // Start Rubric
                +"{"
                    +"\"id\": \"176919_1697\","
                    +"\"points\": 5,"
                    +"\"description\": \"Grammar\","
                    +"\"long_description\": \"\","
                    +"\"ratings\": "
                    +"["
                        +"{"
                            +"\"id\": \"blank\","
                            +"\"points\": 5,"
                            +"\"description\": \"Perfect Grammar\""
                        +"},"
                        +"{"
                            +"\"id\": \"176919_53\","
                            +"\"points\": 4,"
                            +"\"description\": \"1 or two mistakes\""
                        +"},"
                        +"{"
                            +"\"id\": \"blank_2\","
                            +"\"points\": 3,"
                            +"\"description\": \"A few mistakes\""
                        +"},"
                        +"{"
                            +"\"id\": \"176919_1429\","
                            +"\"points\": 2,"
                            +"\"description\": \"Several mistakes\""
                        +"},"
                        +"{"
                            +"\"id\": \"176919_9741\","
                            +"\"points\": 0,"
                            +"\"description\": \"Abysmal\""
                        +"}"
                    +"]"
                +"},"
                +"{"
                    +"\"id\": \"176919_6623\","
                    +"\"points\": 5,"
                    +"\"description\": \"Coolness Factor\","
                    +"\"long_description\": \"\","
                    +"\"ratings\": "
                    +"["
                        +"{"
                            +"\"id\": \"176919_9675\","
                            +"\"points\": 5,"
                            +"\"description\": \"Super cool\""
                        +"},"
                        +"{"
                            +"\"id\": \"176919_3172\","
                            +"\"points\": 4,"
                            +"\"description\": \"Moderately Cool\""
                        +"},"
                        +"{"
                            +"\"id\": \"176919_393\","
                            +"\"points\": 3,"
                            +"\"description\": \"Un-Cool and Geeky\""
                        +"},"
                        +"{"
                            +"\"id\": \"176919_5761\","
                            +"\"points\": 0,"
                            +"\"description\": \"Un-Cool and Nerdy\""
                        +"}"
                    +"]"
                +"},"
                +"{"
                    +"\"id\": \"176919_8253\","
                    +"\"points\": 5,"
                    +"\"description\": \"How much I like you\","
                    +"\"long_description\": \"\","
                    +"\"ratings\": "
                    +"["
                        +"{"
                            +"\"id\": \"176919_5103\","
                            +"\"points\": 5,"
                            +"\"description\": \"You're my favorite in the class\""
                        +"},"
                        +"{"
                            +"\"id\": \"176919_6271\","
                            +"\"points\": 4,"
                            +"\"description\": \"I like having you around\""
                        +"},"
                        +"{"
                            +"\"id\": \"176919_8307\","
                            +"\"points\": 3,"
                            +"\"description\": \"You don't annoy me\""
                        +"},"
                        +"{"
                            +"\"id\": \"176919_377\","
                            +"\"points\": 2,"
                            +"\"description\": \"I can barely tolerate you\""
                        +"},"
                        +"{"
                            +"\"id\": \"176919_2255\","
                            +"\"points\": 0,"
                            +"\"description\": \"I wish you were dead\""
                        +"}"
                    +"]"
                +"}"
            +"]," // End Rubric
            +"\"rubric_settings\": {"
                +"\"points_possible\": 15,"
                +"\"free_form_criterion_comments\": false"
            +"},"
            +"\"locked_for_user\": false"
        +"}";

    String lockInfoJSON = "{"
            +"\"assignment_group_id\": 534104,"
            +"\"automatic_peer_reviews\": false,"
            +"\"due_at\": \"2013-08-15T05:59:00Z\","
            +"\"grade_group_students_individually\": false,"
            +"\"grading_standard_id\": null,"
            +"\"grading_type\": \"points\","
            +"\"group_category_id\": null,"
            +"\"id\": 3546452,"
            +"\"lock_at\": null,"
            +"\"peer_reviews\": false,"
            +"\"points_possible\": 75,"
            +"\"position\": 16,"
            +"\"unlock_at\": null,"
            +"\"lock_info\":"
            +"{" // Start lock_info
                +"\"asset_string\": \"assignment_3546452\","
                +"\"context_module\": "
                +"{" // Start context_module
                    +"\"id\": 805092,"
                    +"\"context_id\": 836357,"
                    +"\"context_type\": \"Course\","
                    +"\"name\": \"Locked Prereq\","
                    +"\"cloned_item_id\": null,"
                    +"\"completion_requirements\":"
                    +"["
                        +"{"
                            +"\"id\": 6756870,"
                            +"\"type\": \"min_score\","
                            +"\"min_score\": \"80\","
                            +"\"max_score\": null"
                        +"},"
                        +"{"
                            +"\"id\": 8944431,"
                            +"\"type\": \"must_submit\","
                            +"\"min_score\": 0,"
                            +"\"max_score\": null"
                        +"},"
                        +"{"
                            +"\"id\": 8944445,"
                            +"\"type\": \"min_score\","
                            +"\"min_score\": \"50\","
                            +"\"max_score\": null"
                        +"},"
                        +"{"
                            +"\"id\": 8951510,"
                            +"\"type\": \"must_view\","
                            +"\"min_score\": 0,"
                            +"\"max_score\": null"
                        +"},"
                        +"{"
                            +"\"id\": 8951513,"
                            +"\"type\": \"must_view\","
                            +"\"min_score\": 0,"
                            +"\"max_score\": null"
                        +"},"
                        +"{"
                            +"\"id\": 8955141,"
                            +"\"type\": \"must_submit\","
                            +"\"min_score\":0,"
                            +"\"max_score\": null"
                        +"},"
                        +"{"
                            +"\"id\": 8955142,"
                            +"\"type\": \"must_view\","
                            +"\"min_score\": 0,"
                            +"\"max_score\": null"
                        +"},"
                        +"{"
                            +"\"id\": 8955144,"
                            +"\"type\": \"must_contribute\","
                            +"\"min_score\":0,"
                            +"\"max_score\": null"
                        +"},"
                        +"{"
                            +"\"id\": 8955147,"
                            +"\"type\": \"must_view\","
                            +"\"min_score\": 0,"
                            +"\"max_score\": null"
                        +"}"
                    +"],"
                    +"\"created_at\": \"2013-03-06T23:44:07Z\","
                    +"\"deleted_at\": null,"
                    +"\"downstream_modules\": null,"
                    +"\"end_at\": null,"
                    +"\"migration_id\": null,"
                    +"\"position\": 7,"
                    +"\"prerequisites\":"
                    +"["
                        +"{"
                        +"\"id\": 793427,"
                        +"\"type\": \"context_module\","
                        +"\"name\": \"Car Movies\""
                        +"}"
                    +"],"
                    +"\"require_sequential_progress\": false,"
                    +"\"start_at\": null,"
                    +"\"unlock_at\": \"2013-07-31T06:00:00Z\","
                    +"\"updated_at\": \"2013-07-23T21:09:46Z\","
                    +"\"workflow_state\": \"active\""
                +"}"// End context_module
            +"},"   // End lock_info
            +"\"course_id\": 836357,"
            +"\"name\": \"Superhero\","
            +"\"submission_types\": "
            +"["
                +"\"online_text_entry\","
                +"\"online_url\""
            +"],"
            +"\"description\": null,"
            +"\"muted\": false,"
            +"\"html_url\": \"https://mobiledev.instructure.com/courses/836357/assignments/3546452\","
            +"\"locked_for_user\": true,"
            +"\"lock_explanation\": \"This assignment is part of the module <b>Locked Prereq</b> and hasn&#39;t been unlocked yet.<br/><a href='/courses/836357/modules'>Visit the course modules page for information on how to unlock this content.</a><a href='/courses/836357/modules/805092/prerequisites/assignment_3546452' style='display: none;' id='module_prerequisites_lookup_link'>&nbsp;</a>\""
        +"}";

    String assignmentDueDates = "{"
            +"\"assignment_group_id\": 534100,"
            +"\"automatic_peer_reviews\": false,"
            +"\"description\": \"<p>List all the different types of layouts that are used in xml.</p>\","
            +"\"due_at\": \"2012-10-25T05:59:00Z\","
            +"\"grade_group_students_individually\": false,"
            +"\"grading_standard_id\": null,"
            +"\"grading_type\": \"points\","
            +"\"group_category_id\": null,"
            +"\"id\": 2241839,"
            +"\"lock_at\": null,"
            +"\"peer_reviews\": false,"
            +"\"points_possible\": 30,"
            +"\"position\": 1,"
            +"\"unlock_at\": null,"
            +"\"course_id\": 833052,"
            +"\"name\": \"Android 101\","
            +"\"submission_types\": ["
                +"\"online_upload\","
                +"\"online_text_entry\","
                +"\"media_recording\""
            +"],"
            +"\"muted\": false,"
            +"\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/2241839\","
            +"\"allowed_extensions\": ["
                +"\"doc\","
                +"\"pdf\","
                +"\"txt\""
            +"],"
            +"\"submission\": {"
                +"\"assignment_id\": 2241839,"
                +"\"attempt\": 15,"
                +"\"body\": \"Hey Hey Hey \","
                +"\"grade\": \"28\","
                +"\"grade_matches_current_submission\": false,"
                +"\"graded_at\": \"2012-10-09T02:01:58Z\","
                +"\"grader_id\": 3356518,"
                +"\"id\": 10186303,"
                +"\"score\": 28,"
                +"\"submission_type\": \"online_text_entry\","
                +"\"submitted_at\": \"2013-09-12T19:44:55Z\","
                +"\"url\": null,"
                +"\"user_id\": 3360251,"
                +"\"workflow_state\": \"submitted\","
                +"\"late\": true,"
                +"\"preview_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/2241839/submissions/3360251?preview=1\""
            +"},"
            +"\"locked_for_user\": false,"
            +"\"all_dates\":["
                +"{"
                    +"\"due_at\":\"2013-06-01T05:59:00Z\","
                    +"\"unlock_at\":null,"
                    +"\"lock_at\":null,"
                    +"\"base\":true"
                +"},"
                +"{"
                    +"\"due_at\":\"2015-01-24T06:59:59Z\","
                    +"\"unlock_at\":null,"
                    +"\"lock_at\":null,"
                    +"\"base\":true"
                +"}"
            +"]"
            +"}";
}
