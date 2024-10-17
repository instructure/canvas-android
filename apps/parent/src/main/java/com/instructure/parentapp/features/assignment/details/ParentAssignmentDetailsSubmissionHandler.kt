/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.features.assignment.details

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsSubmissionHandler
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsViewData
import java.io.File

class ParentAssignmentDetailsSubmissionHandler(
) : AssignmentDetailsSubmissionHandler {
    override var isUploading: Boolean = false
    override var lastSubmissionIsDraft: Boolean = false
    override var lastSubmissionEntry: String? = null
    override var lastSubmissionAssignmentId: Long? = null
    override var lastSubmissionSubmissionType: String? = null

    override fun addAssignmentSubmissionObserver(assignmentId: Long, userId: Long, resources: Resources, data: MutableLiveData<AssignmentDetailsViewData>, refreshAssignment: () -> Unit) = Unit

    override fun removeAssignmentSubmissionObserver() = Unit

    override fun uploadAudioSubmission(context: Context?, course: Course?, assignment: Assignment?, file: File?) = Unit

    override fun getVideoUri(fragment: FragmentActivity): Uri? = null

    override suspend fun getStudioLTITool(assignment: Assignment, courseId: Long?): LTITool? = null
}