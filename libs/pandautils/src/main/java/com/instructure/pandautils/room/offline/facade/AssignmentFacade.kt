/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.pandautils.room.offline.facade

import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.PlannerOverride
import com.instructure.pandautils.room.offline.daos.AssignmentDao
import com.instructure.pandautils.room.offline.daos.AssignmentGroupDao
import com.instructure.pandautils.room.offline.daos.PlannerOverrideDao
import com.instructure.pandautils.room.offline.daos.RubricSettingsDao
import com.instructure.pandautils.room.offline.entities.AssignmentEntity
import com.instructure.pandautils.room.offline.entities.AssignmentGroupEntity
import com.instructure.pandautils.room.offline.entities.PlannerOverrideEntity
import com.instructure.pandautils.room.offline.entities.RubricSettingsEntity

class AssignmentFacade(
    private val assignmentGroupDao: AssignmentGroupDao,
    private val assignmentDao: AssignmentDao,
    private val plannerOverrideDao: PlannerOverrideDao,
    private val rubricSettingsDao: RubricSettingsDao,
    private val submissionFacade: SubmissionFacade,
    private val discussionTopicHeaderFacade: DiscussionTopicHeaderFacade
) {

    suspend fun insertAssignmentGroups(assignmentGroups: List<AssignmentGroup>) {
        assignmentGroups.forEach { assignmentGroup ->
            assignmentGroupDao.insert(AssignmentGroupEntity(assignmentGroup))

            assignmentGroup.assignments.forEach { assignment ->
                val rubricSettingsId =
                    assignment.rubricSettings?.let { rubricSettingsDao.insert(RubricSettingsEntity(it)) }
                val submissionId = assignment.submission?.let { submission ->
                    submissionFacade.insertSubmission(submission)
                }

                val plannerOverrideId = insertPlannerOverride(assignment.plannerOverride)

                val discussionTopicHeaderId =
                    assignment.discussionTopicHeader?.let { discussionTopicHeaderFacade.insertDiscussion(it) }

                val assignmentEntity = AssignmentEntity(
                    assignment = assignment,
                    rubricSettingsId = rubricSettingsId,
                    submissionId = submissionId,
                    discussionTopicHeaderId = discussionTopicHeaderId,
                    plannerOverrideId = plannerOverrideId,
                )

                assignmentDao.insert(assignmentEntity)
            }
        }
    }

    private suspend fun insertPlannerOverride(plannerOverride: PlannerOverride?): Long? {
        return plannerOverride?.let {
            plannerOverrideDao.insert(PlannerOverrideEntity(it))
        }
    }

}