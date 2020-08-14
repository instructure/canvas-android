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

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.canvasapi2.models.RubricCriterionRating
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class AssignmentUnitTest : Assert() {

    @Test
    fun testAssignment() {
        val assignment: Assignment = assignmentJSON.parse()

        Assert.assertTrue(assignment.id > 0)
        Assert.assertEquals(assignment.pointsPossible, 30.0, 0.0)
        Assert.assertEquals(assignment.submissionTypesRaw.size, 3)
        Assert.assertTrue(assignment.submissionTypesRaw[0].equals("online_upload", ignoreCase = true))
        Assert.assertTrue(assignment.submissionTypesRaw[1].equals("online_text_entry", ignoreCase = true))
        Assert.assertTrue(assignment.submissionTypesRaw[2].equals("media_recording", ignoreCase = true))

        Assert.assertEquals(assignment.allowedExtensions.size, 3)
        Assert.assertTrue(assignment.allowedExtensions[0].equals("doc", ignoreCase = true))
        Assert.assertTrue(assignment.allowedExtensions[1].equals("pdf", ignoreCase = true))
        Assert.assertTrue(assignment.allowedExtensions[2].equals("txt", ignoreCase = true))

        Assert.assertEquals(assignment.courseId, 833052)
        Assert.assertNotNull(assignment.description)
        Assert.assertNotNull(assignment.dueAt)
        Assert.assertNotNull(assignment.name)
        Assert.assertNotNull(assignment.submission)
        Assert.assertEquals(assignment.assignmentGroupId, 534100)
    }

    @Test
    fun testLockedAssignment() {
        val lockInfoAssignment: Assignment = lockInfoJSON.parse()

        // If the assignment is Locked for the user, make sure the lock_info & explanation aren't empty/null
        if (lockInfoAssignment.lockedForUser) {
            Assert.assertTrue(!lockInfoAssignment.lockInfo!!.isEmpty)
            Assert.assertNotNull(lockInfoAssignment.lockExplanation)
        }

        val lockInfo = lockInfoAssignment.lockInfo
        Assert.assertNotNull(lockInfo)

        // The lock_info should have a context_module
        val lockedModule = lockInfo?.contextModule
        Assert.assertNotNull(lockedModule)
        Assert.assertNotNull(lockedModule?.id)
        Assert.assertNotNull(lockedModule?.contextId)
        Assert.assertNotNull(lockedModule?.contextType)
        Assert.assertNotNull(lockedModule?.name)
        Assert.assertNotNull(lockedModule?.unlockAt)
        Assert.assertNotNull(lockedModule?.isRequireSequentialProgress)

        val completionRequirements = lockedModule?.completionRequirements
        Assert.assertNotNull(completionRequirements)
        Assert.assertEquals(9, completionRequirements?.size)
        for (requirement in completionRequirements!!) {
            Assert.assertNotNull(requirement.id)
            Assert.assertNotNull(requirement.type)
        }
    }

    @Test
    fun testRubricAssignment() {
        val rubricAssignment: Assignment = rubricAssignmentJSON.parse()

        Assert.assertNotNull(rubricAssignment.rubric)

        val rubricCriterions = rubricAssignment.rubric
        Assert.assertEquals(rubricCriterions?.size, 3)
        for (rubricCriterion in rubricCriterions!!) {
            testRubricCriterion(rubricCriterion)
        }
    }

    private fun testRubricCriterion(rubricCriterion: RubricCriterion) {
        Assert.assertNotNull(rubricCriterion)
        Assert.assertNotNull(rubricCriterion.id)
        Assert.assertNotNull(rubricCriterion.description)
        Assert.assertNotNull(rubricCriterion.longDescription)
        Assert.assertTrue(rubricCriterion.points >= 0)

        for (rubricCriterionRating in rubricCriterion.ratings) {
            testRubricCriterionRating(rubricCriterionRating)
        }
    }

    private fun testRubricCriterionRating(rubricCriterionRating: RubricCriterionRating) {
        Assert.assertNotNull(rubricCriterionRating)
        Assert.assertNotNull(rubricCriterionRating.id)
        Assert.assertNotNull(rubricCriterionRating.description)
        Assert.assertTrue(rubricCriterionRating.points >= 0)
    }

    @Language("JSON")
    private var assignmentJSON = """
      {
        "assignment_group_id": 534100,
        "automatic_peer_reviews": false,
        "description": "<p>List all the different types of layouts that are used in xml.</p>",
        "due_at": "2012-10-25T05:59:00Z",
        "grade_group_students_individually": false,
        "grading_standard_id": null,
        "grading_type": "points",
        "group_category_id": null,
        "id": 2241839,
        "lock_at": null,
        "peer_reviews": false,
        "points_possible": 30,
        "position": 1,
        "unlock_at": null,
        "course_id": 833052,
        "name": "Android 101",
        "submission_types": [
          "online_upload",
          "online_text_entry",
          "media_recording"
        ],
        "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/2241839",
        "allowed_extensions": [
          "doc",
          "pdf",
          "txt"
        ],
        "submission": {
          "assignment_id": 2241839,
          "attempt": 15,
          "body": "Hey Hey Hey ",
          "grade": "28",
          "grade_matches_current_submission": false,
          "graded_at": "2012-10-09T02:01:58Z",
          "grader_id": 3356518,
          "id": 10186303,
          "score": 28,
          "submission_type": "online_text_entry",
          "submitted_at": "2013-09-12T19:44:55Z",
          "url": null,
          "user_id": 3360251,
          "workflow_state": "submitted",
          "late": true,
          "preview_url": "https://mobiledev.instructure.com/courses/833052/assignments/2241839/submissions/3360251?preview=1"
        },
        "locked_for_user": false
      }"""

    @Language("JSON")
    private var rubricAssignmentJSON = """
      {
        "assignment_group_id": 534100,
        "automatic_peer_reviews": false,
        "description": "Replacement description",
        "due_at": "2013-06-01T05:59:00Z",
        "grade_group_students_individually": false,
        "grading_standard_id": null,
        "grading_type": "points",
        "group_category_id": null,
        "id": 3119886,
        "lock_at": null,
        "peer_reviews": false,
        "points_possible": 15,
        "position": 20,
        "unlock_at": null,
        "course_id": 833052,
        "name": "Education",
        "submission_types": [
          "online_text_entry",
          "online_url",
          "media_recording",
          "online_upload"
        ],
        "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/3119886",
        "use_rubric_for_grading": true,
        "free_form_criterion_comments": false,
        "rubric": [
          {
            "id": "176919_1697",
            "points": 5,
            "description": "Grammar",
            "long_description": "",
            "ratings": [
              {
                "id": "blank",
                "points": 5,
                "description": "Perfect Grammar"
              },
              {
                "id": "176919_53",
                "points": 4,
                "description": "1 or two mistakes"
              },
              {
                "id": "blank_2",
                "points": 3,
                "description": "A few mistakes"
              },
              {
                "id": "176919_1429",
                "points": 2,
                "description": "Several mistakes"
              },
              {
                "id": "176919_9741",
                "points": 0,
                "description": "Abysmal"
              }
            ]
          },
          {
            "id": "176919_6623",
            "points": 5,
            "description": "Coolness Factor",
            "long_description": "",
            "ratings": [
              {
                "id": "176919_9675",
                "points": 5,
                "description": "Super cool"
              },
              {
                "id": "176919_3172",
                "points": 4,
                "description": "Moderately Cool"
              },
              {
                "id": "176919_393",
                "points": 3,
                "description": "Un-Cool and Geeky"
              },
              {
                "id": "176919_5761",
                "points": 0,
                "description": "Un-Cool and Nerdy"
              }
            ]
          },
          {
            "id": "176919_8253",
            "points": 5,
            "description": "How much I like you",
            "long_description": "",
            "ratings": [
              {
                "id": "176919_5103",
                "points": 5,
                "description": "You're my favorite in the class"
              },
              {
                "id": "176919_6271",
                "points": 4,
                "description": "I like having you around"
              },
              {
                "id": "176919_8307",
                "points": 3,
                "description": "You don't annoy me"
              },
              {
                "id": "176919_377",
                "points": 2,
                "description": "I can barely tolerate you"
              },
              {
                "id": "176919_2255",
                "points": 0,
                "description": "I wish you were dead"
              }
            ]
          }
        ],
        "rubric_settings": {
          "points_possible": 15,
          "free_form_criterion_comments": false
        },
        "locked_for_user": false
      }"""

    @Language("JSON")
    private var lockInfoJSON = """
      {
        "assignment_group_id": 534104,
        "automatic_peer_reviews": false,
        "due_at": "2013-08-15T05:59:00Z",
        "grade_group_students_individually": false,
        "grading_standard_id": null,
        "grading_type": "points",
        "group_category_id": null,
        "id": 3546452,
        "lock_at": null,
        "peer_reviews": false,
        "points_possible": 75,
        "position": 16,
        "unlock_at": null,
        "lock_info": {
          "asset_string": "assignment_3546452",
          "context_module": {
            "id": 805092,
            "context_id": 836357,
            "context_type": "Course",
            "name": "Locked Prereq",
            "cloned_item_id": null,
            "completion_requirements": [
              {
                "id": 6756870,
                "type": "min_score",
                "min_score": "80",
                "max_score": null
              },
              {
                "id": 8944431,
                "type": "must_submit",
                "min_score": 0,
                "max_score": null
              },
              {
                "id": 8944445,
                "type": "min_score",
                "min_score": 50,
                "max_score": null
              },
              {
                "id": 8951510,
                "type": "must_view",
                "min_score": 0,
                "max_score": null
              },
              {
                "id": 8951513,
                "type": "must_view",
                "min_score": 0,
                "max_score": null
              },
              {
                "id": 8955141,
                "type": "must_submit",
                "min_score": 0,
                "max_score": null
              },
              {
                "id": 8955142,
                "type": "must_view",
                "min_score": 0,
                "max_score": null
              },
              {
                "id": 8955144,
                "type": "must_contribute",
                "min_score": 0,
                "max_score": null
              },
              {
                "id": 8955147,
                "type": "must_view",
                "min_score": 0,
                "max_score": null
              }
            ],
            "created_at": "2013-03-06T23:44:07Z",
            "deleted_at": null,
            "downstream_modules": null,
            "end_at": null,
            "migration_id": null,
            "position": 7,
            "prerequisites": [
              {
                "id": 793427,
                "type": "context_module",
                "name": "Car Movies"
              }
            ],
            "require_sequential_progress": false,
            "start_at": null,
            "unlock_at": "2013-07-31T06:00:00Z",
            "updated_at": "2013-07-23T21:09:46Z",
            "workflow_state": "active"
          }
        },
        "course_id": 836357,
        "name": "Superhero",
        "submission_types": [
          "online_text_entry",
          "online_url"
        ],
        "description": null,
        "html_url": "https://mobiledev.instructure.com/courses/836357/assignments/3546452",
        "locked_for_user": true,
        "lock_explanation": "unlocked"
      }"""
}
