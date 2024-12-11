/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.teacher.factory

import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.teacher.features.assignment.submission.AssignmentSubmissionRepository
import com.instructure.teacher.features.assignment.submission.SubmissionListFilter
import com.instructure.teacher.presenters.SpeedGraderPresenter
import com.instructure.teacher.viewinterface.SpeedGraderView
import com.instructure.pandautils.blueprint.PresenterFactory

class SpeedGraderPresenterFactory(
        private val courseId: Long,
        private val assignmentId: Long,
        private val submissionId: Long, // Id used when we are coming from a push notification
        private val discussionEntries:  DiscussionTopicHeader?,
        private val repository: AssignmentSubmissionRepository,
        private val filteredSubmissionIds: LongArray,
        private val filter: SubmissionListFilter,
        private val filterValue: Double
) : PresenterFactory<SpeedGraderView, SpeedGraderPresenter> {
    override fun create() = SpeedGraderPresenter(courseId, assignmentId, submissionId, discussionEntries, repository, filteredSubmissionIds, filter, filterValue)
}
